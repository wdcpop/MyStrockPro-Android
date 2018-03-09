package node.frontend.titletab;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class InfoTableAdapater extends BaseAdapter {

    private Context c;
    private List<Row> list;
    private LayoutInflater inflater;

    public InfoTableAdapater(Context context, List<Row> list){
        this.c = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(list!=null){
            ret = list.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Row Row = (Row) this.getItem(position);

        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.tablerow, null);
            viewHolder.colOne = (TextView) convertView.findViewById(R.id.columnOne);
            viewHolder.colTwo = (TextView) convertView.findViewById(R.id.columnTwo);
            viewHolder.colThree = (ImageView) convertView.findViewById(R.id.columnThree);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.colOne.setText(Row.getName());
        viewHolder.colOne.setTextSize(16);
        viewHolder.colTwo.setText(Row.getInfo());
        viewHolder.colTwo.setTextSize(16);

        if (Row.getName().equals("Change")){
            System.out.println("Row.getInfo()");
            System.out.println(Row.getInfo().split("\\s\\(")[0]);
            Double changeDouble = Double.parseDouble(Row.getInfo().split("\\s\\(")[0]);
            Drawable img;
            if (changeDouble < 0){
                img = c.getResources().getDrawable(R.drawable.red);
            }
            else {
                img = c.getResources().getDrawable(R.drawable.green);
            }

            System.out.println(img);
            viewHolder.colThree.setImageDrawable(img);
        }

        return convertView;
    }



    public static class ViewHolder{
        public TextView colOne;
        public TextView colTwo;
        public ImageView colThree;
    }

}