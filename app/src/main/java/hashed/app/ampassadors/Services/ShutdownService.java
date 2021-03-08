package hashed.app.ampassadors.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class ShutdownService extends Service {

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);

    Log.d("exoPlayerPlayback","onTaskRemoved");

    GlobalVariables.setAppIsRunning(false);

    getSharedPreferences(getResources().getString(R.string.app_name),MODE_PRIVATE).edit()
            .remove("isPaused")
            .remove("currentlyMessagingUid").apply();


    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            GlobalVariables.getRegisteredNetworkCallback() != null){
     ((ConnectivityManager) getApplicationContext()
             .getSystemService(Context.CONNECTIVITY_SERVICE))
             .unregisterNetworkCallback(GlobalVariables.getRegisteredNetworkCallback());

      GlobalVariables.setRegisteredNetworkCallback(null);

    }else if(GlobalVariables.getCurrentWifiReceiver()!=null){
      unregisterReceiver(GlobalVariables.getCurrentWifiReceiver());
      GlobalVariables.setCurrentWifiReceiver(null);
    }

    this.stopSelf();
  }
}
