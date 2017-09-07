package io.github.mayubao.kuaichuan.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.ui.EaseChatFragment;
import com.hyphenate.easeui.widget.chatrow.EaseCustomChatRowProvider;

import io.github.mayubao.kuaichuan.R;


public class ChatActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);
        EaseChatFragment chatFragment = new EaseChatFragment();
        chatFragment.setArguments(getIntent().getExtras());
        chatFragment.setChatFragmentListener(new EaseChatFragment.EaseChatFragmentHelper() {
            @Override
            public void onSetMessageAttributes(EMMessage message) {
                //添加额外参数
                //message.setAttribute(EaseUI.nameTokens, "我是sb");
                //message.setAttribute(EaseUI.headTokens, "http://avatar.csdn.net/2/1/E/1_yoojia.jpg");
            }

            @Override
            public void onEnterToChatDetails() {

            }

            @Override
            public void onAvatarClick(String username) {

            }

            @Override
            public void onAvatarLongClick(String username) {

            }

            @Override
            public boolean onMessageBubbleClick(EMMessage message) {
                return false;
            }

            @Override
            public void onMessageBubbleLongClick(EMMessage message) {

            }

            @Override
            public boolean onExtendMenuItemClick(int itemId, View view) {
                return false;
            }

            @Override
            public EaseCustomChatRowProvider onSetCustomChatRowProvider() {
                return null;
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.frame_msg, chatFragment).commit();
    }
}
