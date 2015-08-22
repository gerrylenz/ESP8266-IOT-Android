package com.espressif.esp8266_iot.util;

/**
 * Created on 20.07.15
 * http://www.hrupin.com/2011/10/how-to-finish-all-activities-in-your-android-application-through-simple-call
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class BaseActivity extends Activity {
    public static final String FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION = "com.espressif.esp8266_iot.activitys.FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION";
    private BaseActivityReceiver baseActivityReceiver = new BaseActivityReceiver();
    public static final IntentFilter INTENT_FILTER = createIntentFilter();

    //**********************************************************************************************
    private static IntentFilter createIntentFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION);
        return filter;
    }
    //**********************************************************************************************
    protected void registerBaseActivityReceiver() {
        registerReceiver(baseActivityReceiver, INTENT_FILTER);
    }
    //**********************************************************************************************
    protected void unRegisterBaseActivityReceiver() {
        unregisterReceiver(baseActivityReceiver);
    }
    //**********************************************************************************************
    public class BaseActivityReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION)){
                finish();
            }
        }
    }
    //**********************************************************************************************
    protected void closeAllActivities(){
        sendBroadcast(new Intent(FINISH_ALL_ACTIVITIES_ACTIVITY_ACTION));
    }
}