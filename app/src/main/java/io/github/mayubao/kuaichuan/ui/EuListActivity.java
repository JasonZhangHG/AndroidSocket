package io.github.mayubao.kuaichuan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.EaseConstant;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.ui.EaseConversationListFragment;
import com.hyphenate.exceptions.HyphenateException;

import io.github.mayubao.kuaichuan.R;



public class EuListActivity extends FragmentActivity {
    EaseConversationListFragment conversationListFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_es_list);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        conversationListFragment = new EaseConversationListFragment();
        conversationListFragment.hideTitleBar();
        conversationListFragment.setConversationListItemClickListener(new EaseConversationListFragment.EaseConversationListItemClickListener() {

            @Override
            public void onListItemClicked(EMConversation conversation) {
                Intent intent = new Intent(EuListActivity.this, ChatActivity.class);
                intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EMMessage.ChatType.Chat);
                intent.putExtra(EaseConstant.EXTRA_USER_ID, conversation.getLastMessage().getUserName());
                try {
                    intent.putExtra(EaseUI.nameTokens, conversation.getLastMessage().getStringAttribute(EaseUI.nameTokens));
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.frame_main, conversationListFragment).commit();
    }
}
