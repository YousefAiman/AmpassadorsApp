package hashed.app.ampassadors.NotificationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.MainActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationClickReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if (intent.hasExtra("destinationBundle")) {

      Bundle bundle = intent.getBundleExtra("destinationBundle");

      String sourceType = bundle.getString("sourceType");

      if (GlobalVariables.isAppIsRunning()) {

        Intent destinationIntent;

        if (sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)) {
          Log.d("ttt", "sourceType privateMessaging");

          destinationIntent = new Intent(context, PrivateMessagingActivity.class);

        } else if (sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)) {

          destinationIntent = new Intent(context, GroupMessagingActivity.class);

        } else if (sourceType.equals("meetingCreated")) {

          destinationIntent = new Intent(context, MainActivity.class);

        }else if(sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)){

          destinationIntent = new Intent(context, PrivateMessagingActivity.class);

        } else {

          destinationIntent = new Intent(context, MainActivity.class);

        }

        if (sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)){
          destinationIntent.putExtra("messagingUid", bundle.getString("sourceId"));
        }else if(sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)){
          destinationIntent.putExtra("groupId",bundle.getString("sourceId"));
        }else{
          destinationIntent.putExtra("destinationBundle", bundle);
        }

        if (sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)
                || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE) ||
                sourceType.equals("meetingStarted") || sourceType.equals("zoomMeeting") ||
                sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)) {

          final SharedPreferences sharedPreferences =
                  context.getSharedPreferences(context.getResources().getString(R.string.app_name),
                          Context.MODE_PRIVATE);

          if (sharedPreferences.contains("currentlyMessagingUid")) {
            if (bundle.getString("sourceId")
                    .equals(sharedPreferences.getString("currentlyMessagingUid", ""))) {

              Log.d("ttt", "this messaging activity is already open man");
              destinationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                      Intent.FLAG_ACTIVITY_NEW_TASK);

            } else {
              Log.d("ttt", "current messaging is not this");
              destinationIntent.setFlags(
                      Intent.FLAG_ACTIVITY_CLEAR_TOP |
                              Intent.FLAG_ACTIVITY_NEW_TASK);
            }
          } else {
            Log.d("ttt", "no current messaging in shared");
            destinationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          }

        } else if (sourceType.equals("meetingCreated")) {


        }else{

        }
//        destinationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(destinationIntent);
        Log.d("ttt", "clicked notificaiton while app is running");
      } else {

        Intent intent1 = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        if(sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)){
//          intent1.putExtra("groupId",bundle.getString("sourceId"));
//        }else{
          intent1.putExtra("destinationBundle",
                  intent.getBundleExtra("destinationBundle"));
//        }

        context.startActivity(intent1);

        Log.d("ttt", "clicked notificaiton while app isn't running");
      }
    }
  }
}
