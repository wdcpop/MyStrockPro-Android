package node.frontend.titletab;

/**
 * Created by WENTAO on 11/27/2017.
 */

public class FavItem {
    private String symbol;
    private String price;
    private String change;
    private String changePercent;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public FavItem(String symbol, String price, String change, String changePercent){
        this.symbol = symbol;
        this.price = price;
        this.change = change;
        this.changePercent = changePercent;
    }
}
