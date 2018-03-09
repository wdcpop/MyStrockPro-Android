//package wdcpop.stock_dev;
package node.frontend.titletab;

import android.app.Activity;
import com.facebook.FacebookSdk;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private AutoCompleteTextView acTextView;
    private String autoCompleteURL = "http://nodedemo.us-east-2.elasticbeanstalk.com/?autoComplete=";
    private AutoCompleteAdapter autoAdapter;
    private String userInput;
    private Button clearBtn;
    private SharedPreferences localStorage;
    private boolean emptyStar = true;
    private FavListViewAdapter favListViewAdapter;
    private Spinner sortBy;
    private Spinner orderBy;
    private Switch autoRefresh;
    private ListView favListView;
    private Button refreshButton;
    private ProgressBar progressBar;
    private String favList;
    private ArrayAdapter sortAdapter;
    private ArrayAdapter orderAdapter;
    private List<FavItem> favListViewArr;
    private List<FavItem> favListArrCopy;
    private int sort;
    private int order;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        initRefreshButton();

        initSpinnerAdapter();
        initSwitch();
        initFavList();
        acTextView = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
        initClear();

        favListViewArr = new ArrayList<FavItem>();
        int layout = android.R.layout.simple_list_item_1;
        autoAdapter = new AutoCompleteAdapter(MainActivity.this, layout);
        acTextView.setThreshold(1);
        acTextView.setAdapter(autoAdapter);
        localStorage = MainActivity.this.getSharedPreferences("favList",0);
        editor = localStorage.edit();
        registerForContextMenu(favListView);

        sort = 1;
        order = 1;
        try {
            drawFavList();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button getQuote = (Button) findViewById(R.id.getQuote);
        getQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String symbol = String.valueOf(acTextView.getText());
                if (symbol.indexOf('-') >= 0){
                    symbol = symbol.split(" - ")[0].trim();
                }
                else {
                    symbol = symbol.trim();
                }
                if (symbol.length() > 0){
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this,SecondActivity.class);
                    intent.putExtra("symbol", symbol);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a stock name or symbol",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void initRefreshButton(){
        refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshOnce();
            }
        });
    }

    @Override
    protected void onResume()  {
        super.onResume();
        favList = localStorage.getString("favList", null);
        JSONArray  favListJSONArr = null;
        if (favList != null){
            List<FavItem> favListArrCopy = new ArrayList<FavItem>();
            try {
                  favListJSONArr = new JSONArray(favList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < favListJSONArr.length(); i++){
                JSONObject temp = null;
                try {
                    temp = favListJSONArr.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    favListArrCopy.add(
                                new FavItem(
                                        temp.getString("symbol"),
                                        temp.getString("price"),
                                        temp.getString("change"),
                                        temp.getString("changePercent")
                                ));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (favListViewArr.size() != favListArrCopy.size()){
                System.out.println("favListViewArr changed!");
                favListViewAdapter.clear();
                favListViewArr = favListArrCopy;
                favListViewAdapter.addAll(favListViewArr);
                favListViewAdapter.notifyDataSetChanged();
            }
        }

    }

    private void initClear(){
        clearBtn = findViewById(R.id.clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acTextView.setText("");
            }
        });
    }

    private void drawFavList() throws JSONException {
        favList = localStorage.getString("favList", null);
        System.out.println(favList);
        if (favList != null){

            JSONArray favListJSONArr = new JSONArray(favList);
            for (int i = 0; i < favListJSONArr.length(); i++){
                JSONObject temp = favListJSONArr.getJSONObject(i);

                favListViewArr.add(
                        new FavItem(
                                temp.getString("symbol"),
                                temp.getString("price"),
                                temp.getString("change"),
                                temp.getString("changePercent")
                                ));

            }

            favListViewAdapter = new FavListViewAdapter(favListViewArr, MainActivity.this);
            favListView.setAdapter(favListViewAdapter);
        }
    }

    private void  initSpinnerAdapter(){
        sortBy = findViewById(R.id.spinnerSort);
        orderBy = findViewById(R.id.spinnerOrder);

        sortAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.sortBy)){
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent){
                View row = super.getDropDownView(position, convertView, parent);
                row = (TextView) row;

                if(position == 0) {
                    ((TextView) row).setTextColor(Color.GRAY);
                }
                else {
                    ((TextView) row).setTextColor(Color.BLACK);
                }
                return row;
            }


        };

        sortBy.setAdapter(sortAdapter);


        orderAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.orderBy)){
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView (int position, View convertView, ViewGroup parent){
                View row = super.getDropDownView(position, convertView, parent);
                row = (TextView) row;

                if(position == 0) {
                    ((TextView) row).setTextColor(Color.GRAY);
                }
                else {
                    ((TextView) row).setTextColor(Color.BLACK);
                }
                return row;
            }
        };
        orderBy.setAdapter(orderAdapter);


        sortBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<FavItem> sortedArr;
                sort = i;
                if (sort == 2){ // Symbol
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                return t0.getSymbol().compareTo(t1.getSymbol());
                            }
                        });
                    }
                    else {
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                return t1.getSymbol().compareTo(t0.getSymbol());
                            }
                        });
                    }
                }

                else if (sort == 3){ // Price
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending

                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double price0 = Double.parseDouble(t0.getPrice());
                                double price1 = Double.parseDouble(t1.getPrice());
                                if (price0 < price1){
                                    return -1;
                                }
                                else if (price0 > price1){
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    else {
                        sortedArr = new ArrayList<>(favListViewArr);
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double price0 = Double.parseDouble(t0.getPrice());
                                double price1 = Double.parseDouble(t1.getPrice());
                                if (price0 < price1){
                                    return 1;
                                }
                                else if (price0 > price1){
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }
                }

                else if (sort == 4){ // change
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double changePercent0 = Double.parseDouble(t0.getChangePercent().substring(0, t0.getChangePercent().length()-1));
                                double changePercent1 = Double.parseDouble(t1.getChangePercent().substring(0, t1.getChangePercent().length()-1));
                                if (changePercent0 < changePercent1){
                                    return -1;
                                }
                                else if (changePercent0 > changePercent1){
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    else {
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double changePercent0 = Double.parseDouble(t0.getChangePercent().substring(0, t0.getChangePercent().length()-1));
                                double changePercent1 = Double.parseDouble(t1.getChangePercent().substring(0, t1.getChangePercent().length()-1));
                                if (changePercent0 < changePercent1){
                                    return 1;
                                }
                                else if (changePercent0 > changePercent1){
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }
                }
                else {
                    JSONArray favListJSONArr = null;
                    sortedArr = new ArrayList<FavItem>();
                    try {
                        if (favList != null){
                        favListJSONArr = new JSONArray(favList);

                            for (int j = 0; j < favListJSONArr.length(); j++){
                                JSONObject temp = favListJSONArr.getJSONObject(j);

                                sortedArr.add(
                                        new FavItem(
                                                temp.getString("symbol"),
                                                temp.getString("price"),
                                                temp.getString("change"),
                                                temp.getString("changePercent")
                                        ));
                                System.out.println("add item");

                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                favListViewAdapter.clear();
                favListViewAdapter.addAll(sortedArr);
                favListViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        orderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<FavItem> sortedArr = favListViewArr;
                order = i;
                if (sort == 2){ // Symbol
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                return t0.getSymbol().compareTo(t1.getSymbol());
                            }
                        });
                    }
                    else {
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                return t1.getSymbol().compareTo(t0.getSymbol());
                            }
                        });
                    }
                }

                else if (sort == 3){ // Price
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending

                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double price0 = Double.parseDouble(t0.getPrice());
                                double price1 = Double.parseDouble(t1.getPrice());
                                if (price0 < price1){
                                    return -1;
                                }
                                else if (price0 > price1){
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    else {
                        sortedArr = new ArrayList<>(favListViewArr);
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double price0 = Double.parseDouble(t0.getPrice());
                                double price1 = Double.parseDouble(t1.getPrice());
                                if (price0 < price1){
                                    return 1;
                                }
                                else if (price0 > price1){
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }
                }

                else if (sort == 4){ // change
                    sortedArr = new ArrayList<>(favListViewArr);
                    if (order == 1){  //Ascending
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double changePercent0 = Double.parseDouble(t0.getChangePercent().substring(0, t0.getChangePercent().length()-1));
                                double changePercent1 = Double.parseDouble(t1.getChangePercent().substring(0, t1.getChangePercent().length()-1));
                                if (changePercent0 < changePercent1){
                                    return -1;
                                }
                                else if (changePercent0 > changePercent1){
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    else {
                        Collections.sort(sortedArr, new Comparator<FavItem>() {
                            @Override
                            public int compare(FavItem t0, FavItem t1) {
                                double changePercent0 = Double.parseDouble(t0.getChangePercent().substring(0, t0.getChangePercent().length()-1));
                                double changePercent1 = Double.parseDouble(t1.getChangePercent().substring(0, t1.getChangePercent().length()-1));
                                if (changePercent0 < changePercent1){
                                    return 1;
                                }
                                else if (changePercent0 > changePercent1){
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }
                }
                else {
                    JSONArray favListJSONArr = null;
                    sortedArr = new ArrayList<FavItem>();
                    try {
                        favListJSONArr = new JSONArray(favList);
                        for (int j = 0; j < favListJSONArr.length(); j++){
                            JSONObject temp = favListJSONArr.getJSONObject(j);

                            sortedArr.add(
                                    new FavItem(
                                            temp.getString("symbol"),
                                            temp.getString("price"),
                                            temp.getString("change"),
                                            temp.getString("changePercent")
                                    ));
                            System.out.println("add item");

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                favListViewAdapter.clear();
                favListViewAdapter.addAll(sortedArr);
                favListViewAdapter.notifyDataSetChanged();
//

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void refreshOnce(){
        progressBar.setVisibility(View.VISIBLE);
        if (favListViewArr != null && favListViewArr.size() > 0){
            for (FavItem item : favListViewArr){
                fetchDataOnce(item);
            }
        }
    }

    private void initSwitch(){
        autoRefresh = findViewById(R.id.autoSwitch);
        final Timer timer = new Timer();
        autoRefresh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    System.out.println("switch checked!");
                    final Handler handler = new Handler();

                    TimerTask doAsynchronousTask = new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @SuppressWarnings("unchecked")
                                public void run() {
                                    try {
                                        System.out.println("refreshed once");
                                        refreshOnce();
                                    }
                                    catch (Exception e) {
                                        // TODO Auto-generated catch block
                                    }
                                }
                            });
                        }
                    };
                    timer.schedule(doAsynchronousTask, 0,5000);
                }
                else {
                    System.out.println("switch unchecked!");
                    System.out.println("Task cleared");
                    timer.cancel();
                    timer.purge();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void initFavList(){
        favListView = (ListView) findViewById(R.id.favListViewxml);
        favListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SecondActivity.class);
                intent.putExtra("symbol", favListViewArr.get(i).getSymbol());
                startActivity(intent);
            }
        });
    }

    private void fetchDataOnce(final FavItem item){
        final String symbol = item.getSymbol();
        String URL = "http://nodedemo.us-east-2.elasticbeanstalk.com/?symbol=" + symbol + "&short=yes";
            JsonObjectRequest getPriceVolume = new JsonObjectRequest(Request.Method.GET, URL, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {

                                ParseData data = new ParseData(response);
                                JSONObject priceVolume = data.parsePriceVolume();
                                String changePercent = String.valueOf(priceVolume.getDouble("changePercent")) + "%";
                                String newPriceStr = priceVolume.getString("close");
                                double newPrice = Double.parseDouble(newPriceStr);
                                double oldPrice = Double.parseDouble(item.getPrice());
                                if (newPrice != oldPrice){
                                    System.out.println("data changed for: " + symbol);
                                    System.out.println("new price: " + newPriceStr + " old price: " + oldPrice);
                                    item.setChange(String.valueOf(priceVolume.getDouble("change")));
                                    item.setChangePercent(changePercent);
                                    item.setPrice(newPriceStr);
                                    favListViewAdapter.notifyDataSetChanged();
                                }
                                else {
                                    System.out.println("data not change for: " + symbol);
                                    System.out.println("new price: " + newPriceStr + " old price: " + oldPrice);
                                    System.out.println(newPriceStr);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("Error");
                    System.out.println(error.getMessage());
                }
            });
            getPriceVolume.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
}
