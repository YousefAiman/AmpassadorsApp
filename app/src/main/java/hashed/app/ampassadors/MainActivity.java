package hashed.app.ampassadors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import hashed.app.ampassadors.Activities.ConnectionActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.profile;
import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.Activities.sign_in;
import hashed.app.ampassadors.Activities.sign_up;
import hashed.app.ampassadors.Utils.WifiUtil;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if(WifiUtil.isConnectedToInternet(this)){

      final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

      if(user!=null){
        if(getIntent().hasExtra("messagingUid")){

          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              startMessagingActivity();
            }
          },1000);

        }else{

          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              startHomeActivity();
            }
          },1000);


        }
      }else{

        FirebaseAuth.getInstance().signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
          @Override
          public void onSuccess(AuthResult authResult) {

            startHomeActivity();

          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            finish();
          }
        });
      }


    }else{

      startConnectionActivity();

    }

}

  private void startConnectionActivity() {
    new Handler().postDelayed(() -> {
      startActivityForResult(new Intent(MainActivity.this, ConnectionActivity.class),
              ConnectionActivity.CONNECTION_RESULT);
    }, 800);
  }

  private void startMessagingActivity(){
    startActivity( new Intent(MainActivity.this,
            PrivateMessagingActivity.class)
            .putExtra("messagingUid",getIntent().getStringExtra("messagingUid"))
            .putExtra("isFromNotification",false));

    finish();
  }

  private void startHomeActivity(){
    startActivity(new Intent(MainActivity.this, Home_Activity.class));
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(resultCode == ConnectionActivity.CONNECTION_RESULT){

      if(getIntent().hasExtra("messagingBundle")){
        startMessagingActivity();
      }else{
        startHomeActivity();
      }

    }

  }
}