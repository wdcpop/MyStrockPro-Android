package node.frontend.titletab;

/**
 * Created by WENTAO on 11/25/2017.
 */

public class Row {
    private String name;
    private String info;


    public Row(String name, String content){
        this.name = name;
        this.info = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
