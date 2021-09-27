package hashed.app.ampassadors.NotificationUtil;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class FirestoreNotificationSender {


  public static final String
         /*works*/ TYPE_PRIVATE_MESSAGE = "privateMessaging",

  /*works*/ TYPE_GROUP_MESSAGE = "groupMessaging",
  /*works*/ TYPE_GROUP_ADDED = "groupAdded",

  /*works*/  TYPE_POST_LIKE = "postLike",
          TYPE_POLL_LIKE = "pollLike",
  /*works*/     TYPE_POST_COMMENT = "postComment",
  /*works*/     TYPE_POLL_COMMENT = "pollComment",

//          TYPE_POLL_COMMENT = "pollComment",
//          TYPE_POLL_COMMENT = "pollComment",

    TYPE_POST_COMMENT_LIKE = "postCommentLike",
    TYPE_POLL_COMMENT_LIKE = "pollCommentLike",

          TYPE_ZOOM = "zoom",

          TYPE_ZOOM_MEETING = "zoomMeeting",
          TYPE_ZOOM_COURSE = "zoomCourse",

          TYPE_ZOOM_MEETING_ENDED = "zoomMeetingEnded",
          TYPE_ZOOM_COURSE_ENDED = "zoomCourseEnded",

  /*works*/  TYPE_MEETING_ADDED = "meetingAdded",
          TYPE_MEETING_STARTED = "meetingStarted",
  /*works*/     TYPE_MEETING_MESSAGE = "meetingMessaging",

          TYPE_COURSE_STARTED = "courseStarted",
  /*works*/   TYPE_COURSE_MESSAGE = "courseMessaging",
          TYPE_COURSE_ADDED = "courseAdded";


  private static final CollectionReference notificationRef =
          FirebaseFirestore.getInstance().collection("Notifications");

  public static void sendFirestoreNotification(String userId, String type, String body,
                                               String senderName, String destinationId) {

    final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    final String notificationPath = userId + "_" + destinationId + "_" + type;

    notificationRef.document(notificationPath)
            .get().addOnSuccessListener(documentSnapshot -> {
      if (!documentSnapshot.exists()) {

        Log.d("ttt", "adding notification firestore");
        final HashMap<String, Object> notification = new HashMap<>();
        notification.put("senderId", currentUserId);
        notification.put("receiverId", userId);
        notification.put("type", type);
        notification.put("timeCreated", System.currentTimeMillis());
        notification.put("content", body);
        notification.put("senderName", senderName);
        notification.put("destinationId", destinationId);

        notificationRef.document(notificationPath).set(notification);
      } else {

        if (type.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)
        || type.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)
        || type.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)) {
          documentSnapshot.getReference().update("timeCreated",
                  System.currentTimeMillis(), "content", body);
        } else {

          Log.d("ttt", "deleting notification firestore");
//          documentSnapshot.getReference().delete();

        }

      }
    });
  }

  public static void deleteFirestoreNotification(String destinationId, String type) {

    final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    final String notificationPath = currentUserId + "_" + destinationId + "_" + type;
    notificationRef.document(notificationPath).delete();
  }


  public static void deleteNotificationsForId(String id){
    notificationRef.whereEqualTo("destinationId",id)
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if(snapshots!=null){
          for(DocumentSnapshot documentSnapshot:snapshots){
            documentSnapshot.getReference().delete();
          }
        }
      }
    });
  }
}
