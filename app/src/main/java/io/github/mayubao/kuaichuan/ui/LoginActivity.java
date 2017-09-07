package io.github.mayubao.kuaichuan.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.mayubao.kuaichuan.AppContext;
import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.core.utils.ToastUtils;



public class LoginActivity extends Activity {
    @Bind(R.id.edit_user)
    EditText mEditUser;
    @Bind(R.id.tv_next)
    TextView mTvNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }
    @OnClick(R.id.tv_next)
    public void onClick() {
        //设置环信用户名
        String userid = mEditUser.getText().toString().trim();
        if (userid != null && userid.length() > 0) {
            AppContext.userid = userid;
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            ToastUtils.show(LoginActivity.this, "userid不能为空");
        }
    }
}
