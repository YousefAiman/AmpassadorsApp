package hashed.app.ampassadors.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import hashed.app.ampassadors.R;

public class MessagingUtil {

  public static void leaveGroup(Context context, String userId, String groupId,
                                DocumentReference firebaseMessageDocRef,
                                DatabaseReference databaseMessagingRef){

    String alertMessage,loadingMessage,positiveMessage;

    if(userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
      alertMessage = "Are you sure you want to leave this group?";
      loadingMessage = "Leaving group!";
      positiveMessage = "Leave";
    }else{
      alertMessage = "Are you sure you want to remove user this group?";
      loadingMessage = "Removing from group!";
      positiveMessage = "Remove";
    }

    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(alertMessage);
    alert.setPositiveButton(positiveMessage, (dialog, which) -> {

      final ProgressDialog progressDialog = new ProgressDialog(context);
      progressDialog.setMessage(loadingMessage);
      progressDialog.setCancelable(false);
      progressDialog.show();

      firebaseMessageDocRef.update("users", FieldValue.arrayRemove(userId))
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void v) {
                  databaseMessagingRef.child(userId).removeValue(
                          (error, ref) -> {
                            if(error!=null){
                              Toast.makeText(context,
                                      "Failed to leave group! Please try again",
                                      Toast.LENGTH_SHORT).show();
                            }else{

                              FirebaseFirestore.getInstance()
                                      .collection("Notifications")
                                      .whereEqualTo("receiverId",userId)
                                      .whereEqualTo("destinationId",groupId)
                                      .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot snapshots) {
                                  if(!snapshots.isEmpty()){
                                    for(DocumentSnapshot snapshot:snapshots){
                                      snapshot.getReference().delete();
                                    }
                                  }
                                }
                              }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                  lastKeyRef = null;
                                  progressDialog.dismiss();

                                  if(userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                    ((Activity)context).finish();
                                  }

                                }
                              });
                            }
                          });
                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          progressDialog.dismiss();
          Toast.makeText(context, "Failed to leave group! Please try again",
                  Toast.LENGTH_SHORT).show();
        }
      });
    });
    alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
    alert.create().show();

  }

}
