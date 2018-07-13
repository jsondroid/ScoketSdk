package jsondroid.android.com.scoketsdk.nettools.wifi.Ap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;

public class WifiAPUtil {
	private static final String TAG = "WifiAPUtil";
	public final static boolean DEBUG = true;
	//默认wifi秘密
	private static final String DEFAULT_AP_PASSWORD = "12345678";
	private static WifiAPUtil sInstance;

	private static Context mContext;
	private WifiManager mWifiManager;
	//监听wifi热点的状态变化
	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED = 11;
	public static final int WIFI_AP_STATE_ENABLING = 12;
	public static final int WIFI_AP_STATE_ENABLED = 13;
	public static final int WIFI_AP_STATE_FAILED = 14;

	public boolean isRegister=false;
	public enum WifiSecurityType {
		WIFICIPHER_NOPASS, WIFICIPHER_WPA, WIFICIPHER_WEP, WIFICIPHER_INVALID, WIFICIPHER_WPA2
	}
	private WifiAPUtil(Context context) {
		if(DEBUG) Log.d(TAG,"WifiAPUtils construct");
		mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	protected void finalize() {
		if(DEBUG) Log.d(TAG,"finalize");
		if(isRegister){
			unregisterReceiver();
			isRegister=false;
		}


	}
	public void unregisterReceiver(){
		if(isRegister){
			if(mWifiStateBroadcastReceiver!=null){
				mContext.unregisterReceiver(mWifiStateBroadcastReceiver);
				mWifiStateBroadcastReceiver=null;
				isRegister=false;
			}
		}

	}
	public static WifiAPUtil getInstance(Context c) {
		if (null == sInstance)
			sInstance = new WifiAPUtil(c);
		return sInstance;
	}

	//获取热点状态

		private int getWifiAPState() {
			         int state = -1;
			         try {
				             Method method2 = mWifiManager.getClass().getMethod("getWifiApState");
				             state = (Integer) method2.invoke(mWifiManager);
				         } catch (Exception e) {}
			         Log.d("WifiAP", "getWifiAPState.state " + (state));
			         return state;
			     }


	/**开启热点*/
	public void startAP(){
		//boolean wifiApIsOn = getWifiAPState()==WIFI_AP_STATE_ENABLED || getWifiAPState()==WIFI_AP_STATE_ENABLING;
	/*	if(wifiApIsOn){
			if(apCallBack!=null)
			apCallBack.openAPState(MESSAGE_AP_STATE_ENABLED,getValidApSsid(),getValidPassword(),getValidSecurity());
		}else {*/
			new Thread(new Runnable() {
				@Override
				public void run() {
					setWifiApEnabled();
				}
			}).start();
		//}
	}


	private boolean isNowOpenAp = false;
	public boolean isSendAp = false;
	private boolean setWifiApEnabled() {
		//开启wifi热点需要关闭wifi
		/*while(mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED){
			mWifiManager.setWifiEnabled(false);
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
		}*/
		/*// 确保wifi 热点关闭。
		while(getWifiAPState() != WIFI_AP_STATE_DISABLED){
			try {
				Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
						WifiConfiguration.class, boolean.class);
				method1.invoke(mWifiManager, null, false);

				Thread.sleep(200);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				return false;
			}
		}*/
		//开启wifi热点
		mWifiManager.setWifiEnabled(false);
		try {
			Thread.sleep(200);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		try {
			isNowOpenAp = true;
			Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			boolean b = (boolean)method1.invoke(mWifiManager, null, true);
			Log.e(TAG, "setWifiApEnabled: "+b );
			if(!b){
				//开启失败
			}
			mHandler.sendEmptyMessage(88);
			Thread.sleep(3000);
			if(isSendAp){
				isSendAp = false;
			}else{
				mHandler.sendEmptyMessageDelayed(MESSAGE_AP_STATE_ENABLED,2000);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			isNowOpenAp = false;
			//开启异常处理
			return false;
		}
		return true;
	}


	public static  int MESSAGE_AP_STATE_ENABLED=13;//开启热点成功
	public static int MESSAGE_AP_STATE_FAILED=15;//开启热点失败
	private  APCallBack apCallBack;//回调热点状态

	public WifiAPUtil setApCallBack(APCallBack apCallBack) {
		this.apCallBack = apCallBack;
		this.isSendAp = false;
		return sInstance;
	}

	private Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==88){
				IntentFilter filter = new IntentFilter();
				filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
				mContext.registerReceiver(mWifiStateBroadcastReceiver, filter);
				isRegister=true;
			}else{
				if(apCallBack!=null)
					apCallBack.openAPState(msg.what,getValidApSsid(),getValidPassword(),getValidSecurity());
			}
		}
	};
	//监听wifi热点状态变化
	private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(DEBUG)Log.i(TAG,"热点广播接收: "+intent.getAction());
			if(WIFI_AP_STATE_CHANGED_ACTION.equals(intent.getAction())) {

				int cstate = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1);
				Log.e(TAG,"热点连接状态: "+cstate);
				if(cstate == WIFI_AP_STATE_ENABLED) {
					if(mHandler != null&&getWifiAPState()==WIFI_AP_STATE_ENABLED){
						isSendAp = true;
						mHandler.sendEmptyMessageDelayed(MESSAGE_AP_STATE_ENABLED,3000);
					}else{
						if(mHandler != null)
							mHandler.sendEmptyMessage(MESSAGE_AP_STATE_FAILED);
					}
				}if(cstate == WIFI_AP_STATE_DISABLED||cstate == WIFI_AP_STATE_FAILED) {
					if(isNowOpenAp){
						isNowOpenAp = false;
						return;
					}
					if(mHandler != null)
						mHandler.sendEmptyMessage(MESSAGE_AP_STATE_FAILED);
				}
			}
		}
	};
	//获取热点ssid
	public String getValidApSsid() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration configuration = (WifiConfiguration)method.invoke(mWifiManager);
			return configuration.SSID;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	//获取热点Bssid
	public String getValidApBSsid() {
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration configuration = (WifiConfiguration)method.invoke(mWifiManager);
			return configuration.BSSID;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
	}
	//获取热点密码
	public String getValidPassword(){
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			WifiConfiguration configuration = (WifiConfiguration)method.invoke(mWifiManager);
			return configuration.preSharedKey;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}

	}
	//获取热点安全类型
	public int getValidSecurity(){
		WifiConfiguration configuration;
		try {
			Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
			configuration = (WifiConfiguration)method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return WifiSecurityType.WIFICIPHER_INVALID.ordinal();
		}

		if(DEBUG)Log.i(TAG,"getSecurity security="+configuration.allowedKeyManagement);
		if(configuration.allowedKeyManagement.get(KeyMgmt.NONE)) {
			return WifiSecurityType.WIFICIPHER_NOPASS.ordinal();
		}else if(configuration.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
			return WifiSecurityType.WIFICIPHER_WPA.ordinal();
		}else if(configuration.allowedKeyManagement.get(4)) { //4 means WPA2_PSK 
			return WifiSecurityType.WIFICIPHER_WPA2.ordinal();
		}
		return WifiSecurityType.WIFICIPHER_INVALID.ordinal();
	}


	public interface APCallBack{
		public void openAPState(int state, String apssid, String appwd, int pwdtype);
	}
}
