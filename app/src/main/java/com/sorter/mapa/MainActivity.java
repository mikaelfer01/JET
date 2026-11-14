package com.sorter.mapa;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;

/** Abre o sorter, deixa o usuário fazer login e injeta a tela Mapa (somente leitura). */
public class MainActivity extends Activity {
    static final String HOME = "http://10.1.164.9/";
    WebView web;

    // Injeta o painel se NÃO estiver na tela de login (sem tocar em senha).
    static final String INJECT =
        "(function(){try{if(window.__sorterMobile)return;"
        + "if(document.querySelector('input[type=password]'))return;"
        + "var s=document.createElement('script');s.src='/__mapa.js';document.body.appendChild(s);}catch(e){}})();";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        CookieManager.getInstance().setAcceptCookie(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                if (r.getUrl().getPath() != null && r.getUrl().getPath().equals("/__mapa.js")) {
                    try {
                        InputStream in = getAssets().open("mapa.js");
                        return new WebResourceResponse("text/javascript", "utf-8", in);
                    } catch (Exception e) { /* cai no padrão */ }
                }
                return super.shouldInterceptRequest(v, r);
            }
            @Override
            public void onPageFinished(WebView v, String url) {
                v.evaluateJavascript(INJECT, null);
            }
            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, android.webkit.WebResourceError e) {
                if (r.isForMainFrame()) {
                    v.loadDataWithBaseURL(null,
                        "<html><body style='font-family:sans-serif;text-align:center;padding:40px'>"
                        + "<h2>Sem conexão com o sorter</h2><p>Conecte ao Wi-Fi da empresa.</p>"
                        + "<button style='font-size:18px;padding:12px 24px' onclick=\"location.href='" + HOME + "'\">Tentar de novo</button>"
                        + "</body></html>", "text/html", "utf-8", null);
                }
            }
        });
        web.loadUrl(HOME);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() { super.onResume(); web.onResume(); }
    @Override
    protected void onPause() { web.onPause(); super.onPause(); }
}
