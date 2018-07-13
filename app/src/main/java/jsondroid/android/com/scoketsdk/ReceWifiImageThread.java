package jsondroid.android.com.scoketsdk;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by wenbaohe on 2016/12/19.
 *
 * 接收图片的独立线程
 */

public class ReceWifiImageThread extends  Thread{
    private static final String TAG = "ReceWifiImageThread";
    public static final int CONNET_ENABLE = 0x112;//连接成功
    public static final int CONNET_DISABLE = 0x114;//连接失败
    public static final int REC_SUCCESS = 0x116;//接收成功
    public static final int REC_FAIL = 0x118;//接收失败

    private static Socket msocket;
    private String path=Environment.getExternalStorageDirectory().getPath() + File.separator + "str"+22+".apk";
    private String host;

    public ReceWifiImageThread() {
        this.host = "192.168.0.21";
    }

    @Override
    public void run() {
        msocket=new Socket();
        try {

            msocket.connect((new InetSocketAddress(host, 8976)), 3000);

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(msocket.getOutputStream())), true);
            // 填充信息
            out.println("send");
            Log.i("connet","连接成功");
            receiveFile(msocket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopConnet(){
        if (msocket!=null)
            try {
                msocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void receiveFile(Socket socket) {
        byte[] inputByte = null;
        int length = 0;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        try {
            try {
                File dir=new File(Environment.getExternalStorageDirectory().getPath());
                if(!dir.exists())
                    dir.mkdirs();

                dis = new DataInputStream(socket.getInputStream());

                File file=new File(path);
                fos = new FileOutputStream(file);
                inputByte = new byte[1024];
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    System.out.println("接收中..."+length);
                    String smg = new String(Arrays.copyOf(inputByte, length)).trim();
                    Log.e("smg---->", smg.length() + "");
                    if (smg.equals("OK")) {
                        Log.e("停止---->", smg);
                        break;
                    }
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
                System.out.println("完成接收");
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    try {
                        socket.close();
                        socket=null;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
            }
        } catch (Exception e) {
            if (socket != null)
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
    }




    /*private void receiveFile(Socket socket) throws IOException {
        byte[] inputByte = null;
        int length = 0;
        DataInputStream din = null;
        FileOutputStream fout = null;
        try {

            File dir = new File(Imageconfig.getInstance().imagePath);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(Imageconfig.getInstance().imagePath + din.readUTF());
//                File file = new File(Imageconfig.getImageFilePath());
            path = file.getPath();
            VLCApplication.images.add(path);
            din = new DataInputStream(socket.getInputStream());
            fout = new FileOutputStream(file);//din.readUTF()
            inputByte = new byte[1024];
            Log.e("开始接收图片", "开始接收数据...");
            while (true) {
                if (din != null) {
                    length = din.read(inputByte, 0, inputByte.length);
                }
                if (length == -1) {
                    break;
                }
                System.out.println(length);
                fout.write(inputByte, 0, length);
                fout.flush();
            }
            *//**//**切图（切光图）*//**//*
            draImsrc = Imageconfig.drawMap(path);
            VLCApplication.draImages.add(draImsrc);
            Log.e("接收图片", "完成接收");
            handler.sendMessage(handler.obtainMessage(REC_SUCCESS, draImsrc));

        } catch (Exception ex) {
            if (socket != null)
                socket.close();
            handler.sendMessage(handler.obtainMessage(REC_FAIL, ""));
            if (VLCApplication.draImages.contains(draImsrc)) {
                VLCApplication.draImages.remove(draImsrc);
            }
            if (VLCApplication.images.contains(path)) {
                VLCApplication.images.remove(path);
            }
            ex.printStackTrace();
        } finally {
            if (fout != null)
                fout.close();
            if (din != null)
                din.close();
        }
    }*/
}
