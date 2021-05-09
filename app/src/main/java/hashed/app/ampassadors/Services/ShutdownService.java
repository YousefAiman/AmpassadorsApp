package hashed.app.ampassadors.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class ShutdownService extends Service {

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d("ttt","shut down service created");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_NOT_STICKY;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {

    super.onTaskRemoved(rootIntent);
//    Toast.makeText(this, "app shutdown", Toast.LENGTH_SHORT).show();

    Log.d("ttt","task removed");

    GlobalVariables.getInstance().setAppIsRunning(false);

    getSharedPreferences(getResources().getString(R.string.app_name), MODE_PRIVATE).edit()
            .remove("isPaused")
            .remove("currentlyMessagingUid").apply();


    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    if(currentUser != null && !currentUser.isAnonymous()){
      FirebaseFirestore.getInstance().collection("Users")
              .document(currentUser.getUid()).update("status",false);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            GlobalVariables.getInstance().getRegisteredNetworkCallback() != null) {
      ((ConnectivityManager) getApplicationContext()
              .getSystemService(Context.CONNECTIVITY_SERVICE))
              .unregisterNetworkCallback(GlobalVariables.getInstance().getRegisteredNetworkCallback());

      GlobalVariables.getInstance().setRegisteredNetworkCallback(null);

    } else if (GlobalVariables.getInstance().getCurrentWifiReceiver() != null) {
      unregisterReceiver(GlobalVariables.getInstance().getCurrentWifiReceiver());
      GlobalVariables.getInstance().setCurrentWifiReceiver(null);
    }

//  stopService(rootIntent);

    this.stopSelf();
  }
}
