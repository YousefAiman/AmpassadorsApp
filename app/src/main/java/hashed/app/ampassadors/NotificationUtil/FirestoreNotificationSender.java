package hashed.app.ampassadors.NotificationUtil;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FirestoreNotificationSender {


  public static final String TYPE_PRIVATE_MESSAGE = "privateMessaging",
          TYPE_GROUP_MESSAGE = "groupMessaging",
          TYPE_LIKE = "postLike",TYPE_COMMENT = "postComment",TYPE_ZOOM = "zoomMeeting",
          TYPE_MEETING_ADDED = "meetingAdded",TYPE_MEETING_STARTED = "meetingStarted",
          TYPE_GROUP_ADDED = "groupAdded";



  private static final CollectionReference notificationRef =
          FirebaseFirestore.getInstance().collection("Notifications");

  public static void sendFirestoreNotification(String userId, String type, String body,
                                               String senderName, String destinationId) {

    final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    final String notificationPath =
            currentUserId + "_" + destinationId + "_" + type;

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

}
