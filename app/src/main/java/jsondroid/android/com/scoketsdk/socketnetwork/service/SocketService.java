package jsondroid.android.com.scoketsdk.socketnetwork.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jsondroid.android.com.scoketsdk.R;

/**
 * Created by wenbaohe on 2018/6/6.
 */

public class SocketService extends Service implements ISocketIml, Runnable {
    private final String TAG = "SocketService--->";

    private final int RECONNET_WHAT = 0x23;

    private long timeOut;
    private long reNettimeOut;//重连间隔时间
    private String ipaddress;
    private int port;
    private boolean isReConnet = false;//是否重连
    private int recount = 3;//重连次数
    private volatile boolean isConneting = false;//是否正在连接中，防止过多连接

    private SocketService.LocalBinder binder = new SocketService.LocalBinder();

    private Socket socket;
    private SocketAddress remoteAddr;
    private ExecutorService executorService;
    private WeakReference<Handler> handler;

    private IOnSocketConnetListener onSocketConnetListener;
    private IOnReceiveFileListener onReceiveFileListener;

    public void initSocket(SocketBuilder socketBuilder) {
        if (socketBuilder == null) {
            socketBuilder = new SocketBuilder();
        }
        this.timeOut = socketBuilder.getTimeOut();
        this.ipaddress = socketBuilder.getIpaddress();
        this.port = socketBuilder.getPort();
        this.isReConnet = socketBuilder.isReConnet();
        this.recount = socketBuilder.getRecount();
        this.reNettimeOut = socketBuilder.getReNettimeOut();

        remoteAddr = new InetSocketAddress(ipaddress, port);
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

        handler = new WeakReference<Handler>(new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECONNET_WHAT:
                        onReConnet();
                        break;
                }
            }
        });
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.app_name) + "正在运行")
                .setContentText("")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        Notification notification = builder.build();
        startForeground(1, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }


    /**
     * 开始连接
     */
    @Override
    public synchronized void onStartConnet() {
        if (!isConneting) {
            if (socket == null || !socket.isConnected()) {
                isRecivceing = false;
                count = recount;
                executorService.execute(this);
            }
            isConneting = true;
        }
    }

    /**
     * 断开连接
     */
    @Override
    public void onDisConnet() {
        if (socket != null) {
            isRecivceing = false;
            try {
                socket.close();
                socket = null;
                isConneting = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (this.onSocketConnetListener != null) {
                onSocketConnetListener.onDisConnet(ipaddress);
            }
        }
        if (handler.get() != null) {
            handler.get().removeCallbacksAndMessages(null);
        }
    }


    /**
     * 发送信息
     */
    @Override
    public boolean sendMsg(final byte[] msg) {
        if (socket != null && socket.isConnected() && isSocketConnet()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 填充信息
                    out.println(new String(msg));
                }
            });
            return true;
        } else {
            return false;
        }

    }


    /**
     * 重连
     */
    private int count;

    private void onReConnet() {
        if (isReConnet) {
            if (count > 0) {
                Log.e("重连次数-->", "" + count);
                executorService.execute(this);
            }
            count--;
        }

    }

    /**
     * 探测是否与服务端断开
     */
    public Boolean isSocketConnet() {
        if (socket != null) {
            try {
                socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
                return true;
            } catch (Exception se) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            Log.e(TAG, "等待连接中...");
            socket.connect(remoteAddr, (int) timeOut);
            if (this.onSocketConnetListener != null) {
                onSocketConnetListener.onConnet();
            }
            Log.e(TAG, "连接成功...");

        } catch (IOException e) {
            /**连接失败后重连*/
            if (HandlSocketException.handleConnetFiald(e)) {
                if (handler.get() != null) {
                    handler.get().sendEmptyMessageDelayed(RECONNET_WHAT, this.reNettimeOut);
                }
                if (this.onSocketConnetListener != null) {
                    this.onSocketConnetListener.onReConneting();
                }
            }
            if (this.onSocketConnetListener != null && (!isReConnet || (isReConnet && count == 0))) {
                onSocketConnetListener.onConnetFail(ipaddress, e);
                Log.e(TAG, "连接失败");
                isConneting = false;
            }
            e.printStackTrace();

            if (socket != null) {
                try {
                    socket.close();
                    socket = null;
                    isConneting = false;
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


    public InetAddress getInetAddress() {
        if (socket != null && !socket.isClosed()) {
            return socket.getInetAddress();
        }
        return null;
    }

    public InetAddress getLocalAddress() {
        if (socket != null && !socket.isClosed()) {
            return socket.getLocalAddress();
        }
        return null;
    }

    public int getLocalPort() {
        if (socket != null && !socket.isClosed()) {
            return socket.getLocalPort();
        }
        return port;
    }

    public int getPort() {
        if (socket != null && !socket.isClosed()) {
            return socket.getPort();
        }
        return -1;
    }


    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public SocketService getSocketService(IOnSocketConnetListener onSocketConnetListener) {
            SocketService.this.onSocketConnetListener = onSocketConnetListener;
            return SocketService.this;
        }
    }


    @Override
    public void onDestroy() {
        onDisConnet();
        if (handler.get() != null) {
            handler.get().removeCallbacksAndMessages(null);
            handler.clear();
            handler = null;
        }
        executorService.shutdownNow();
        stopForeground(true);
        super.onDestroy();
    }


    private volatile int filesize = 0;
    private volatile boolean isRecivceing = false;//是否正在连接

    /**
     * 只能单线程使用
     */
    public void setReceive(String cmd, String filepath, IOnReceiveFileListener onReceiveFileListener) {
        this.onReceiveFileListener = onReceiveFileListener;
        if (!isRecivceing) {
            filesize = 0;
            executorService.execute(new ReceiveFileThread(filepath, socket));
            sendMsg(cmd.getBytes());
            isRecivceing = true;
        }

    }


    public class ReceiveFileThread extends Thread {
        private String filepath;
        private Socket socketFile;

        public ReceiveFileThread(String filepath, Socket socketFile) {
            this.filepath = filepath;
            this.socketFile = socketFile;
        }

        @Override
        public void run() {
            if (socketFile != null && socketFile.isConnected()) {
                Log.e("线程---", "" + this.getId());
                try {
                    receiveFile(socketFile.getInputStream(), filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                    onDisConnet();
                    if (HandlSocketException.handleConnetClose(e)) {
                        if (onSocketConnetListener != null) {
                            onSocketConnetListener.onDisConnet(ipaddress);
                        }
                    }
                    if (HandlSocketException.handleConnetReset(e)) {
                        if (onReceiveFileListener != null) {
                            onReceiveFileListener.onFial();
                        }
                    }
                }
            }

        }
    }

    public synchronized void receiveFile(InputStream input, String path) {
        byte[] inputByte = null;
        int length = 0;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        try {
            try {
                File dir = new File(Environment.getExternalStorageDirectory().getPath());
                if (!dir.exists())
                    dir.mkdirs();
                dis = new DataInputStream(input);
                File file = new File(path);
                fos = new FileOutputStream(file);
                inputByte = new byte[1024];
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    String smg = new String(Arrays.copyOf(inputByte, length)).trim();
                    if (smg.equals("OK")) {
                        break;
                    }
                    fos.write(inputByte, 0, length);
                    fos.flush();
                    filesize = filesize + length;
                    isRecivceing = true;
                    Log.e("接收中--->", filesize + "---" + length);
                    if (onReceiveFileListener != null) {
                        onReceiveFileListener.onReceiveing(filesize);
                    }
                }
                isRecivceing = false;
                System.out.println("接收完成");
                if (onReceiveFileListener != null) {
                    onReceiveFileListener.onSuccess();
                }
            } finally {
                if (fos != null)
                    fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            onDisConnet();
            if (HandlSocketException.handleConnetClose(e)) {
                if (onSocketConnetListener != null) {
                    onSocketConnetListener.onDisConnet(ipaddress);
                }
            }
            if (HandlSocketException.handleConnetReset(e)) {
                if (onReceiveFileListener != null) {
                    onReceiveFileListener.onFial();
                }
            }
//            if (socket != null)
//                try {
//                    socket.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                }
        }
    }

    /**
     * 判断文件 并创建文件夹
     */
    private File checkFileex(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path.substring(0, path.lastIndexOf("/")));
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(path);
    }

}
