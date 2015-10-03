package com.nikki.i.ren.ren_i_nikki;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.Set;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Get message type for GCM
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String gcmMessageType = gcm.getMessageType(intent);
        Bundle extras = intent.getExtras();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            Log.d(Config.TAG, key + "=" + extras.get(key).toString());
        }
        Log.d(Config.TAG, "msgtype:" + gcmMessageType);
    }
}
