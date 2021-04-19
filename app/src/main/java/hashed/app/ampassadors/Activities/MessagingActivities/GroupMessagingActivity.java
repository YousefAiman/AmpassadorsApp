package hashed.app.ampassadors.Activities.MessagingActivities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import hashed.app.ampassadors.Activities.GroupEditingActivity;
import hashed.app.ampassadors.Activities.GroupInfoActivity;
import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.MessagingUtil;

public class GroupMessagingActivity extends MessagingActivity{

  private String groupImageUrl,groupName;
  private String currentMessagingSenders = MEMBERS;
  private List<String> groupAdmins;
  private List<ListenerRegistration> listenerRegistrations;
  private boolean userISAnAdmin,userIsGroupCreator;
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    handleNotification(FirestoreNotificationSender.TYPE_GROUP_MESSAGE);

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


    final boolean[] isInitial = {true};
    listenerRegistrations = new ArrayList<>();


    listenerRegistrations.add(
    firebaseMessageDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

        if (value != null) {

          if (isInitial[0]) {

            if (value.contains("imageUrl")) {
              groupImageUrl = value.getString("imageUrl");
              if(groupImageUrl!=null && !groupImageUrl.isEmpty()){
                Picasso.get().load(groupImageUrl).fit().into(messagingTbProfileIv);
              }
            }
            messagingTbNameTv.setText(groupName = value.getString("groupName"));

            final String creatorId = value.getString("creatorId");

            if(userIsGroupCreator = creatorId.equals(currentUid)){
              toolbar.inflateMenu(R.menu.group_creator_menu);

              if(value.contains("messagingSenders")) {
                currentMessagingSenders = value.getString("messagingSenders");
              }

            }else{

              groupAdmins = (List<String>) value.get("groupAdmins");

              userISAnAdmin = groupAdmins.contains(currentUid);

              if(value.contains("messagingSenders")){

                String newMessagingStatus = value.getString("messagingSenders");

                if(groupAdmins == null || !groupAdmins.contains(currentUid)){
                  changeMessagingStatus(newMessagingStatus,currentMessagingSenders);
                }
                currentMessagingSenders = newMessagingStatus;

              }

              if(groupAdmins != null && groupAdmins.contains(currentUid)){
                toolbar.inflateMenu(R.menu.group_admin_menu);
              }else{
                toolbar.inflateMenu(R.menu.group_user_menu);
              }
            }


            isInitial[0] = false;

          } else {

            Log.d("ttt", "firebase messaigng doc event");


            if(!((List<String>)value.get("users")).contains(currentUid)){

              Toast.makeText(GroupMessagingActivity.this,
                      "You have been removed from this group by an admin!",
                      Toast.LENGTH_SHORT).show();

              finish();
              return;
            }

            if(!userIsGroupCreator){


            if(value.contains("messagingSenders")){
              String newMessagingStatus = value.getString("messagingSenders");

              if(!newMessagingStatus.equals(currentMessagingSenders)){

                groupAdmins = (List<String>) value.get("groupAdmins");

                if(groupAdmins == null || !groupAdmins.contains(currentUid)){
                  changeMessagingStatus(newMessagingStatus,currentMessagingSenders);
                }
                currentMessagingSenders = newMessagingStatus;

              }
            }

            if(value.contains("groupAdmins")){
              groupAdmins = (List<String>) value.get("groupAdmins");
              if(!userISAnAdmin && groupAdmins.contains(currentUid)){
                //user is now an admin
//                toolbar.getMenu().setvi
                userISAnAdmin = true;
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.group_creator_menu);
              }else if(userISAnAdmin && !groupAdmins.contains(currentUid)){
                //user is removed from admins
                userISAnAdmin = false;
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.group_user_menu);
              }
            }
            }else{

              if(value.contains("messagingSenders")) {
                currentMessagingSenders = value.getString("messagingSenders");
              }

            }
          }
        }
      }
    }));
  }

  @Override
  void createMessagingDocument(PrivateMessage privateMessage) {
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

    if (data == null) {

      data = new Data(
              currentUid,
              currentUserName+": "+body,
              groupName,
              groupImageUrl!=null?groupImageUrl:currentImageUrl,
              "Group Messages",
              FirestoreNotificationSender.TYPE_GROUP_MESSAGE,
              messagingUid
      );

    } else {
      data.setBody(currentUserName+": "+body);
    }

    final List<String> groupUsers = new ArrayList<>();
    firebaseMessageDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {
        if(snapshot.exists()){
          groupUsers.addAll((List<String>) snapshot.get("users"));
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful() && !groupUsers.isEmpty()){
          sendNotificationsToMembers(currentUserName+": "+body,groupUsers);
        }
      }
    });
  }

  private void sendNotificationsToMembers(String body,List<String> groupUsers) {

    for (String userId : groupUsers) {

      usersRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
        if (documentSnapshot.exists()) {
          if (documentSnapshot.contains("ActivelyMessaging")) {
            final String messaging = documentSnapshot.getString("ActivelyMessaging");
            if (messaging == null || !messaging.equals(messagingUid)) {
              Log.d("ttt", "sendBothNotifs");

              Log.d("firestoreNotifications","sending to: "+userId);
              FirestoreNotificationSender.sendFirestoreNotification(userId,
                      FirestoreNotificationSender.TYPE_GROUP_MESSAGE,
                      body, groupName, messagingUid);

              CloudMessagingNotificationsSender.sendNotification(userId, data);
            }
          } else {
            Log.d("ttt", "sendBothNotifs");

            Log.d("firestoreNotifications","sending to: "+userId);

            FirestoreNotificationSender.sendFirestoreNotification(userId,
                    FirestoreNotificationSender.TYPE_GROUP_MESSAGE,
                    body, groupName, messagingUid);

            CloudMessagingNotificationsSender.sendNotification(userId, data);

          }
        }
      });
    }

  }

  @Override
  void showMessageOptionsBottomSheet() {
    showUserOptionsBottomSheet();
  }

  @Override
  void sendMessage(PrivateMessage privateMessage) {

    messagingEd.setText("");
    messagingEd.setClickable(false);

    final DatabaseReference childRef = currentMessagingRef.child("messages").
            child(String.valueOf(System.currentTimeMillis()));

    childRef.setValue(privateMessage)
            .addOnSuccessListener(v -> {

              sendBothNotifications(privateMessage);

              messageSendIv.setClickable(true);

            }).addOnFailureListener(e -> {

      Toast.makeText(GroupMessagingActivity.this, R.string.message_send_failed,
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
            this, this, true);

    privateMessagingRv.setAdapter(adapter);

    messagingDatabaseRef.child(messagingUid).addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          messagesProgressBar.setVisibility(View.VISIBLE);
          fetchMessagesFromSnapshot(snapshot);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

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


  @Override
  public boolean onMenuItemClick(MenuItem item) {

    if(item.getItemId() == R.id.action_send_messages){

      if(userISAnAdmin || userIsGroupCreator){
        showMessageSendingOptionsDialog(currentMessagingSenders);
      }else{
        Toast.makeText(this, "You are not an admin of this group anymore!",
                Toast.LENGTH_SHORT).show();
      }

    }else if(item.getItemId() == R.id.action_leave_group){

      MessagingUtil.leaveGroup(this,currentUid,messagingUid,firebaseMessageDocRef,
              currentMessagingRef);

    }else if(item.getItemId() == R.id.action_edit_group){


      if(userISAnAdmin || userIsGroupCreator){
        startActivity(new Intent(GroupMessagingActivity.this, GroupEditingActivity.class)
                .putExtra("firebaseMessageDocRefId", firebaseMessageDocRef.getId())
                .putExtra("groupType","messagingGroup")
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }else{
        Toast.makeText(this, "You are not an admin of this group anymore!",
                Toast.LENGTH_SHORT).show();
      }



    }else if(item.getItemId() == R.id.action_group_info){

      startActivity(new Intent(GroupMessagingActivity.this, GroupInfoActivity.class)
              .putExtra("firebaseMessageDocRefId", firebaseMessageDocRef.getId())
              .putExtra("groupType","messagingGroup")
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }else if(item.getItemId() == R.id.action_delete_group){
    showDeleteGroupAlert();
    }

    return false;
  }

  private void showDeleteGroupAlert(){

    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Are you sure you want to delete this group?");
//    alert.setMessage("This will remove the group permai");
    alert.setPositiveButton("Delete", (dialogInterface, i) -> {

      if(listenerRegistrations!=null && !listenerRegistrations.isEmpty()){
        for(ListenerRegistration listenerRegistration:listenerRegistrations){
          listenerRegistration.remove();
        }
      }

      ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setMessage("Deleting group...");
      progressDialog.setCancelable(false);
      progressDialog.show();


      if (firebaseMessageDocRef != null) {


        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        firebaseMessageDocRef.delete();

        FirestoreNotificationSender.deleteNotificationsForId(messagingUid);

        if(groupImageUrl!=null && !groupImageUrl.isEmpty()){
          firebaseStorage.getReferenceFromUrl(groupImageUrl).delete();
        }

        if(databaseMessagesRef!=null){

          databaseMessagesRef.get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
              for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                if (snapshot1.hasChild("attachmentUrl")) {
                  firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(snapshot1
                          .child("attachmentUrl").getValue(String.class)))
                          .delete();
                }
                if (snapshot1.hasChild("videoThumbnail")) {
                  firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(snapshot1
                          .child("videoThumbnail").getValue(String.class)))
                          .delete();
                }
              }
            }

          }).addOnCompleteListener(task -> {
            currentMessagingRef.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                finish();
              }
            });
          });
        }else{
          currentMessagingRef.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              progressDialog.dismiss();
              finish();
            }
          });
        }

      }

    });

    alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
    alert.create().show();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if(listenerRegistrations!=null && !listenerRegistrations.isEmpty()){
      for(ListenerRegistration listenerRegistration:listenerRegistrations){
        listenerRegistration.remove();
      }
    }

  }
}
