package jsondroid.android.com.scoketsdk.nettools;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import java.lang.ref.WeakReference;

import jsondroid.android.com.scoketsdk.nettools.interfaceinfo.OnWifiListenter;

/**
 * Created by Jsondroid on 2017/5/12.
 * 监控网络工具类
 */

public class NetWorkUtil implements OnWifiListenter {

    private final static int WIFI_CONNET = 0x290;
    private final static int DISCONNET = 0x291;
    private final static int NETAvailable = 0x292;
    private final static int NetDislable = 0x293;

    public final static int TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
    public final static int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;

    private final String key_Status = "Status";
    private final String key_NetType = "NetType";
    private final String key_WifiName = "WifiName";
    private final String key_ip = "ip";


    private WifiBroadcastReceiver wifiBroadcastReceiver;
    private static NetWorkUtil instance;

    public static NetWorkUtil getInstance() {    //对获取实例的方法进行同步
        if (instance == null) {
            synchronized (NetWorkUtil.class) {
                if (instance == null)
                    instance = new NetWorkUtil();
            }
        }
        return instance;
    }

    public synchronized void registerWifiBroadcastReceiver(Context context) {
        registerWifiBroadcastReceiver(context, null);
    }

    public synchronized void registerWifiBroadcastReceiver(Context context, PingUtils.PingBulder pingBulder) {
        if (wifiBroadcastReceiver == null) {
            wifiBroadcastReceiver = new WifiBroadcastReceiver(this, pingBulder);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.setPriority(1000);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(wifiBroadcastReceiver, intentFilter);
        }
        handler = new WeakHandler(context);
    }

    public synchronized void unWifiBroadcastReceiver(Context context) {
        if (wifiBroadcastReceiver != null) {
            context.unregisterReceiver(wifiBroadcastReceiver);
            wifiBroadcastReceiver = null;
        }
        onWifiListenter = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    private OnWifiListenter onWifiListenter;

    public void setOnWifiListenter(OnWifiListenter onWifiListenter) {
        this.onWifiListenter = onWifiListenter;
    }

    private WeakHandler handler;

    private class WeakHandler extends Handler {
        WeakReference<Context> reference;

        public WeakHandler(Context context) {
            this.reference = new WeakReference<Context>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            if (reference.get() == null) {
                return;
            }
            if (onWifiListenter != null) {
                Bundle data = msg.getData();
                switch (msg.what) {
                    case WIFI_CONNET:
                        onWifiListenter.onConnet((int) data.get(key_NetType), (String) data.get(key_WifiName), (String) data.get(key_ip));
                        break;
                    case DISCONNET:
                        onWifiListenter.ondisConnet((int) data.get(key_NetType));
                        break;
                    case NETAvailable:
                        onWifiListenter.onNetAvailable((int) data.get(key_Status), (int) data.get(key_NetType));
                        break;
                    case NetDislable:
                        onWifiListenter.onNetDislable((int) data.get(key_Status));
                        break;
                }
            }

        }
    }

    private void sendMsg(int what, Bundle bundle) {
        Message message = handler.obtainMessage();
        message.what = what;
        message.setData(bundle);
        handler.sendMessage(message);
    }


    /**
     * 监听本地wifi 和数据流量打开关闭
     */
    @Override
    public void onConnet(int nettype, String wifiname, String ip) {
        Bundle bundle = new Bundle();
        bundle.putInt(key_NetType, nettype);
        bundle.putString(key_WifiName, wifiname);
        bundle.putString(key_ip, ip);
        sendMsg(WIFI_CONNET, bundle);
    }

    /**
     * 监听本地wifi 和数据流量打开关闭
     */
    @Override
    public void ondisConnet(int nettype) {
        Bundle bundle = new Bundle();
        bundle.putInt(key_NetType, nettype);
        sendMsg(DISCONNET, bundle);
    }

    /**
     * 可连接网络监听
     */
    @Override
    public void onNetAvailable(int status, int nettype) {
        Bundle bundle = new Bundle();
        bundle.putInt(key_NetType, nettype);
        bundle.putInt(key_Status, status);
        sendMsg(NETAvailable, bundle);
    }

    /**
     * 不可连接网络监听
     */
    @Override
    public void onNetDislable(int status) {
        Bundle bundle = new Bundle();
        bundle.putInt(key_Status, status);
        sendMsg(NetDislable, bundle);
    }


}
