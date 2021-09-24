package hashed.app.ampassadors.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import hashed.app.ampassadors.Activities.CourseActivity;
import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.NotificationUtil.NotificationDeleteListener;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.ZoomRequestCreator;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

  static int notificationNum = 0;
  private final RequestOptions requestOptions =
          new RequestOptions().override(100, 100);
  private NotificationManager notificationManager;
  private SharedPreferences sharedPreferences;
  private List<ListenerRegistration> listenerRegistrationList;

  public static void startMessagingService(Context context) {

    context.startService(new Intent(context, FirebaseMessagingService.class));

    context.getApplicationContext().getPackageManager().setComponentEnabledSetting(
            new ComponentName(context.getApplicationContext(), FirebaseMessagingService.class),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP);

  }

  @Override
  public void onNewToken(@NonNull String s) {
    super.onNewToken(s);

    FirebaseAuth auth = FirebaseAuth.getInstance();

    if(auth.getCurrentUser()!=null && !auth.getCurrentUser().isAnonymous()){
      FirebaseFirestore.getInstance().collection("Users")
              .document(auth.getCurrentUser().getUid()).update("token", s);
    }


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
    if (FirebaseAuth.getInstance().getCurrentUser() == null){
      return;
    }

    if(remoteMessage.getNotification() != null){
      Log.d("ttt","type notification notification no data");
      final String title = remoteMessage.getNotification().getTitle();
      if(title!=null && !title.isEmpty()){

        final String zoomMeetingId = remoteMessage.getNotification().getBody();
        Log.d("ttt","meetingId: "+zoomMeetingId);
        Log.d("ttt","title: "+title);
        try {
          switch (Integer.parseInt(title)){

            case ZoomRequestCreator.ZOOM_MEETING_ENDED:

              //a zoom meeting ended
              Log.d("ttt","zoom meeting ended");
              endZoomMeeting(zoomMeetingId);

              break;
          }

        }catch (NumberFormatException e){
          Log.d("ttt","notification body error");
        }
      }

    }else{

      if (remoteMessage.getData().isEmpty()) {
        return;
      }

//      if (remoteMessage.getData().containsKey("senderUid")
//              && remoteMessage.getData().get("senderUid").
//              equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//        Log.d("ttt", "this notification is from me wtf");
//        return;
//      }

      try {
        final String sourceType = remoteMessage.getData().get("sourceType");

        if(sourceType == null){
          return;
        }

        if (sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE) ||
                sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)||
            sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)||
                sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
        && getSharedPreferences().contains("currentlyMessagingUid")) {

            final Map<String, String> data = remoteMessage.getData();

            //checking the current user i'm messaging
            if (Objects.equals(data.get("senderUid"), getSharedPreferences().getString("currentlyMessagingUid", ""))) {
                //current user i'm messaging is the notification sender
                if((getSharedPreferences().contains("isPaused") && getSharedPreferences().getBoolean("isPaused", true))){
                  //will send notification send activity is paused
                sendNotification(remoteMessage);
              }

            }else{
              sendNotification(remoteMessage);
            }
        } else {
          sendNotification(remoteMessage);
        }
      } catch (ExecutionException | InterruptedException e) {
        e.printStackTrace();
      }

    }

  }

  public void sendNotification(RemoteMessage remoteMessage) throws ExecutionException,
          InterruptedException {

    Log.d("ttt", "sending notification");
    final Map<String, String> data = remoteMessage.getData();
//    final String type = data.get("type");
    final String sourceType = data.get("sourceType");
    final String sourceId = data.get("sourceId");
    if(sourceType == null || sourceId == null){
      return;
    }
    final String identifierTitle = sourceId + sourceType;

    if (GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle) &&
            !(sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE) ||
                    sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)
                    || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
                    || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE))) {
      Log.d("ttt", "already received this notifacaiton and it's not a message");
      return;
    }

    Log.d("ttt", "type: " + sourceType);
    createChannel(sourceType);
    Log.d("ttt", "before builder createiong");

    final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, sourceType)
              .setSmallIcon(R.drawable.app_icon_small)
              .setContentTitle(data.get("title"))
              .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
              .setContentText(data.get("body"))
              .setAutoCancel(true)
              .setPriority(NotificationCompat.PRIORITY_HIGH)
              .setGroup(sourceType);

      final String imageUrl = data.get("senderImageUrl");
      Log.d("ttt", "before imageUrl");

      if (imageUrl != null && !imageUrl.isEmpty()) {
        builder.setLargeIcon(
                Glide.with(this)
                        .asBitmap()
                        .apply(requestOptions)
                        .centerCrop()
                        .load(imageUrl)
                        .submit()
                        .get());
      } else {
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.drawable.app_icon_small));
      }

      Log.d("ttt", "after imageUrl");
      Log.d("ttt", "after builder createiong");
