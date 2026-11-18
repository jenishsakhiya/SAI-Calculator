package com.sai.softjaws;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

/** Wraps the offline calculator page (assets/index.html) in a full-screen WebView. */
public class MainActivity extends Activity {

    private static final int BRAND = Color.parseColor("#1A9FE0");
    private static final int BG = Color.parseColor("#F4F6F9");

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // remembers the standard discount
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        webView.setWebViewClient(new WebViewClient());

        setContentView(buildEdgeToEdgeLayout());

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    /**
     * Android 15+ forces edge-to-edge for targetSdk 35, so the page would otherwise draw
     * under the status bar (clock / notifications). We opt into edge-to-edge on every
     * version for consistent behaviour, then place a brand-coloured spacer behind the
     * status bar and a background-coloured spacer behind the navigation bar / keyboard.
     */
    @SuppressWarnings("deprecation")
    private View buildEdgeToEdgeLayout() {
        Window w = getWindow();
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        View statusSpacer = new View(this);
        statusSpacer.setBackgroundColor(BRAND);
        View navSpacer = new View(this);
        navSpacer.setBackgroundColor(BG);

        root.addView(statusSpacer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        root.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(navSpacer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top, bottom, left, right;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Insets bars = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                Insets ime = insets.getInsets(WindowInsets.Type.ime());
                top = bars.top;
                bottom = Math.max(bars.bottom, ime.bottom);   // keep inputs above the keyboard
                left = bars.left;
                right = bars.right;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom(); // includes keyboard (adjustResize)
                left = insets.getSystemWindowInsetLeft();
                right = insets.getSystemWindowInsetRight();
            }
            statusSpacer.getLayoutParams().height = top;
            navSpacer.getLayoutParams().height = bottom;
            root.setPadding(left, 0, right, 0);
            statusSpacer.requestLayout();
            navSpacer.requestLayout();
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    ? WindowInsets.CONSUMED
                    : insets.consumeSystemWindowInsets();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(false);
        } else {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);   // dark nav icons on light bg
        }
        return root;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                // light status icons on the blue bar, dark nav icons on the light background
                c.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    /** Back button: calculator screen -> product list; product list -> exit app. */
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        webView.evaluateJavascript(
            "(window.__appBack && window.__appBack()) ? 'yes' : 'no'",
            result -> {
                if (result == null || !result.contains("yes")) {
                    MainActivity.super.onBackPressed();
                }
            });
    }
}
