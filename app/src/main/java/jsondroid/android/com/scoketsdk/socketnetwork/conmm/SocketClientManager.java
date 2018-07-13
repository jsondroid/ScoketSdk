package jsondroid.android.com.scoketsdk.socketnetwork.conmm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jsondroid.android.com.scoketsdk.socketnetwork.listener.OnReceiveFileListener;
import jsondroid.android.com.scoketsdk.socketnetwork.listener.OnSocketConnetListener;
import jsondroid.android.com.scoketsdk.socketnetwork.service.IOnReceiveFileListener;
import jsondroid.android.com.scoketsdk.socketnetwork.service.IOnSocketConnetListener;
import jsondroid.android.com.scoketsdk.socketnetwork.service.SocketBuilder;
import jsondroid.android.com.scoketsdk.socketnetwork.service.SocketService;

/**
 * Created by wenbaohe on 2018/6/12.
 */

public class SocketClientManager {

    private static SocketClientManager instance;
    private SocketService socketService;

    public static SocketClientManager getInstance() {
        if (instance == null) {
            synchronized (SocketClientManager.class) {
                if (instance == null) {
                    instance = new SocketClientManager();
                }
            }
        }
        return instance;
    }

    public SocketClientManager setService(IBinder service) {
        socketService = ((SocketService.LocalBinder) service).getSocketService(onSocketConnetListener);
        return instance;
    }

    public void initSocket(SocketBuilder socketBuilder) {
        socketService.initSocket(socketBuilder);
    }


    public synchronized void startConnet() {
        if (socketService != null) {
            socketService.onStartConnet();
        }
    }

    public synchronized void onDisConnet() {
        if (socketService != null) {
            socketService.onDisConnet();
        }
    }

    public synchronized boolean sendMSG(byte[] data) {
        if (socketService != null) {
            return socketService.sendMsg(data);
        }
        return false;
    }

    public synchronized void sendMSG(String data, String path) {
        if (socketService != null) {
            socketService.setReceive(data, path, onReceiveFileListener);
        }
    }


    private List<OnSocketConnetListener> onSocketConnetListeners = new ArrayList<>();
    private List<OnReceiveFileListener> onReceiveFileListeners = new ArrayList<>();

    public void setOnReceiveFileListener(OnReceiveFileListener onReceiveFileListener) {
        if (onReceiveFileListeners.contains(onReceiveFileListener)) {
            onReceiveFileListeners.remove(onReceiveFileListener);
        }
        onReceiveFileListeners.add(onReceiveFileListener);
    }

    public void addOnReceiveFileListener(OnReceiveFileListener onReceiveFileListener) {
        if (!onReceiveFileListeners.contains(onReceiveFileListener)) {
            onReceiveFileListeners.add(onReceiveFileListener);
        }
    }

    public void removeOnReceiveFileListener(OnReceiveFileListener onReceiveFileListener) {
        if (onReceiveFileListeners.contains(onReceiveFileListener)) {
            onReceiveFileListeners.remove(onReceiveFileListener);
        }
    }

    public void setOnSocketConnetListener(OnSocketConnetListener onSocketConnetListener) {
        if (onSocketConnetListeners.contains(onSocketConnetListener)) {
            onSocketConnetListeners.remove(onSocketConnetListener);
        }
        onSocketConnetListeners.add(onSocketConnetListener);
    }

    public void addOnSocketConnetListener(OnSocketConnetListener onSocketConnetListener) {
        if (!onSocketConnetListeners.contains(onSocketConnetListener)) {
            onSocketConnetListeners.add(onSocketConnetListener);
        }
    }

    public void removeOnSocketConnetListener(OnSocketConnetListener onSocketConnetListener) {
        if (onSocketConnetListeners.contains(onSocketConnetListener)) {
            onSocketConnetListeners.remove(onSocketConnetListener);
        }
    }