//    if (GlobalVariables.getMessagesNotificationMap() == null)
//      GlobalVariables.setMessagesNotificationMap(new HashMap<>());

      builder.setDeleteIntent(
              PendingIntent.getBroadcast(this, notificationNum,
                      new Intent(this, NotificationDeleteListener.class)
                              .putExtra("notificationIdentifierTitle", identifierTitle)
                      , PendingIntent.FLAG_UPDATE_CURRENT));


//      final Intent newIntent = new Intent(this, NotificationClickReceiver.class);
//      newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
////      final Bundle messagingBundle = new Bundle();
////      messagingBundle.putString("sourceId", data.get("sourceId"));
////      messagingBundle.putString("sourceType", data.get("sourceType"));
////      newIntent.putExtra("destinationBundle", messagingBundle);
////      newIntent.putExtra("sourceId", data.get("sourceId"));
//
//      newIntent.putExtra("sourceType", data.get("sourceType"));
//      newIntent.putExtra("sourceId", data.get("sourceId"));

      if (sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)
      || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)
      || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)
      || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)) {

        Log.d("ttt", "type is message");

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                notificationNum,
                directToIntent(sourceId,sourceType),
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

//        final NotificationManagerCompat manager = NotificationManagerCompat.from(this);

        if (!GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {

          Log.d("ttt", "global variables contains: " + identifierTitle);
          notificationNum++;
          GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);
          Log.d("ttt", "this notification doesn't exist so building");
          getNotificationManager().notify(notificationNum, builder.build());

          if (Build.VERSION.SDK_INT < 26) {
            BadgeUtil.incrementBadgeNum(this);
          }

        } else {

          Log.d("ttt", "global variables doesn't contain: " + identifierTitle);

          Log.d("ttt", "this notification already exists just updating");
          getNotificationManager().notify(
                  GlobalVariables.getMessagesNotificationMap().get(identifierTitle), builder.build());

        }

      } else {

        GlobalVariables.getMessagesNotificationMap().put(identifierTitle, notificationNum);

        if (Build.VERSION.SDK_INT < 26) {
          BadgeUtil.incrementBadgeNum(this);
        }
        notificationNum++;


        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                notificationNum, directToIntent(sourceId,sourceType), PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);
        getNotificationManager().notify(notificationNum, builder.build());

//        listenToNotificationRemoval(data,notificationNum);

      }
  }

  private void listenToNotificationRemoval(Map<String, String> data,int notifId){

    if(listenerRegistrationList == null)
      listenerRegistrationList = new ArrayList<>();

//     String sourceType = data.get("sourceType");
//
//    switch (sourceType){
//
//      case "Post Like":
//        sourceType = FirestoreNotificationSender.TYPE_LIKE;
//        break;
//
//      case "Group Messages":
//        sourceType = FirestoreNotificationSender.TYPE_GROUP_MESSAGE;
//        break;
//
//        case "Course Messages":
//        sourceType = FirestoreNotificationSender.TYPE_COURSE_MESSAGE;
//        break;
//
//        case "Group added":
//        sourceType = FirestoreNotificationSender.TYPE_GROUP_ADDED;
//        break;
//
//        case "Meeting Messages":
//        sourceType = FirestoreNotificationSender.TYPE_MEETING_MESSAGE;
//        break;
//
//      case "Meeting added":
//        sourceType = FirestoreNotificationSender.TYPE_MEETING_ADDED;
//        break;
//
//      case "Private Messages":
//        sourceType = FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE;
//        break;
//
//      case "Post Comment":
//        sourceType = FirestoreNotificationSender.TYPE_COMMENT;
//        break;
//
//    }

    String documentPath = FirebaseAuth.getInstance().getCurrentUser().getUid() + "_"+
                    data.get("sourceId") +"_"+ data.get("sourceType");

    Log.d("ttt","documentPath: "+documentPath);

    final int index = listenerRegistrationList.size();

    listenerRegistrationList.add(
            FirebaseFirestore.getInstance().collection("Notifications")
            .document(documentPath)
            .addSnapshotListener((value, error) -> {
              if(value != null && !value.exists()){

                Log.d("ttt","removed notification: "+value.getId());

                getNotificationManager().cancel(notifId);

                removeListener(index);
              }
            }));
  }

  private void endZoomMeeting(final String zoomMeetingId){

    FirebaseFirestore.getInstance().collection("Meetings")
            .whereEqualTo("currentZoomMeeting.id",zoomMeetingId)
            .limit(1)
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

        if(queryDocumentSnapshots!=null && !queryDocumentSnapshots.isEmpty()){

          final DocumentSnapshot snap =queryDocumentSnapshots.getDocuments().get(0);
          if(snap.contains("currentZoomMeeting") && snap.get("currentZoomMeeting") != null){
            queryDocumentSnapshots.getDocuments().get(0).getReference()
                    .update("currentZoomMeeting",null);
          }
        }

      }
    });

  }


  private Intent directToIntent(String sourceId,String sourceType){

    Intent destinationIntent = null;

        Log.d("ttt","AppIsRunning");
        if(sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)

                || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)
                || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)

                || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
                || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_STARTED)
                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)

                || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)
                || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_STARTED)
                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)
