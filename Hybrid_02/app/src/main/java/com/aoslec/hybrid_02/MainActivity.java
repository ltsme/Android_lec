package com.aoslec.hybrid_02;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    WebView webView = null;
    Button btnPage1, btnPage2, btnPage3;
    String strPage1 = "<html>\n" +
            "<head>\n" +
            "\n" +
            "\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<title>Numerical Arithmetic</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>Hello World!</h1>\n" +
            "</body>\n" +
            "</html>";
    String strPage2 = "<html>\n" +
            "<head>\n" +
            "\n" +
            "\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<title>Numerical Arithmetic</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<img src=\"http://192.168.2.3:8080/test/dog.jpg\" alt=\"강아쥐\">\n" +
            "</body>\n" +
            "</html>";
    String strPage3 = "<html>\n" +
            "<head>\n" +
            "\n" +
            "\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "<title>Numerical Arithmetic</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<img src=\"http://192.168.2.3:8080/test/dog.jpg\" alt=\"강아쥐2\" width=\"300px\" height=\"300px\">\n" +
            "</body>\n" +
            "</html>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        btnPage1 = findViewById(R.id.btn_page1);
        btnPage2 = findViewById(R.id.btn_page2);
        btnPage3 = findViewById(R.id.btn_page3);

        btnPage1.setOnClickListener(onClickListener);
        btnPage2.setOnClickListener(onClickListener);
        btnPage3.setOnClickListener(onClickListener);

        // Web Setting
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // JavaScript 사용 가능
        webSettings.setBuiltInZoomControls(true); // 확대 축소 가능
        webSettings.setDisplayZoomControls(false); // 돋보기 없애기

        // Link시 다른 Browser 안보이게
//        webView.setWebViewClient(new WebViewClient(){
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//                btnReload.setText("로딩 중...");
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                btnReload.setText(webView.getTitle());
//            }
//        });

//        webView.loadUrl("http://192.168.0.8:8080/test/hello.jsp");

//        webView.loadData(strPage1, "text/html; charset=utf-8", "utf-8");
        webView.loadData(strPage1, null, null);

    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_page1:
                    btnPage1Click();
                    break;
                case R.id.btn_page2:
//                    webView.loadUrl("http://192.168.0.8:8080/test/dog.jsp");
                    webView.loadDataWithBaseURL(null, strPage2, null, null, null);
                    break;
                case R.id.btn_page3:
//                    webView.loadUrl("http://192.168.0.8:8080/test/bigdog.jsp");
//                    webView.loadDataWithBaseURL(null, strPage3, "text/html", "UTF-8", null);
                    webView.loadDataWithBaseURL(null, strPage3, null, null, null);
                    break;
            }



        }
    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(webView.canGoBack()){
            webView.goBack();
        }else {
            finish();
        }
    }

    public void btnPage1Click(){
        webView.loadUrl("http://192.168.0.8:8080/test/Arithmetic.jsp");
    }


}