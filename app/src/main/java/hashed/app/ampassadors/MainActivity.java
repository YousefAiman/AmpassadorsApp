package hashed.app.ampassadors;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import hashed.app.ampassadors.Activities.ConnectionActivity;
import hashed.app.ampassadors.Activities.CourseActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
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

//    FirebaseMessaging.getInstance().subscribeToTopic("82900345876");

    if (WifiUtil.isConnectedToInternet(this)) {
      checkUserCredentials();
    } else {
      startConnectionActivity();
    }


//    FirebaseFirestore.getInstance().collectionGroup("Comments")
//            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//      @Override
//      public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//        if(queryDocumentSnapshots!=null){
//          for(DocumentSnapshot snap:queryDocumentSnapshots){
//
//            final String userId = snap.getString("userId");
//
//            if(userId!=null){
//
//              FirebaseFirestore.getInstance().collection("Users")
//                      .document(userId)
//                      .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                  if(documentSnapshot == null || !documentSnapshot.exists() || (documentSnapshot.contains("rejected") && documentSnapshot.getBoolean("rejected"))){
//                    snap.getReference().update("isDeleted",true);
//
//                    final DocumentReference parentSnap = snap.getReference().getParent().getParent();
//
//                    if(parentSnap!=null){
//                      parentSnap.update("comments",FieldValue.increment(-1));
//                    }
//
//                  }
//
//                }
//              });
//            }
//
//          }
//        }
//      }
//    });

//    FirebaseDatabase.getInstance().getReference()
//            .child("GroupMessages").child("69803fed-51a6-474c-8a95-fd7d88f09488").child("Messages")
//            .orderByChild("zoomMeeting")
//            .addListenerForSingleValueEvent(new ValueEventListener() {
//              @Override
//              public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                Iterator<DataSnapshot> iterator=  snapshot.getChildren().iterator();
//
//                while(iterator.hasNext()){
//                  iterator.
//                }
//
//              }
//
//              @Override
//              public void onCancelled(@NonNull DatabaseError error) {
//
//              }
//            });




//    startService(new Intent(getBaseContext(), ShutdownService.class));

  }

  private void checkUserCredentials(){

    SharedPreferences sharedPreferences =
            getSharedPreferences(getResources().getString(R.string.shared_name),
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

                      FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                          documentSnapshot.getReference().update("token",s);
                          GlobalVariables.setCurrentToken(s);
                        }
                      });

                    }
                  }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful()){

                final Intent intent = getIntent();
                if (intent!=null && intent.hasExtra("sourceId") && intent.hasExtra("sourceType")) {

                  startActivity(directToIntent(intent.getStringExtra("sourceId"),intent.getStringExtra("sourceType")));
                  finish();
//                  final Bundle destinationBundle = getIntent().getBundleExtra("destinationBundle");
//
//                  final String sourceType = destinationBundle.getString("sourceType");
//                  final String sourceId = destinationBundle.getString("sourceId");
//
//                  Intent intent = null;
//
//                  switch (sourceType) {
//                    case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
//                      intent = startPrivateMessagingActivity("messagingUid",sourceId);
//                      break;
//                    case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
//                      intent = startMeetingMessagingActivity(sourceId);
//                      break;
//                    case FirestoreNotificationSender.TYPE_MEETING_STARTED:
//                      intent = startMeetingsHomeActivity();
//                      break;
//
//                      case FirestoreNotificationSender.TYPE_GROUP_ADDED:
//                        intent = startPrivateMessagingActivity("groupId",sourceId);
//                      break;
//
//                    default:
//                      startHomeActivity();
//                      return;
//                  }
//
//                  Intent finalIntent = intent;
//                  startActivity(finalIntent);
//                  finish();

//                  Intent finalIntent = intent;
//                  new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                  }, 800);

                } else {

                  startHomeActivity();
                  finish();

//                  new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                  }, 500);
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


  private Intent directToIntent(String sourceId,String sourceType){

    Intent destinationIntent = null;

    Log.d("ttt","AppIsRunning");
    if(sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)

            || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)

            || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_STARTED)
            || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)

            || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_STARTED)
            || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)
