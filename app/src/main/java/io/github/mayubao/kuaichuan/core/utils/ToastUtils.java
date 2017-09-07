package io.github.mayubao.kuaichuan.core.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;


public class ToastUtils {

    static Toast toast = null;
    public static void show(Context context, String text) {
        try {
            if(toast!=null){
                toast.setText(text);
            }else{
                toast= Toast.makeText(context, text, Toast.LENGTH_LONG);
            }
            toast.show();
        } catch (Exception e) {//子线程中Toast异常情况处理
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            Looper.loop();
        }
    }
}
