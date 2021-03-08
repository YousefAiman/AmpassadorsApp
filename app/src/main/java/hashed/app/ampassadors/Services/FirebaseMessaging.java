package hashed.app.ampassadors.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.NotificationUtil.NotificationClickReceiver;
import hashed.app.ampassadors.NotificationUtil.NotificationData;
import hashed.app.ampassadors.NotificationUtil.NotificationDeleteListener;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class FirebaseMessaging extends FirebaseMessagingService {

  static int notificationNum = 0;
  private final RequestOptions requestOptions =
          new RequestOptions().override(100, 100);
  private NotificationManager notificationManager;
  private SharedPreferences sharedPreferences;
  private static NotificationClickReceiver notificationClickReceiver;

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);

    FirebaseFirestore.getInstance().collection("Users").document(
            FirebaseAuth.getInstance().getCurrentUser().getUid())
            .update("token", s);

    GlobalVariables.setCurrentToken(s);
    Log.d("ttt", "new token: " + s);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("ttt", "mesageing servie create dman");
  }

  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);


    Log.d("ttt", "message received");

    Log.d("ttt", "from: " + remoteMessage.getData().get("user"));

    if(remoteMessage.getData().get("senderUid").equals(
            FirebaseAuth.getInstance().getCurrentUser().getUid())){
      Log.d("ttt", "this notification is from me wtf");
      return;
    }

    if (notificationManager == null) {
      notificationManager = (NotificationManager)
              getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    if (sharedPreferences == null) {
      sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name),
              Context.MODE_PRIVATE);
    }

    try {
      if (remoteMessage.getData().get("type").equals("message")) {
        if (sharedPreferences.contains("currentlyMessagingUid")) {
          final Map<String, String> data = remoteMessage.getData();

          if (Objects.equals(data.get("senderUid"),
                  sharedPreferences.getString("currentlyMessagingUid", ""))){

            if(sharedPreferences.contains("isPaused") &&
                    sharedPreferences.getBoolean("isPaused",false)){
              sendNotification(remoteMessage);
            }
          }else{
            sendNotification(remoteMessage);
          }

        } else {
          sendNotification(remoteMessage);
        }
      } else {
        sendNotification(remoteMessage);
      }
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
    }
//        }
  }

  public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException, InterruptedException {

    Log.d("ttt", "sending notification");

    final Map<String, String> data = remoteMessage.getData();
//    final String title = data.get("title");
    final String type = data.get("type");

    Log.d("ttt", "type: " + type);
    createChannel(type);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, type)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(data.get("title"))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentText(data.get("body"))
            .setAutoCancel(true);

    builder.setPriority(NotificationCompat.PRIORITY_HIGH);

    if (data.containsKey("imageUrl")) {
      builder.setLargeIcon(
              Glide.with(this)
                      .asBitmap()
                      .apply(requestOptions)
                      .centerCrop()
                      .load(data.get("senderImageUrl"))
                      .submit()
                      .get());
    } else {
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_icon);
      builder.setLargeIcon(bitmap);
    }


    builder.setGroup(type);


//    if (GlobalVariables.getMessagesNotificationMap() == null)
//      GlobalVariables.setMessagesNotificationMap(new HashMap<>());

    final String identifierTitle = data.get("senderUid") + type;

    builder.setDeleteIntent(
            PendingIntent.getBroadcast(this, notificationNum,
                    new Intent(this, NotificationDeleteListener.class)
                            .putExtra("notificationIdentifierTitle", identifierTitle)
                    , PendingIntent.FLAG_UPDATE_CURRENT));

    if (type.equals("message")) {

      final Intent newIntent = new Intent(this, NotificationClickReceiver.class);
      newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      newIntent.putExtra("messagingUid",data.get("senderUid"));


      final PendingIntent pendingIntent = PendingIntent
              .getBroadcast(this, notificationNum, newIntent,
                      PendingIntent.FLAG_UPDATE_CURRENT);

      builder.setContentIntent(pendingIntent);


      NotificationManagerCompat manager = NotificationManagerCompat.from(this);

      if (!GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {

        notificationNum++;
        GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);
        Log.d("ttt", "this notification doesn't exist so building");
        manager.notify(notificationNum, builder.build());

        if (Build.VERSION.SDK_INT < 26) {
          BadgeUtil.incrementBadgeNum(this);
        }

      } else {
        Log.d("ttt", "this notification already exists just updating");
        manager.notify(GlobalVariables.getMessagesNotificationMap().get(identifierTitle)
                , builder.build());

      }

    } else {

      if (Build.VERSION.SDK_INT < 26) {
        BadgeUtil.incrementBadgeNum(this);
      }
      notificationNum++;

      GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);

      notificationManager.notify(notificationNum, builder.build());
    }

  }

  private void createChannel(String channelId) {
    if (Build.VERSION.SDK_INT >= 26) {
      if (notificationManager.getNotificationChannel(channelId) == null) {
        Log.d("ttt", "didn't find: " + channelId);
        Log.d("ttt", "creating notificaiton channel");
        NotificationChannel channel = new NotificationChannel(channelId, channelId +
                " channel", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setDescription("notifications");
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null);
        channel.enableVibration(true);
        notificationManager.createNotificationChannel(channel);
      }
    }
  }

  public static void startMessagingService(Context context) {

    context.startService(new Intent(context, FirebaseMessaging.class));

    context.getApplicationContext().getPackageManager().setComponentEnabledSetting(
            new ComponentName(context.getApplicationContext(), FirebaseMessaging.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);

  }

}
