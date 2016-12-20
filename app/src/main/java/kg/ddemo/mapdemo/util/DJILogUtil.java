package kg.ddemo.mapdemo.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import kg.ddemo.mapdemo.BuildConfig;
import kg.ddemo.mapdemo.MyApplication;


/**
 * @author :key.guan
 * Description:尽量增加log
 * Copyright (c) 2016. DJI All Rights Reserved.
 */
public class DJILogUtil {
   private static final String TAG = "offlinemap("+ BuildConfig.VERSION_CODE+")";

    //log-path： Sdcard/DJI/dji.system.upgrade/LOG/CACHE/
    public static boolean IsDebugForLog = true;

    public static void E(String tag, String method, Throwable t, int errorNo, String strMsg) {
        E( tag+"."+method,  t,  errorNo,  strMsg);
    }
    public static void E(String method, Throwable t, int errorNo, String strMsg) {
        if (t != null) {
            t.printStackTrace();
        }
        String log = "method=" + method + ",t=" + t + ",errorNo = " + errorNo + ",strMsg=" + strMsg;
        E(log);
    }

    /**
     * @verId 1
     * @desc Toast方便测试，http://my.oschina.net/u/1244156/blog/217649
     * @author : key.guan
     * @date : 2016/9/9 9:47
     */
    public static int toastId = 0;
    public static void E(final Context context, final String log) {
        if (!IsDebugForLog) {
            return;
        }
        if (null == context) {
            E("null == context,will not show the toast, " + log);
            return;
        }

        final String showLog = "[ toastId=" + toastId + " ]:" + log;
        E(showLog);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toastId++;
                Toast.makeText(context.getApplicationContext(), showLog, Toast.LENGTH_LONG).show();
            }
        }, 0);
    }
    public static void Toast(final String log) {
        /*if (!IsDebugForToast) {
            rturn;
        }*/
        final Context context = MyApplication.getAppContext();
        final String showLog = "[ toastId=" + toastId + " ]:" + log;
        E(showLog);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toastId++;
                Toast.makeText(context, showLog, Toast.LENGTH_LONG).show();
            }
        }, 0);
    }
    public static void Toast(String tag, Throwable t, int errorNo, String strMsg) {
        if (t != null) {
            t.printStackTrace();
        }
        String log = ",tag=" + tag + ",t=" + t + ",errorNo = " + errorNo + ",strMsg=" + strMsg;
        Toast(log);
    }

    /**
     * @verId 1
     * @desc 需要屏蔽log，只需要完成此处屏蔽即可
     * @author : key.guan
     * @date : 2016/9/3 13:33
     */
    public static void E(String tag, String log) {
        log = tag +"->"+ log;
        E(log);
    }
    public static void E(String log) {
        if ( IsDebugForLog ) {
            Log.e(TAG, log);
        }
    }
    public static void I(String tag, String log) {
        log = tag +"->"+ log;
        I(log);
    }
    public static void I(String log) {
        if (IsDebugForLog) {
            Log.i(TAG, log);
        }

    }


}
