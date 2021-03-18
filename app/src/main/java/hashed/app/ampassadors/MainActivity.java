package hashed.app.ampassadors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import hashed.app.ampassadors.Activities.ConnectionActivity;
import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.WelcomeActivity;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.WifiUtil;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (WifiUtil.isConnectedToInternet(this)) {

      final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
      SharedPreferences sharedPreferences =
              getSharedPreferences(getResources().getString(R.string.app_name),
                      Context.MODE_PRIVATE);


      if(!sharedPreferences.contains("firstTime")){

        startActivity(new Intent(this, WelcomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        sharedPreferences.edit().putBoolean("firstTime",false).apply();

      }else{

        if (user != null) {

          if (!user.isAnonymous()) {

            FirebaseMessagingService.startMessagingService(this);

            if (getIntent().hasExtra("destinationBundle")) {

              final Bundle destinationBundle = getIntent().getBundleExtra("destinationBundle");

              final String sourceType = destinationBundle.getString("sourceType");
              final String sourceId = destinationBundle.getString("sourceId");

              Intent intent = null;

              switch (sourceType) {
                case "privateMessaging":
                  intent = startPrivateMessagingActivity(sourceId);
                  break;
                case "groupMessaging":
                  intent = startGroupMessagingActivity(sourceId);
                  break;
                case "meetingStarted":
                  intent = startMeetingsHomeActivity();
                  break;
                default:
                  startHomeActivity();
                  break;
              }


              Intent finalIntent = intent;
              new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                  startActivity(finalIntent);
                }
              }, 800);

            } else {

              new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                  startHomeActivity();
                }
              }, 1000);
            }


          } else {
            new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                startHomeActivity();
              }
            }, 1000);
          }


        } else {

          FirebaseAuth.getInstance().signInAnonymously()
                  .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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

      }



    } else {

      startConnectionActivity();

    }

  }

  private void startConnectionActivity() {
    new Handler().postDelayed(() -> {
      startActivityForResult(new Intent(MainActivity.this, ConnectionActivity.class),
              ConnectionActivity.CONNECTION_RESULT);
    }, 800);
  }

  private Intent startPrivateMessagingActivity(String userId) {
    return new Intent(MainActivity.this,
            PrivateMessagingActivity.class).putExtra("messagingUid", userId);
  }

  private Intent startGroupMessagingActivity(String groupId) {
    return new Intent(MainActivity.this,
            GroupMessagingActivity.class).putExtra("messagingUid", groupId);
  }


  private Intent startMeetingsHomeActivity() {
    return new Intent(MainActivity.this,
            Home_Activity.class).putExtra("showMeetings", true);
  }


  private void startHomeActivity() {
    startActivity(new Intent(MainActivity.this, Home_Activity.class));
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == ConnectionActivity.CONNECTION_RESULT) {

      if (getIntent().hasExtra("messagingBundle")) {
//        startMessagingActivity();
      } else {
        startHomeActivity();
      }

    }

  }
}