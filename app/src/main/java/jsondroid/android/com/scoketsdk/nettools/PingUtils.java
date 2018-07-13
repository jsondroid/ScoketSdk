package jsondroid.android.com.scoketsdk.nettools;

import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jsondroid.android.com.scoketsdk.nettools.interfaceinfo.OnPingListenter;

/**
 * Created by wenbaohe on 2018/5/18.
 */

public class PingUtils {
    final String netAddress;//网络地址（ip或完整的域名地址）
    final String linuxcmd;//linux命令
    final int pingcount;//ping 的次数
    final long pingTimeout;//ping的超时时间（单位：毫秒）
    private int nettype = -1;//网络类型（wifi、数据流量）

    private OnPingListenter onPingListenter;

    private PingUtils() {
        this(new PingBulder());
    }

    public PingUtils(PingBulder pingBulder) {
        this.netAddress = pingBulder.netAddress;
        this.pingcount = pingBulder.pingcount;
        this.pingTimeout = pingBulder.pingTimeout;

        if (TextUtils.isEmpty(pingBulder.linuxcmd)) {
            this.linuxcmd = "ping -c " + pingcount + " -w " + pingTimeout + " " + this.netAddress;
        } else {
            this.linuxcmd = pingBulder.linuxcmd;
        }
//        Log.e("PingUtils--->",pingBulder.toString());
    }

    public synchronized void onPing(OnPingListenter onPingListenter, int nettype) {
        this.onPingListenter = onPingListenter;
        this.nettype = nettype;
        new PingThread().start();
        Log.e("PingUtils--->", this.toString());
    }

    private class PingThread extends Thread {
        @Override
        public void run() {
            synchronized (PingThread.class) {
                ping();
            }
        }
    }

    private boolean ping() {
        InputStream input = null;
        BufferedReader in;
        StringBuffer stringBuffer;
        try {
            Process p = Runtime.getRuntime().exec(this.linuxcmd);
            // 读取ping的内容
            input = p.getInputStream();
            in = new BufferedReader(new InputStreamReader(input));
            stringBuffer = new StringBuffer();
            String content = "";
            while ((content = in.readLine()) != null) {
                stringBuffer.append(content);
            }
            // PING的状态
            int status = p.waitFor();  //status 为 0 ，ping成功，即为可以对外访问；为2则失败，即联网但不可以上网
            if (status == 0) {
                if (onPingListenter != null) {
                    onPingListenter.onSuccess(status, nettype);
                }
                return true;
            } else {
                if (onPingListenter != null) {
                    onPingListenter.onFail(status, new Throwable("ping status is " + status));
                }
                return false;
            }
        } catch (Exception e) {
            if (onPingListenter != null) {
                onPingListenter.onFail(2, e);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    public static final class PingBulder {
        private String netAddress;//网络地址（ip或完整的域名地址）
        private String linuxcmd;//linux命令
        private int pingcount;//ping 的次数
        private long pingTimeout;//ping的超时时间（单位：毫秒）

        public PingBulder() {
            this.netAddress = "www.baidu.com";
            this.pingcount = 2;
            this.pingTimeout = 100;
        }

        PingBulder(PingUtils pingUtils) {
            this.netAddress = pingUtils.netAddress;
            this.pingcount = pingUtils.pingcount;
            this.pingTimeout = pingUtils.pingTimeout;
            this.linuxcmd = pingUtils.linuxcmd;
        }

        public PingBulder setNetAddress(String netAddress) {
            if (TextUtils.isEmpty(netAddress)) {
                throw new NullPointerException("netAddress is Null");
            } else if (netAddress.startsWith("http://") || netAddress.startsWith("https://")) {
                this.netAddress = CheckUtils.getCompleteDomainName(netAddress);
            }
            this.netAddress = netAddress;
            return this;
        }

        public PingBulder setLinuxcmd(String linuxcmd) {
            if (TextUtils.isEmpty(linuxcmd)) {
                this.linuxcmd = "ping -c " + pingcount + " -w " + pingTimeout + " " + this.netAddress;
            } else {
                this.linuxcmd = linuxcmd;
            }
            return this;
        }

        public PingBulder setPingcount(int pingcount) {
            this.pingcount = pingcount;
            return this;
        }

        public PingBulder setPingTimeout(long pingTimeout) {
            this.pingTimeout = pingTimeout;
            return this;
        }

        @Override
        public String toString() {
            return "PingBulder{" +
                    "netAddress='" + netAddress + '\'' +
                    ", linuxcmd='" + linuxcmd + '\'' +
                    ", pingcount=" + pingcount +
                    ", pingTimeout=" + pingTimeout +
                    '}';
        }

        public PingUtils build() {
            return new PingUtils(this);
        }
    }

    @Override
    public String toString() {
        return "PingUtils{" +
                "pingTimeout=" + pingTimeout +
                ", pingcount=" + pingcount +
                ", linuxcmd='" + linuxcmd + '\'' +
                ", netAddress='" + netAddress + '\'' +
                ", nettype=" + nettype +
                '}';
    }
}
