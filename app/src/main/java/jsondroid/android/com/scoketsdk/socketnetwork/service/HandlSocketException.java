package jsondroid.android.com.scoketsdk.socketnetwork.service;

import android.util.Log;

import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by wenbaohe on 2018/6/6.
 */

public class HandlSocketException {

    public static boolean handleConnetFiald(Throwable e) {
        if ((e instanceof SocketTimeoutException) && e.getMessage().contains("failed to connect to") || (e instanceof SocketException) || e.getMessage().contains("Socket closed")) {
            return true;
        }
        return false;
    }

    public static boolean handleConnetReset(Throwable e) {
        if (e.getMessage().contains("Connection reset") && (e instanceof SocketException)) {
            return true;
        }
        return false;
    }

    public static boolean handleConnetClose(Throwable e) {
        if (e.getMessage().contains("Socket closed") && (e instanceof SocketException)) {
            return true;
        }
        return false;
    }
}
