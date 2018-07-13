package jsondroid.android.com.scoketsdk.socketnetwork.service;

/**
 * Created by wenbaohe on 2018/6/6.
 */

public interface ISocketIml {

    public void onStartConnet();

    public void onDisConnet();

    public boolean sendMsg(byte[] msg);


}