//                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)
//                || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)
    ) {

      Log.d("ttt","messaging type");

      switch (sourceType) {
        case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
          destinationIntent = new Intent(this, PrivateMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
        case FirestoreNotificationSender.TYPE_GROUP_ADDED:
          destinationIntent = new Intent(this, GroupMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
        case FirestoreNotificationSender.TYPE_COURSE_STARTED:
        case FirestoreNotificationSender.TYPE_ZOOM_COURSE:
          destinationIntent = new Intent(this, CourseMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
        case FirestoreNotificationSender.TYPE_MEETING_STARTED:
        case FirestoreNotificationSender.TYPE_ZOOM_MEETING:
          destinationIntent = new Intent(this, MeetingMessagingActivity.class);
          break;
      }

      Log.d("ttt","sourceId: "+sourceId);
      destinationIntent.putExtra("messagingUid", sourceId);

      if(sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING) ||
              sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)){
        destinationIntent.putExtra("type",sourceType);
      }

      destinationIntent.setFlags(getIntentFlags(sourceId));
//          this.startActivity(destinationIntent);

    }else{

      switch (sourceType){
        case FirestoreNotificationSender.TYPE_MEETING_ADDED:
          destinationIntent = new Intent(this, MeetingActivity.class);
          destinationIntent.putExtra("meetingID",sourceId);

          Log.d("ttt","meeting source: "+sourceId);
//              fetchObjectAndStartIntent(MeetingActivity.class,context,"Meetings",
//                      Meeting.class,sourceId,"meeting");

          break;

        case FirestoreNotificationSender.TYPE_COURSE_ADDED:

          destinationIntent = new Intent(this, CourseActivity.class);
          destinationIntent.putExtra("courseID",sourceId);

          break;

        case FirestoreNotificationSender.TYPE_POST_LIKE:
        case FirestoreNotificationSender.TYPE_POST_COMMENT:

          destinationIntent = new Intent(this, PostNewsActivity.class)
                  .putExtra("notificationPostId",sourceId);

          break;

        case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:
        case FirestoreNotificationSender.TYPE_POST_REPLY:

          String[] arr = sourceId.split("\\|");

          if(arr.length>0){
            destinationIntent = new Intent(this,PostNewsActivity.class)
                    .putExtra("notificationPostId",arr[0])
                    .putExtra("notificationCreatorId",arr[1]);
          }else{
            destinationIntent = new Intent(this,PostNewsActivity.class)
                    .putExtra("notificationPostId",sourceId);
          }


          break;

        case FirestoreNotificationSender.TYPE_POLL_LIKE:
        case FirestoreNotificationSender.TYPE_POLL_COMMENT:
        case FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE:
        case FirestoreNotificationSender.TYPE_POLL_REPLY:

          destinationIntent =  new Intent(this, PostPollActivity.class)
                  .putExtra("postId",sourceId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      }
    }

    if(destinationIntent!=null){
      destinationIntent.putExtra("notificationType",sourceType);
    }

    return destinationIntent;
  }

  private int getIntentFlags(String sourceId){

    Log.d("ttt","checkCurrentMessagingActivity");
    final SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.shared_name),
            Context.MODE_PRIVATE);

    if (GlobalVariables.isAppIsRunning() && sharedPreferences.contains("currentlyMessagingUid")) {
      if (sourceId.equals(sharedPreferences.getString("currentlyMessagingUid", ""))) {
        Log.d("ttt", "this messaging activity is already open man");
        return Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      } else {
        Log.d("ttt", "current messaging is not this");
        return Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      }
    } else {
      Log.d("ttt", "no current messaging in shared");
      return Intent.FLAG_ACTIVITY_NEW_TASK;
    }

  }



}