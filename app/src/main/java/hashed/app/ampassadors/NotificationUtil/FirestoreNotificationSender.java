package hashed.app.ampassadors.NotificationUtil;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class FirestoreNotificationSender {

  private static final CollectionReference notificationRef =
          FirebaseFirestore.getInstance().collection("Notifications");

  private static final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

  public static void sendFirestoreNotification(String userId, String type, String body,
                                               String senderName, String destinationId) {

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

        if (type.equals("message")) {
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
    final String notificationPath = currentUserId + "_" + destinationId + "_" + type;
    notificationRef.document(notificationPath).delete();
  }

}
