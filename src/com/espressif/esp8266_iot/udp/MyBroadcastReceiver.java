package com.espressif.esp8266_iot.udp;

/**
 * Created by gerry on 25.07.2015.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Time Changed", Toast.LENGTH_LONG).show();
        Log.d("BROAD","Time");
    }
}