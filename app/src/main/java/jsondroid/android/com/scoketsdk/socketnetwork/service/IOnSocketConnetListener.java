package jsondroid.android.com.scoketsdk.socketnetwork.service;

/**
 * Created by wenbaohe on 2018/6/6.
 */

public interface IOnSocketConnetListener {

    public void onReConneting();

    public void onConnet();

    public void onDisConnet(String address);

    public void onError(Throwable e);

    public void onConnetFail(String address, Throwable e);
}
