package hashed.app.ampassadors.Activities.MessagingActivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PrivateMessagingActivity extends MessagingActivity{

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    handleNotification(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE);
  }

  @Override
  void getMessagingUid() {

    final Intent intent = getIntent();
    messagingUid = intent.getStringExtra("messagingUid");

    messagingDatabaseRef =
            FirebaseDatabase.getInstance().getReference().child("PrivateMessages").getRef();

    firebaseMessageDocRef = FirebaseFirestore.getInstance()
            .collection("PrivateMessages").document(messagingUid);

  }


  @Override
  void getUserData() {

    usersRef.document(messagingUid).get().addOnSuccessListener(documentSnapshot -> {
      if (documentSnapshot.exists()) {

        if (documentSnapshot.contains("imageUrl")) {
          String userImageUrl = documentSnapshot.getString("imageUrl");
          if(userImageUrl!=null && !userImageUrl.isEmpty()){
            Picasso.get().load(userImageUrl).fit().into(messagingTbProfileIv);
          }
        }
        messagingTbNameTv.setText(documentSnapshot.getString("username"));

      }
    });

  }



  @Override
  void createMessagingDocument(PrivateMessage privateMessage) {


    boolean currentUidIsFirst = currentUid.toUpperCase()
            .compareTo(messagingUid.toUpperCase()) < 0;
    String id;
    if(currentUidIsFirst){
      id = currentUid+"-"+messagingUid;
    }else{
      id = messagingUid+"-"+currentUid;
    }

    removeSenderFirstMessageListener(id);

//    currentMessagingRef.child("messages").child(
//            String.valueOf(System.currentTimeMillis())).setValue(privateMessage);

    final Map<String, Object> messagingDocumentMap = new HashMap<>();
    messagingDocumentMap.put("DeletedFor:" + currentUid, false);
    messagingDocumentMap.put("DeletedFor:" + messagingUid, false);

    final Map<String, PrivateMessage> messages = new HashMap<>();
    messages.put(String.valueOf(System.currentTimeMillis()), privateMessage);
//    currentMessagingRef.child("messages")
//            .setValue(String.valueOf(System.currentTimeMillis()), privateMessage);
//
    messagingDocumentMap.put("messages", messages);

    currentMessagingRef = messagingDatabaseRef.child(id);

//    currentMessagingRef.child("messages")
//            .child(String.valueOf(System.currentTimeMillis()))
//            .setValue(privateMessage);

    currentMessagingRef.setValue(messagingDocumentMap).addOnSuccessListener(v -> {

//      DatabaseReference senderFirstMessageRef
//              = messagingDatabaseRef.child(messagingUid + "-" + currentUid);
//
//      senderFirstMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
//        @Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//          if(snapshot!=null && snapshot.exists()){
//
//            Log.d("ttt","other user created a messaging documetn amn");
//
//          }else{
//
//            Log.d("ttt","no other messaging document created");
//          }
//
//        }
//        @Override
//        public void onCancelled(@NonNull DatabaseError error) {
//          Log.d("ttt","cancceled document: "+error.getMessage());
//        }
//      });
//      if()
//
      HashMap<String, String> lastSeenMap = new HashMap<>();
      lastSeenMap.put(currentUid,"0");
      lastSeenMap.put(messagingUid,"0");

      currentMessagingRef.child("UsersLastSeenMessages").setValue(lastSeenMap);

      final Map<String, Object> messageDocumentPreviewMap = new HashMap<>();

      final List<String> users = new ArrayList<>();
      users.add(currentUid);
      users.add(messagingUid);

      messageDocumentPreviewMap.put("users", users);
      messageDocumentPreviewMap.put("databaseRefId", currentMessagingRef.getKey());
      messageDocumentPreviewMap.put("latestMessageTime", privateMessage.getTime());

      firebaseMessageDocRef =
              FirebaseFirestore.getInstance().collection("PrivateMessages")
                      .document(currentMessagingRef.getKey()!=null?
      currentMessagingRef.getKey():messagingUid);

      databaseMessagesRef = currentMessagingRef.child("messages");

        firebaseMessageDocRef.set(messageDocumentPreviewMap);

      sendNotification(privateMessage);

      createMessagesListener();

      messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
      messageSendIv.setClickable(true);

    }).addOnFailureListener(e -> {

      Toast.makeText(this,
              R.string.message_send_failed, Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    });

  }


  private void sendBothNotifications(PrivateMessage privateMessage) {

    String body;
    switch (privateMessage.getType()) {

      case Files.IMAGE:
        body = getString(R.string.sent_an_image);
        break;

      case Files.DOCUMENT:
        body = getString(R.string.sent_an_attachment);
        break;

      case Files.AUDIO:
        body = getString(R.string.sent_audio_message);
        break;


      case Files.VIDEO:
        body = getString(R.string.sent_a_video);
        break;

      default:
        body = privateMessage.getContent();
    }


    FirestoreNotificationSender.sendFirestoreNotification(
            messagingUid,
            FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE,
            body,
            getResources().getString(R.string.new_message)+" "+currentUserName,
            currentUid);

    if (data == null) {

      data = new Data(
              currentUid,
              body,
              getResources().getString(R.string.new_message) + " " + currentUserName,
              currentImageUrl,
              "Private Messages",
              FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE,
              currentUid
      );

    } else {
      data.setBody(body);
    }
    CloudMessagingNotificationsSender.sendNotification(messagingUid, data);

  }


  void sendNotification(PrivateMessage privateMessage) {

    usersRef.document(messagingUid)
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.contains("ActivelyMessaging")) {
          final String messaging = documentSnapshot.getString("ActivelyMessaging");
          if (messaging == null || !messaging.equals(currentUid)) {
            Log.d("ttt", "sendBothNotifs");
            sendBothNotifications(privateMessage);
          }
        } else {
          Log.d("ttt", "sendBothNotifs");
          sendBothNotifications(privateMessage);
        }

      }
    });

  }

  @Override
  void showMessageOptionsBottomSheet() {

    messageAttachIv.setClickable(false);

    final BottomSheetDialog bsd = new BottomSheetDialog(this, R.style.SheetDialog);
    final View parentView = getLayoutInflater().inflate(R.layout.message_options_bsd, null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.imageIv).setOnClickListener(view -> {

      if (checkIsUploading()) {
        return;
      }

      bsd.dismiss();

      Files.startImageFetchIntent(PrivateMessagingActivity.this);
    });

    parentView.findViewById(R.id.audioIv).setOnClickListener(view -> {
      if (checkIsUploading()) {
        return;
      }
    });

    parentView.findViewById(R.id.videoIv).setOnClickListener(view -> {
      if (checkIsUploading()) {
        return;
      }
      bsd.dismiss();
      Files.startVideoFetchIntent(PrivateMessagingActivity.this);
    });

    parentView.findViewById(R.id.documentIv).setOnClickListener(view -> {
      if (checkIsUploading()) {
        return;
      }
      bsd.dismiss();
      Files.startDocumentFetchIntent(PrivateMessagingActivity.this);
    });

    bsd.setOnDismissListener(dialog -> messageAttachIv.setClickable(true));

    bsd.setContentView(parentView);
    bsd.show();

  }

  @Override
  void sendMessage(PrivateMessage privateMessage) {

    messagingEd.setText("");
    messagingEd.setClickable(false);

    if (lastKeyRef == null) {
      createMessagingDocument(privateMessage);
      return;
    }

    final DatabaseReference childRef = currentMessagingRef.child("messages").
            child(String.valueOf(System.currentTimeMillis()));

    childRef.setValue(privateMessage)
            .addOnSuccessListener(v -> {

              sendNotification(privateMessage);

              messageSendIv.setClickable(true);

            }).addOnFailureListener(e -> {

      Toast.makeText(PrivateMessagingActivity.this, R.string.message_send_failed,
              Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    }).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseMessageDocRef.update("latestMessageTime", privateMessage.getTime());
      }
    });

  }

  @Override
  void fetchPreviousMessages() {

    adapter = new PrivateMessagingAdapter(privateMessages, this,
            this, this, this,
            this, this, false);

    privateMessagingRv.setAdapter(adapter);

//    List<String> idsList = new ArrayList<>();
//    idsList.add(currentUid);
//    idsList.add(messagingUid);
//    Log.d("ttt","looking for it in ");
//    FirebaseFirestore.getInstance().collection("PrivateMessages")
//            .whereEqualTo("type","privateMessages")
//            .whereEqualTo("users",idsList).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//      @Override
//      public void onSuccess(QuerySnapshot snapshots) {
//
//        if(snapshots!=null && !snapshots.isEmpty()){
//          if(snapshots.getDocuments().get(0).exists()){
//            Log.d("ttt","found this messages document in firestore at: "
//            +snapshots.getDocuments().get(0).getId());
//          }
//        }else{
//          Log.d("ttt","no messages found");
//        }
//
//      }
//    }).addOnFailureListener(new OnFailureListener() {
//      @Override
//      public void onFailure(@NonNull Exception e) {
//        Log.d("ttt","no messages found: "+e.getMessage());
//      }
//    });

    messagingDatabaseRef.child(currentUid + "-" + messagingUid)
              .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                  if (snapshot.exists()) {
                    messagesProgressBar.setVisibility(View.VISIBLE);
                    fetchMessagesFromSnapshot(snapshot);

                  } else {

                    messagingDatabaseRef.child(messagingUid + "-" + currentUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()) {
                                  Log.d("ttt","on data changed for single event value man");
                                  messagesProgressBar.setVisibility(View.VISIBLE);
                                  fetchMessagesFromSnapshot(snapshot);

                                } else {

                                  currentMessagingRef = messagingDatabaseRef.child(currentUid + "-" +
                                          messagingUid);
                                  firebaseMessageDocRef = FirebaseFirestore.getInstance()
                                          .collection("PrivateMessages")
                                          .document(currentMessagingRef.getKey());


                                  ValueEventListener valueEventListener;

                                  boolean currentUidIsFirst = currentUid.toUpperCase()
                                          .compareTo(messagingUid.toUpperCase()) < 0;
                                  String id;
                                  if(currentUidIsFirst){
                                    id = currentUid+"-"+messagingUid;
                                  }else{
                                    id = messagingUid+"-"+currentUid;
                                  }

                                  DatabaseReference senderFirstMessageRef
                                           = messagingDatabaseRef.child(id);

                                  senderFirstMessageRef.addValueEventListener(valueEventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                              if(snapshot!=null && snapshot.exists()){

                                                Log.d("ttt","first message sender data changed");
                                                removeSenderFirstMessageListener(id);

                                                currentMessagingRef = messagingDatabaseRef
                                                        .child(id);

                                                firebaseMessageDocRef = FirebaseFirestore.getInstance()
                                                        .collection("PrivateMessages")
                                                        .document(currentMessagingRef.getKey());

                                                messagesProgressBar.setVisibility(View.VISIBLE);
                                                fetchMessagesFromSnapshot(snapshot);

                                              }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                          });

                                  if(valueEventListeners == null){
                                    valueEventListeners = new HashMap<>();
                                  }

                                  valueEventListeners.put(
                                          messagingDatabaseRef.child(id), valueEventListener);


//                                  FirebaseFirestore.getInstance()
//                                          .collection("PrivateMessages")
//                                          .document(messagingUid + "-" + currentUid)
//                                          .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                                              if(value!=null && value.exists()){
//
//                                                Log.d("ttt","added to firestore: "+value.getId());
//
//
////                                                currentMessagingRef.addListenerForSingleValueEvent(new ValueEventListener() {
////                                                  @Override
////                                                  public void onDataChange(@NonNull DataSnapshot snapshot) {
////                                                    if(snapshot.exists()){
////                                                      fetchMessagesFromSnapshot(snapshot);
////                                                    }
////                                                  }
////
////                                                  @Override
////                                                  public void onCancelled(@NonNull DatabaseError error) {
////
////                                                  }
////                                                });
////
//                                              }
//                                            }
//                                          });

                                  messageSendIv.setOnClickListener(new FirstMessageClickListener());
                                  messageAttachIv.setClickable(true);
                                  micIv.setClickable(true);


                                }

                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "receiverUid - senderUid onCancelled:"
                                        + error.getMessage());
                              }
                            });

                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.d(TAG, "senderUid - receiverUid onCancelled:"
                          + error.getMessage());
                }
              });


  }

  private void fetchMessagesFromSnapshot(DataSnapshot dataSnapshot) {

    currentMessagingRef = dataSnapshot.getRef();
    databaseMessagesRef = currentMessagingRef.child("messages");

    firebaseMessageDocRef = FirebaseFirestore.getInstance()
            .collection("PrivateMessages")
            .document(Objects.requireNonNull(currentMessagingRef.getKey()));

    createMessagesListener();

    messageAttachIv.setClickable(true);
    micIv.setClickable(true);

  }

  private class FirstMessageClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString().trim();
      if (!content.isEmpty()) {

        messagingEd.setText("");
        messagingEd.setClickable(false);

        final PrivateMessage privateMessage = new PrivateMessage(
                content,
                System.currentTimeMillis(),
                currentUid,
                Files.TEXT);

        createMessagingDocument(privateMessage);

      } else {
        Toast.makeText(view.getContext(),
                R.string.message_send_empty, Toast.LENGTH_SHORT).show();
      }
    }
  }


  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }


  private void removeSenderFirstMessageListener(String id){

    Log.d("ttt", "removeSenderFirstMessageListener: removed");

    DatabaseReference senderFirstMessageRef
            = messagingDatabaseRef.child(id);
    if(valueEventListeners.get(senderFirstMessageRef.getRef()) != null ){

      senderFirstMessageRef.removeEventListener(
              valueEventListeners.get(senderFirstMessageRef.getRef()));

    }
    valueEventListeners.remove(senderFirstMessageRef.getRef());
  }
}
