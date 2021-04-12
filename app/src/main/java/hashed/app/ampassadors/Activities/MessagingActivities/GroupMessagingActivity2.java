package hashed.app.ampassadors.Activities.MessagingActivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
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
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.MessagingUtil;

public class GroupMessagingActivity2 extends MessagingActivity{

  private String groupImageUrl,groupName;
  private String currentMessagingSenders = MEMBERS;
  private List<String> groupAdmins;
  private List<ListenerRegistration> listenerRegistrations;
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

            groupAdmins = (List<String>) value.get("groupAdmins");

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

            isInitial[0] = false;

          } else {

            Log.d("ttt", "firebase messaigng doc event");


            if(!((List<String>)value.get("users")).contains(currentUid)){

              Toast.makeText(GroupMessagingActivity2.this,
                      "You have been removed from this group by an admin!",
                      Toast.LENGTH_SHORT).show();

              finish();
              return;
            }

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
              currentUid
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

              FirestoreNotificationSender.sendFirestoreNotification(userId,
                      FirestoreNotificationSender.TYPE_GROUP_MESSAGE,
                      body, groupName, messagingUid);

              CloudMessagingNotificationsSender.sendNotification(userId, data);
            }
          } else {
            Log.d("ttt", "sendBothNotifs");

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

    messageAttachIv.setClickable(false);

    final BottomSheetDialog bsd = new BottomSheetDialog(this, R.style.SheetDialog);
    final View parentView = getLayoutInflater().inflate(R.layout.message_options_bsd, null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.imageIv).setOnClickListener(view -> {

      if (checkIsUploading()) {
        return;
      }

      bsd.dismiss();

      Files.startImageFetchIntent(GroupMessagingActivity2.this);
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
      Files.startVideoFetchIntent(GroupMessagingActivity2.this);
    });

    parentView.findViewById(R.id.documentIv).setOnClickListener(view -> {
      if (checkIsUploading()) {
        return;
      }
      bsd.dismiss();
      Files.startDocumentFetchIntent(GroupMessagingActivity2.this);
    });

    bsd.setOnDismissListener(dialog -> messageAttachIv.setClickable(true));

    bsd.setContentView(parentView);
    bsd.show();

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

      Toast.makeText(GroupMessagingActivity2.this, R.string.message_send_failed,
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

    if (item.getItemId() == R.id.action_end_meeting) {

      if (firebaseMessageDocRef != null) {
        firebaseMessageDocRef.update("hasEnded", true).addOnSuccessListener(v -> finish());
      }
    }else if(item.getItemId() == R.id.action_send_messages){

      showMessageSendingOptionsDialog(currentMessagingSenders);

    }else if(item.getItemId() == R.id.action_leave_group){

      MessagingUtil.leaveGroup(this,currentUid,messagingUid,firebaseMessageDocRef,
              currentMessagingRef);

    }else if(item.getItemId() == R.id.action_edit_group){


      startActivity(new Intent(GroupMessagingActivity2.this, GroupEditingActivity.class)
              .putExtra("firebaseMessageDocRefId", firebaseMessageDocRef.getId())
              .putExtra("groupType","messagingGroup")
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }else if(item.getItemId() == R.id.action_group_info){

      startActivity(new Intent(GroupMessagingActivity2.this, GroupInfoActivity.class)
              .putExtra("firebaseMessageDocRefId", firebaseMessageDocRef.getId())
              .putExtra("groupType","messagingGroup")
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    return false;
  }

}