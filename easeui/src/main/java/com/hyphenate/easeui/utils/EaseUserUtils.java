package com.hyphenate.easeui.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.easeui.R;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.controller.EaseUI.EaseUserProfileProvider;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.exceptions.HyphenateException;

public class EaseUserUtils {

    static EaseUserProfileProvider userProvider;

    static {
        userProvider = EaseUI.getInstance().getUserProfileProvider();
    }

    /**
     * get EaseUser according username
     *
     * @param username
     * @return
     */
    public static EaseUser getUserInfo(String username) {
        if (userProvider != null)
            return userProvider.getUser(username);

        return null;
    }

    /**
     * set user avatar
     * 设置用户头像，在APP主页设置消息接受监听，所附加的message
     *
     * @param username
     */
    public static void setUserAvatar(Context context, String username, EMMessage emMessage, ImageView imageView) {
/*        EaseUser user = getUserInfo(username);
        if (user != null && user.getAvatar() != null) {
            try {
//                int avatarResId = Integer.parseInt(user.getAvatar());
                System.out.println("------user---1"+user.getAvatar());
                Glide.with(context).load(user.getAvatar()).into(imageView);
                GlideImgManager.glideLoader(context,user.getAvatar(), R.drawable.ease_default_avatar, R.drawable.ease_default_avatar,imageView, 0);
            } catch (Exception e) {
                //use default avatar
                System.out.println("------user---2"+user.getAvatar());
                GlideImgManager.glideLoader(context,user.getAvatar(), R.drawable.ease_default_avatar, R.drawable.ease_default_avatar,imageView, 0);
//                Glide.with(context).load(user.getAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.ease_default_avatar).into(imageView);
            }
        }
        else {
            System.out.println("------user---3"+user.getAvatar());
            GlideImgManager.glideLoader(context,"", R.drawable.ease_default_avatar, R.drawable.ease_default_avatar,imageView, 0);
//
        } */
        if (emMessage != null) {
            try {
                Log.e("headTokens", "==" + EaseUI.headTokens);
                String headurl = emMessage.getStringAttribute(EaseUI.headTokens);
                if (headurl != null && headurl.length() > 0) {
                    Glide.with(context).load(headurl).dontAnimate().dontTransform().error(R.drawable.ease_default_avatar).placeholder(R.drawable.ease_default_avatar).into(imageView);
                } else {
                    Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);

                }
            } catch (HyphenateException e) {
                e.printStackTrace();
                Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
            }
        } else {
            Glide.with(context).load(R.drawable.ease_default_avatar).into(imageView);
        }
    }

    /**
     * set user's nickname
     */
    public static void setUserNick(String username, EMMessage emMessage, TextView textView) {
        /*if (textView != null) {
            EaseUser user = getUserInfo(username);
            if (user != null && user.getNick() != null) {
                textView.setText(user.getNick());
            } else {
                textView.setText(username);
            }
        }*/
        if (emMessage != null) {
            try {
                String nickname = emMessage.getStringAttribute(EaseUI.nameTokens);
                if (nickname != null && nickname.length() > 0) {
                    textView.setText(nickname);
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
                textView.setText(username);
            }
        } else {
            textView.setText(username);
        }
    }

}
