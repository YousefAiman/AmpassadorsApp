package hashed.app.ampassadors.Utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseDeleteUtil {


  public static void deleteAllMeetingsAndMessages(){

    DatabaseReference groupsRef =
            FirebaseDatabase.getInstance().getReference().child("GroupMessages");

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    CollectionReference meetingsRef =
            FirebaseFirestore.getInstance().collection("Meetings");

    CollectionReference notificationsRef =
            FirebaseFirestore.getInstance().collection("Notifications");

    meetingsRef.limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        if(!snapshots.isEmpty()){

          for(DocumentSnapshot snapshot:snapshots){

            groupsRef.child(snapshot.getId()).child("Messages")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snap) {

                        for(DataSnapshot snapshot1:snap.getChildren()){
                          final int type = snapshot1.child("type").getValue(Integer.class);

                          if(type != Files.ZOOM && type != Files.TEXT){

                            if(snapshot1.hasChild("attachmentUrl")){

                              final String attachmentUrl = snapshot1.child("attachmentUrl")
                                      .getValue(String.class);

                              if(attachmentUrl!=null && !attachmentUrl.isEmpty()){
                                firebaseStorage.getReferenceFromUrl(attachmentUrl)
                                        .delete();
                              }
                            }

                            if(type == Files.VIDEO){

                              final String videoThumbnail = snapshot1.child("videoThumbnail")
                                      .getValue(String.class);

                              if(videoThumbnail!=null && !videoThumbnail.isEmpty()){
                                firebaseStorage.getReferenceFromUrl(videoThumbnail)
                                        .delete();
                              }

                            }


                          }
                        }

                        groupsRef.child(snapshot.getId()).removeValue();

                        notificationsRef.whereEqualTo("destinationId",snapshot.getId())
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                          @Override
                          public void onSuccess(QuerySnapshot snapshots) {
                           if(!snapshots.isEmpty()){
                             for(DocumentSnapshot snapshot1:snapshots){
                               snapshot1.getReference().delete();
                             }
                           }
                          }
                        });

                        snapshot.getReference().delete();
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                    });


          }

        }

      }
    });


  }
}
