package ru.ifsoft.network.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import ru.ifsoft.network.AgoraVideoCallActivity;
import ru.ifsoft.network.AppActivity;
import ru.ifsoft.network.ChatFragment;
import ru.ifsoft.network.DialogsActivity;
import ru.ifsoft.network.MainActivity;
import ru.ifsoft.network.R;
import ru.ifsoft.network.app.App;
import ru.ifsoft.network.constants.Constants;

public class MyFcmListenerService extends FirebaseMessagingService implements Constants {

    private int flag;

    public MyFcmListenerService () {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            flag = PendingIntent.FLAG_IMMUTABLE;

        } else {

            flag =  PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {

        String from = message.getFrom();
        Map data = message.getData();

        Log.e("Message", "Could not parse malformed JSON: \"" + data.toString() + "\"");

        generateNotification(getApplicationContext(), data);
    }

    @Override
    public void onNewToken(String token) {

        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        App.getInstance().setGcmToken(token);
    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {

        sendNotification("Upstream message sent. Id=" + msgId);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {

        Log.e("Message", "Could not parse malformed JSON: \"" + msg + "\"");
    }

    /**
     * Create a notification to inform the user that server has sent a message.
     */
    private void generateNotification(Context context, Map data) {

        String CHANNEL_ID = getString(R.string.default_notification_channel_id); // id for channel.

        CharSequence name = context.getString(R.string.channel_name);     // user visible name of channel.

        NotificationChannel mChannel;

        String msgId = "0";
        String msgFromUserId = "0";
        String msgFromUserState = "0";
        String msgFromUserVerify = "0";
        String msgFromUserUsername = "";
        String msgFromUserFullname = "";
        String msgFromUserPhotoUrl = "";
        String msgMessage = "";
        String msgImgUrl = "";
        String msgCreateAt = "0";
        String msgDate = "";
        String msgTimeAgo = "";
        String msgRemoveAt = "0";

        String message = data.get("msg").toString();
        String type = data.get("type").toString();
        String actionId = data.get("id").toString();
        String accountId = data.get("accountId").toString();

        if (Integer.valueOf(type) == GCM_NOTIFY_MESSAGE) {

            msgId = data.get("msgId").toString();
            msgFromUserId = data.get("msgFromUserId").toString();
            msgFromUserState = data.get("msgFromUserState").toString();
            msgFromUserVerify = data.get("msgFromUserVerify").toString();

            if (data.containsKey("msgFromUserUsername")) {

                msgFromUserUsername = data.get("msgFromUserUsername").toString();
            }

            if (data.containsKey("msgFromUserFullname")) {

                msgFromUserFullname = data.get("msgFromUserFullname").toString();
            }

            if (data.containsKey("msgFromUserPhotoUrl")) {

                msgFromUserPhotoUrl = data.get("msgFromUserPhotoUrl").toString();
            }

            if (data.containsKey("msgMessage")) {

                msgMessage = data.get("msgMessage").toString();
            }

            if (data.containsKey("msgImgUrl")) {

                msgImgUrl = data.get("msgImgUrl").toString();
            }

            msgCreateAt = data.get("msgCreateAt").toString();
            msgDate = data.get("msgDate").toString();
            msgTimeAgo = data.get("msgTimeAgo").toString();
            msgRemoveAt = data.get("msgRemoveAt").toString();
        }

        int icon = R.drawable.ic_action_push_notification;
        long when = System.currentTimeMillis();
        String title = context.getString(R.string.app_name);

//        App.getInstance().reload();

        switch (Integer.valueOf(type)) {

            case GCM_NOTIFY_SYSTEM: {

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_action_push_notification)
                                .setContentTitle(title)
                                .setContentText(message);

                Intent resultIntent;

                if (App.getInstance().getId() != 0) {

                    resultIntent = new Intent(context, MainActivity.class);

                } else {

                    resultIntent = new Intent(context, AppActivity.class);
                }

                resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    int importance = NotificationManager.IMPORTANCE_HIGH;

                    mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                    mNotificationManager.createNotificationChannel(mChannel);
                }

                mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                mBuilder.setAutoCancel(true);
                mNotificationManager.notify(0, mBuilder.build());

                break;
            }

            case GCM_NOTIFY_CUSTOM: {

                if (App.getInstance().getId() != 0) {

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_PERSONAL: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_LIKE: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowLikesGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_like);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_IMAGE_LIKE: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowLikesGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_like);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_FOLLOWER: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowFollowersGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_friend_request);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_FRIEND_REQUEST_ACCEPTED: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNewFriendsCount(App.getInstance().getNewFriendsCount() + 1);

                    message = context.getString(R.string.label_gcm_friend_request_accepted);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", 1); // 1 = friends page
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_COMMENT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowCommentsGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_comment);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_IMAGE_COMMENT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowCommentsGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_comment);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_COMMENT_REPLY: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowCommentReplyGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_comment_reply);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_IMAGE_COMMENT_REPLY: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowCommentReplyGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_comment_reply);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_GIFT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    if (App.getInstance().getAllowGiftsGCM() == ENABLED) {

                        message = context.getString(R.string.label_gcm_gift);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        Intent resultIntent = new Intent(context, MainActivity.class);
                        resultIntent.putExtra("pageId", 3); // 3 = notifications page
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(MainActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            case GCM_NOTIFY_PROFILE_PHOTO_REJECT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    message = context.getString(R.string.label_gcm_profile_photo_reject);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", 3); // 3 = notifications page
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_PROFILE_COVER_REJECT: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    App.getInstance().setNotificationsCount(App.getInstance().getNotificationsCount() + 1);

                    message = context.getString(R.string.label_gcm_profile_cover_reject);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_action_push_notification)
                                    .setContentTitle(title)
                                    .setContentText(message);

                    Intent resultIntent = new Intent(context, MainActivity.class);
                    resultIntent.putExtra("pageId", 3); // 3 = notifications page
                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        int importance = NotificationManager.IMPORTANCE_HIGH;

                        mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                        mNotificationManager.createNotificationChannel(mChannel);
                    }

                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setAutoCancel(true);
                    mNotificationManager.notify(0, mBuilder.build());
                }

                break;
            }

            case GCM_NOTIFY_MESSAGE: {

                Log.e("FCM", "New Message");

                if (App.getInstance().getId() != 0 && Long.valueOf(accountId) == App.getInstance().getId()) {

                    if (App.getInstance().getCurrentChatId() == Integer.valueOf(actionId)) {

                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);

                        i.putExtra("msgId", Integer.valueOf(msgId));
                        i.putExtra("msgFromUserId", Long.valueOf(msgFromUserId));
                        i.putExtra("msgFromUserState", Integer.valueOf(msgFromUserState));
                        i.putExtra("msgFromUserVerify", Integer.valueOf(msgFromUserVerify));
                        i.putExtra("msgFromUserUsername", String.valueOf(msgFromUserUsername));
                        i.putExtra("msgFromUserFullname", String.valueOf(msgFromUserFullname));
                        i.putExtra("msgFromUserPhotoUrl", String.valueOf(msgFromUserPhotoUrl));
                        i.putExtra("msgMessage", String.valueOf(msgMessage));
                        i.putExtra("msgImgUrl", String.valueOf(msgImgUrl));
                        i.putExtra("msgCreateAt", Integer.valueOf(msgCreateAt));
                        i.putExtra("msgDate", String.valueOf(msgDate));
                        i.putExtra("msgTimeAgo", String.valueOf(msgTimeAgo));

                        i.setPackage(App.getInstance().getPackageName());

                        context.sendBroadcast(i);

                    } else {

                        if (App.getInstance().getMessagesCount() == 0) App.getInstance().setMessagesCount(App.getInstance().getMessagesCount() + 1);

                        if (App.getInstance().getAllowMessagesGCM() == ENABLED) {

                             message = context.getString(R.string.label_gcm_message);

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context, CHANNEL_ID)
                                            .setSmallIcon(R.drawable.ic_action_push_notification)
                                            .setContentTitle(title)
                                            .setContentText(message);

                            Intent resultIntent = new Intent(context, DialogsActivity.class);
                            resultIntent.putExtra("fcm", true);
                            resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(DialogsActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, flag);
                            mBuilder.setContentIntent(resultPendingIntent);

                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                int importance = NotificationManager.IMPORTANCE_HIGH;

                                mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                                mNotificationManager.createNotificationChannel(mChannel);
                            }

                            mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                            mBuilder.setAutoCancel(true);
                            mNotificationManager.notify(0, mBuilder.build());
                        }
                    }
                }

                break;
            }

            case GCM_NOTIFY_SEEN: {

                if (App.getInstance().getId() != 0 && Long.valueOf(accountId) == App.getInstance().getId()) {

                    Log.e("SEEN", "IN LISTENER");

                    if (App.getInstance().getCurrentChatId() == Integer.valueOf(actionId)) {

                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_SEEN);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        i.setPackage(App.getInstance().getPackageName());
                        context.sendBroadcast(i);
                    }

                    break;
                }
            }

            case GCM_NOTIFY_TYPING_START: {

                if (App.getInstance().getId() != 0 && Long.valueOf(accountId) == App.getInstance().getId()) {

                    if (App.getInstance().getCurrentChatId() == Integer.valueOf(actionId)) {

                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_TYPING_START);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        i.setPackage(App.getInstance().getPackageName());
                        context.sendBroadcast(i);
                    }

                    break;
                }
            }

            case GCM_NOTIFY_TYPING_END: {

                if (App.getInstance().getId() != 0 && Long.valueOf(accountId) == App.getInstance().getId()) {

                    if (App.getInstance().getCurrentChatId() == Integer.valueOf(actionId)) {

                        Intent i = new Intent(ChatFragment.BROADCAST_ACTION_TYPING_END);
                        i.putExtra(ChatFragment.PARAM_TASK, 0);
                        i.putExtra(ChatFragment.PARAM_STATUS, ChatFragment.STATUS_START);
                        i.setPackage(App.getInstance().getPackageName());
                        context.sendBroadcast(i);
                    }

                    break;
                }
            }

            case GCM_NOTIFY_AGORA_VIDEO_CALL: {

                if (App.getInstance().getId() != 0 && Long.toString(App.getInstance().getId()).equals(accountId)) {

                    if (App.getInstance().getAllowVideoCalls() == 1) {

                        message = context.getString(R.string.label_gcm_new_video_call);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_action_push_notification)
                                        .setContentTitle(title)
                                        .setContentText(message);

                        long call_id = (long) Long.parseLong(actionId);
                        int req_id = Integer.parseInt(actionId);

                        Intent resultIntent = new Intent(context, AgoraVideoCallActivity.class);
                        resultIntent.putExtra("gcm", true);
                        resultIntent.putExtra("call_id", call_id);
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                        Log.e("call_id cast", Long.toString(call_id));
                        Log.e("call_id", actionId);

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(AgoraVideoCallActivity.class);
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(req_id, flag);
                        //PendingIntent contentIntent = PendingIntent.getActivity(context, UNIQUE_INT_PER_CALL, notificationIntent, 0);
                        mBuilder.setContentIntent(resultPendingIntent);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                            int importance = NotificationManager.IMPORTANCE_HIGH;

                            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

                            mNotificationManager.createNotificationChannel(mChannel);
                        }

                        mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        mBuilder.setAutoCancel(true);
                        mNotificationManager.notify(0, mBuilder.build());
                    }
                }

                break;
            }

            default: {

                break;
            }
        }
    }
}