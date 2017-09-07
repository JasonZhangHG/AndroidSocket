package io.github.mayubao.kuaichuan.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.exceptions.HyphenateException;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mayubao.kuaichuan.AppContext;
import io.github.mayubao.kuaichuan.Constant;
import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.common.BaseActivity;
import io.github.mayubao.kuaichuan.core.utils.FileUtils;
import io.github.mayubao.kuaichuan.core.utils.TextUtils;
import io.github.mayubao.kuaichuan.core.utils.ToastUtils;
import io.github.mayubao.kuaichuan.ui.view.MyScrollView;
import io.github.mayubao.kuaichuan.utils.NavigatorUtils;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, MyScrollView.OnScrollListener {

    private static final String TAG = HomeActivity.class.getSimpleName();


    /**
     * 左右两大块 UI
     */
   /* @Bind(R.id.drawer_layout)
    LinearLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;*/

    TextView tv_name;

    /**
     * top bar 相关UI
     */
    @Bind(R.id.ll_mini_main)
    LinearLayout ll_mini_main;
    @Bind(R.id.tv_title)
    TextView tv_title;
    @Bind(R.id.iv_mini_avator)
    ImageView iv_mini_avator;
    @Bind(R.id.btn_send)
    Button btn_send;
    @Bind(R.id.btn_receive)
    Button btn_receive;
    @Bind(R.id.tv_userid)
    TextView tv_userid;

    /**
     * 其他UI
     */
    @Bind(R.id.msv_content)
    MyScrollView mScrollView;
    @Bind(R.id.ll_main)
    LinearLayout ll_main;
    @Bind(R.id.btn_send_big)
    Button btn_send_big;
    @Bind(R.id.btn_receive_big)
    Button btn_receive_big;

    @Bind(R.id.rl_device)
    RelativeLayout rl_device;
    @Bind(R.id.tv_device_desc)
    TextView tv_device_desc;
    @Bind(R.id.rl_file)
    RelativeLayout rl_file;
    @Bind(R.id.tv_file_desc)
    TextView tv_file_desc;
    @Bind(R.id.rl_storage)
    RelativeLayout rl_storage;
    @Bind(R.id.tv_storage_desc)
    TextView tv_storage_desc;
    @Bind(R.id.btn_easeui)
    Button btn_easeui;
    @Bind(R.id.edit_userid)
    EditText edit_userid;
    @Bind(R.id.btn_easelist)
    Button btn_easelist;

    //大的我要发送和我要接受按钮的LinearLayout的高度
    int mContentHeight = 0;


    //
    boolean mIsExist = false;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);
        rl_storage.setVisibility(View.GONE);
        rl_device.setVisibility(View.GONE);
        tv_userid.setText(AppContext.userid);
        initESUI();
        //发起聊天点击事件。具体界面由easeUI负责
        btn_easeui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String gouser = edit_userid.getText().toString().trim();
                if (gouser.length() > 0) {
                    Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                    intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EMMessage.ChatType.Chat);
                    intent.putExtra(EaseConstant.EXTRA_USER_ID, gouser);
                    startActivity(intent);
                } else {
                    ToastUtils.show(HomeActivity.this, "聊天对象userid不能为空");
                }
            }
        });
        //跳转聊天列表
        btn_easelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, EuListActivity.class);
                startActivity(intent);
            }
        });
        // iv_mini_avator 个人头像，可更改icon_radish.png 达到修改app的目的
        //查找对方的头像并不是获取到的，而是自己app的头像，所以只能使用同一个头像的


        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILE);
        } else {
            //初始化
            init();
        }
        setSTATUScolor(this, getResources().getColor(R.color.colorPrimaryDark));
    }

    /**
     * 修改顶部状态栏颜色（沉浸式状态栏）
     */
    public static void setSTATUScolor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window window = activity.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setNavigationBarTintEnabled(false);
                tintManager.setTintColor(color);
            }
        }
    }

    @Override
    protected void onResume() {
        updateBottomData();
        super.onResume();
    }

    /**
     * 进行权限请求的回调判断
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //初始化
                init();
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_send_file));
                finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 初始化
     */
    private void init() {
        //这里隐藏了侧滑的代码
     /*   ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);*/

        //设置设备名称
        String device = TextUtils.isNullOrBlank(android.os.Build.DEVICE) ? Constant.DEFAULT_SSID : android.os.Build.DEVICE;
        try {//设置左边抽屉的设备名称
        /*    tv_name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);*/
            tv_name.setText(device);
        } catch (Exception e) {
            //maybe occur some exception
        }
        mScrollView.setOnScrollListener(this);
        ll_mini_main.setClickable(false);
        ll_mini_main.setVisibility(View.GONE);
        updateBottomData();
    }

    /**
     * 更新底部 设备数，文件数，节省流量数的数据（这里隐藏部分内容，只显示文件数）
     */
    private void updateBottomData() {
        //TODO 设备数的更新
        //TODO 文件数的更新
        tv_file_desc.setText(String.valueOf(FileUtils.getReceiveFileCount()));
        //TODO 节省流量数的更新
        tv_storage_desc.setText(String.valueOf(FileUtils.getReceiveFileListTotalLength()));

    }

    @Override
    public void onBackPressed() {
     /*   if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {*/
//                super.onBackPressed();
        if (mIsExist) {
            this.finish();
        } else {
            ToastUtils.show(getContext(), getContext().getResources().getString(R.string.tip_call_back_agin_and_exist)
                    .replace("{appName}", getContext().getResources().getString(R.string.app_name)));
            mIsExist = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsExist = false;
                }
            }, 2 * 1000);

        }

        // }
        // }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //   mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        if (id == R.id.nav_about) {
            Log.i(TAG, "R.id.nav_about------>>> click");
            showAboutMeDialog();
        } else if (id == R.id.nav_web_transfer) {
            Log.i(TAG, "R.id.nav_web_transfer------>>> click");
//            NavigatorUtils.toWebTransferUI(getContext());
            NavigatorUtils.toChooseFileUI(getContext(), true);
        } else {
            ToastUtils.show(getContext(), getResources().getString(R.string.tip_next_version_update));
        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //  mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 环信的注册方法（不管输入的用户名是什么都进行注册，这里未进行密码的判断，统一使用123456）
     */
    private void initESUI() {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    EMClient.getInstance().createAccount(AppContext.userid, "123456");
                    Log.e("um", "easeUi--环信注册成功");
                } catch (HyphenateException e) {
                    e.printStackTrace();
                    int errorCode = e.getErrorCode();
                    Log.e("um", "easeUi--" + errorCode);
                }
                EMClient.getInstance().login(AppContext.userid, "123456", new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {
                        //    EMClient.getInstance().groupManager().loadAllGroups();
                        EMClient.getInstance().chatManager().loadAllConversations();
                        EMClient.getInstance().updateCurrentUserNick("测试昵称");//更新环信用户昵称（说明是为了ios推送，未测试）
                        Log.e("main", "登录聊天服务器成功！");
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.e("main", "登录聊天服务器失败：" + code + "==:" + message);
                    }
                });
            }
        }.start();
    }

    @OnClick({R.id.btn_send, R.id.btn_receive, R.id.btn_send_big, R.id.btn_receive_big, R.id.iv_mini_avator,
            R.id.rl_device, R.id.rl_file, R.id.rl_storage})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
            case R.id.btn_send_big: {
                /**
                 * 发送按钮，弹出选择框
                 */
                final BottomSheetDialog sheetDialog = new BottomSheetDialog(this);
                View view1 = View.inflate(HomeActivity.this, R.layout.selector_wifi_bollt, null);
                view1.findViewById(R.id.tv_wifi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //正常 //跳转到应用间选择
                        NavigatorUtils.toChooseFileUI(getContext());
                        sheetDialog.cancel();
                    }
                });
                view1.findViewById(R.id.tv_blue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * 跳转蓝牙发送
                         */
                        Intent intent = new Intent(HomeActivity.this, ChooseFileActivity.class);
                        intent.putExtra("type", "blue");
                        startActivity(intent);
                        sheetDialog.cancel();
                    }
                });
                view1.findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sheetDialog.cancel();
                    }
                });
                sheetDialog.setContentView(view1);
                sheetDialog.show();

                break;
            }
            case R.id.btn_receive:
            case R.id.btn_receive_big: {
                final BottomSheetDialog sheetDialog = new BottomSheetDialog(this);
                View view1 = View.inflate(HomeActivity.this, R.layout.selector_wifi_bollt, null);
                view1.findViewById(R.id.tv_wifi).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //正常 //跳转到应用间接受
                        NavigatorUtils.toReceiverWaitingUI(getContext());
                        sheetDialog.cancel();
                    }
                });
                view1.findViewById(R.id.tv_blue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * 跳转蓝牙接收
                         */
                        Intent intent = new Intent(HomeActivity.this, BlueReceiverActivity.class);
                        intent.putExtra("type", "blue");
                        startActivity(intent);
                        sheetDialog.cancel();
                    }
                });
                view1.findViewById(R.id.tv_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sheetDialog.cancel();
                    }
                });
                sheetDialog.setContentView(view1);
                sheetDialog.show();

                break;
            }
            case R.id.iv_mini_avator: {
          /*      if(mDrawerLayout != null){
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }*/
                break;
            }
            case R.id.rl_file:
            case R.id.rl_storage: {
                NavigatorUtils.toSystemFileChooser(getContext());
                break;
            }

        }
    }

    //自定义ScrollView的监听
    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        Log.i(TAG, "l-->" + l + ",t-->" + t + ",oldl-->" + oldl + ",oldt-->" + oldt);
        mContentHeight = ll_main.getMeasuredHeight();
