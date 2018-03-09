package node.frontend.titletab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by WENTAO on 11/22/2017.
 */

public class ParseData {
    private JSONObject response;

    public ParseData(JSONObject response){
        this.response = response;
    }

    public JSONObject parsePriceVolume() throws JSONException {
            JSONObject data = new JSONObject();
            ArrayList<String> date = new ArrayList<String>();
            ArrayList<Double> price = new ArrayList<Double>();
            ArrayList<Integer> volumes = new ArrayList<Integer>();
            String timeStamp = (String) response.getJSONObject("Meta Data").getString("3. Last Refreshed");
            JSONObject timeSeries = response.getJSONObject("Time Series (Daily)");


            Iterator<String> keys = timeSeries.keys();
            while(keys.hasNext()) {
                String key = (String)keys.next();
                date.add(key);
                JSONObject dailyData = timeSeries.getJSONObject(key);
                price.add(roundDecimal(dailyData.getString("4. close")));
                volumes.add(Integer.parseInt(dailyData.getString("5. volume")));
            }

            double open = roundDecimal(timeSeries.getJSONObject(timeStamp).getString("1. open"));
            double high = roundDecimal(timeSeries.getJSONObject(timeStamp).getString("2. high"));
            double low = roundDecimal(timeSeries.getJSONObject(timeStamp).getString("3. low"));
            String range = high + " - " + low;
            int volume =  volumes.get(0);
            double close = price.get(0);
            double lastPrice = price.get(1);
            double change = roundDecimal(String.valueOf(close - lastPrice));
            double changePercent = roundDecimal(String.valueOf((change * 100.0) / lastPrice));
             timeStamp = timeStamp.length() <= 10 ? timeStamp + " 16:00 EDT" : timeStamp + " EDT";
            data.put("low", low);
            data.put("open", open);
            data.put("time", timeStamp);
            data.put("high", high);
            data.put("close", close);
            data.put("lastPrice", lastPrice);
            data.put("change", change);
            data.put("changePercent", changePercent);
            data.put("volume", volume);
            data.put("price", price);
            data.put("range", range);
            data.put("volumes", volumes);
            return data;
    }


    public JSONArray parseNews() throws JSONException{
        JSONArray data = new JSONArray();
        JSONArray items = this.response.getJSONObject("rss").getJSONArray("channel").getJSONObject(0).getJSONArray("item");
        for (int i = 0; i < items.length() ; i++)
        {
            JSONObject temp = items.getJSONObject(i);
            JSONObject item = new JSONObject();
            String title = temp.getJSONArray("title").getString(0);
            String link = temp.getJSONArray("link").getString(0);
            String author = temp.getJSONArray("sa:author_name").getString(0);
            String dateFull = temp.getJSONArray("pubDate").getString(0);
            String date = dateFull.substring(0, dateFull.length() - 6);
            String timeZone = dateFull.substring(dateFull.length() - 5,  dateFull.length());
            if (timeZone.equals("-0500")){
                date = date + " EDT";
            }
            else if (timeZone.equals("-0400")){
                date =  date + " EST";
            }

            item.put("title", title);
            item.put("date", "Date: " + date);
            item.put("link", link);
            item.put("author", "Author: " + author);
            data.put(item);
        }
        return data;
    }

    private double roundDecimal (String num){
        double temp = Double.parseDouble(num);
        DecimalFormat df = new DecimalFormat("#.00");
        String num2Decimal = df.format(temp);
        return Double.parseDouble(num2Decimal);
    }
}
