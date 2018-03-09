package node.frontend.titletab;

/**
 * Created by WENTAO on 11/25/2017.
 */

public class OneNews {
    private String title;
    private String author;
    private String date;
    private String link;


    public OneNews(String title, String author, String date, String link){
        this.title = title;
        this.author = author;
        this.date = date;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getLink() {
        return link;
    }
}
