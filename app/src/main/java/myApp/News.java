package node.frontend.titletab;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WENTAO on 11/21/2017.
 */

public class News extends Fragment {
    private View rootView;
    private String symbol;
    private ListView newsFeedView;
    private ProgressBar progressBarNews;
    private List<OneNews> newsList;
    private TextView errMsgView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.news, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errMsgView = rootView.findViewById(R.id.errorMsgNews);
        progressBarNews = rootView.findViewById(R.id.progress_bar_news);
        symbol = getArguments().getString("symbol");
        String newsURL = "http://nodedemo.us-east-2.elasticbeanstalk.com/?symbol=" + symbol + "&news=yes";

        newsFeedView = (ListView) rootView.findViewById(R.id.newsListView);
        errMsgView.setVisibility(View.INVISIBLE);
        newsFeedView.setVisibility(View.INVISIBLE);
        progressBarNews.setVisibility(View.VISIBLE);
        JsonObjectRequest getNewsFeed = new JsonObjectRequest(Request.Method.GET, newsURL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            newsList = new ArrayList<OneNews>();
                            ParseData data = new ParseData(response);
                            JSONArray newsArr = data.parseNews();
                            for (int i = 0; i < newsArr.length(); i++) {
                                JSONObject news = newsArr.getJSONObject(i);
                                newsList.add(new OneNews(
                                        news.getString("title"),
                                        news.getString("author"),
                                        news.getString("date"),
                                        news.getString("link")
                                ));
                            }

                            NewsFeedAdapter newsFeedAdapter = new NewsFeedAdapter(getActivity(), newsList);
                            newsFeedView.setAdapter(newsFeedAdapter);
                            errMsgView.setVisibility(View.INVISIBLE);
                            newsFeedView.setVisibility(View.VISIBLE);
                            progressBarNews.setVisibility(View.INVISIBLE);
                        } catch (JSONException e) {
                            errMsgView.setVisibility(View.VISIBLE);
                            newsFeedView.setVisibility(View.INVISIBLE);
                            progressBarNews.setVisibility(View.INVISIBLE);
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error News Page");
                errMsgView.setVisibility(View.VISIBLE);
                newsFeedView.setVisibility(View.INVISIBLE);
                progressBarNews.setVisibility(View.INVISIBLE);
                System.out.println(error.getMessage());
            }
        });

        newsFeedView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OneNews news = newsList.get(i);
                String link = news.getLink();
                Uri url = Uri.parse(link);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(url);
                startActivity(intent);
            }
        });
        getNewsFeed.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        MySingleton.getInstance(getActivity()).addToRequestque(getNewsFeed);
    }
}