//        Log.i(TAG, "content height : " + mContentHeight);
//        float alpha = t / (float)mContentHeight;
//        Log.i(TAG, "content alpha : " + alpha);
//        tv_title.setAlpha(alpha);
        //一半的位置时候
        // topbar上面的两个小按钮 跟 主页上面的两个大按钮的alpha值是对立的 即 alpha 与 1-alpha的关系
        if (t > mContentHeight / 2) {
            float sAlpha = (t - mContentHeight / 2) / (float) (mContentHeight / 2);
            ll_mini_main.setVisibility(View.VISIBLE);
            ll_main.setAlpha(1 - sAlpha);
            ll_mini_main.setAlpha(sAlpha);
            tv_title.setAlpha(0);
        } else {
            float tAlpha = t / (float) mContentHeight / 2;
            tv_title.setAlpha(1 - tAlpha);
            ll_mini_main.setVisibility(View.INVISIBLE);
            ll_mini_main.setAlpha(0);
        }

    }

    /**
     * 显示对话框
     */
    private void showAboutMeDialog() {
        View contentView = View.inflate(getContext(), R.layout.view_about_me, null);
        contentView.findViewById(R.id.tv_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProject();
            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.title_about_me))
                .setView(contentView)
                .setPositiveButton(getResources().getString(R.string.str_weiguan), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toProject();
                    }
                })
                .create()
                .show();
    }

    /**
     * 跳转到项目
     */
    private void toProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Constant.GITHUB_PROJECT_SITE);
        intent.setData(uri);
        getContext().startActivity(intent);
    }
}
