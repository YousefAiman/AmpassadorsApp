package hashed.app.ampassadors.NotificationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.MainActivity;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationClickReceiver extends BroadcastReceiver {


  private SharedPreferences sharedPreferences;

  @Override
  public void onReceive(Context context, Intent intent) {


    if (intent.hasExtra("messagingBundle")) {

      if(GlobalVariables.isAppIsRunning()) {

        if (sharedPreferences == null) {
          sharedPreferences = context.getSharedPreferences("rbeno", Context.MODE_PRIVATE);
        }

        Intent intent1 = new Intent(context, PrivateMessagingActivity.class)
                .putExtra("messagingBundle",
                        intent.getBundleExtra("messagingBundle"));

        if (sharedPreferences.contains("currentMessagingUserId")) {
          final Bundle messagingBundle = intent.getBundleExtra("messagingBundle");
          if (messagingBundle.getString("promouserid")
                  .equals(sharedPreferences.getString("currentMessagingUserId", "")) &&
                  messagingBundle.getLong("intendedpromoid") ==
                          sharedPreferences.getLong("currentMessagingPromoId", 0)) {
            Log.d("ttt", "this messaging activity is already open man");
            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
          } else {
            Log.d("ttt", "current messaging is not this");
            intent1.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
          }
        } else {
          Log.d("ttt", "no current messaging in shared");
          intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent1);
        Log.d("ttt", "clicked notificaiton while app is running");
      } else {

        context.startActivity(new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("messagingBundle",
                        intent.getBundleExtra("messagingBundle")));

        Log.d("ttt", "clicked notificaiton while app isn't running");
      }


    }

  }
}
