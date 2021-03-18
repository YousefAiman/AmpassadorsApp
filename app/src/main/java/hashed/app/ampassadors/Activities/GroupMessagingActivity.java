package hashed.app.ampassadors.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;
import hashed.app.ampassadors.Fragments.FilePickerPreviewFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoPickerPreviewFragment;
import hashed.app.ampassadors.Fragments.ZoomMeetingCreationFragment;
import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class GroupMessagingActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener, PrivateMessagingAdapter.DeleteMessageListener,
        PrivateMessagingAdapter.VideoMessageListener, PrivateMessagingAdapter.DocumentMessageListener,
        View.OnClickListener, RecyclerView.OnLayoutChangeListener,
        PrivateMessagingAdapter.ImageMessageListener {

  public static final int RECORD_AUDIO_REQUEST = 30;
  //constants
  private static final String TAG = "GroupMessages";
  private static final int MESSAGES_PAGE_SIZE = 15;
  //database
  private static final DatabaseReference databaseReference
          = FirebaseDatabase.getInstance().getReference().child("GroupMessages").getRef();

  private static final CollectionReference meetingsRef
          = FirebaseFirestore.getInstance().collection("Meetings"),
          usersRef = FirebaseFirestore.getInstance().collection("Users");
  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  //messages
  private final ArrayList<PrivateMessage> privateMessages = new ArrayList<>();
  private final DateFormat secondMinuteFormat =
          new SimpleDateFormat("mm:ss", Locale.getDefault());
  private DatabaseReference currentMessagingRef;
  private String firstKeyRef;
  private String lastKeyRef;
  private DocumentReference firebaseMessageDocRef;
  //event listeners
  private Map<DatabaseReference, ChildEventListener> childEventListeners;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;
  private String groupId;
  private PrivateMessagingAdapter adapter;
  private toTopScrollListener currentScrollListener;
  private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTasks;
  private boolean isLoadingMessages;
  //views
  private RecyclerView privateMessagingRv;
  private ImageView messageSendIv;
  private ImageView messageAttachIv;
  private ImageView micIv;
  private ImageView cancelIv;
  private EditText messagingEd;
  private ProgressBar messagesProgressBar;
  private FrameLayout pickerFrameLayout;
  private ImageView messagingTbProfileIv;
  private TextView messagingTbNameTv;
  //attachments
  private int messageAttachmentUploadedIndex = -1;
  private BroadcastReceiver downloadCompleteReceiver;
  //audio messages
  private MediaRecorder mediaRecorder;
  private Handler progressHandle;
  private Runnable progressRunnable;


  //notifications
  private SharedPreferences sharedPreferences;
  private Data data;
  private String currentGroupName;
  private String currentGroupImage;
  private String currentUserName;
  private List<String> groupMembers;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_private_messaging);


    //getting group id
    getGroupId();

    //setting up toolbar and its actions
    setUpToolBarAndActions();

    //initializing Views
    initializeViews();


    //handling notification is it exists
    handleNotification();

    //getting current user data
    getMyData();

    //groupData
    getGroupData();


    //fetching group messages and listening for new
    fetchGroupPreviousMessages();


  }


  private void setUpToolBarAndActions() {

    final Toolbar toolbar = findViewById(R.id.privateMessagingTb);
    toolbar.inflateMenu(R.menu.group_messaging_toolbar_menu);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    toolbar.setOnMenuItemClickListener(this);

  }


  //Activity actions and views
  private void initializeViews() {

    privateMessagingRv = findViewById(R.id.privateMessagingRv);
    messageSendIv = findViewById(R.id.messageSendIv);
    messagingEd = findViewById(R.id.messagingEd);
    messageAttachIv = findViewById(R.id.messageAttachIv);
    messagesProgressBar = findViewById(R.id.messagesProgressBar);
    micIv = findViewById(R.id.micIv);
    pickerFrameLayout = findViewById(R.id.pickerFrameLayout);
    cancelIv = findViewById(R.id.cancelIv);
    messagingTbProfileIv = findViewById(R.id.messagingTbProfileIv);
    messagingTbNameTv = findViewById(R.id.messagingTbNameTv);

    privateMessagingRv.addOnLayoutChangeListener(this);
    messageAttachIv.setOnClickListener(this);
    micIv.setOnClickListener(this);
  }

  private void getGroupId() {

    final Intent intent = getIntent();

    if (intent.hasExtra("destinationBundle")) {

      final String sourceId = intent.getStringExtra("sourceId");
      final String sourceType = intent.getStringExtra("sourceType");

      if (sourceType.equals("zoomMeeting")) {

        groupId = sourceId.split("-")[0];
        final String joinUrl = sourceId.split("-")[1];

        if (joinUrl != null && !joinUrl.isEmpty()) {
          startZoomMeetingIntent(joinUrl);
        }

      } else {
        groupId = sourceId;
      }
    } else {
      groupId = intent.getStringExtra("messagingUid");
    }


    if (intent.hasExtra("isFromNotification") && Build.VERSION.SDK_INT < 26) {
      BadgeUtil.decrementBadgeNum(this);
    }

    usersRef.document(currentUid).update("ActivelyMessaging", groupId);

  }


  //firestore user data
  private void getGroupData() {

    meetingsRef.document(groupId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot ds) {
        if (ds.exists()) {

          if (ds.contains("imageUrl")) {
            currentGroupImage = ds.getString("imageUrl");
          }
          currentGroupName = ds.getString("title");
          groupMembers = (List<String>) ds.get("members");
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
          Picasso.get().load(currentGroupImage).fit().into(messagingTbProfileIv);
          messagingTbNameTv.setText(currentGroupName);
        }
      }
    });

  }

  //my data
  private void getMyData() {

    usersRef.document(currentUid).get().addOnSuccessListener(documentSnapshot -> {
      if (documentSnapshot.exists()) {
//        currentImageUrl = documentSnapshot.getString("imageUrl");
        currentUserName = documentSnapshot.getString("username");
      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
//          Picasso.get().load(currentImageUrl).fit().into(messagingTbProfileIv);
          messagingTbNameTv.setText(currentUserName);
        }
      }
    });
  }

  //Group messages
  private void fetchGroupPreviousMessages() {

    adapter = new PrivateMessagingAdapter(privateMessages,
            this, this, this,
            this, this, true);
    privateMessagingRv.setAdapter(adapter);

    currentMessagingRef = databaseReference.child(groupId);

    firebaseMessageDocRef = FirebaseFirestore.getInstance().collection("Meetings")
            .document(groupId);

    firebaseMessageDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

        if (value != null) {
          if (value.getBoolean("hasEnded")) {
            Toast.makeText(GroupMessagingActivity.this,
                    R.string.EndMeeting_Message,
                    Toast.LENGTH_SHORT).show();
            finish();
          }
        }
      }
    });
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
            messageSendIv.setOnClickListener(new FirstMessageClickListener());
          }
        }

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });
  }

  private void createMessagesListener() {

    currentMessagingRef.child("Messages").orderByKey().limitToLast(MESSAGES_PAGE_SIZE)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot child : snapshot.getChildren()) {
                  privateMessages.add(child.getValue(PrivateMessage.class));
                }

                firstKeyRef = Iterables.get(snapshot.getChildren(), 0).getKey();
                lastKeyRef = Iterables.getLast(snapshot.getChildren()).getKey();

                adapter.notifyDataSetChanged();

                scrollToBottom();

                messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
                addListenerForNewMessages();

                if (Integer.parseInt(lastKeyRef) + 1 > MESSAGES_PAGE_SIZE) {
                  privateMessagingRv.addOnScrollListener(
                          currentScrollListener = new toTopScrollListener());
                }
                addDeleteFieldListener();
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupMessagingActivity.this,
                        R.string.message_load_failed, Toast.LENGTH_SHORT).show();
                finish();
              }
            });

  }

  private void createGroupDocument(PrivateMessage privateMessage) {

    final Map<String, PrivateMessage> messages = new HashMap<>();
    messages.put("0", privateMessage);

    currentMessagingRef.child("Messages").setValue(messages)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

                firstKeyRef = "0";
                lastKeyRef = "0";

                createMessagesListener();

                messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
                messageSendIv.setClickable(true);

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        Toast.makeText(GroupMessagingActivity.this,
                R.string.message_send_failed, Toast.LENGTH_SHORT).show();

        messageSendIv.setClickable(true);
      }
    });
  }


  //database messages listeners
  private void addListenerForNewMessages() {

    ChildEventListener childEventListener;

    final Query query = currentMessagingRef.child("Messages").orderByKey()
            .startAt(String.valueOf(Integer.parseInt(lastKeyRef) + 1));

    query.addChildEventListener(childEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot,
                               @Nullable String previousChildName) {


        lastKeyRef = snapshot.getKey();
        final PrivateMessage message = snapshot.getValue(PrivateMessage.class);

//        if(message.getType() == Files.IMAGE && message.getAttachmentUrl() == null) {
//          addFileMessageUploadListener(snapshot.child("attachmentUrl").getRef()
//                  ,privateMessages.size());
//        }
        if (messageAttachmentUploadedIndex != -1) {

          privateMessages.set(messageAttachmentUploadedIndex, message);
          adapter.notifyItemChanged(messageAttachmentUploadedIndex);
          messageAttachmentUploadedIndex = -1;
        } else {

          privateMessages.add(message);
          adapter.notifyItemInserted(privateMessages.size());
          scrollToBottom();
        }

//        if(message != null && message.getSender().equals(currentUid)){
//
//        }

      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot,
                                 @Nullable String previousChildName) {

      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {

      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot,
                               @Nullable String previousChildName) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });


    childEventListeners = new HashMap<>();
    childEventListeners.put(query.getRef(), childEventListener);
  }

  private void addDeleteFieldListener() {

    valueEventListeners = new HashMap<>();

    ValueEventListener valueEventListener;
    currentMessagingRef
            .child("lastDeleted")
            .addValueEventListener(valueEventListener = new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                  final String id = snapshot.getValue(String.class);

                  if (id != null) {

                    final int deletedIndex =
                            Math.abs(Integer.parseInt(firstKeyRef) - Integer.parseInt(id));

                    if (deletedIndex < privateMessages.size() &&
                            !privateMessages.get(deletedIndex).getDeleted()) {
                      privateMessages.get(deletedIndex).setDeleted(true);
                      adapter.notifyItemChanged(deletedIndex);
                    }

                  }

//                  final PrivateMessage message = snapshot.getValue(PrivateMessage.class);
//
//                  for(int i=0;i<privateMessages.size();i++){
//                    final PrivateMessage privateMessage = privateMessages.get(i);
//                    if(privateMessage.getContent().equals(message.getContent())
//                            && privateMessage.getTime() == message.getTime()){
////                      if(!privateMessages.get(i).getDeleted()){
//                        privateMessages.get(i).setDeleted(true);
//                        adapter.notifyItemChanged(i);
////                      }
//                      break;
//                    }
//                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });

    valueEventListeners.put(currentMessagingRef.child("lastDeleted").getRef(), valueEventListener);

  }

  private void sendMessage(PrivateMessage privateMessage) {

    messagingEd.setText("");
    messagingEd.setClickable(false);

    if (lastKeyRef == null) {
      createGroupDocument(privateMessage);
      return;
    }

    final DatabaseReference childRef = currentMessagingRef.child("Messages")
            .child(String.valueOf(Integer.parseInt(lastKeyRef) + 1));

    childRef.setValue(privateMessage).addOnSuccessListener(v -> {

      if (privateMessage.getType() == Files.ZOOM) {
        sendZoomMeetingNotification(privateMessage.getZoomMeeting().getTopic(),
                privateMessage.getZoomMeeting().getJoinUrl());
      } else {
        checkUserActivityAndSendNotifications(privateMessage.getContent(), privateMessage.getType());
      }

      messageSendIv.setClickable(true);

    }).addOnFailureListener(e -> {

      Toast.makeText(this, R.string.message_send_failed, Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    });

  }

  private void showMessageOptionsBottomSheet() {

    messageAttachIv.setClickable(false);

    final BottomSheetDialog bsd = new BottomSheetDialog(this, R.style.SheetDialog);
    final View parentView = getLayoutInflater().inflate(R.layout.group_message_options_bsd,
            null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.imageIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();

        Files.startImageFetchIntent(GroupMessagingActivity.this);
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
        Files.startVideoFetchIntent(GroupMessagingActivity.this);
      }
    });

    parentView.findViewById(R.id.documentIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();
        Files.startDocumentFetchIntent(GroupMessagingActivity.this);
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

  private void startAudioRecording() {

    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

      micIv.setClickable(false);

      messagingEd.setEnabled(false);
      messagingEd.setFocusable(false);

      new Handler().post(new Runnable() {
        @Override
        public void run() {
          messageAttachIv.setVisibility(View.GONE);
          messageSendIv.setVisibility(View.GONE);
          cancelIv.setVisibility(View.VISIBLE);

          DrawableCompat.setTint(
                  DrawableCompat.wrap(micIv.getDrawable()),
                  getResources().getColor(R.color.red)
          );

          messagingEd.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
          messagingEd.setText("00:00");

        }
      });

      startAudioRecorder();

    } else {
      ActivityCompat.requestPermissions(this,
              new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST);
    }


  }

  private void startAudioRecorder() {

    mediaRecorder = new MediaRecorder();
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

    final String fileName = getExternalCacheDir().getAbsolutePath() + "/NewRecording.3gp";

    mediaRecorder.setOutputFile(fileName);

    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

    try {
//      progressHandle.postDelayed(runnable, 1000);

      mediaRecorder.prepare();
    } catch (IOException e) {
      stopAudioRecorder(null, 0, true);
      e.printStackTrace();
    }

    mediaRecorder.start();

    final long startTime = System.currentTimeMillis();

    progressHandle = new Handler();

    progressRunnable = new Runnable() {
      @Override
      public void run() {
        try {

          long millis = System.currentTimeMillis() - startTime;
          messagingEd.setText(secondMinuteFormat.format(millis));
          //messagingEd.setText(secondMinuteFormat.format((long) mediaRecorder.getCurrentPosition()));
          progressHandle.postDelayed(this, 500);
        } catch (IllegalStateException ed) {
          ed.printStackTrace();
        }
      }
    };

    progressHandle.postDelayed(progressRunnable, 0);

    micIv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        progressHandle.removeCallbacks(progressRunnable);
        stopAudioRecorder(fileName, startTime, false);
        progressHandle = null;
        progressRunnable = null;
      }
    });

    cancelIv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        progressHandle.removeCallbacks(progressRunnable);
        stopAudioRecorder(null, 0, true);
        progressHandle = null;
        progressRunnable = null;
      }
    });


  }

  private void stopAudioRecorder(String fileName, long startTime, boolean cancel) {

    mediaRecorder.stop();
    mediaRecorder.release();
    mediaRecorder = null;

    if (!cancel) {

      final long length = System.currentTimeMillis() - startTime;

      sendFileMessage(Uri.fromFile(new File(fileName)), Files.AUDIO, null, length, null);

//      deleteFile(fileName);

    }

    cancelIv.setOnClickListener(null);
    micIv.setOnClickListener(this);

    messagingEd.setEnabled(true);
//    messagingEd.setInputType(InputType.TYPE_CLASS_TEXT);
    messagingEd.setFocusable(true);
    messagingEd.setFocusableInTouchMode(true);

    new Handler().post(new Runnable() {
      @Override
      public void run() {

        messageAttachIv.setVisibility(View.VISIBLE);
        messageSendIv.setVisibility(View.VISIBLE);
        cancelIv.setVisibility(View.GONE);

        DrawableCompat.setTint(
                DrawableCompat.wrap(micIv.getDrawable()),
                getResources().getColor(R.color.black)
        );

        messagingEd.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        messagingEd.setText("");

      }
    });

  }

  public void sendFileMessage(Uri uri, int fileType, String message, long audioLength,
                              String fileName) {

    if (fileType != Files.TEXT && uploadTasks == null) {
      uploadTasks = new HashMap<>();
    }

    String storageRef = null;

    messageAttachmentUploadedIndex = privateMessages.size();

    privateMessages.add(new PrivateMessage(message,
            System.currentTimeMillis(), currentUid, fileType));
    adapter.notifyItemInserted(privateMessages.size());
    scrollToBottom();

    switch (fileType) {
      case Files.IMAGE:
        storageRef = Files.MESSAGE_IMAGE_REF;
        break;
      case Files.AUDIO:
        storageRef = Files.MESSAGE_RECORD_REF;
        break;
      case Files.DOCUMENT:
        storageRef = Files.MESSAGE_DOCUMENT_REF;
        break;

    }

    final StorageReference reference = FirebaseStorage.getInstance().getReference()
            .child(storageRef).child(UUID.randomUUID().toString() + "-" +
                    System.currentTimeMillis());


    final UploadTask uploadTask = reference.putFile(uri);

    StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                uploadTasks.remove(uploadTask);

                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                  public void onSuccess(Uri uri) {

                    PrivateMessage privateMessage = null;

                    if (fileType == Files.AUDIO) {

                      privateMessage = new PrivateMessage(
                              audioLength,
                              System.currentTimeMillis(),
                              currentUid,
                              fileType,
                              uri.toString());

                      micIv.setClickable(true);

                    } else if (fileType == Files.IMAGE) {

                      privateMessage = new PrivateMessage(
                              message,
                              System.currentTimeMillis(),
                              currentUid,
                              fileType,
                              uri.toString());

                    } else if (fileType == Files.DOCUMENT) {

                      privateMessage = new PrivateMessage(
                              System.currentTimeMillis(),
                              message,
                              currentUid,
                              fileType,
                              uri.toString(),
                              fileName);

                    }

                    sendMessage(privateMessage);

                  }
                });

              }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                new File(uri.getPath()).delete();

              }
            });


    uploadTasks.put(uploadTask, onSuccessListener);

  }

  public void uploadVideoMessage(Uri videoUri, String message, Bitmap videoThumbnail) {

    messageAttachmentUploadedIndex = privateMessages.size();

    privateMessages.add(new PrivateMessage(message,
            System.currentTimeMillis(), currentUid, Files.VIDEO));
    adapter.notifyItemInserted(privateMessages.size());
    scrollToBottom();


    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);

    if (uploadTasks == null) {
      uploadTasks = new HashMap<>();
    }

    final StorageReference videoThumbnailRef = FirebaseStorage.getInstance().getReference()
            .child(Files.MESSAGE_IMAGE_REF).child(UUID.randomUUID().toString() + "-" +
                    System.currentTimeMillis());

    final UploadTask thumbnailUploadTask = videoThumbnailRef.putBytes(baos.toByteArray());

    StorageTask<UploadTask.TaskSnapshot> thumbnailOnSuccessListener =
            thumbnailUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                videoThumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                  public void onSuccess(Uri thumbnailDownloadUrl) {

                    uploadTasks.remove(thumbnailUploadTask);

                    final StorageReference videoRef = FirebaseStorage.getInstance().getReference()
                            .child(Files.MESSAGE_VIDEO_REF).child(UUID.randomUUID().toString() + "-" +
                                    System.currentTimeMillis());

                    final UploadTask videoUploadTask = videoRef.putFile(videoUri);

                    StorageTask<UploadTask.TaskSnapshot> videoOnSuccessListener =
                            videoUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                              @Override
                              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                uploadTasks.remove(videoUploadTask);

                                videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                  @Override
                                  public void onSuccess(Uri videoDownloadUrl) {
                                    PrivateMessage privateMessage = new PrivateMessage(
                                            message,
                                            System.currentTimeMillis(),
                                            currentUid,
                                            Files.VIDEO,
                                            videoDownloadUrl.toString(),
                                            thumbnailDownloadUrl.toString());

                                    sendMessage(privateMessage);

                                  }
                                });
                              }
                            });

                    uploadTasks.put(videoUploadTask, videoOnSuccessListener);
                  }
                });

              }
            });

    uploadTasks.put(thumbnailUploadTask, thumbnailOnSuccessListener);


  }

  public void sendZoomMessage(String content, ZoomMeeting zoomMeeting) {

    PrivateMessage privateMessage = new PrivateMessage(
            content,
            System.currentTimeMillis(),
            currentUid,
            Files.ZOOM);

    privateMessage.setZoomMeeting(zoomMeeting);

    sendMessage(privateMessage);


    if (zoomMeeting.getStartUrl() != null && !zoomMeeting.getStartUrl().isEmpty()) {
      startZoomMeetingIntent(zoomMeeting.getStartUrl());
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

  //clickers
  @Override
  public void onClick(View view) {

    if (view.getId() == R.id.messageAttachIv) {

      showMessageOptionsBottomSheet();

    } else if (view.getId() == R.id.micIv) {

      startAudioRecording();

    }

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {

    if (item.getItemId() == R.id.action_end_meeting) {

      if (firebaseMessageDocRef != null) {
        firebaseMessageDocRef.update("hasEnded", true).addOnSuccessListener(v -> finish());
      }
    }
    return false;
  }

  private void cancelUploadTasks() {

    if (uploadTasks != null && !uploadTasks.isEmpty()) {
      for (UploadTask uploadTask : uploadTasks.keySet()) {

        if (uploadTask.isComplete()) {

//complete so doing nothing

//          Log.d("ttt","task complete so deleting from ref");
//          uploadTask.getSnapshot().getStorage().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//              Log.d("ttt","ref delete sucess");
//            }
//          }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//              Log.d("ttt","ref delete failed: "+e.getMessage());
//            }
//          });

        } else {

          Log.d("ttt", "task not complete so adding new listener, " +
                  "and trying to cancel: " + uploadTask.cancel());

          if (uploadTasks.containsKey(uploadTask)) {

            uploadTask.removeOnSuccessListener(
                    (OnSuccessListener<? super UploadTask.TaskSnapshot>) uploadTasks.get(uploadTask));

          }

          uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

              uploadTask.getSnapshot().getStorage().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  Log.d("ttt", "ref delete sucess");
                }
              }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Log.d("ttt", "ref delete failed: " + e.getMessage());
                }
              });

            }
          });

        }
      }
    }


  }

  private void scrollToBottom() {
    privateMessagingRv.post(() ->
            privateMessagingRv.scrollToPosition(privateMessages.size() - 1));

  }

  private void getMoreTopMessages() {

    currentMessagingRef
            .child("Messages")
            .orderByKey()
            .limitToLast(MESSAGES_PAGE_SIZE)
            .endAt(String.valueOf(Integer.parseInt(firstKeyRef) - 1))
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                final List<PrivateMessage> newMessages = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                  newMessages.add(child.getValue(PrivateMessage.class));

                }

                privateMessages.addAll(0, newMessages);
                adapter.notifyItemRangeInserted(0, newMessages.size());


                firstKeyRef = String.valueOf(Integer.parseInt(lastKeyRef)
                        - privateMessages.size());

                messagesProgressBar.setVisibility(View.GONE);

                if (newMessages.size() < MESSAGES_PAGE_SIZE) {
                  privateMessagingRv.removeOnScrollListener(currentScrollListener);
                }

                isLoadingMessages = false;
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }

  @Override
  public void onLayoutChange(View v, int left, final int top, int right, final int bottom,
                             int oldLeft, final int oldTop, int oldRight, final int oldBottom) {


//    if (oldBottom != 0) {
//
//      LinearLayoutManager linearLayoutManager  = (LinearLayoutManager) privateMessagingRv.getLayoutManager();
//
//      Log.d("ttt","oldBottom: "+oldBottom);
//      Log.d("ttt","bottom: "+bottom);
//      Log.d("ttt","lastVisiblePosition: "+lastVisiblePosition);
//      Log.d("ttt","computeVerticalScrollOffset: "+
//              privateMessagingRv.computeVerticalScrollOffset());
//
//    Log.d("ttt","computeVerticalScrollRange: "+
//            privateMessagingRv.computeVerticalScrollRange());

    if (oldBottom != 0) {
      privateMessagingRv.scrollBy(0, oldBottom - bottom);
    }

  }

  //adapter interfaces
  @Override
  public void deleteMessage(PrivateMessage message, DialogInterface dialog) {

    final String id =
            String.valueOf(Integer.parseInt(firstKeyRef) + privateMessages.indexOf(message));

    currentMessagingRef.child("Messages").child(id).child("deleted").setValue(true).
            addOnSuccessListener(v -> currentMessagingRef.child("lastDeleted").setValue(id)
                    .addOnSuccessListener(vo -> dialog.dismiss()).addOnFailureListener(e ->
                            dialog.dismiss())).addOnFailureListener(e -> {
      dialog.dismiss();

      Toast.makeText(this, R.string.Error_Delete_Message, Toast.LENGTH_SHORT).show();

      Log.d("ttt", "failed: " + e.getMessage());
    });

  }

  @Override
  public void playVideo(String url) {

    pickerFrameLayout.setVisibility(View.VISIBLE);

    getSupportFragmentManager().beginTransaction().replace(pickerFrameLayout.getId(),
            new VideoFullScreenFragment(url)).commit();

  }

  //Acitivy life cycle management
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

      switch (requestCode) {

        case Files.PICK_FILE:
          Files.startDocumentFetchIntent(this);
          break;

        case Files.PICK_IMAGE:
          Files.startImageFetchIntent(this);
          break;

        case Files.PICK_VIDEO:
          Files.startVideoFetchIntent(this);
          break;

      }

    } else {

      //permission denied
    }


  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == Files.PICK_IMAGE) {

      if (resultCode == RESULT_OK && data != null) {

        showFullScreenFragment(new FilePickerPreviewFragment(data.getData(), Files.IMAGE));

      } else {
        //problem with image retrieving


      }

    } else if (requestCode == Files.PICK_VIDEO) {

      if (resultCode == RESULT_OK && data != null) {

        if (Files.isFromGooglePhotos(data.getData())) {
          Toast.makeText(this, R.string.Error_Messsage_to_upload_video +
                  R.string.Google_video_Message, Toast.LENGTH_LONG).show();
          return;
        }

        showFullScreenFragment(new VideoPickerPreviewFragment(data.getData()));

      } else {
        //problem with image retrieving


      }

    } else if (requestCode == Files.PICK_FILE) {
      if (resultCode == RESULT_OK && data != null) {

        if (Files.getFileSizeInMB(this, data.getData()) > Files.MAX_FILE_SIZE) {
          Toast.makeText(this, R.string.Message_Error_Uplaod_BigFile
                  + Files.MAX_FILE_SIZE + R.string.DataScale, Toast.LENGTH_SHORT).show();
        } else {
          showFullScreenFragment(new FilePickerPreviewFragment(data.getData(), Files.DOCUMENT));
        }

      } else {
        //problem with image retrieving


      }
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    privateMessagingRv.removeOnLayoutChangeListener(this);

    if (childEventListeners != null && !childEventListeners.isEmpty()) {
      for (DatabaseReference reference : childEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(childEventListeners.get(reference)));
      }
    }
    if (valueEventListeners != null && !valueEventListeners.isEmpty()) {
      for (DatabaseReference reference : valueEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(valueEventListeners.get(reference)));
      }
    }

    if (downloadCompleteReceiver != null) {
      unregisterReceiver(downloadCompleteReceiver);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (mediaRecorder != null) {
      mediaRecorder.release();
      mediaRecorder = null;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (progressHandle != null && progressRunnable != null) {

      stopAudioRecorder(null, 0, true);
      progressHandle.removeCallbacks(progressRunnable);
    }

  }

  @Override
  public void onBackPressed() {

    if (pickerFrameLayout.getVisibility() == View.VISIBLE) {
      dismissFullScreenFragment();
    } else if (uploadTasks != null && !uploadTasks.isEmpty()) {

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle(getString(R.string.Auth_from_sending_message));
      alert.setMessage(getString(R.string.Leaving_Message));

      alert.setPositiveButton(R.string.YES, (dialogInterface, i) -> {
        cancelUploadTasks();
        dialogInterface.dismiss();
        finish();
      });

      alert.setNegativeButton(R.string.No, (dialog, which) -> {
        dialog.cancel();
      });
      alert.create().show();

    } else {
      super.onBackPressed();
    }

  }

  private void showFullScreenFragment(Fragment fragment) {

    pickerFrameLayout.setVisibility(View.VISIBLE);

    getSupportFragmentManager().beginTransaction().replace(pickerFrameLayout.getId(),
            fragment, "fullScreen").commit();

  }

  private void dismissFullScreenFragment() {

    if (pickerFrameLayout.getVisibility() == View.VISIBLE) {
      pickerFrameLayout.setVisibility(View.GONE);

      if (!getSupportFragmentManager().getFragments().isEmpty()) {

        getSupportFragmentManager().beginTransaction().remove(
                getSupportFragmentManager().getFragments().get(0)
        ).commit();

      }

    }
  }

  @Override
  public void startDownload(int position, String url, String fileName) {

    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setMessage(getString(R.string.DownLoad_Asking) + fileName);

    alertDialogBuilder.setPositiveButton(getText(R.string.Download_btn), (dialogInterface, i) -> {
      downloadFile(position, url, fileName);
    });

    alertDialogBuilder.setNegativeButton(getText(R.string.Cansle), (dialogInterface, i) -> {
      dialogInterface.dismiss();
    });

    alertDialogBuilder.show();


  }

  @Override
  public void showImage(String url) {

    showFullScreenFragment(new ImageFullScreenFragment(url));

  }

  @Override
  public boolean cancelDownload(int position, long downloadID) {

    final DownloadManager downloadManager = (DownloadManager)
            this.getSystemService(Context.DOWNLOAD_SERVICE);

    downloadManager.remove(downloadID);
    privateMessages.get(position).setUploadTask(null);
    adapter.notifyItemChanged(position);

    return true;
  }

  private void downloadFile(int position, String url, String fileName) {

    DownloadManager.Request request;

    request = new DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription(getString(R.string.Download))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      request.setRequiresCharging(false);
    }

    DownloadManager downloadManager = (DownloadManager)
            this.getSystemService(Context.DOWNLOAD_SERVICE);


    request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,
            fileName);

    long downloadId = downloadManager.enqueue(request);

    if (position < privateMessages.size()) {

      privateMessages.get(position).setUploadTask(
              new PrivateMessage.UploadTask(downloadId, true));

      adapter.notifyItemChanged(position);

      if (downloadCompleteReceiver == null) {
        setUpDownloadReceiver();
      }

    }

  }

  private void setUpDownloadReceiver() {

    downloadCompleteReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (id != -1) {

//          openDownloadedFile(id);

          for (int i = 0; i < privateMessages.size(); i++) {
            if (privateMessages.get(i).getUploadTask() != null) {
              if (privateMessages.get(i).getUploadTask().getDownloadId() == id) {
                privateMessages.get(i).getUploadTask().setCompleted(true);
                adapter.notifyItemChanged(i);
//              privateMessages.get(i)
                break;
              }
            }
          }
        }

      }
    };

    registerReceiver(downloadCompleteReceiver,
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

  }

  // remove messaging notifcation if one exists
  private void handleNotification() {

    sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name),
            Context.MODE_PRIVATE);

    sharedPreferences.edit()
            .putString("currentlyMessagingUid", groupId).apply();

    if (GlobalVariables.getMessagesNotificationMap() != null) {

      final String identifierTitle = groupId + "message";

      if (GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {
        Log.d("ttt", "removing: " + identifierTitle);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(GlobalVariables.getMessagesNotificationMap().get(identifierTitle));

        GlobalVariables.getMessagesNotificationMap().remove(identifierTitle);
      }
    }

  }

  private void sendZoomMeetingNotification(String topic, String joinUrl) {

    String body = getString(R.string.Start_zoom_Meeting) + topic;

    if (data == null) {

      data = new Data(
              currentUid,
              body,
              currentGroupName,
              currentGroupImage,
              "message",
              "zoomMeeting",
              groupId + "-" + joinUrl
      );

    } else {
      data.setBody(body);
    }


    sendNotificationsToMembers(body);
  }

  //notifications methods
  private void checkUserActivityAndSendNotifications(String message, int messageType) {


    String body;
    switch (messageType) {

      case Files.IMAGE:
        body = currentUserName + getString(R.string.send_An_Message);
        break;

      case Files.DOCUMENT:
      case Files.AUDIO:
        body = currentUserName + getString(R.string.sen_Attachment);
        break;

      case Files.VIDEO:
        body = currentUserName + getString(R.string.send_video);
        break;


      default:
        body = currentUserName + ": " + message;
    }

    if (data == null) {

      data = new Data(
              currentUid,
              body,
              currentGroupName,
              currentGroupImage,
              "message",
              "privateMessaging",
              groupId
      );

    } else {
      data.setBody(body);
    }

    sendNotificationsToMembers(body);

  }

  private void sendNotificationsToMembers(String body) {
    for (String userId : groupMembers) {

      usersRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
        if (documentSnapshot.exists()) {
          if (documentSnapshot.contains("ActivelyMessaging")) {
            final String messaging = documentSnapshot.getString("ActivelyMessaging");
            if (messaging == null || !messaging.equals(groupId)) {
              Log.d("ttt", "sendBothNotifs");
              sendBothNotifs(body, userId);
            }
          } else {
            Log.d("ttt", "sendBothNotifs");
            sendBothNotifs(body, userId);
          }
        }
      });
    }

  }

  private void sendBothNotifs(String body, String userId) {

    FirestoreNotificationSender.sendFirestoreNotification(userId, data.getSourceType(), body,
            currentGroupName, groupId);

    CloudMessagingNotificationsSender.sendNotification(userId, data);

  }

  //click listeners
  private class FirstMessageClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

      Log.d("ttt", "clicked send button");
      final String content = messagingEd.getText().toString();
      if (!content.isEmpty()) {

//        if (WifiUtil.checkWifiConnection(view.getContext())) {
        messagingEd.setText("");
        messagingEd.setClickable(false);

        final PrivateMessage privateMessage = new PrivateMessage(
                content,
                System.currentTimeMillis(),
                currentUid,
                Files.TEXT);

//        final Map<String, Object> privateMessageMap = new HashMap<>();
//        privateMessageMap.put("content",content);
//        privateMessageMap.put("content",content);

        createGroupDocument(privateMessage);

      } else {
        Toast.makeText(view.getContext(),
                R.string.message_send_empty, Toast.LENGTH_SHORT).show();
      }
    }
  }

  private class TextMessageSenderClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString();

      if (!content.equals("")) {

//        if (WifiUtil.checkWifiConnection(view.getContext())) {

        PrivateMessage privateMessage = new PrivateMessage(
                content,
                System.currentTimeMillis(),
                currentUid,
                Files.TEXT);

        sendMessage(privateMessage);

//        }
      } else {
        Toast.makeText(view.getContext(),
                R.string.message_send_empty, Toast.LENGTH_SHORT).show();
      }


    }
  }

  //scroll methods
  private class toTopScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

      int firstVisible = ((LinearLayoutManager) recyclerView.getLayoutManager())
              .findFirstCompletelyVisibleItemPosition();

      if (!isLoadingMessages && (firstVisible == 0 || firstVisible == -1) &&
              !recyclerView.canScrollVertically(-1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d(TAG, "is at top man");

        isLoadingMessages = true;
        messagesProgressBar.setVisibility(View.GONE);
        getMoreTopMessages();

      }
    }
  }
}