package jsondroid.android.com.scoketsdk.socketnetwork.service;

/**
 * 配置socket
 */
public class SocketBuilder {
    private long timeOut;
    private long reNettimeOut;
    private String ipaddress;
    private int port;
    private boolean isReConnet = false;//是否重连
    private int recount = 3;//重连次数

    public SocketBuilder() {
        this.timeOut = 5 * 1000;
        this.reNettimeOut = 5 * 1000;
        this.ipaddress = "";
        this.port = -1;
        this.isReConnet = false;
        this.recount = 3;
    }

    public long getReNettimeOut() {
        return reNettimeOut;
    }

    public void setReNettimeOut(long reNettimeOut) {
        this.reNettimeOut = reNettimeOut;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isReConnet() {
        return isReConnet;
    }

    public void setReConnet(boolean reConnet) {
        isReConnet = reConnet;
    }

    public int getRecount() {
        return recount;
    }

    public void setRecount(int recount) {
        this.recount = recount;
    }
}