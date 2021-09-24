package hashed.app.ampassadors;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new AppLifeCycleTracker());
    }


     class AppLifeCycleTracker implements ActivityLifecycleCallbacks {

        private int numStarted = 0;
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if(numStarted == 0){
                updateUserStatus(true);
            }
            numStarted++;
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            numStarted--;
            if(numStarted == 0){
                updateUserStatus(false);
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }

    private void updateUserStatus(boolean isOnline){

        if(isOnline && hasOfflinePreference()){
            return;
        }

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            FirebaseFirestore.getInstance().collection("Users")
                    .document(currentUser.getUid()).update("status", isOnline);
        }
    }
    private boolean hasOfflinePreference(){

        final SharedPreferences sharedPreferences =
                getSharedPreferences(getResources().getString(R.string.app_name), Context.MODE_PRIVATE);

        return (sharedPreferences.contains("alwaysOffline") && sharedPreferences.getBoolean("alwaysOffline",false));
    }

}