//                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)
//                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)
        ) {

          Log.d("ttt","messaging type");

          switch (sourceType) {
            case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
              destinationIntent = new Intent(this, PrivateMessagingActivity.class);
              break;

            case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
              case FirestoreNotificationSender.TYPE_GROUP_ADDED:
              destinationIntent = new Intent(this, GroupMessagingActivity.class);
              break;

            case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
            case FirestoreNotificationSender.TYPE_COURSE_STARTED:
            case FirestoreNotificationSender.TYPE_ZOOM_COURSE:
              destinationIntent = new Intent(this, CourseMessagingActivity.class);
              break;

            case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
            case FirestoreNotificationSender.TYPE_MEETING_STARTED:
            case FirestoreNotificationSender.TYPE_ZOOM_MEETING:
              destinationIntent = new Intent(this, MeetingMessagingActivity.class);
              break;
          }

          Log.d("ttt","sourceId: "+sourceId);
          destinationIntent.putExtra("messagingUid", sourceId);

          if(sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING) ||
          sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)){
            destinationIntent.putExtra("type",FirestoreNotificationSender.TYPE_ZOOM);
          }

          destinationIntent.setFlags(getIntentFlags(sourceId));
//          this.startActivity(destinationIntent);

        }else{

          switch (sourceType){
            case FirestoreNotificationSender.TYPE_MEETING_ADDED:
              destinationIntent = new Intent(this,MeetingActivity.class);
              destinationIntent.putExtra("meetingID",sourceId);

              Log.d("ttt","meeting source: "+sourceId);
//              fetchObjectAndStartIntent(MeetingActivity.class,context,"Meetings",
//                      Meeting.class,sourceId,"meeting");

              break;

            case FirestoreNotificationSender.TYPE_COURSE_ADDED:

              destinationIntent = new Intent(this,CourseActivity.class);
              destinationIntent.putExtra("courseID",sourceId);

              break;

            case FirestoreNotificationSender.TYPE_POST_LIKE:
            case FirestoreNotificationSender.TYPE_POST_COMMENT:
            case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:

              destinationIntent = new Intent(this,PostNewsActivity.class)
                      .putExtra("notificationPostId",sourceId);

              break;

            case FirestoreNotificationSender.TYPE_POLL_LIKE:
            case FirestoreNotificationSender.TYPE_POLL_COMMENT:
            case FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE:

             destinationIntent =  new Intent(this, PostPollActivity.class)
                      .putExtra("postId",sourceId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

          }
        }

        return destinationIntent;
  }

  private int getIntentFlags(String sourceId){

    Log.d("ttt","checkCurrentMessagingActivity");
    if (GlobalVariables.isAppIsRunning() && getSharedPreferences().contains("currentlyMessagingUid")) {
      if (sourceId.equals(getSharedPreferences().getString("currentlyMessagingUid", ""))) {
        Log.d("ttt", "this messaging activity is already open man");
        return Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      } else {
        Log.d("ttt", "current messaging is not this");
        return Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      }
    } else {
      Log.d("ttt", "no current messaging in shared");
      return Intent.FLAG_ACTIVITY_NEW_TASK;
    }

  }

  private void removeListener(int index){
    if(listenerRegistrationList!=null && listenerRegistrationList.size() > index){
      listenerRegistrationList.remove(index);
    }
  }

  private void createChannel(String channelId) {
    if (Build.VERSION.SDK_INT >= 26) {
      if (getNotificationManager().getNotificationChannel(channelId) == null) {
        Log.d("ttt", "didn't find: " + channelId);
        Log.d("ttt", "creating notificaiton channel");
        NotificationChannel channel = new NotificationChannel(channelId, channelId,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setDescription(channelId);
        channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                null);
        channel.enableVibration(true);
        getNotificationManager().createNotificationChannel(channel);
      }
    }
  }

  public NotificationManager getNotificationManager() {
    if (notificationManager == null) {
      notificationManager = (NotificationManager)
              getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }
    return notificationManager;
  }

  public SharedPreferences getSharedPreferences() {
    if(sharedPreferences == null){
      sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name),
              Context.MODE_PRIVATE);
    }
    return sharedPreferences;
  }
}