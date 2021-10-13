package hashed.app.ampassadors.NotificationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;

import hashed.app.ampassadors.Activities.CourseActivity;
import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationClickReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    if (intent.hasExtra("sourceId") && intent.hasExtra("sourceType")) {

      Log.d("ttt","has both extras");

      final String sourceType = intent.getStringExtra("sourceType");
      final String sourceId = intent.getStringExtra("sourceId");

      if (GlobalVariables.isAppIsRunning()) {

        Log.d("ttt","AppIsRunning");
        if(sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)
        || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)
        || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)
        || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
        || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)){

          Log.d("ttt","messaging type");

          Intent destinationIntent = null;
          switch (sourceType) {
            case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
              destinationIntent = new Intent(context, PrivateMessagingActivity.class);
              break;
            case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
            case FirestoreNotificationSender.TYPE_GROUP_ADDED:
              destinationIntent = new Intent(context, GroupMessagingActivity.class);
              break;
            case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
              destinationIntent = new Intent(context, CourseMessagingActivity.class);
              break;
            case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
              destinationIntent = new Intent(context, MeetingMessagingActivity.class);
              break;
          }
          Log.d("ttt","sourceId: "+sourceId);
          destinationIntent.putExtra("messagingUid", sourceId);
          checkCurrentMessagingActivity(context,destinationIntent,sourceId);

          context.startActivity(destinationIntent);
          }else{

          switch (sourceType){

            case FirestoreNotificationSender.TYPE_MEETING_STARTED:
            case FirestoreNotificationSender.TYPE_MEETING_ADDED:

              Log.d("ttt","meeting source: "+sourceId);
              fetchObjectAndStartIntent(MeetingActivity.class,context,"Meetings",
                      Meeting.class,sourceId,"meeting");

              break;
            case FirestoreNotificationSender.TYPE_POST_COMMENT:
            case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:
            case FirestoreNotificationSender.TYPE_POST_LIKE:

              Intent postIntent = new Intent(context, PostNewsActivity.class)
                      .putExtra("postId",sourceId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

              final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

              FirebaseFirestore.getInstance().collection("Users")
                      .document(currentUid)
                      .collection("UserPosts")
                      .document(sourceId)
                      .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                  if(documentSnapshot.exists()){
                    postIntent.putExtra("isForUser",true);
                    postIntent.putExtra("publisherId",currentUid);
                  }

                  context.startActivity(postIntent);
                }
              });

              break;
            case FirestoreNotificationSender.TYPE_COURSE_STARTED:

              fetchObjectAndStartIntent(CourseActivity.class,context,"Courses",
                      Course.class,sourceId,"course");
              break;
          }
        }

      }else{

        Log.d("ttt","AppIs not Running");

      }
    }
  }

  private void checkCurrentMessagingActivity(Context context,Intent destinationIntent,
                                             String sourceId){
    final SharedPreferences sharedPreferences =
            context.getSharedPreferences(context.getResources().getString(R.string.shared_name),
                    Context.MODE_PRIVATE);
    Log.d("ttt","checkCurrentMessagingActivity");
    if (sharedPreferences.contains("currentlyMessagingUid")) {
      if (sourceId.equals(sharedPreferences.getString("currentlyMessagingUid", ""))) {

        Log.d("ttt", "this messaging activity is already open man");
        destinationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);

      } else {
        Log.d("ttt", "current messaging is not this");
        destinationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
      }
    } else {
      Log.d("ttt", "no current messaging in shared");
      destinationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

  }


  private void fetchObjectAndStartIntent(Class<?> destinationClass,
                                         Context context,
                                         String collectionName,
                                         Class<?> objectClass,
                                         String id,
                                         String extraName){

    final Intent intent = new Intent(context, destinationClass)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    FirebaseFirestore.getInstance().collection(collectionName).document(id)
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {

        if(documentSnapshot.exists()){
          intent.putExtra(extraName,(Serializable) documentSnapshot.toObject(objectClass));
        }

      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful() && intent.hasExtra(extraName)){
          context.startActivity(intent);
        }
      }
    });


  }
}
