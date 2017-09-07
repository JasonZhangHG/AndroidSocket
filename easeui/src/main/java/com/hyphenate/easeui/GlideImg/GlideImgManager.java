package com.hyphenate.easeui.GlideImg;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by MZ安卓 on 2016/12/21.
 */

public class GlideImgManager {

    public static void glideLoader(Context context, String url, int erroImg, int emptyImg, ImageView iv) {
        //原生 API
        Glide.with(context).load(url).placeholder(emptyImg).error(erroImg).into(iv);
    }
    /**
     * load normal  for  circle or round img
     *
     * @param url
     * @param erroImg
     * @param emptyImg
     * @param iv
     * @param tag
     */
    public static void glideLoader(Context context, String url, int erroImg, int emptyImg, ImageView iv, int tag) {
        if (0 == tag) {
            Glide.with(context).load(url).placeholder(emptyImg).error(erroImg).transform(new GlideCircleTransform(context)).into(iv);
        } else if (1 == tag) {

            Glide.with(context).load(url).placeholder(emptyImg).error(erroImg).transform(new GlideRoundTransform(context, 10)).into(iv);
        }
    }
}
