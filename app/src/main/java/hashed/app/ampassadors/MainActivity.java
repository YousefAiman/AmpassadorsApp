package hashed.app.ampassadors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Activities.ConnectionActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.VideoWelcomeActivity;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.WifiUtil;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (WifiUtil.isConnectedToInternet(this)) {
      checkUserCredentials();

    } else {
      startConnectionActivity();
    }
  }

  private void checkUserCredentials(){

    SharedPreferences sharedPreferences =
            getSharedPreferences(getResources().getString(R.string.app_name),
                    Context.MODE_PRIVATE);


    if(!sharedPreferences.contains("firstTime")){

      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity(new Intent(MainActivity.this, VideoWelcomeActivity.class)
                  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
          finish();
        }
      },1000);


      sharedPreferences.edit().putBoolean("firstTime",false).apply();

    }else{

      final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


      if (user != null) {

        if (!user.isAnonymous()) {

          FirebaseMessagingService.startMessagingService(this);

          FirebaseFirestore.getInstance().collection("Users")
                  .document(user.getUid()).get().
                  addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                      GlobalVariables.setRole(documentSnapshot.getString("Role"));
                      if(documentSnapshot.contains("token")){
                        GlobalVariables.setCurrentToken(documentSnapshot.getString("token"));
                      }
                    }
                  }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful()){
                if (getIntent().hasExtra("destinationBundle")) {

                  final Bundle destinationBundle = getIntent().getBundleExtra("destinationBundle");

                  final String sourceType = destinationBundle.getString("sourceType");
                  final String sourceId = destinationBundle.getString("sourceId");

                  Intent intent = null;

                  switch (sourceType) {
                    case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
                      intent = startPrivateMessagingActivity("messagingUid",sourceId);
                      break;
                    case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
                      intent = startMeetingMessagingActivity(sourceId);
                      break;
                    case FirestoreNotificationSender.TYPE_MEETING_STARTED:
                      intent = startMeetingsHomeActivity();
                      break;

                      case FirestoreNotificationSender.TYPE_GROUP_ADDED:
                        intent = startPrivateMessagingActivity("groupId",sourceId);
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
                      finish();
                    }
                  }, 800);

                } else {
                  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      startHomeActivity();
                      finish();
                    }
                  }, 500);
                }
              }
            }
          });
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


  }

  private void startConnectionActivity() {
    new Handler().postDelayed(() -> {
      startActivityForResult(new Intent(MainActivity.this, ConnectionActivity.class),
              ConnectionActivity.CONNECTION_RESULT);
//      finish();
    }, 800);
  }

  private Intent startPrivateMessagingActivity(String key,String messagingId) {
    return new Intent(MainActivity.this,
            PrivateMessagingActivity.class)
            .putExtra(key, messagingId);
  }

  private Intent startMeetingMessagingActivity(String groupId) {
    return new Intent(MainActivity.this,
            MeetingMessagingActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra("messagingUid", groupId);

  }


  private Intent startMeetingsHomeActivity() {
    return new Intent(MainActivity.this,
            Home_Activity.class).putExtra("showMeetings", true);
  }


  private void startHomeActivity() {
    startActivity(new Intent(MainActivity.this, Home_Activity.class)
    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    finish();
  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == ConnectionActivity.CONNECTION_RESULT) {
      checkUserCredentials();
    }
  }
}