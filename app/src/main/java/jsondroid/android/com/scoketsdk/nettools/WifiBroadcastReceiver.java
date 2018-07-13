package jsondroid.android.com.scoketsdk.nettools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import jsondroid.android.com.scoketsdk.nettools.interfaceinfo.OnPingListenter;
import jsondroid.android.com.scoketsdk.nettools.interfaceinfo.OnWifiListenter;


/**
 * Created by  Jsondroid on 2017/5/12.
 * 监听网络变化
 */

public class WifiBroadcastReceiver extends BroadcastReceiver implements OnPingListenter {

    private final String TAG = "WifiReceiver--->";

    private PingUtils pingUtils;

    private OnWifiListenter onWifiListenter;

    public WifiBroadcastReceiver(OnWifiListenter onWifiListenter, PingUtils.PingBulder bulder) {
        this.onWifiListenter = onWifiListenter;
        if (bulder != null) {
            this.pingUtils = bulder.build();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();//获取网络的连接情况
        if (activeNetInfo != null && activeNetInfo.isAvailable()) {
            int sttype = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);//这里的获取状态值和activeNetInfo.getType()是不一样的
            if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && sttype == ConnectivityManager.TYPE_WIFI) {
                Log.d(TAG, "已连接到wifi" + activeNetInfo.getTypeName());
                if (pingUtils != null) {
                    pingUtils.onPing(this,ConnectivityManager.TYPE_WIFI);
                }
                if (onWifiListenter != null) {
                    onWifiListenter.onConnet(activeNetInfo.getType(), activeNetInfo.getTypeName(), "");
                }
            } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE && sttype == ConnectivityManager.TYPE_MOBILE) {
                Log.e(TAG, "已连接到数据流量" + activeNetInfo.getTypeName());
                if (pingUtils != null) {
                    pingUtils.onPing(this,ConnectivityManager.TYPE_WIFI);
                }
                if (onWifiListenter != null) {
                    onWifiListenter.onConnet(activeNetInfo.getType(), activeNetInfo.getTypeName(), "");
                }
            }
        } else {
            Log.d(TAG, "未连接网络");
            if (onWifiListenter != null) {
                onWifiListenter.ondisConnet(-1);
            }
        }
    }


    @Override
    public void onSuccess(int status,int nettype) {
        if (onWifiListenter != null) {
            onWifiListenter.onNetAvailable(status,nettype);
        }
    }

    @Override
    public void onFail(int status, Throwable throwable) {
        if (onWifiListenter != null) {
            onWifiListenter.onNetDislable(status);
        }
    }
}
