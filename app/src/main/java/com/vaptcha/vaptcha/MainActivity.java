package com.vaptcha.vaptcha;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView btn;
    private WebView webview;
    public static final String PASS = "pass";//通过
    public static final String CANCEL = "cancel";//取消
    public static final String ERROR = "error";//错误
    public String src;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //设置webview
        setVaptcha();
    }

    private void setVaptcha() {
        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setSupportMultipleWindows(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        // 持久化存储cookie
        CookieManager instance = CookieManager.getInstance();
        // 允许使用cookie
        instance.setAcceptCookie(true);
        instance.setAcceptThirdPartyCookies(webview,true);
        // 设置不使用默认浏览器，而直接使用WebView组件加载页面。
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT < 21) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }

                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        // 设置WebView组件支持加载JavaScript。
        webview.getSettings().setJavaScriptEnabled(true);
        // 建立JavaScript调用Java接口的桥梁。
        webview.addJavascriptInterface(new vaptchaInterface(), "vaptchaInterface");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }
    }

    public class vaptchaInterface {
        @JavascriptInterface
        public void signal(String json) {
            //json格式{signal:"",data:""}
            //signal: pass (通过) ； cancel（取消）
            try {
                final JSONObject jsonObject = new JSONObject(json);
                String signal = jsonObject.getString("signal");
                final String data = jsonObject.getString("data");
                if (PASS.equals(signal)) {//通过
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "验证通过:" + data, Toast.LENGTH_SHORT).show();
                            webview.loadUrl("");
                            webview.setVisibility(View.GONE);
                        }
                    });
                } else if (CANCEL.equals(signal)) {//取消
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "验证取消:" + data, Toast.LENGTH_SHORT).show();
                            webview.loadUrl("");
                            webview.setVisibility(View.GONE);
                        }
                    });
                } else if (ERROR.equals(signal)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "错误：" + data, Toast.LENGTH_SHORT).show();
                            webview.loadUrl("");
                            webview.setVisibility(View.GONE);

                        }
                    });
                } else {//其他html页面返回的状态参数


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        btn = (TextView) findViewById(R.id.btn);
        btn.setOnClickListener(this);
        webview = (WebView) findViewById(R.id.webview);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                // 加载业务页面。
                src = "你的地址"; // 在这里配置你的android.html所在地址,eg: https://xxx.com/yyy/android.html
                webview.loadUrl(src + "?vid=5b4d9c33a485e50410192331&scene=0&lang=zh-CN&area=cn");
                webview.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