    /**
     * 连接状态
     */
    private IOnSocketConnetListener onSocketConnetListener = new IOnSocketConnetListener() {
        @Override
        public void onReConneting() {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnSocketConnetListener> connetListeners = onSocketConnetListeners;
                    for (OnSocketConnetListener connetListener : connetListeners) {
                        if (connetListener != null) {
                            connetListener.onReConneting();
                        }
                    }
                }
            });
        }

        @Override
        public void onConnet() {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnSocketConnetListener> connetListeners = onSocketConnetListeners;
                    for (OnSocketConnetListener connetListener : connetListeners) {
                        if (connetListener != null) {
                            connetListener.onConnet();
                        }
                    }
                }
            });
        }

        @Override
        public void onDisConnet(String address) {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnSocketConnetListener> connetListeners = onSocketConnetListeners;
                    for (OnSocketConnetListener connetListener : connetListeners) {
                        if (connetListener != null) {
                            connetListener.onDisConnet();
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Throwable e) {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @Override
        public void onConnetFail(String address, Throwable e) {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnSocketConnetListener> connetListeners = onSocketConnetListeners;
                    for (OnSocketConnetListener connetListener : connetListeners) {
                        if (connetListener != null) {
                            connetListener.onConnetFail();
                        }
                    }
                }
            });
        }
    };

    /**
     * 接收文件
     */
    private IOnReceiveFileListener onReceiveFileListener = new IOnReceiveFileListener() {
        @Override
        public void onReceiveing(final int progress) {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnReceiveFileListener> onReceiveFileListeners1 = onReceiveFileListeners;
                    for (OnReceiveFileListener onReceiveFileListener : onReceiveFileListeners1) {
                        if (onReceiveFileListener != null) {
                            onReceiveFileListener.onReceiveing(progress);
                        }
                    }
                }
            });
        }

        @Override
        public void onSuccess() {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnReceiveFileListener> onReceiveFileListeners1 = onReceiveFileListeners;
                    for (OnReceiveFileListener onReceiveFileListener : onReceiveFileListeners1) {
                        if (onReceiveFileListener != null) {
                            onReceiveFileListener.onSuccess();
                        }
                    }
                }
            });
        }

        @Override
        public void onFial() {
            MainHandler.getInstance().runInMainThread(new Runnable() {
                @Override
                public void run() {
                    List<OnReceiveFileListener> onReceiveFileListeners1 = onReceiveFileListeners;
                    for (OnReceiveFileListener onReceiveFileListener : onReceiveFileListeners1) {
                        if (onReceiveFileListener != null) {
                            onReceiveFileListener.onFial();
                        }
                    }
                }
            });
        }
    };


    /**
     * 注册广播
     */
    private boolean isReg = false;

    public void registerReceiver(Context context) {
        if (!isReg) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.setPriority(1000);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            context.registerReceiver(wifibroadcast, intentFilter);
            isReg = true;
        }

    }

    public void unregisterReceiver(Context context) {
        if (isReg) {
            context.unregisterReceiver(wifibroadcast);
            isReg = false;
        }
    }

    /**
     * 监听网络情况
     */
    private BroadcastReceiver wifibroadcast = new BroadcastReceiver() {
        String TAG = BroadcastReceiver.class.getName();

        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();//获取网络的连接情况
            if (activeNetInfo != null && activeNetInfo.isAvailable()) {
                int sttype = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);//这里的获取状态值和activeNetInfo.getType()是不一样的
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && sttype == ConnectivityManager.TYPE_WIFI) {
                    Log.d(TAG, "已连接到wifi" + activeNetInfo.getTypeName());
                } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE && sttype == ConnectivityManager.TYPE_MOBILE) {
                    Log.e(TAG, "已连接到数据流量" + activeNetInfo.getTypeName());
                }
            } else {
                Log.d(TAG, "未连接网络");
                MainHandler.getInstance().runInMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onDisConnet();
                    }
                });
            }
        }
    };


    public void relese() {
        onReceiveFileListeners.clear();
        onSocketConnetListeners.clear();
    }
}
