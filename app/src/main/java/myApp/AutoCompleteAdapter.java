package node.frontend.titletab;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by WENTAO on 11/17/2017.
 */

class AutoCompleteAdapter extends ArrayAdapter<String>{
    private ArrayList<String> data;
    private Context mainActivity;
    private final String server = "http://nodedemo.us-east-2.elasticbeanstalk.com/?autoComplete=";

    AutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        this.data = new ArrayList<>();
        this.mainActivity = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    HttpURLConnection conn = null;
                    InputStream input = null;
                    try {
                        String url = server + constraint.toString();
                        final ArrayList<String> suggestions = new ArrayList<>();
                        JsonArrayRequest autoCompleteReq = new JsonArrayRequest(Request.Method.GET, url, null,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            System.out.println("auto complete response");
                                            for (int i = 0; i < response.length(); i++){

                                                JSONObject companyInfo = response.getJSONObject(i);
                                                String symbol = (String) companyInfo.get("Symbol");
                                                String name = (String) companyInfo.get("Name");
                                                String exchange = (String) companyInfo.get("Exchange");
                                                suggestions.add(symbol + " - " + name + " (" + exchange + ")");
                                                if (i == 4){
                                                    break;
                                                }
                                            }
                                            System.out.println(suggestions);

                                        } catch (JSONException e) {
                                            System.out.println("error!: "+e);
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("Error");
                                System.out.println(error.getMessage());
                            }
                        });
                        autoCompleteReq.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                        MySingleton.getInstance(mainActivity).addToRequestque(autoCompleteReq);
                        System.out.println("System.out.println(suggestions.size());");
                        System.out.println(suggestions.size());

                        data = suggestions;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {

                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else notifyDataSetInvalidated();
            }
        };
    }
}