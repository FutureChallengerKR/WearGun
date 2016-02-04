package com.fc.aa;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("ListenerService", "onMessageReceived()");
        if(messageEvent.getPath().equals("/exec")) {
            Intent viewIntent = new Intent(this, MainActivity.class);
            PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, viewIntent, 0);

            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            b.setContentText("게임이 시작되려 합니다.");
            b.setSmallIcon(R.drawable.icon);
            b.setContentTitle("Wear Gun");
            b.setLocalOnly(true);
            b.setContentIntent(viewPendingIntent);
            b.setVibrate(new long[]{500,500,500});
            NotificationManagerCompat man = NotificationManagerCompat.from(this);
            man.notify(0, b.build());
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}