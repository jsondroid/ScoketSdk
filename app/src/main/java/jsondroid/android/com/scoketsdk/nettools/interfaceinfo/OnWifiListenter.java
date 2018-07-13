package jsondroid.android.com.scoketsdk.nettools.interfaceinfo;

/**
 * Created by wenbaohe on 2018/5/18.
 */

public interface OnWifiListenter {
    public void onConnet(int nettype, String wifiname, String ip);
    public void ondisConnet(int nettype);
    public void onNetAvailable(int status, int nettype);
    public void onNetDislable(int status);
}
