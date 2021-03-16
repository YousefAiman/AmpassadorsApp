package hashed.app.ampassadors.NotificationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationDeleteListener extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.d("ttt", "notification dismissedd man!!");

    if (intent.hasExtra("notificationIdentifierTitle") &&
            GlobalVariables.getMessagesNotificationMap() != null)
      GlobalVariables.getMessagesNotificationMap().remove(
              intent.getStringExtra("notificationIdentifierTitle"));

  }

}