package jsondroid.android.com.scoketsdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;

import jsondroid.android.com.scoketsdk.socketnetwork.conmm.SocketClientManager;
import jsondroid.android.com.scoketsdk.socketnetwork.listener.OnReceiveFileListener;
import jsondroid.android.com.scoketsdk.socketnetwork.listener.OnSocketConnetListener;
import jsondroid.android.com.scoketsdk.socketnetwork.service.SocketBuilder;
import jsondroid.android.com.scoketsdk.socketnetwork.service.SocketService;

public class MainActivity extends AppCompatActivity implements OnReceiveFileListener, OnSocketConnetListener {


    private EditText editText;
    private TextView tv_notice;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        tv_notice = (TextView) findViewById(R.id.tv_notice);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        SocketClientManager.getInstance().registerReceiver(this);
        SocketClientManager.getInstance().setOnReceiveFileListener(this);
        SocketClientManager.getInstance().setOnSocketConnetListener(this);

    }


    public void onclickbtn1(View view) {
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//        new ReceWifiImageThread().start();
    }

    public void onclickbtn2(View view) {
        SocketClientManager.getInstance().startConnet();
    }

    public void onclickbtn3(View view) {
        SocketClientManager.getInstance().onDisConnet();
    }

    private int code = 0;

    public void onclickbtn4(View view) {
        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "text" + code + ".apk";
        SocketClientManager.getInstance().sendMSG(editText.getText().toString(), path);
        code++;
    }


    ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketBuilder socketBuilder = new SocketBuilder();
            socketBuilder.setIpaddress("192.168.0.21");//192.168.0.21
            socketBuilder.setPort(9292);
            socketBuilder.setReConnet(true);
            socketBuilder.setRecount(4);
            Log.e("绑定成功", "绑定成功");
            SocketClientManager.getInstance().setService(service).initSocket(socketBuilder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onDestroy() {
        unbindService(connection);
        SocketClientManager.getInstance().unregisterReceiver(this);
        super.onDestroy();
    }

    @Override
    public void onReceiveing(int progress) {
        tv_notice.setText("文件获取：\n" + progress + "/字节");
//        shownotice("文件获取：\n" + progress + "/字节");
    }

    @Override
    public void onSuccess() {
        shownotice("文件获取：\n" + "获取成功");
    }

    @Override
    public void onFial() {
        shownotice("文件获取：\n" + "获取失败");
    }

    @Override
    public void onConnet() {
        shownotice("连接：\n" + "连接成功");
    }

    @Override
    public void onReConneting() {
        shownotice("连接：\n" + "正在重连");
    }

    @Override
    public void onDisConnet() {
        shownotice("连接：\n" + "已断开连接");
    }

    @Override
    public void onConnetFail() {
        shownotice("连接：\n" + "连接失败");
    }

    private void shownotice(String msg) {
        tv_notice.append(msg);
        tv_notice.append("\n");
        onfullScroll();
    }

    private void onfullScroll() {
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
            }
        }, 200);
    }
}
