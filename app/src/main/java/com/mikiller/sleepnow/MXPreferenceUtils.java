package com.mikiller.sleepnow;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

/**
 * Created by Mikiller on 2017/5/26.
 */

public class MXPreferenceUtils {
    public static final String REPORTS = "xnews";
    private SharedPreferences sp;
    private Context mContext;

    private MXPreferenceUtils() {
    }

    private static class CREATER {
        private static MXPreferenceUtils instance = new MXPreferenceUtils();
    }

    public static MXPreferenceUtils getInstance(Context context, String name) {
        CREATER.instance.mContext = context;
        CREATER.instance.sp = context.getSharedPreferences(name, Context.MODE_APPEND);
        return CREATER.instance;
    }

    public static MXPreferenceUtils getInstance(){
        return CREATER.instance;
    }

    public void removeData(String id){
        SharedPreferences.Editor editor = sp.edit();
        Log.e("mxpreference", "remove data: " + id);
        editor.remove(id).commit();
    }

    public Map getAll(){
        return sp.getAll();
    }

    public void setStartTime(String time){
        sp.edit().putString("startTime", time).commit();
    }

    public void setEndTime(String time){
        sp.edit().putString("endTime", time).commit();
    }

    public String getStartTime() {
        return sp.getString("startTime", "22:00");
    }

    public String getEndTime() {
        return sp.getString("endTime", "6:00");
    }
}
