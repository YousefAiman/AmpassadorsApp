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

      if (GlobalVariables.isAppIsRunning()) {

        Bundle bundle = intent.getBundleExtra("destinationBundle");

        Intent destinationIntent;

        String sourceType = bundle.getString("sourceType");

        if (sourceType.equals("privateMessaging")) {
          Log.d("ttt", "sourceType privateMessaging");

          destinationIntent = new Intent(context, PrivateMessagingActivity.class);

        } else if (sourceType.equals("groupMessaging")) {

          destinationIntent = new Intent(context, GroupMessagingActivity.class);

        } else if (sourceType.equals("meetingCreated")) {

          destinationIntent = new Intent(context, MainActivity.class);

        } else {

          destinationIntent = new Intent(context, MainActivity.class);

        }


        if (sourceType.equals("privateMessaging")){
          destinationIntent.putExtra("messagingUid", bundle.getString("sourceId"));
        }else{
          destinationIntent.putExtra("destinationBundle", bundle);
        }



        if (sourceType.equals("privateMessaging") || sourceType.equals("groupMessaging") ||
                sourceType.equals("meetingStarted") || sourceType.equals("zoomMeeting")) {


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


        }

        context.startActivity(destinationIntent);
        Log.d("ttt", "clicked notificaiton while app is running");
      } else {

        context.startActivity(new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("destinationBundle",
                        intent.getBundleExtra("destinationBundle")));


        Log.d("ttt", "clicked notificaiton while app isn't running");
      }
    }
  }
}
