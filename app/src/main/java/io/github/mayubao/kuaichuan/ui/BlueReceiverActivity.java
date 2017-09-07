package io.github.mayubao.kuaichuan.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mayubao.kuaichuan.Constant;
import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.common.BaseActivity;
import io.github.mayubao.kuaichuan.core.utils.FileUtils;
import io.github.mayubao.kuaichuan.core.utils.TextUtils;
import io.github.mayubao.kuaichuan.ui.view.RadarLayout;

/**
 * 本页：蓝牙接收，界面复用wifi接受的

 */

public class BlueReceiverActivity extends BaseActivity {
    @Bind(R.id.tv_back)
    TextView mTvBack;
    @Bind(R.id.radarLayout)
    RadarLayout mRadarLayout;
    @Bind(R.id.tv_device_name)
    TextView mTvDeviceName;
    @Bind(R.id.tv_desc)
    TextView mTvDesc;
    @Bind(R.id.tv_top_tip)
    TextView mTop;

    BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket serverSocket;
    private BluetoothSocket socket;
    private InputStream is;//输入流
    UUID myUUID = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");//标识文件传输
    AcceptThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_waiting);
        ButterKnife.bind(this);
        mRadarLayout.start();
        String ssid = TextUtils.isNullOrBlank(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE;
        mTvDeviceName.setText(ssid);
        mTvDesc.setText("正在等待连接");
        init();
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.enable(); //开启
        /**
         * 进入页面才打开蓝牙，所以需要等待蓝牙真正开启才能执行嵌套字服务，这里使用handle延迟启动，根据手机蓝牙开启速度不同，也许还需要调高一点
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mThread = new AcceptThread();
                mThread.start();
            }
        }, 2000);

    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                mTop.setText("接受文件：" + msg.obj.toString());
            } else if (msg.what == 20) {
                mTvDesc.setText(msg.obj.toString() + "");
            } else {
                String str = msg.obj.toString();
                mTvDesc.setText(str);
                Toast.makeText(getApplicationContext(), str,
                        Toast.LENGTH_LONG).show();
                if (str.equals("数据传输完成")) {
                    /**
                     * 需要的话，传输文成可以finish关闭本页面
                     */
                    //   finish();
                }
            }

            super.handleMessage(msg);
        }
    };
    String name;

    //服务端监听客户端的线程类
    private class AcceptThread extends Thread {
        public AcceptThread() {
            //serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth_Socket", myUUID);
            try {
                /**
                 * uuid方法链接成功率不高，改用此方法创建socket
                 */
                Method mthd = mBluetoothAdapter.getClass().getMethod("listenUsingRfcommOn",
                        new Class[]{int.class});
                serverSocket = (BluetoothServerSocket) mthd.invoke(mBluetoothAdapter,
                        new Object[]{29});
                Log.e("get", "建立服务套接字成功");
                sendMessage(0, "蓝牙服务启动成功,等待接收数据");
            } catch (Exception e) {
                sendMessage(0, "蓝牙服务启动失败,请重试");
            }
        }

        public void run() {
            try {

                Log.e("get", "等待客户连接...");
                /**
                 * serverSocket.accept(); 是阻塞式的，会一直等待，链接之后才会执行后面的代码，所以需要在子线程中运行
                 */
                socket = serverSocket.accept();
                Log.e("get", "已建立与客户连接.");
                BluetoothDevice device = socket.getRemoteDevice();
                Log.e("get", "接受客户连接 , 远端设备名字:" + device.getName() + " , 远端设备地址:" + device.getAddress());
                sendMessage(0, "数据接收开始");
                is = socket.getInputStream();

                /**
                 * 拿到文件长度
                 */
                byte[] s = new byte[256];
                int sReadbyte = 0;
                while (sReadbyte < s.length) {
                    int read = is.read(s, sReadbyte, s.length - sReadbyte);
                    //判断是不是读到了数据流的末尾 ，防止出现死循环。
                    if (read == -1) {
                        break;
                    }
                    sReadbyte += read;
                }
                String sstr = new String(s, "UTF-8").trim();
                long size = Long.valueOf(sstr);
                Log.e("get", "size：" + size);
                /**
                 * 拿到文件名称（Base64加密文本）
                 */
                byte[] info = new byte[256];
                int iReadbyte = 0;
                while (iReadbyte < info.length) {
                    int read = is.read(info, iReadbyte, info.length - iReadbyte);
                    //判断是不是读到了数据流的末尾 ，防止出现死循环。
                    if (read == -1) {
                        break;
                    }
                    iReadbyte += read;
                }
                String file_name = new String(info).trim();
                Log.e("get", "file_name：" + file_name);
                sendMessage(10, file_name);
                /**
                 * 对加密文件名进行解密，创建文件输出流开始保存
                 */
                name = new String(Base64.decode(file_name.getBytes(), Base64.DEFAULT));
                Log.e("get", "name:" + name);
                FileOutputStream outputStream = new FileOutputStream(FileUtils.getRootDirPath() + name);
                byte[] buf = new byte[1024];
                int len = 0;
                long allsize = 0;
                try {
                    while ((len = is.read(buf)) > 0) {
                        allsize += len;
                        outputStream.write(buf, 0, len);
                        outputStream.flush();
                        sendMessage(20, "正在接收数据   \n\n大小:" + (double) allsize / 1024 / 1024 + "M / " + (double) size / 1024 / 1024 + "M    \n\n进度:" + allsize * 100 / size + "%");
                        /**
                         * 通过 接收到的文件长度size，与已经接收到的数据长度allsize，来判断文件是否接收完毕
                         */
                        if (size == allsize) break;
                    }
                } catch (SocketTimeoutException exception) {
                    Log.e("get", "timeOut");
                }
                is.close();
                outputStream.close();
                Log.e("get", "close");
                sendMessage(0, "数据传输完成");
                //  mBluetoothAdapter.disable();
            } catch (Exception e) {
                /**
                 * 当出错时，删除错误的文件
                 */
                Log.e("get", "发生错误:" + e.getMessage());
                File file = new File(FileUtils.getRootDirPath() + name);
                file.delete();
                sendMessage(0, "发生错误");
            }
        }
    }

    private void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        handler.sendMessage(msg);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.disable();
    }

    @OnClick(R.id.tv_back)
    public void onClick() {
        finish();
    }
}
