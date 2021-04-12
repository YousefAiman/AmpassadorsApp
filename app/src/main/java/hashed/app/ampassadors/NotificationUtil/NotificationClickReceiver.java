package hashed.app.ampassadors.NotificationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.GroupMessagingActivity2;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.MessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity2;
import hashed.app.ampassadors.MainActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationClickReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if (intent.hasExtra("sourceId") && intent.hasExtra("sourceType")) {


      String sourceType = intent.getStringExtra("sourceType");
      String sourceId = intent.getStringExtra("sourceId");

      if (GlobalVariables.isAppIsRunning()) {

        Intent destinationIntent;

        switch (sourceType){

          case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
            destinationIntent = new Intent(context, PrivateMessagingActivity2.class);
            break;

          case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
            destinationIntent = new Intent(context, GroupMessagingActivity2.class);
            break;
          case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
            destinationIntent = new Intent(context, CourseMessagingActivity.class);
            break;
          case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
            destinationIntent = new Intent(context, MeetingMessagingActivity.class);
            break;

//          destinationIntent.putExtra("messagingUid",sourceId);

          case FirestoreNotificationSender.TYPE_MEETING_ADDED:
          case FirestoreNotificationSender.TYPE_MEETING_STARTED:
          case FirestoreNotificationSender.TYPE_LIKE:
          case FirestoreNotificationSender.TYPE_COMMENT:
          case FirestoreNotificationSender.TYPE_COURSE_STARTED:
          case FirestoreNotificationSender.TYPE_GROUP_ADDED:

            destinationIntent = new Intent(context, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            destinationIntent.putExtra("destinationId",sourceId);

            break;

          default:

            destinationIntent = new Intent(context, MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        }

        context.startActivity(destinationIntent);

        Log.d("ttt", "clicked notificaiton while app isn't running");
      }
    }
  }

  private void checkCurrentMessagingActivity(Context context,Intent destinationIntent,
                                             String sourceId){
    final SharedPreferences sharedPreferences =
            context.getSharedPreferences(context.getResources().getString(R.string.app_name),
                    Context.MODE_PRIVATE);

    if (sharedPreferences.contains("currentlyMessagingUid")) {
      if (sourceId.equals(sharedPreferences.getString("currentlyMessagingUid", ""))) {

        Log.d("ttt", "this messaging activity is already open man");
        destinationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
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

  }
}
