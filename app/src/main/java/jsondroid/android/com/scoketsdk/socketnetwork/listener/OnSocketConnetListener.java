package jsondroid.android.com.scoketsdk.socketnetwork.listener;

/**
 * Created by wenbaohe on 2018/7/12.
 */

public interface OnSocketConnetListener {
    public void onConnet();

    public void onReConneting();//重连中

    public void onDisConnet();

    public void onConnetFail();
}
