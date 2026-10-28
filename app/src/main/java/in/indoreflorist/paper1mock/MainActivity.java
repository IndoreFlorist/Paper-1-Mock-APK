package in.indoreflorist.paper1mock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

public final class MainActivity extends Activity {

    private static final String TARGET_URL = "https://indoreflorist.github.io/Paper-1-Mock/";
    private static final String ALLOWED_HOST = "indoreflorist.github.io";
    private static final int FILE_CHOOSER_REQUEST_CODE = 1001;

    private WebView webView;
    private LinearLayout errorOverlay;
    private ValueCallback<Uri[]> filePathCallback;
    private boolean hasMainFrameError = false;
    private OnBackInvokedCallback backInvokedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureWindowForFullScreen();

        FrameLayout rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.parseColor("#0F172A"));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#0F172A"));
        configureWebView(webView);

        errorOverlay = createErrorOverlay();
        errorOverlay.setVisibility(View.GONE);

        FrameLayout.LayoutParams matchParent = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );

        rootContainer.addView(webView, matchParent);
        rootContainer.addView(errorOverlay, matchParent);
        setContentView(rootContainer);

        applyImmersiveFullScreen();
        setupLegacyUiVisibilityListener();
        registerModernBackHandler();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(TARGET_URL);
        }
    }

    private void configureWindowForFullScreen() {
        Window window = getWindow();
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(params);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        }
    }

    private void applyImmersiveFullScreen() {
        Window window = getWindow();
        if (window == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            View decorView = window.getDecorView();
            int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(flags);
        }
    }

    @SuppressWarnings("deprecation")
    private void setupLegacyUiVisibilityListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        applyImmersiveFullScreen();
                    }
                }
            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView(WebView view) {
        WebSettings settings = view.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(view, false);

        view.setWebViewClient(new SecureWebViewClient());
        view.setWebChromeClient(new FilePickerWebChromeClient());
    }

    private LinearLayout createErrorOverlay() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#0F172A"));
        int padding = dpToPx(28);
        layout.setPadding(padding, padding, padding, padding);

        TextView titleView = new TextView(this);
        titleView.setText("Connection Unavailable");
        titleView.setTextColor(Color.parseColor("#F8FAFC"));
        titleView.setTextSize(20f);
        titleView.setGravity(Gravity.CENTER);

        TextView messageView = new TextView(this);
        messageView.setText("Unable to load Paper 1 Mock.\nPlease check your internet connection and try again.");
        messageView.setTextColor(Color.parseColor("#94A3B8"));
        messageView.setTextSize(15f);
        messageView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        msgParams.topMargin = dpToPx(12);
        msgParams.bottomMargin = dpToPx(24);
        messageView.setLayoutParams(msgParams);

        Button retryButton = new Button(this);
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackgroundColor(Color.parseColor("#2563EB"));
        int btnPadH = dpToPx(28);
        int btnPadV = dpToPx(12);
        retryButton.setPadding(btnPadH, btnPadV, btnPadH, btnPadV);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryLoadingWebsite();
            }
        });

        layout.addView(titleView);
        layout.addView(messageView);
        layout.addView(retryButton);
        return layout;
    }

    private void retryLoadingWebsite() {
        hasMainFrameError = false;
        errorOverlay.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);

        String currentUrl = webView.getUrl();
        if (currentUrl != null && currentUrl.startsWith("https://" + ALLOWED_HOST)) {
            webView.reload();
        } else {
            webView.loadUrl(TARGET_URL);
        }
        applyImmersiveFullScreen();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private final class SecureWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (uri == null) {
                return true;
            }

            String scheme = uri.getScheme();
            String host = uri.getHost();

            if ("https".equalsIgnoreCase(scheme) && ALLOWED_HOST.equalsIgnoreCase(host)) {
                return false;
            }

            if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme) || "mailto".equalsIgnoreCase(scheme)) {
                try {
                    Intent externalIntent = new Intent(Intent.ACTION_VIEW, uri);
                    externalIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                    startActivity(externalIntent);
                } catch (ActivityNotFoundException ignored) {
                }
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (request != null && request.isForMainFrame()) {
                hasMainFrameError = true;
                webView.setVisibility(View.INVISIBLE);
                errorOverlay.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (!hasMainFrameError) {
                errorOverlay.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
            CookieManager.getInstance().flush();
        }
    }

    private final class FilePickerWebChromeClient extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(
                WebView webView,
                ValueCallback<Uri[]> filePathCallback,
                FileChooserParams fileChooserParams
        ) {
            cancelPendingFileCallback();
            MainActivity.this.filePathCallback = filePathCallback;

            Intent contentSelectionIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");

            if (fileChooserParams != null && fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) {
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            Intent chooserIntent = Intent.createChooser(contentSelectionIntent, "Select Question Bank (.js)");
            try {
                startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE);
                return true;
            } catch (ActivityNotFoundException e) {
                cancelPendingFileCallback();
                return false;
            }
        }
    }

    private void cancelPendingFileCallback() {
        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) {
                return;
            }

            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    results = new Uri[count];
                    for (int i = 0; i < count; i++) {
                        results[i] = data.getClipData().getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                } else {
                    results = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                }
            }

            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }

        applyImmersiveFullScreen();
    }

    private void handleBackNavigation() {
        if (errorOverlay != null && errorOverlay.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            hasMainFrameError = false;
            errorOverlay.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            webView.goBack();
            return;
        }

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    private void registerModernBackHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backInvokedCallback = new OnBackInvokedCallback() {
                @Override
                public void onBackInvoked() {
                    handleBackNavigation();
                }
            };
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    backInvokedCallback
            );
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleBackNavigation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveFullScreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
        applyImmersiveFullScreen();
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            CookieManager.getInstance().flush();
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        cancelPendingFileCallback();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && backInvokedCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(backInvokedCallback);
        }
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
