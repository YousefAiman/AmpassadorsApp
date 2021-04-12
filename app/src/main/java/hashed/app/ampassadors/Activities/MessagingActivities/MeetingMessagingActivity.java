package hashed.app.ampassadors.Activities.MessagingActivities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;
import hashed.app.ampassadors.Fragments.ZoomMeetingCreationFragment;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class MeetingMessagingActivity extends MessagingActivity{

  private String groupImageUrl,groupName;
  private List<ListenerRegistration> listenerRegistrations;
  private ConstraintLayout zoomConstraint;
  private String currentMessagingSenders = MEMBERS;
  private String creatorId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    zoomConstraint = findViewById(R.id.zoomConstraint);

    handleNotification(FirestoreNotificationSender.TYPE_MEETING_MESSAGE);

  }

  @Override
  void getMessagingUid() {

    final Intent intent = getIntent();
    final String type = intent.getStringExtra("type");
    final String sourceId = intent.getStringExtra("messagingUid");

    if (type.equals(FirestoreNotificationSender.TYPE_ZOOM)) {

      messagingUid = sourceId.split("-")[0];
      final String joinUrl = sourceId.split("-")[1];

      if (joinUrl != null && !joinUrl.isEmpty()) {
        startZoomMeetingIntent(joinUrl);
      }

    } else {
      messagingUid = sourceId;
    }


    messagingDatabaseRef =
            FirebaseDatabase.getInstance().getReference().child("GroupMessages").getRef();

    databaseMessagesRef =  messagingDatabaseRef.child(messagingUid).child("Messages");

    firebaseMessageDocRef = FirebaseFirestore.getInstance()
            .collection("Meetings").document(messagingUid);

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

                    messagingTbNameTv.setText(groupName = value.getString("title"));

                    if(value.contains("messagingSenders")){
                      String newMessagingStatus = value.getString("messagingSenders");

                      if(creatorId == null || !creatorId.equals(currentUid)){
                        changeMessagingStatus(newMessagingStatus,currentMessagingSenders);
                      }

                      currentMessagingSenders = newMessagingStatus;
                    }

                    if(creatorId!=null && creatorId.equals(currentUid)){
                      toolbar.inflateMenu(R.menu.meeting_group_admin_menu);
                    }else{
//                      toolbar.inflateMenu(R.menu.group_user_menu);
                    }

                    isInitial[0] = false;
                  } else {

                    Log.d("ttt", "firebase messaigng doc event");

                    if(value.contains("messagingSenders")){
                      String newMessagingStatus = value.getString("messagingSenders");

                      if(creatorId == null || !creatorId.equals(currentUid)){
                        changeMessagingStatus(newMessagingStatus,currentMessagingSenders);
                      }

                      currentMessagingSenders = newMessagingStatus;
                    }

                    if (value.contains("hasEnded") && value.getBoolean("hasEnded")) {
                      Toast.makeText(MeetingMessagingActivity.this,
                              R.string.EndMeeting_Message,
                              Toast.LENGTH_SHORT).show();
                      finish();

                    }

                    if (value.contains("currentZoomMeeting")) {

                      final ZoomMeeting zoomMeeting =
                              value.get("currentZoomMeeting", ZoomMeeting.class);

                      if (zoomMeeting != null) {

                        final long endTime = zoomMeeting.getStartTime() +
                                (zoomMeeting.getDuration() * DateUtils.MINUTE_IN_MILLIS);

                        Log.d("ttt", "current time: " + System.currentTimeMillis());
                        Log.d("ttt", "endTime: " + endTime);

                        if (System.currentTimeMillis() >= endTime) {
                          value.getReference().update("currentZoomMeeting", null);
                        } else {
                          showZoomMeeting(zoomMeeting);
                        }
                      } else if (zoomConstraint.getVisibility() == View.VISIBLE) {

                        zoomConstraint.setVisibility(View.GONE);

                        Toast.makeText(MeetingMessagingActivity.this,
                                "Zoom Meeting has ended!",
                                Toast.LENGTH_SHORT).show();
                    }
                  }
                  }
                }
              }
            }));


  }

  @Override
  void createMessagingDocument(PrivateMessage privateMessage) {

    final Map<String, PrivateMessage> messages = new HashMap<>();
    messages.put(String.valueOf(System.currentTimeMillis()), privateMessage);

    currentMessagingRef.child("Messages").setValue(messages)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

                createMessagesListener();

                messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
                messageSendIv.setClickable(true);

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        Toast.makeText(MeetingMessagingActivity.this,
                R.string.message_send_failed, Toast.LENGTH_SHORT).show();

        messageSendIv.setClickable(true);
      }
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
          groupUsers.addAll((List<String>) snapshot.get("members"));
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
    final View parentView = getLayoutInflater().inflate(R.layout.group_message_options_bsd,
            null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.imageIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();

        Files.startImageFetchIntent(MeetingMessagingActivity.this);
      }
    });

    parentView.findViewById(R.id.audioIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

//        bsd.dismiss();
//
//        Files.startImageFetchIntent(PrivateMessagingActivity.this);
      }
    });

    parentView.findViewById(R.id.videoIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();
        Files.startVideoFetchIntent(MeetingMessagingActivity.this);
      }
    });

    parentView.findViewById(R.id.documentIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();
        Files.startDocumentFetchIntent(MeetingMessagingActivity.this);
      }
    });

    parentView.findViewById(R.id.zoomIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        pickerFrameLayout.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction().replace(pickerFrameLayout.getId(),
                new ZoomMeetingCreationFragment()).commit();


        bsd.dismiss();


      }
    });

    bsd.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialogInterface) {
        messageAttachIv.setClickable(true);
      }
    });

    bsd.setContentView(parentView);
    bsd.show();
  }

  @Override
  void sendMessage(PrivateMessage privateMessage) {

    messagingEd.setText("");
    messagingEd.setClickable(false);

    final DatabaseReference childRef = currentMessagingRef.child("Messages").
            child(String.valueOf(System.currentTimeMillis()));

    childRef.setValue(privateMessage)
            .addOnSuccessListener(v -> {

              sendBothNotifications(privateMessage);

              messageSendIv.setClickable(true);

            }).addOnFailureListener(e -> {

      Toast.makeText(MeetingMessagingActivity.this, R.string.message_send_failed,
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


    messagesProgressBar.setVisibility(View.VISIBLE);

    adapter = new PrivateMessagingAdapter(privateMessages,
            this, this, this,
            this, this, this,true);
    privateMessagingRv.setAdapter(adapter);

    currentMessagingRef = messagingDatabaseRef.child(messagingUid);

    listenerRegistrations.add(
            firebaseMessageDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
              @Override
              public void onEvent(@Nullable DocumentSnapshot value,
                                  @Nullable FirebaseFirestoreException error) {

                if (value != null) {
                  Log.d("ttt","firebase messaigng doc event");
                  if (value.contains("hasEnded") && value.getBoolean("hasEnded")) {
                    Toast.makeText(MeetingMessagingActivity.this,
                            R.string.EndMeeting_Message,
                            Toast.LENGTH_SHORT).show();
                    finish();

                  }else if(value.contains("currentZoomMeeting")){

                    final ZoomMeeting zoomMeeting =  value.get("currentZoomMeeting",ZoomMeeting.class);

                    if(zoomMeeting!=null){

                      final long endTime = zoomMeeting.getStartTime() +
                              (zoomMeeting.getDuration() * DateUtils.MINUTE_IN_MILLIS);

                      Log.d("ttt","current time: "+System.currentTimeMillis());
                      Log.d("ttt","endTime: "+endTime);

                      if(System.currentTimeMillis() >= endTime){
                        value.getReference().update("currentZoomMeeting",null);
                      }else{
                        showZoomMeeting(zoomMeeting);
                      }
                    }else if(zoomConstraint.getVisibility() == View.VISIBLE){

                      zoomConstraint.setVisibility(View.GONE);

                      Toast.makeText(MeetingMessagingActivity.this, "Zoom Meeting has ended!",
                              Toast.LENGTH_SHORT).show();
                    }
                  }
                }
              }
            }));
    Log.d("ttt", "looking to group");
    currentMessagingRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {

        if (snapshot.exists()) {
          Log.d("ttt", "found group");
          if (snapshot.hasChild("Messages")) {

            createMessagesListener();

          } else {
            Log.d("ttt", "didn't find group");
            messagesProgressBar.setVisibility(View.GONE);
            messageSendIv.setOnClickListener(new FirstMessageClickListener());
          }

          messageAttachIv.setClickable(true);
          micIv.setClickable(true);

        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }

  private class FirstMessageClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

      Log.d("ttt", "clicked send button");
      final String content = messagingEd.getText().toString();
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

  private void startZoomMeetingIntent(String url) {

    final Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
    try {

      if (urlIntent.resolveActivity(getPackageManager()) != null) {
        startActivity(urlIntent);
      }

    } catch (NullPointerException ignored) {

    }
  }


  private void showZoomMeeting(ZoomMeeting zoomMeeting){

    TextView zoomMeetingTopicTv = findViewById(R.id.zoomMeetingTopicTv);
    TextView zoomMeetingStartTimeTv = findViewById(R.id.zoomMeetingStartTimeTv);
    TextView zoomMeetingDurationTv = findViewById(R.id.zoomMeetingDurationTv);
    Button zoomMeetingJoinBtn = findViewById(R.id.zoomMeetingJoinBtn);

    zoomConstraint.setVisibility(View.VISIBLE);

    zoomMeetingTopicTv.setText(zoomMeeting.getTopic());
    zoomMeetingStartTimeTv.setText(TimeFormatter.formatTime(zoomMeeting.getStartTime()));

    int hours = zoomMeeting.getDuration() / 60;
    int minutes = zoomMeeting.getDuration() % 60;

//    String time = String.format(Locale.getDefault(),"%02d", hours, minutes);

    zoomMeetingDurationTv.setText(hours+":"+minutes);

    zoomMeetingJoinBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (zoomMeeting.getStartUrl() != null && !zoomMeeting.getStartUrl().isEmpty()) {
          startZoomMeetingIntent(zoomMeeting.getStartUrl());
        }

      }
    });

  }


  @Override
  public boolean onMenuItemClick(MenuItem item) {

      if (item.getItemId() == R.id.action_end_meeting) {

        if (firebaseMessageDocRef != null) {
          firebaseMessageDocRef.update("hasEnded", true).addOnSuccessListener(v ->
                  finish());
        }

      }else if(item.getItemId() == R.id.action_send_messages){

        showMessageSendingOptionsDialog(currentMessagingSenders);

      }else if(item.getItemId() == R.id.action_edit_group){

      }


    return false;
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

