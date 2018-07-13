package jsondroid.android.com.scoketsdk.nettools.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wenbaohe on 2016/12/14.
 *
 * wifi 工具类
 */

public class WifiTools {

    private static WifiTools wifitootls;
    private Context context;
    private WifiManager wifiManager;


    public static WifiTools getinstance(Context context){
        synchronized (WifiTools.class) {
            if (wifitootls == null) {
                wifitootls = new WifiTools(context);
            }
        }
        return wifitootls;
    }
    public WifiTools(Context context) {
        this.context = context;
        wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    }

    /**是否打开wifi*/
    public boolean isOpenWifi(){
        return wifiManager.isWifiEnabled();
    }

    /**打开wifi*/
    public void openWifi(){
        wifiManager.setWifiEnabled(true);
    }

    /**是否连接WIFI*/
    public  boolean isWifiConnected()
    {
        if(!isOpenWifi()){
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();

    }


    /**判断当前连接的wifi是否为5g*/
    public boolean is5GHg(){
        String bssid=wifiManager.getConnectionInfo().getBSSID();
        return is5GHg(bssid);
    }

    /**根据ssid判断当前连接的wifi是否为5g*/
    public boolean is5GHg(String ssid){
        if(!isOpenWifi()){
            return true;
        }
        List<ScanResult> scanResults=wifiManager.getScanResults();
        for (ScanResult scanResult:scanResults){
            if(scanResult.SSID.equals(ssid)){
                String s=scanResult.frequency+"";
                return s.startsWith("5");
            }
        }
        return false;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /**获取当前连接wifi的SSID*/
    public String getConnetWifiSSID(){
        String ssid="";
        if(isOpenWifi()&&isWifiConnected()){
            ssid=wifiManager.getConnectionInfo().getSSID();
        }
        return replace(ssid);
    }


    /**获取当前连接wifi的BSSID*/
    public String getConnetWifiBSSid(){
        String bssid="";
        if(isOpenWifi()&&isWifiConnected()){
            bssid=wifiManager.getConnectionInfo().getBSSID();
        }
        return replace(bssid);
    }
    /**根据ssid获取wifi的密码加密类型*/
    public int getWifiPwdType(String ssid){
        int pwdtype=1;
        if(isOpenWifi()&&isWifiConnected()){
            //通过本地搜索wifi列表去拿实时的加密类型
            List<ScanResult> wifils= getScanResult();
            for(ScanResult scanResult:wifils){
                if(scanResult.SSID.equals(ssid)){
                    pwdtype=getSecurity(scanResult.capabilities);
                    break;
                }
            }
        }
        return pwdtype;
    }
    /**根据ssid获取wifi的密码加密类型*/
    public String getWifiCapabilities(String ssid){
        String capabilities="";
        if(isOpenWifi()&&isWifiConnected()){
            //通过本地搜索wifi列表去拿实时的加密类型
            List<ScanResult> wifils= getScanResult();
            for(ScanResult scanResult:wifils){
                if(replace(scanResult.SSID).equals(ssid)){
                    capabilities=scanResult.capabilities;
                    return capabilities;
                }
            }
        }
        return capabilities;
    }
    /**判断加密类型*/
    public int getSecurity(String capabilities) {
        int type = 1;
        if (!TextUtils.isEmpty(capabilities)) {
            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                type = 3;
            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                type = 2;
            } else {
                type = 1;
            }
        }
        return type;
    }

    //获取当前连接的wifimac地址
    public String getwifiMac(){
        String mac="";
        if(isOpenWifi()&&isWifiConnected()){
            android.net.wifi.WifiInfo wifiInfo=wifiManager.getConnectionInfo();
            if(wifiInfo!=null){
                mac=wifiInfo.getMacAddress();
            }
        }
        return mac;
    }

    /**根据ssid获取本地指定的wifi密码*/
    public String getconenttWifiPWD(String connetingSSID) {
        String pwd = "";
        List<WifiConfiguration> configurationList=getConetWifiList();
        if(configurationList==null||configurationList.isEmpty()){
            return pwd;
        }else{
            for (WifiConfiguration wcf:configurationList){
                if(replace(wcf.SSID).equals(connetingSSID)){
                    pwd=wcf.preSharedKey;
                    break;
                }
            }
        }
        return replace(pwd);
    }

    /**根据ssid获取单个wifi的ssid和密码*//*
    public WifiInfos getWifiIno(String ssid){
        WifiInfos wifiInfo= null;
        *//**是否root过的*//*
        if(isRoot()){
            try {
                List<WifiInfos>wifiInfoses=Read();
                if(wifiInfoses!=null&&!wifiInfoses.isEmpty()){
                    for (WifiInfos wf:wifiInfoses){
                        if(wf.Ssid.equals(ssid)){
                            return wf;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else{
            wifiInfo=new WifiInfos();
            wifiInfo.Password=getconenttWifiPWD(ssid);
            wifiInfo.Ssid=ssid;
        }

        return wifiInfo;
    }*/

    /**获取本地连接过的wifi配置信息*/
    private List<WifiConfiguration> getConetWifiList(){
        List<WifiConfiguration> configurationList=null;
        Method method = null;
        try {
            method = wifiManager.getClass().getMethod("getConfiguredNetworks");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            configurationList = (List<WifiConfiguration>) method.invoke(wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configurationList;
    }

    /**获取搜索到的wifi列表*/
    public ArrayList<ScanResult> getScanResult(){
        return (ArrayList<ScanResult>)wifiManager.getScanResults();
    }

    //获取wifi密码
    public List<WifiInfos> Read() throws Exception {

        List<WifiInfos> wifiInfos=new ArrayList<WifiInfos>();

        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/*.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                throw e;
            }
        }
        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
        Matcher networkMatcher = network.matcher(wifiConf.toString() );
        while (networkMatcher.find() ) {
            String networkBlock = networkMatcher.group();
            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
            Matcher ssidMatcher = ssid.matcher(networkBlock);

            if (ssidMatcher.find() ) {
                WifiInfos wifiInfo=new WifiInfos();
                wifiInfo.Ssid=ssidMatcher.group(1);
                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                Matcher pskMatcher = psk.matcher(networkBlock);
                if (pskMatcher.find() ) {
                    wifiInfo.Password=pskMatcher.group(1);
                } else {
                    wifiInfo.Password="";//无密码
                }
                wifiInfos.add(wifiInfo);
            }

        }

        return wifiInfos;
    }
    public  class WifiInfos {
        public String Ssid="";
        public String Password="";
    }


    /**获取搜索到的wifi*/
    public ArrayList<ScanResult> getActivWifiList(){
        ArrayList<ScanResult> scanResults=new ArrayList<>();
        List<ScanResult> ls=wifiManager.getScanResults();
        for (ScanResult scn:ls) {
            scanResults.add(scn);
        }
        return scanResults;
    }

    /**去除前后双引号*/
    private String replace(String str){
        if(str!=null&&!str.isEmpty()){
            if((str.startsWith("\"")&&str.endsWith("\""))||(str.startsWith("\\“")&&str.endsWith("\\”"))){
                if(str.indexOf("\"")==0) str = str.substring(1,str.length());   //去掉第一个 "
                String sstr = str.substring(0,str.length()-1);  //去掉最后一个 "
                return sstr;
            }
        }
        return str;
    }

    /**是否被root*/
    public boolean isRoot(){
        boolean bool = false;
        try{
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())){
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {

        }
        return bool;
    }

    /**获取当前网络的网关*/
    public String getGateway(){
        DhcpInfo di = wifiManager.getDhcpInfo();
        long getewayIpL=di.gateway;
        String getwayIpS=long2ip(getewayIpL);//网关地址
        if (getwayIpS==null||getwayIpS.isEmpty())
            return "";

        return getwayIpS;
    }

    /**获取当前网络的网关*/
    public String getNetmask(){
        DhcpInfo di = wifiManager.getDhcpInfo();
        long netmaskIpL=di.netmask;
        String netmaskIpS=long2ip(netmaskIpL);//子网掩码地址

        if (netmaskIpS==null||netmaskIpS.isEmpty())
            return "";

        return netmaskIpS;
    }
    public String long2ip(long ip){
        StringBuffer sb=new StringBuffer();
        sb.append(String.valueOf((int)(ip&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>8)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>16)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>24)&0xff)));
        return sb.toString();
    }

    public static class SharpWifiInfo{
        private String ssid;
        private String pwd;
        private String capabilities;

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getPwd() {
            return pwd;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }

        public String getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(String capabilities) {
            this.capabilities = capabilities;
        }
    }
}
