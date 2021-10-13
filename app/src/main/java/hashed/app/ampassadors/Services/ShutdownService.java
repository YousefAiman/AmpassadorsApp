package hashed.app.ampassadors.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShutdownService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ttt", "shut down service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {

//    Toast.makeText(this, "app shutdown", Toast.LENGTH_SHORT).show();
        super.onTaskRemoved(rootIntent);

        Log.d("ttt", "task removed");

//        GlobalVariables.setAppIsRunning(false);
//        getSharedPreferences(getResources().getString(R.string.shared_name), MODE_PRIVATE).edit()
//                .remove("isPaused")
//                .remove("currentlyMessagingUid").apply();

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            Log.d("ttt","current user exitst");

                    FirebaseFirestore.getInstance().collection("Users")
                            .document(currentUser.getUid()).update("status", false)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d("ttt","updating status to false is successful");
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("ttt","updating status to false is complete");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d("ttt","updating status to false has failed because: "+e.getMessage());
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            Log.d("ttt","updating status was cancelled");
                        }
                    });

        }else{
            Log.d("ttt","curretn user is null or anon");
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
//                GlobalVariables.getRegisteredNetworkCallback() != null) {
//            ((ConnectivityManager) getApplicationContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE))
//                    .unregisterNetworkCallback(GlobalVariables.getRegisteredNetworkCallback());
//
//            GlobalVariables.setRegisteredNetworkCallback(null);
//
//        } else if (GlobalVariables.getCurrentWifiReceiver() != null) {
//            unregisterReceiver(GlobalVariables.getCurrentWifiReceiver());
//            GlobalVariables.setCurrentWifiReceiver(null);
//        }

        Log.d("ttt","before app destroy tasks done");
//  stopService(rootIntent);
        stopSelf();

        Log.d("ttt","after super calls");
    }
}
