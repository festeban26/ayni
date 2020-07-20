package com.festeban26.ayni.services.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.festeban26.ayni.AppAuth;
import com.festeban26.ayni.R;
import com.festeban26.ayni.activities.MessageActivity;
import com.festeban26.ayni.activities.MyTripAsDriverDetailsActivity;
import com.festeban26.ayni.firebase.utility.DbPath;
import com.festeban26.ayni.utils.IntentNames;
import com.festeban26.ayni.utils.Preferences;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static String NOTIFICATIONS_CHANNEL_ID_TRIP_UPDATES = "NOTIFICATIONS_CHANNEL_ID_TRIP_UPDATES";
    private final static String NOTIFICATIONS_CHANNEL_ID_MESSAGING = "NOTIFICATIONS_CHANNEL_MESSAGING";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = new HashMap<>(remoteMessage.getData());

        String type = data.get("type");
        if (type != null) {
            if (type.equalsIgnoreCase("messaging")) {
                String title = getString(R.string.String_Notifications_NewMessageTitle);
                String body = data.get("message");
                sendNotification(title, body, MyFirebaseMessagingService.NOTIFICATIONS_CHANNEL_ID_MESSAGING, data);
            } else if (type.equalsIgnoreCase("trip_updates")) {
                String title = getString(R.string.String_Notifications_SomeoneHasJustBookedYourTrip);
                String body = "";
                sendNotification(title, body, MyFirebaseMessagingService.NOTIFICATIONS_CHANNEL_ID_TRIP_UPDATES, data);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        SharedPreferences preferences = getSharedPreferences(Preferences.FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Preferences.NOTIFICATIONS_TOKEN, token);
        editor.apply();

        boolean isUserLoggedIn = AppAuth.getInstance().isUserLoggedIn(this);

        if (isUserLoggedIn) {
            sendRegistrationToServer(token);
        }

    }

    public void sendRegistrationToServer(String token) {
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        String userId = AppAuth.getInstance().getCurrentFacebookUser(this).getId();
        FirebaseDatabase.getInstance().getReference(DbPath.NOTIFICATION_TOKENS)
                .child(userId).setValue(map);
    }


    private void sendNotification(String notificationTitle, String notificationBody, String notificationChannelId) {
        sendNotification(notificationTitle, notificationBody, notificationChannelId, null);
    }


    private void sendNotification(String notificationTitle, String notificationBody, String NOTIFICATION_CHANNEL_ID, @Nullable Map<String, String> data) {

        PendingIntent pendingIntent = null;
        if (data != null) {
            String type = data.get("type");
            if (type != null) {
                if (type.equalsIgnoreCase("messaging")) {

                    String sender = data.get("sender");

                    if (sender != null) {
                        Intent intent = new Intent(this, MessageActivity.class);
                        intent.putExtra(IntentNames.MESSAGES_ACTIVITY__USER_ID, sender);
                        pendingIntent = PendingIntent.getActivity(this, 0, intent,
                                PendingIntent.FLAG_ONE_SHOT);
                    }
                } else if (type.equalsIgnoreCase("trip_updates")) {


                    String originCity = data.get("originCity");
                    String destinationCity = data.get("destinationCity");
                    String year = data.get("year");
                    String month = data.get("month");
                    String day = data.get("day");
                    String id = data.get("id");

                    if (originCity != null && destinationCity != null && year != null && month != null && day != null && id != null) {

                        Intent intent = new Intent(this, MyTripAsDriverDetailsActivity.class);

                        Bundle bundle = new Bundle();
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__ORIGIN_CITY, originCity);
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__DESTINATION_CITY, destinationCity);
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__YEAR, year);
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__MONTH, month);
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__DAY, day);
                        bundle.putString(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__ID, id);

                        intent.putExtra(IntentNames.MY_TRIP_AS_DRIVER_DETAILS_ACTIVITY__BUNDLE, bundle);

                        pendingIntent = PendingIntent.getActivity(this, 0, intent,
                                PendingIntent.FLAG_ONE_SHOT);
                    }
                }
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription(NOTIFICATION_CHANNEL_ID);
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(R.color.Color_Post);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher) //Notification icon
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setContentIntent(pendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());
    }
}
