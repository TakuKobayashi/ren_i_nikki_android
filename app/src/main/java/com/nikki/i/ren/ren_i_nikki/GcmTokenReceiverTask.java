package com.nikki.i.ren.ren_i_nikki;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.Set;

public class GcmTokenReceiverTask extends AsyncTask<String, String, String>{

    private Context mContext;
    private TokenReceievedCallback mCallback;

    public GcmTokenReceiverTask(Context context) {
        mContext = context;
    }

    public void setOnTokenReceievedCallback(TokenReceievedCallback callback) {
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... senderIds) {
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
        String regId = null;
        try {
            regId = gcm.register(senderIds);
            Log.v("GCM", "regId is " + regId);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("GCM", "regId is " + regId);
        }
        return regId;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mCallback != null) mCallback.onRecieve(result);
    }

    public interface TokenReceievedCallback {
        public void onRecieve(String regId);
    }
}
