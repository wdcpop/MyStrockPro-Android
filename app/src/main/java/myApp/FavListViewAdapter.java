package node.frontend.titletab;

import android.content.ContentProvider;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class FavListViewAdapter extends ArrayAdapter<FavItem> {

    private List<FavItem> list;
    private LayoutInflater inflater;


    public FavListViewAdapter(List<FavItem> list, Context context){
        super(context, android.R.layout.simple_list_item_1, list);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FavItem favItem = (FavItem) this.getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.favitem, parent, false);
        }

        TextView symbol = (TextView) convertView.findViewById(R.id.favListSymbol);
        TextView price = (TextView) convertView.findViewById(R.id.price);
        TextView change = (TextView) convertView.findViewById(R.id.change);


        symbol.setText(favItem.getSymbol());
        price.setText(favItem.getPrice());
        String changeStr = favItem.getChange();
        if (changeStr.indexOf("-") < 0){
            change.setTextColor(Color.GREEN);
        }
        else {
            change.setTextColor(Color.RED);
        }
        change.setText(changeStr + "(" + favItem.getChangePercent() + ")");
        return convertView;
    }


}