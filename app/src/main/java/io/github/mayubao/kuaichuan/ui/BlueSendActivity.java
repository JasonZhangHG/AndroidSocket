package io.github.mayubao.kuaichuan.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mayubao.kuaichuan.AppContext;
import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.common.BaseActivity;
import io.github.mayubao.kuaichuan.common.BlueBean;
import io.github.mayubao.kuaichuan.core.entity.FileInfo;
import io.github.mayubao.kuaichuan.core.utils.ToastUtils;
import io.github.mayubao.kuaichuan.ui.adapter.BlueSendAdapter;
import io.github.mayubao.kuaichuan.ui.view.RadarScanView;




public class BlueSendActivity extends BaseActivity {
    @Bind(R.id.tv_back)
    TextView mTvBack;
    @Bind(R.id.radarView)
    RadarScanView mRadarView;
    @Bind(R.id.lv_result)
    ListView mLvResult;
    @Bind(R.id.tv_top_tip)
    TextView top;

    List<BlueBean> mScanResultList = new ArrayList<>();
    BlueSendAdapter mBlueSendAdapter;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;//远程设备
    BluetoothSocket clientSocket;//长链接
    OutputStream os;//输出流

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_receiver);
        ButterKnife.bind(this);
        mRadarView.startScan();
        // 设置广播信息过滤
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
        // 注册蓝牙搜索广播接收者，接收并处理搜索结果
        this.registerReceiver(receiver, filter);
        //如果当前在搜索，就先取消搜索

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙控制器
        mBluetoothAdapter.enable(); //开启


        mBlueSendAdapter = new BlueSendAdapter(BlueSendActivity.this, mScanResultList);
        mLvResult.setAdapter(mBlueSendAdapter);
        mLvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * item点击开始执行嵌套字服务
                 */
                new AcceptThread(mScanResultList.get(position).getUid()).start();

            }
        });
        //开启搜索
        mBluetoothAdapter.startDiscovery();
        /**
         * 蓝牙开启需要时间，使用handler延迟
         */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //获取已经配对的蓝牙设备
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        BlueBean scanResult = new BlueBean(device.getName(), device.getAddress());
                        mScanResultList.add(scanResult);
                        mBlueSendAdapter.notifyDataSetChanged();
                    }
                }
            }
        }, 1000);

        /**
         * 点击扫描动态控件重新进行蓝牙搜索操作
         */
        mRadarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLvResult.getVisibility() != View.VISIBLE) {
                    return;
                }
                //如果当前在搜索，就先取消搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    mScanResultList.clear();
                    mBlueSendAdapter.notifyDataSetChanged();
                }
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        BlueBean scanResult = new BlueBean(device.getName(), device.getAddress());
                        mScanResultList.add(scanResult);
                        mBlueSendAdapter.notifyDataSetChanged();
                    }
                }
                //开启搜索
                mBluetoothAdapter.startDiscovery();
            }
        });
    }

    private class AcceptThread extends Thread {
        public AcceptThread(String uid) {
            try {
                //判断当前是否正在搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                if (device == null) {
                    //获得远程设备
                    device = mBluetoothAdapter.getRemoteDevice(uid);
                }
            } catch (Exception e) {
            }
        }

        public void run() {
            try {
                if (clientSocket == null) {
                    //判断当前是否正在搜索
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    //创建客户端蓝牙Socket
                    // clientSocket = device.createRfcommSocketToServiceRecord(myUUID);
                    try {
                        /**
                         * 创建发送服务
                         */
                        Method method = device.getClass()
                                .getMethod("createRfcommSocket",
                                        new Class[]{int.class});
                        clientSocket = (BluetoothSocket) method.invoke(device,
                                new Object[]{29});
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    /**
                     * 链接接收端
                     */
                    //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                    Log.e("send", "链接服务端");
                    clientSocket.connect();
                    Log.e("send", "连接建立");
                    sendMessage(0,"开始传输数据");
                    //获得输出流（客户端指向服务端输出文本）
                    os = clientSocket.getOutputStream();
                    /**
                     * 拿到文件相关信息
                     */
                    Map<String, FileInfo> fileInfoMap = AppContext.getAppContext().getFileInfoMap();
                    FileInfo fileInfo = fileInfoMap.entrySet().iterator().next().getValue();
                    String path = fileInfo.getFilePath();
                    String name = fileInfo.getName();
                    Log.e("send", "path:" + path + "=name:" + name);
                    /**
                     * 对文件长度 进行处理，以String形式发送过去
                     */
                    String sizeStr = String.valueOf(fileInfo.getSize());
                    Log.e("send", "sizeStr:" + sizeStr);
                    byte[] s = sizeStr.getBytes("UTF-8");
                    byte[] infos = Arrays.copyOf(s, 256);
                    /**
                     * 对文件名进行加密，然后转为256长度byte[]数组
                     */
                    String strBase64 = new String(Base64.encode(name.getBytes("UTF-8"), Base64.DEFAULT));
                    byte[] b = strBase64.getBytes();
                    byte[] info = Arrays.copyOf(b, 256);
                    /**
                     * 对2个byte[]数组进行合并，并转为输出流
                     */
                    byte[] byte_3 = new byte[infos.length + info.length];
                    System.arraycopy(infos, 0, byte_3, 0, infos.length);
                    System.arraycopy(info, 0, byte_3, infos.length, info.length);
                    ByteArrayInputStream bais = new ByteArrayInputStream(byte_3);
                    /**
                     * 生成文件对应的输出流
                     */
                    FileInputStream fs = new FileInputStream(path);
                    byte[] buf = new byte[1024];
                    int len = 0;
                    long allsize = 0;
                    //合并流
                    /**
                     * 将文件信息流 和 文件流 进行合并后发送
                     */
                    SequenceInputStream sis = new SequenceInputStream(bais, fs);
                    while ((len = sis.read(buf)) != -1) {
                        allsize += len;
                        os.write(buf, 0, len);
                        os.flush();
                        sendMessage(20,"开始传输数据\n" + name + "\n大小:" + (double) allsize / 1024 / 1024 + "M / " + (double) fileInfo.getSize() / 1024 / 1024 + "M" + "\n\n进度:" + allsize * 100 / fileInfo.getSize() + "%");
                    }

                    Log.e("send", "send完成");
                    sendMessage(0,"数据发送完成");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("error", "send出错");
                sendMessage(0,"数据发送过程中出错");
            }
        }
    }
    private void sendMessage(int what, Object obj) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = obj;
        mHandler.sendMessage(msg);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.disable();
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 20) {
                top.setText(msg.obj.toString());
                return;
            }
            String str = msg.obj.toString();
            ToastUtils.show(getContext(), String.valueOf(msg.obj));
            top.setText(str);
            if (str.equals("开始传输数据")) {
                mLvResult.setVisibility(View.GONE);
            }
            if (str.equals("数据发送完成")) {
                /**
                 * 文件发送完成关闭页面
                 */
                //  finish();
            }
        }
    };

    /**
     * 定义广播接收器
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.e("sousuo", "搜索到一个：" + device.getName());
                    BlueBean scanResult = new BlueBean(device.getName(), device.getAddress());
                    mScanResultList.add(scanResult);
                    mBlueSendAdapter.notifyDataSetChanged();
                    // mLvResult.setAdapter(mBlueSendAdapter);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //已搜素完成
            }
        }
    };

    @OnClick(R.id.tv_back)
    public void onClick() {
        finish();
    }
}
