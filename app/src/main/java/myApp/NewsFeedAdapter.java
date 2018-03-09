package node.frontend.titletab;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NewsFeedAdapter extends BaseAdapter {

    private List<OneNews> list;
    private LayoutInflater inflater;

    public NewsFeedAdapter(Context context, List<OneNews> list){
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

        OneNews oneNews = (OneNews) this.getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.newsfeed, null);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.author = (TextView) convertView.findViewById(R.id.author);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(oneNews.getTitle());
        viewHolder.author.setText(oneNews.getAuthor());
        viewHolder.date.setText(oneNews.getDate());
        return convertView;
    }


    public static class ViewHolder{
        public TextView title;
        public TextView author;
        public TextView date;
    }

}