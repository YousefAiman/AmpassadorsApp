package hashed.app.ampassadors.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import hashed.app.ampassadors.Utils.GlobalVariables;

public class WifiReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {

    final ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);


    final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

    if (netInfo != null && netInfo.isConnected()) {
      Log.d("ttt", "wifi online");
      GlobalVariables.setWifiIsOn(true);
    } else {
      Log.d("ttt", "wifi offline");
      GlobalVariables.setWifiIsOn(false);
      Toast.makeText(context, "You are currently offline!", Toast.LENGTH_SHORT).show();
    }
  }

}
