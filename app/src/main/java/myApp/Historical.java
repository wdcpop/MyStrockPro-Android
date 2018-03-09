package node.frontend.titletab;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by WENTAO on 11/21/2017.
 */

public class Historical extends Fragment {
    private View rootView;
    private WebView webView;
    private String symbol;
    private TextView errMsgView;
    private String chartStringIMG;
    private ProgressBar progressBarHis;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null){
            rootView = inflater.inflate(R.layout.historical, container, false);
        }

        return rootView;
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chartStringIMG = "";
        progressBarHis = rootView.findViewById(R.id.progress_bar_historical);
        symbol = getArguments().getString("symbol");
        errMsgView = rootView.findViewById(R.id.errorMsgHistorical);
        webView = (WebView) rootView.findViewById(R.id.historicalCharts);
        initWebView();
        errMsgView.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.INVISIBLE);
        progressBarHis.setVisibility(View.VISIBLE);

        webView.loadUrl("file:///android_asset/HISTORICAL.html");

        if (chartStringIMG.toUpperCase() != "ERROR"){
            System.out.println("draw historical charts");
            errMsgView.setVisibility(View.INVISIBLE);
            webView.setVisibility(View.VISIBLE);
        }
        else {
            System.out.println("show historical err");
            webView.setVisibility(View.INVISIBLE);
            errMsgView.setVisibility(View.VISIBLE);
        }
    }


    @SuppressLint("JavascriptInterface")
    private void initWebView(){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (!webView.getSettings().getLoadsImagesAutomatically()) {
                    webView.getSettings().setLoadsImagesAutomatically(true);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                super.onConsoleMessage(message, lineNumber, sourceID);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.addJavascriptInterface(this, "historical");
    }

    @JavascriptInterface
    public String getSymbol(){
        return symbol;
    }

    @JavascriptInterface
    public void getChart(String chartString){
        chartStringIMG = chartString;
        System.out.println("chartStringHistorical");
        System.out.println(chartStringIMG);
        if (!chartStringIMG.equals("Error")){
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressBarHis.setVisibility(View.INVISIBLE);
//                    webView.setVisibility(View.VISIBLE);
//                    errMsgView.setVisibility(View.INVISIBLE);
//                }
//            });

        }
        else {
            System.out.println("JS exception");
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    webView.setVisibility(View.INVISIBLE);
//                    progressBarHis.setVisibility(View.INVISIBLE);
//                    errMsgView.setVisibility(View.VISIBLE);
//                }
//            });
        }
    }
}

