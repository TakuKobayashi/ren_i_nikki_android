package com.nikki.i.ren.ren_i_nikki;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.Set;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private final static int REQUEST_CODE = 1;

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
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(gcmMessageType)) {
            sendNotification(context, intent);
        }
    }

    private void sendNotification(Context context, Intent intent) {
        Intent tappedIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, REQUEST_CODE, tappedIntent, PendingIntent.FLAG_ONE_SHOT);

        // LargeIcon の Bitmap を生成
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        // NotificationBuilderを作成
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent);
        // ステータスバーに表示されるテキスト
        builder.setTicker("Ticker");
        // アイコン
        builder.setSmallIcon(R.mipmap.ic_launcher);
        // Notificationを開いたときに表示されるタイトル
        builder.setContentTitle("ContentTitle");
        // Notificationを開いたときに表示されるサブタイトル
        builder.setContentText("ContentText");
        // Notificationを開いたときに表示されるアイコン
        builder.setLargeIcon(largeIcon);
        // 通知するタイミング
        builder.setWhen(System.currentTimeMillis());
        // 通知時の音・バイブ・ライト
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        // タップするとキャンセル(消える)
        builder.setAutoCancel(true);

        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        // Notificationを作成して通知
        manager.notify(1, builder.build());
    }
}
