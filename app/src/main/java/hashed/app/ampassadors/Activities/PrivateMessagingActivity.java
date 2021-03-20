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
import android.database.Cursor;
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

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class PrivateMessagingActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener, PrivateMessagingAdapter.DeleteMessageListener,
        PrivateMessagingAdapter.VideoMessageListener, PrivateMessagingAdapter.DocumentMessageListener,
        View.OnClickListener, RecyclerView.OnLayoutChangeListener,
        PrivateMessagingAdapter.ImageMessageListener {

  public static final int RECORD_AUDIO_REQUEST = 30;
  //constants
  private static final String TAG = "privateMessaging";
  private static final int MESSAGES_PAGE_SIZE = 25;
  //database
  private static final DatabaseReference databaseReference
          = FirebaseDatabase.getInstance().getReference().child("PrivateMessages").getRef();

  private static final CollectionReference usersRef
          = FirebaseFirestore.getInstance().collection("Users");
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
  private String messagingUid;
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
//  int lastVisiblePosition;


  //notifications
  private SharedPreferences sharedPreferences;
  private Data data;
  private String currentUserName;
  private String currentImageUrl;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_private_messaging);


    //getting the messaging user id
    getMessagingUid();

    //setting up toolbar and its actions
    setUpToolBarAndActions();

    //initializing Views
    initializeViews();

    //handling notification is it exists
    handleNotification();

    //getting current user data
    getMyData();

    //getting messaging userInfo
    getUserData();

    //fetching previous messages and listen to new
    fetchPreviousMessages();


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

  private void setUpToolBarAndActions() {

    final Toolbar toolbar = findViewById(R.id.privateMessagingTb);
    toolbar.inflateMenu(R.menu.private_messaging_toolbar_menu);


    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    toolbar.setOnMenuItemClickListener(this);

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {

    if (item.getItemId() == R.id.action_delete) {

      deleteMessageDocument();

    }

    return false;
  }

  //get messaging user id
  private void getMessagingUid() {

    final Intent intent = getIntent();

    messagingUid = intent.getStringExtra("messagingUid");

    if (intent.hasExtra("isFromNotification") && Build.VERSION.SDK_INT < 26) {
      BadgeUtil.decrementBadgeNum(this);
    }

    usersRef.document(currentUid).update("ActivelyMessaging", messagingUid);

  }

  //firestore user data
  private void getUserData() {
    usersRef.document(messagingUid).get().addOnSuccessListener(documentSnapshot -> {
      if (documentSnapshot.exists()) {

        if (documentSnapshot.contains("imageUrl")) {
          Picasso.get().load(documentSnapshot.getString("imageUrl")).fit().into(messagingTbProfileIv);
        }

        messagingTbNameTv.setText(documentSnapshot.getString("username"));

      }
    });
  }

  private void getMyData() {

    usersRef.document(currentUid).get().addOnSuccessListener(documentSnapshot -> {
      if (documentSnapshot.exists()) {
        if (documentSnapshot.contains("imageUrl")) {
          currentImageUrl = documentSnapshot.getString("imageUrl");
        }
        currentUserName = documentSnapshot.getString("username");
      }
    });
  }


  // remove messaging notifcation if one exists
  private void handleNotification() {

    sharedPreferences = getSharedPreferences(getResources().getString(R.string.app_name),
            Context.MODE_PRIVATE);

    sharedPreferences.edit()
            .putString("currentlyMessagingUid", currentUid).apply();

    if (GlobalVariables.getMessagesNotificationMap() != null) {

      if (GlobalVariables.getMessagesNotificationMap().containsKey(messagingUid)) {
        Log.d("ttt", "removing: " + messagingUid);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.cancel(GlobalVariables.getMessagesNotificationMap().get(messagingUid));

        GlobalVariables.getMessagesNotificationMap().remove(messagingUid);
      }
    }

  }

  //notifcations methods
  private void checkUserActivityAndSendNotifications(String message, int messageType) {

    String body;
    switch (messageType) {

      case Files.IMAGE:
        body = currentUserName + R.string.send_An_Message;
        break;

      case Files.DOCUMENT:
      case Files.AUDIO:
        body = currentUserName + R.string.send_An_Message;
        break;

      case Files.VIDEO:
        body = currentUserName +R.string.send_video ;
        break;


      default:
        body = currentUserName + ": " + message;
    }


    usersRef.document(messagingUid)
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {

        if (documentSnapshot.contains("ActivelyMessaging")) {
          final String messaging = documentSnapshot.getString("ActivelyMessaging");
          if (messaging == null || !messaging.equals(currentUid)) {
            Log.d("ttt", "sendBothNotifs");
            sendBothNotifs(message);
          }
        } else {
          Log.d("ttt", "sendBothNotifs");
          sendBothNotifs(message);
        }

      }
    });

  }

  private void sendBothNotifs(String message) {

    FirestoreNotificationSender.sendFirestoreNotification(messagingUid, "privateMessage",
            message, currentUserName, messagingUid);

    sendCloudNotification(message);
  }

  private void sendCloudNotification(String message) {
    Log.d("ttt", "sending cloud notificaiton");

    if (data == null) {

      data = new Data(
              currentUid,
              message,
              getResources().getString(R.string.new_message) + " " + currentUserName,
              currentImageUrl,
              "message",
              "privateMessaging",
              currentUid
      );

    } else {
      data.setBody(message);
    }

    CloudMessagingNotificationsSender.sendNotification(messagingUid, data);

  }


  //database messages functions
  private void fetchPreviousMessages() {

    adapter = new PrivateMessagingAdapter(privateMessages,
            this,
            this,
            this,
            this,
            this,
            false);

    privateMessagingRv.setAdapter(adapter);

    Log.d("privateMessaging", "start fetching");

    databaseReference.child(currentUid + "-" + messagingUid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                  messagesProgressBar.setVisibility(View.VISIBLE);
                  fetchMessagesFromSnapshot(snapshot);

                } else {

                  databaseReference.child(messagingUid + "-" + currentUid)
                          .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                              if (snapshot.exists()) {
                                messagesProgressBar.setVisibility(View.VISIBLE);
                                fetchMessagesFromSnapshot(snapshot);

                              } else {

                                currentMessagingRef = databaseReference.child(currentUid + "-" + messagingUid);
                                firebaseMessageDocRef = FirebaseFirestore.getInstance()
                                        .collection("PrivateMessages")
                                        .document(currentMessagingRef.getKey());

                                messageSendIv.setOnClickListener(new FirstMessageClickListener());

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

    firebaseMessageDocRef = FirebaseFirestore.getInstance()
            .collection("PrivateMessages")
            .document(Objects.requireNonNull(currentMessagingRef.getKey()));

    createMessagesListener();

  }

  private void createMessagesListener() {

    currentMessagingRef.child("messages").orderByKey().limitToLast(MESSAGES_PAGE_SIZE)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                  return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                  if (firstKeyRef == null) {
                    firstKeyRef = child.getKey();
                  }
                  privateMessages.add(child.getValue(PrivateMessage.class));
                }

//                firstKeyRef = Iterables.get(snapshot.getChildren(),0).getKey();

                lastKeyRef = Iterables.getLast(snapshot.getChildren()).getKey();

                adapter.notifyDataSetChanged();

                scrollToBottom();

                messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
                addListenerForNewMessages();

                if (Integer.parseInt(lastKeyRef) + 1 > MESSAGES_PAGE_SIZE) {
                  privateMessagingRv.addOnScrollListener(
                          currentScrollListener = new toTopScrollListener());
                }

                currentMessagingRef.child("LastSeenMessage:" + currentUid).setValue(lastKeyRef);

                addDeleteFieldListener();

                messagesProgressBar.setVisibility(View.GONE);
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                messagesProgressBar.setVisibility(View.GONE);
                Toast.makeText(PrivateMessagingActivity.this,
                        R.string.message_load_failed, Toast.LENGTH_SHORT).show();
              }
            });

  }

  //database messages listeners
  private void addListenerForNewMessages() {

    ChildEventListener childEventListener;

    final Query query = currentMessagingRef.child("messages").orderByKey()
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

  @Override
  public void showImage(String url) {
    showFullScreenFragment(new ImageFullScreenFragment(url));
  }

  //messaging methods
  private void createMessagingDocument(PrivateMessage privateMessage) {

    final Map<String, Object> messagingDocumentMap = new HashMap<>();

    messagingDocumentMap.put("LastSeenMessage:" + currentUid, "0");
    messagingDocumentMap.put("LastSeenMessage:" + messagingUid, "0");
    messagingDocumentMap.put("DeletedFor:" + currentUid, false);
    messagingDocumentMap.put("DeletedFor:" + messagingUid, false);


    final Map<String, PrivateMessage> messages = new HashMap<>();
    messages.put("0", privateMessage);

    messagingDocumentMap.put("messages", messages);

    currentMessagingRef.setValue(messagingDocumentMap).addOnSuccessListener(v -> {

      final Map<String, Object> messageDocumentPreviewMap = new HashMap<>();

      final List<String> users = new ArrayList<>();
      users.add(currentUid);
      users.add(messagingUid);

      messageDocumentPreviewMap.put("users", users);
      messageDocumentPreviewMap.put("databaseRefId", currentMessagingRef.getKey());
      messageDocumentPreviewMap.put("latestMessageTime", privateMessage.getTime());

      firebaseMessageDocRef =
              FirebaseFirestore.getInstance().collection("PrivateMessages")
                      .document(currentMessagingRef.getKey());

      firebaseMessageDocRef.set(messageDocumentPreviewMap);

//      privateMessages.add(privateMessage);
//      adapter.notifyDataSetChanged();

      firstKeyRef = "0";
      lastKeyRef = "0";


      createMessagesListener();

      messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
      messageSendIv.setClickable(true);

    }).addOnFailureListener(e -> {

      Toast.makeText(this,
              R.string.message_send_failed, Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    });

  }

  private void sendMessage(PrivateMessage privateMessage) {

    messagingEd.setText("");
    messagingEd.setClickable(false);

    if (lastKeyRef == null) {
      createMessagingDocument(privateMessage);
      return;
    }

    Log.d("PrivateMessaging", "added message at: " + (Integer.parseInt(lastKeyRef) + 1));
    final DatabaseReference childRef = currentMessagingRef.child("messages")
            .child(String.valueOf((Integer.parseInt(lastKeyRef) + 1)));

    childRef.setValue(privateMessage).addOnSuccessListener(v -> {

      checkUserActivityAndSendNotifications(privateMessage.getContent(), privateMessage.getType());

//            checkUserActivityAndSendNotifications(messageMap.getContent());
      messageSendIv.setClickable(true);

    }).addOnFailureListener(e -> {

      Toast.makeText(this, R.string.message_send_failed, Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    }).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseMessageDocRef.update("latestMessageTime", privateMessage.getTime());
      }
    });

  }

  private boolean checkIsUploading(){
    if (uploadTasks != null && !uploadTasks.isEmpty()) {
      Toast.makeText(this, "Please wait while your previous attachment is being sent!"
              , Toast.LENGTH_SHORT).show();
      return true;
    }
    return false;
  }
  private void showMessageOptionsBottomSheet() {

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
//        bsd.dismiss();
//
//        Files.startImageFetchIntent(PrivateMessagingActivity.this);
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

    bsd.setOnDismissListener(dialogInterface -> messageAttachIv.setClickable(true));

    bsd.setContentView(parentView);
    bsd.show();

  }

  private void startAudioRecording() {

      if (checkIsUploading()) {
        return;
      }

    if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

      micIv.setClickable(false);

      messagingEd.setEnabled(false);
      messagingEd.setFocusable(false);

      new Handler().post(() -> {
        messageAttachIv.setVisibility(View.GONE);
        messageSendIv.setVisibility(View.GONE);
        cancelIv.setVisibility(View.VISIBLE);

        DrawableCompat.setTint(
                DrawableCompat.wrap(micIv.getDrawable()),
                getResources().getColor(R.color.red)
        );

        messagingEd.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        messagingEd.setText("00:00");

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

    micIv.setOnClickListener(view -> {
      progressHandle.removeCallbacks(progressRunnable);
      stopAudioRecorder(fileName, startTime, false);
      progressHandle = null;
      progressRunnable = null;
    });

    cancelIv.setOnClickListener(view -> {
      progressHandle.removeCallbacks(progressRunnable);
      stopAudioRecorder(null, 0, true);
      progressHandle = null;
      progressRunnable = null;
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

    new Handler().post(() -> {

      messageAttachIv.setVisibility(View.VISIBLE);
      messageSendIv.setVisibility(View.VISIBLE);
      cancelIv.setVisibility(View.GONE);

      DrawableCompat.setTint(
              DrawableCompat.wrap(micIv.getDrawable()),
              getResources().getColor(R.color.black)
      );

      messagingEd.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
      messagingEd.setText("");

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
            uploadTask.addOnSuccessListener(taskSnapshot -> {

              uploadTasks.remove(uploadTask);

              reference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                PrivateMessage privateMessage = null;

                HashMap<String, Object> messageMap = new HashMap<>();


                if (fileType == Files.AUDIO) {

                  privateMessage = new PrivateMessage(
                          audioLength,
                          System.currentTimeMillis(),
                          currentUid,
                          fileType,
                          uri1.toString());

                  micIv.setClickable(true);

                } else if (fileType == Files.IMAGE) {

                  privateMessage = new PrivateMessage(
                          message,
                          System.currentTimeMillis(),
                          currentUid,
                          fileType,
                          uri1.toString());

                } else if (fileType == Files.DOCUMENT) {

                  privateMessage = new PrivateMessage(
                          System.currentTimeMillis(),
                          message,
                          currentUid,
                          fileType,
                          uri1.toString(),
                          fileName);

                }

                sendMessage(privateMessage);

              });

            }).addOnCompleteListener(task -> new File(uri.getPath()).delete());


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
            thumbnailUploadTask.addOnSuccessListener(taskSnapshot ->
                    videoThumbnailRef.getDownloadUrl().addOnSuccessListener(thumbnailDownloadUrl -> {

                      uploadTasks.remove(thumbnailUploadTask);

                      final StorageReference videoRef = FirebaseStorage.getInstance().getReference()
                              .child(Files.MESSAGE_VIDEO_REF).child(UUID.randomUUID().toString() + "-" +
                                      System.currentTimeMillis());

                      final UploadTask videoUploadTask = videoRef.putFile(videoUri);

                      StorageTask<UploadTask.TaskSnapshot> videoOnSuccessListener =
                              videoUploadTask.addOnSuccessListener(taskSnapshot1 -> {

                                uploadTasks.remove(videoUploadTask);

                                videoRef.getDownloadUrl().addOnSuccessListener(videoDownloadUrl -> {
                                  PrivateMessage privateMessage = new PrivateMessage(
                                          message,
                                          System.currentTimeMillis(),
                                          currentUid,
                                          Files.VIDEO,
                                          videoDownloadUrl.toString(),
                                          thumbnailDownloadUrl.toString());

                                  sendMessage(privateMessage);

                                });
                              });

                      uploadTasks.put(videoUploadTask, videoOnSuccessListener);
                    }));

    uploadTasks.put(thumbnailUploadTask, thumbnailOnSuccessListener);


  }

  private void addFileMessageUploadListener(DatabaseReference attachmentRef,
                                            int index) {

    attachmentRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {

        Log.d(TAG, "ATTACHMETN CAHNGED");

        if (snapshot.exists()) {
          final String url = snapshot.getValue(String.class);
          if (url != null) {
            privateMessages.get(index).setAttachmentUrl(url);
            adapter.notifyItemChanged(index);
          }
        }

        attachmentRef.removeEventListener(this);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

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

          uploadTask.addOnSuccessListener(taskSnapshot ->
                  uploadTask.getSnapshot().getStorage().delete()
                          .addOnSuccessListener(v -> Log.d("ttt", "ref delete sucess")).
                          addOnFailureListener(e -> Log.d("ttt", "ref delete failed: " +
                                  e.getMessage())));

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
            .child("messages")
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
                  Log.d(TAG, "removing scorll lsitener");
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

    currentMessagingRef.child("messages").child(id).child("deleted").setValue(true).
            addOnSuccessListener(v -> currentMessagingRef.child("lastDeleted").setValue(id)
                    .addOnSuccessListener(Void -> {

                      if (privateMessages.indexOf(message) == privateMessages.size() - 1) {
                        firebaseMessageDocRef.update("lastMessageDeleted",
                                Integer.valueOf(id));
                      }

                      dialog.dismiss();

                    }).addOnFailureListener(e -> dialog.dismiss())).addOnFailureListener(e -> {
      dialog.dismiss();

      Toast.makeText(this, "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();

      Log.d("ttt", "failed: " + e.getMessage());
    });

  }

  @Override
  public void playVideo(String url) {

    pickerFrameLayout.setVisibility(View.VISIBLE);

    getSupportFragmentManager().beginTransaction().replace(pickerFrameLayout.getId(),
            new VideoFullScreenFragment(url)).commit();

  }

  //interfaces
  @Override
  public void onClick(View view) {

    if (view.getId() == R.id.messageAttachIv) {

      showMessageOptionsBottomSheet();

    } else if (view.getId() == R.id.micIv) {

      startAudioRecording();

    }

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

        case RECORD_AUDIO_REQUEST:
          startAudioRecording();
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
          Toast.makeText(this, "لا يمكن رفع فيديو مباشرة من صور جوجل!" +
                  " يجب تحميل الفيديو اولا ثم المحاولة مرة اخرى", Toast.LENGTH_LONG).show();
          return;
        }

        showFullScreenFragment(new VideoPickerPreviewFragment(data.getData()));

      } else {
        //problem with image retrieving


      }

    } else if (requestCode == Files.PICK_FILE) {
      if (resultCode == RESULT_OK && data != null) {

        if (Files.getFileSizeInMB(this, data.getData()) > Files.MAX_FILE_SIZE) {
          Toast.makeText(this, "You can't send files bigger than "
                  + Files.MAX_FILE_SIZE + " MB!", Toast.LENGTH_SHORT).show();
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

    if (sharedPreferences != null) {
      sharedPreferences.edit().remove("isPaused").remove("currentlyMessagingUid").apply();
    }

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
//
//    if(promotionDeleteReceiver!=null){
//      unregisterReceiver(promotionDeleteReceiver);
//    }

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
  protected void onResume() {
    super.onResume();

    if (currentUid != null) {
      usersRef.document(currentUid).update("ActivelyMessaging", messagingUid);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (progressHandle != null && progressRunnable != null) {

      stopAudioRecorder(null, 0, true);
      progressHandle.removeCallbacks(progressRunnable);
    }

    if (currentMessagingRef != null && lastKeyRef != null) {
      currentMessagingRef.child("LastSeenMessage:" + currentUid).setValue(lastKeyRef);
    }

    if (currentUid != null) {
      usersRef.document(currentUid).update("ActivelyMessaging", null);
    }

    if (sharedPreferences != null) {
      sharedPreferences.edit().putBoolean("isPaused", true).apply();
    }
  }

  @Override
  public void onBackPressed() {

    if (pickerFrameLayout.getVisibility() == View.VISIBLE) {
      dismissFullScreenFragment();
    } else if (uploadTasks != null && !uploadTasks.isEmpty()) {

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Do you want to leave while your message is being sent?");
      alert.setMessage("leaving will cancel the message!");

      alert.setPositiveButton("Yes", (dialogInterface, i) -> {
        cancelUploadTasks();
        dialogInterface.dismiss();
        finish();
      });

      alert.setNegativeButton("No", (dialog, which) -> dialog.cancel());
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
    alertDialogBuilder.setMessage("Do you want to download " + fileName);

    alertDialogBuilder.setPositiveButton("Download", (dialogInterface, i) -> {
      downloadFile(position, url, fileName);
    });

    alertDialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
      dialogInterface.dismiss();
    });

    alertDialogBuilder.show();


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

//    String downloadPath =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                    .getAbsolutePath();

//    final Uri uri = Uri.parse(url);
    DownloadManager.Request request;

    request = new DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading")
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

  private void openDownloadedFile(long id) {

    DownloadManager.Query query = new DownloadManager.Query();
    query.setFilterById(id);
    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
    Cursor c = downloadManager.query(query);
    if (c.moveToFirst()) {
      int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
      if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

        final String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

        startActivity(Files.getFileLaunchIntentFromUri(uri));

      }
    }

  }

  private void deleteMessageDocument() {

    if (firebaseMessageDocRef != null) {
      finish();

      FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

      firebaseMessageDocRef.delete();

      currentMessagingRef.child("messages")
              .get().addOnSuccessListener(snapshot -> {

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

      }).addOnCompleteListener(task -> currentMessagingRef.getRef().removeValue());

    }
  }

  //click listeners
  //click listeners
  private class FirstMessageClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString().trim();
      if (!content.isEmpty()) {

//        if (WifiUtil.checkWifiConnection(view.getContext())) {
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

  private class TextMessageSenderClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString().trim();

      if (!content.isEmpty()) {

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
        messagesProgressBar.setVisibility(View.VISIBLE);
        getMoreTopMessages();

      }
    }
  }
}