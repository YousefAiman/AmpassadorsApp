package hashed.app.ampassadors.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Utils.GlobalVariables;

public class WifiReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {

    final ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);


    final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userRef = null;
    if(currentUser!=null){
       userRef =  FirebaseFirestore.getInstance().collection("Users")
              .document(currentUser.getUid());
    }

    if (netInfo != null && netInfo.isConnected()) {
      Log.d("ttt", "wifi online");
      GlobalVariables.setWifiIsOn(true);
      if(GlobalVariables.isAppIsRunning() && userRef!=null){
        userRef.update("status", true);
      }
    } else {
      Log.d("ttt", "wifi offline");
      GlobalVariables.setWifiIsOn(false);
      if(GlobalVariables.isAppIsRunning() && userRef!=null){
        userRef.update("status", false);
      }
      Toast.makeText(context, "You are currently offline!", Toast.LENGTH_SHORT).show();
    }
  }

}
