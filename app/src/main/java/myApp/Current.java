package node.frontend.titletab;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by WENTAO on 11/21/2017.
 */

public class Current extends Fragment {

    private View rootView;
    private WebView webView;
    private ProgressBar progressBar2;
    private String symbol;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private Button changeBtn;
    private Button fbButton;
    private String chartStringIMG;
    private ScrollView scrollView;
    private String indicatorStr;
    private TextView errorMsgChart;
    private Button starButton;
    private ListView tableListView;
    private ArrayAdapter indicatorAdapter;
    private Spinner inicatorSpinner;
    private SharedPreferences localStorage;
    private TextView errMsgView;

    private boolean emptyStar = true;
    SharedPreferences.Editor editor;
    List<Row> tableList;
    private FavItem favItem;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null){
            rootView = inflater.inflate(R.layout.current, container, false);
        }
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chartStringIMG = "";
        scrollView = rootView.findViewById(R.id.scrollView);
        errMsgView = rootView.findViewById(R.id.errorMsg);
        errorMsgChart = rootView.findViewById(R.id.errorMsgChart);
        errorMsgChart.setVisibility(View.INVISIBLE);
        tableListView = (ListView) rootView.findViewById(R.id.tableListView);
        localStorage = getActivity().getSharedPreferences("favList",0);
        editor = localStorage.edit();
        progressBar2 = rootView.findViewById(R.id.progress_bar2);
        starButton = (Button) rootView.findViewById(R.id.star);
        symbol = getArguments().getString("symbol");
        initFBButton();

        indicatorStr = "PRICE";
        webView = (WebView) rootView.findViewById(R.id.stockCharts);
        initWebView();
        initIndicatorSpinner();
        initChangeButton();
        checkStar();
        errMsgView.setVisibility(View.INVISIBLE);
        tableListView.setVisibility(View.INVISIBLE);
        progressBar2.setVisibility(View.VISIBLE);
        fbButton.setEnabled(false);
        starButton.setEnabled(false);


        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray favListJSONArr;
                if (emptyStar){
                    starButton.setBackgroundResource(R.drawable.star);
                    String favList = localStorage.getString("favList", null);
                    try {
                        favListJSONArr = favList == null ? new JSONArray() : new JSONArray(favList);
                        JSONObject temp = new JSONObject();
                        System.out.println("add item");

                        temp.put("symbol", favItem.getSymbol());
                        temp.put("price", favItem.getPrice());
                        temp.put("change", favItem.getChange());
                        temp.put("changePercent", favItem.getChangePercent());
                        favListJSONArr.put(temp);
                        editor.putString("favList",favListJSONArr.toString());
                        editor.commit();
                        System.out.println(favListJSONArr.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                else if (!emptyStar){
                    starButton.setBackgroundResource(R.drawable.empty);
                    try {
                        System.out.println("remove item");
                        favListJSONArr = new JSONArray(localStorage.getString("favList", null));
                        for (int i = 0; i < favListJSONArr.length(); i++){
                            JSONObject temp = favListJSONArr.getJSONObject(i);
                            if (temp.getString("symbol").toUpperCase().equals(favItem.getSymbol().toUpperCase())){
                                System.out.println(temp.getString("symbol").toUpperCase());
                                System.out.println(favItem.getSymbol().toUpperCase());
                                favListJSONArr.remove(i);
                                break;
                            }
                        }

                        editor.putString("favList",favListJSONArr.toString());
                        editor.commit();
                        System.out.println(favListJSONArr.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                emptyStar = !emptyStar;
            }
        });

        String URL = "http://nodedemo.us-east-2.elasticbeanstalk.com/?symbol=" + symbol + "&short=yes";

        JsonObjectRequest getPriceVolume = new JsonObjectRequest(Request.Method.GET, URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            tableListView.setVisibility(View.VISIBLE);

                            ParseData data = new ParseData(response);
                            JSONObject priceVolume = data.parsePriceVolume();
                            System.out.println("priceVolume");
                            System.out.println(priceVolume);
                            String changePercent = String.valueOf(priceVolume.getDouble("changePercent")) + "%";
                            tableList = new ArrayList<Row>();
                            favItem = new FavItem(symbol.toUpperCase(), String.valueOf(priceVolume.getString("close")), String.valueOf(priceVolume.getDouble("change")), changePercent);
                            tableList.add(new Row("Stock Symbol", symbol.toUpperCase()));
                            tableList.add(new Row("Last Price", String.valueOf(priceVolume.getDouble("lastPrice"))));
                            tableList.add(new Row("Change", String.valueOf(priceVolume.getDouble("change")) + " ("+ changePercent + ")"));
                            tableList.add(new Row("Timestamp", priceVolume.getString("time")));
                            tableList.add(new Row("Open", String.valueOf(priceVolume.getString("open"))));
                            tableList.add(new Row("Close", String.valueOf(priceVolume.getString("close"))));
                            tableList.add(new Row("Day's Range", priceVolume.getString("range")));
                            tableList.add(new Row("Volume", String.valueOf(priceVolume.getInt("volume"))));
                            InfoTableAdapater infoTableAdapter = new InfoTableAdapater(getActivity(), tableList);
                            tableListView.setAdapter(infoTableAdapter);
                            starButton.setEnabled(true);

                        } catch (JSONException e) {
                            System.out.println("parse data error");
                            starButton.setEnabled(false);
                            errMsgView.setVisibility(View.VISIBLE);
                            e.printStackTrace();
                        }
                        progressBar2.setVisibility(View.INVISIBLE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error");
                System.out.println(error.getMessage());
            }
        });
        getPriceVolume.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        MySingleton.getInstance(getActivity()).addToRequestque(getPriceVolume);

    }


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

        webView.addJavascriptInterface(this, "JSONStr");
    }

    private void  initIndicatorSpinner(){
        inicatorSpinner = rootView.findViewById(R.id.spinnerIndicator);
        indicatorAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.indicators));
        inicatorSpinner.setAdapter(indicatorAdapter);
        inicatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    indicatorStr = "PRICE";

                }
                else if (i == 1) {
                    indicatorStr = "SMA";

                }
                else if (i == 2) {
                    indicatorStr = "EMA";

                }
                else if (i == 3) {
                    indicatorStr = "STOCH";

                }
                else if (i == 4) {
                    indicatorStr = "RSI";
                }
                else if (i == 5) {
                    indicatorStr = "ADX";
                }
                else if (i == 6) {
                    indicatorStr = "CCI";
                }
                else if (i == 7) {
                    indicatorStr = "BBANDS";
                }
                else if (i == 8) {
                    indicatorStr = "MACD";
                }
                else {
                }
                changeBtn.setEnabled(true);
                changeBtn.setTextColor(Color.BLACK);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("onNothingSelected");
            }
        });
    }

    private void initFBButton(){

        fbButton = (Button) rootView.findViewById(R.id.fbButton);
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(getActivity());
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShareDialog.canShow(ShareLinkContent.class)){
                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("https://export.highcharts.com/" + chartStringIMG))
                            .build();
                    shareDialog.show(content);
                }
            }
        });
    }

    private void checkStar(){
        String favListStr = localStorage.getString("favList", null);
        if (favListStr != null){
            try {
                JSONArray favListJSONArr = new JSONArray(favListStr);
                for (int i = 0; i < favListJSONArr.length(); i++){
                    JSONObject temp = favListJSONArr.getJSONObject(i);
                    if (temp.getString("symbol").toUpperCase().equals(symbol.toUpperCase())){
                        System.out.println("set star full");
                        System.out.println(temp.getString("symbol"));
                        System.out.println(symbol);
                        starButton.setBackgroundResource(R.drawable.star);
                        emptyStar = false;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            emptyStar= true;
            starButton.setBackgroundResource(R.drawable.empty);
        }
    }

    private void initChangeButton(){
        changeBtn = rootView.findViewById(R.id.changeBtn);
        changeBtn.setEnabled(false);
        changeBtn.setTextColor(Color.GRAY);
        errorMsgChart.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.INVISIBLE);
        errorMsgChart.setVisibility(View.INVISIBLE);

        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeBtn.setEnabled(false);
                changeBtn.setTextColor(Color.GRAY);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                String fileURL = "file:///android_asset/" + indicatorStr + ".html";
                webView.loadUrl(fileURL);

            }
        });
    }


    @JavascriptInterface
    public String getSymbol(){
        return symbol;
    }


    @JavascriptInterface
    public void getChart(String chartString){
        chartStringIMG = chartString;

        System.out.println("chartStringIMG");
        System.out.println(chartStringIMG);
        if (!chartStringIMG.equals("Error")){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fbButton.setEnabled(true);
                    webView.setVisibility(View.VISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                    errorMsgChart.setVisibility(View.INVISIBLE);
                }
            });

        }
        else {
            System.out.println("JS exception");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.setVisibility(View.INVISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                    errorMsgChart.setVisibility(View.VISIBLE);
                    fbButton.setEnabled(false);
                }
            });

        }

    }

}

