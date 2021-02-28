package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;
import hashed.app.ampassadors.Fragments.FullscreenFragment2;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PrivateMessagingActivity extends AppCompatActivity
        implements Toolbar.OnMenuItemClickListener,PrivateMessagingAdapter.DeleteMessageListener,
        View.OnClickListener {




  //constants
  private static final String TAG = "privateMessaging";
  private static final int MESSAGES_PAGE_SIZE = 15;
  public static final int IMAGE_RESULT = 20;


  //database
  private final DatabaseReference databaseReference
          = FirebaseDatabase.getInstance().getReference().child("PrivateMessages").getRef();
  private DatabaseReference currentMessagingRef;
  private String firstKeyRef;
  private String lastKeyRef;

  //event listeners
  private Map<DatabaseReference, ChildEventListener> childEventListeners;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;


  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private String messagingUid;

  //messages
  private final ArrayList<PrivateMessage> privateMessages = new ArrayList<>();
  private PrivateMessagingAdapter adapter;
  private toTopScrollListener currentScrollListener;


  //views
  private RecyclerView privateMessagingRv;
  private ImageView messageSendIv;
  private ImageView messageAttachIv;
  private ImageView micIv;
  private EditText messagingEd;
  private ProgressBar messagesProgressBar;
  private FrameLayout pickerFrameLayout;

  private boolean isLoadingMessages;


  //attachments
  private Files files;
  private int messageAttachmentUploadedIndex = -1;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_private_messaging);

    if(currentUid.equals("C9ZjQWY10ihMLxa0jBGJjyUWbUr1")){
      messagingUid = "q3ApcRTckxfVKEptb6E9wZspXyz1";
    }else{
      messagingUid = "C9ZjQWY10ihMLxa0jBGJjyUWbUr1";
    }

    //setting up toolbar and its actions
    setUpToolBarAndActions();

    //initializing Views
    initializeViews();

    //Adding recycler layout manager and layout change listener
    setRecyclerLayoutManagerAndChangListener();

    //fetching previous messages and listen to new
    fetchPreviousMessages();

  }


  private void initializeViews(){

    privateMessagingRv = findViewById(R.id.privateMessagingRv);
    messageSendIv = findViewById(R.id.messageSendIv);
    messagingEd = findViewById(R.id.messagingEd);
    messageAttachIv = findViewById(R.id.messageAttachIv);
    messagesProgressBar = findViewById(R.id.messagesProgressBar);
    micIv = findViewById(R.id.micIv);
    pickerFrameLayout = findViewById(R.id.pickerFrameLayout);


    messageAttachIv.setOnClickListener(this);
    micIv.setOnClickListener(this);
  }

  private void setUpToolBarAndActions(){

    final Toolbar toolbar = findViewById(R.id.privateMessagingTb);

    toolbar.setNavigationOnClickListener(v-> onBackPressed()) ;
    toolbar.setOnMenuItemClickListener(this);

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {


    return false;
  }


  private void setRecyclerLayoutManagerAndChangListener(){

    final LinearLayoutManager llm = new LinearLayoutManager(this,
            RecyclerView.VERTICAL, false) {
//      @Override
//      public void onItemsAdded(@NonNull RecyclerView recyclerView,int positionStart,int itemCount){
////        h
//      }
    };

    privateMessagingRv.setLayoutManager(llm);

    privateMessagingRv.addOnLayoutChangeListener(
            (view, i, i1, i2, bottom, i4, i5, i6, oldBottom) -> {
      if (bottom < oldBottom) {
        scrollToBottom();
      }
    });
  }

  //database messages functions
  private void fetchPreviousMessages(){

    adapter = new PrivateMessagingAdapter(privateMessages,this,this);
    privateMessagingRv.setAdapter(adapter);


    databaseReference.child(currentUid+"-"+messagingUid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {

        if(snapshot.exists()){

          fetchMessagesFromSnapshot(snapshot);

        }else{

          databaseReference.child(messagingUid+"-"+currentUid)
                  .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                      if(snapshot.exists()){

                        fetchMessagesFromSnapshot(snapshot);

                      }else{

                        currentMessagingRef = databaseReference.child(currentUid+"-"+messagingUid);
                        messageSendIv.setOnClickListener(new FirstMessageClickListener());

                      }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                      Log.d(TAG,"receiverUid - senderUid onCancelled:"
                      +error.getMessage());
                    }
                  });

        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d(TAG,"senderUid - receiverUid onCancelled:"
                +error.getMessage());
      }
    });

  }

  private void fetchMessagesFromSnapshot(DataSnapshot dataSnapshot){

    currentMessagingRef = dataSnapshot.getRef();

    currentMessagingRef.child("messages").orderByKey().limitToLast(MESSAGES_PAGE_SIZE)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot child:snapshot.getChildren()){
                  privateMessages.add(child.getValue(PrivateMessage.class));
                }

                firstKeyRef = Iterables.get(snapshot.getChildren(),0).getKey();

                lastKeyRef = Iterables.getLast(snapshot.getChildren()).getKey();

                adapter.notifyDataSetChanged();

                scrollToBottom();

                messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
                addListenerForNewMessages();

                if(Integer.parseInt(lastKeyRef) + 1 > MESSAGES_PAGE_SIZE){
                  privateMessagingRv.addOnScrollListener(
                          currentScrollListener = new toTopScrollListener());
                }

                addDeleteFieldListener();

              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                 Toast.makeText(PrivateMessagingActivity.this,
                      R.string.message_load_failed, Toast.LENGTH_SHORT).show();
                finish();
              }
            });

  }

  private void addListenerForNewMessages(){

    ChildEventListener childEventListener;

    final Query query = currentMessagingRef.child("messages").orderByKey()
                    .startAt(String.valueOf(Integer.parseInt(lastKeyRef)+1));

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
        if(messageAttachmentUploadedIndex!=-1){

          privateMessages.set(messageAttachmentUploadedIndex,message);
          adapter.notifyItemChanged(messageAttachmentUploadedIndex);
          messageAttachmentUploadedIndex = -1;
        }else{

          privateMessages.add(message);
          adapter.notifyItemInserted(privateMessages.size());

        }



        if(message != null && message.getSender().equals(currentUid)){

        }

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

  void addDeleteFieldListener(){

    valueEventListeners = new HashMap<>();

    ValueEventListener valueEventListener;
    currentMessagingRef
            .child("lastDeleted")
            .addValueEventListener(valueEventListener = new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                  final String id = snapshot.getValue(String.class);

                  if(id!=null){

                    final int deletedIndex =
                            Math.abs(Integer.parseInt(firstKeyRef) - Integer.parseInt(id));

                    privateMessages.get(deletedIndex).setDeleted(true);
                    adapter.notifyItemChanged(deletedIndex);

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

    valueEventListeners.put(currentMessagingRef.child("lastDeleted").getRef(),valueEventListener);

  }

  //click listeners
  private class FirstMessageClickListener implements View.OnClickListener{

    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString();
      if (!content.isEmpty()) {

//        if (WifiUtil.checkWifiConnection(view.getContext())) {
        messagingEd.setText("");
        messagingEd.setClickable(false);

        final Map<String, Object> messagingDocumentMap = new HashMap<>();
        messagingDocumentMap.put("sender", currentUid);
        messagingDocumentMap.put("receiver", messagingUid);
        messagingDocumentMap.put("LastSeenMessage:"+currentUid, 0);
        messagingDocumentMap.put("LastSeenMessage:"+messagingUid, 0);
        messagingDocumentMap.put("DeletedFor:"+currentUid,false);
        messagingDocumentMap.put("DeletedFor:"+messagingUid,false);

        final PrivateMessage privateMessage = new PrivateMessage(
                content,
                System.currentTimeMillis()/1000,
                currentUid,
                Files.TEXT);


        final Map<String,PrivateMessage> messages = new HashMap<>();
        messages.put("0",privateMessage);

        messagingDocumentMap.put("messages",messages);


        currentMessagingRef.setValue(messagingDocumentMap).addOnSuccessListener(v -> {

          privateMessages.add(privateMessage);
          adapter.notifyDataSetChanged();

          firstKeyRef = "0";
          lastKeyRef = "0";

//          addUserDeleteEventListener();
//          addListenerForNewMessages();
//          addDeleteFieldListener();
//          checkUserActivityAndSendNotifications(messageMap.getContent());

          messageSendIv.setOnClickListener(new TextMessageSenderClickListener());
          messageSendIv.setClickable(true);

        }).addOnFailureListener(e -> {

          Toast.makeText(view.getContext(),
                  R.string.message_send_failed, Toast.LENGTH_SHORT).show();

          messageSendIv.setClickable(true);

        });

//        }
      } else {
        Toast.makeText(view.getContext(),
                R.string.message_send_empty, Toast.LENGTH_SHORT).show();
      }
    }
  }

  private class TextMessageSenderClickListener implements View.OnClickListener{
    @Override
    public void onClick(View view) {

      final String content = messagingEd.getText().toString();

      if (!content.equals("")) {

//        if (WifiUtil.checkWifiConnection(view.getContext())) {
        sendMessage(content,Files.TEXT,null);

//        }
      } else {
        Toast.makeText(view.getContext(),
                R.string.message_send_empty, Toast.LENGTH_SHORT).show();
      }


    }
  }

  private DatabaseReference sendMessage(String content, int type,String attachmentUrl){

    messagingEd.setText("");
    messagingEd.setClickable(false);

    PrivateMessage messageMap = new PrivateMessage(content,
            System.currentTimeMillis()/1000, currentUid, type);

    if(attachmentUrl!=null){
      messageMap.setAttachmentUrl(attachmentUrl);

    }
    final DatabaseReference childRef =  currentMessagingRef.child("messages")
            .child(String.valueOf(Integer.parseInt(lastKeyRef) + 1));

    childRef.setValue(messageMap).addOnSuccessListener(v -> {

//            checkUserActivityAndSendNotifications(messageMap.getContent());
      messageSendIv.setClickable(true);

    }).addOnFailureListener(e -> {

      Toast.makeText(this, R.string.message_send_failed, Toast.LENGTH_SHORT).show();

      messageSendIv.setClickable(true);

    });

    return childRef;
  }

//  private class ImageMessageClickListener implements View.OnClickListener{
//    @Override
//    public void onClick(View view) {
//
//    }
//  }
  @Override
  public void onClick(View view) {

    if(view.getId() == R.id.messageAttachIv){

      showMessageOptionsBottomSheet();

    }

  }

  private void showMessageOptionsBottomSheet(){

    final BottomSheetDialog bsd = new BottomSheetDialog(this,R.style.SheetDialog);
    final View parentView = getLayoutInflater().inflate(R.layout.message_options_bsd,null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.imageIv).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();

        files = new Files(PrivateMessagingActivity.this);

        files.startImageFetchIntent();

      }
    });

    bsd.setContentView(parentView);
    bsd.show();

  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == Files.PICK_IMAGE){

      if(resultCode == RESULT_OK  && data != null){

        showFullScreenFragment(data.getData());

      }else{
        //problem with image retrieving


      }

    }

  }

  public void sendFileMessage(Uri uri,int fileType,String message){

    String storageRef = null;
//
//    final DatabaseReference lastChildRef = sendMessage(message,fileType);
    messageAttachmentUploadedIndex = privateMessages.size();

     privateMessages.add(new PrivateMessage(message,
             System.currentTimeMillis()/1000, currentUid, fileType));


     adapter.notifyItemInserted(privateMessages.size());
    scrollToBottom();

    switch (fileType){
      case Files.IMAGE:
        storageRef = Files.MESSAGE_IMAGE_REF;
      break;
    }

    final StorageReference reference = FirebaseStorage.getInstance().getReference()
            .child(storageRef + uri.getPath() + System.currentTimeMillis()/1000);

    reference.putFile(uri).continueWithTask(task -> {
      if (!task.isSuccessful()) {
        throw task.getException();
      }

      return reference.getDownloadUrl();
    }).addOnCompleteListener(task -> {
      if (task.isSuccessful()) {

//        privateMessages.get(messageAttachmentUploadedIndex)
//                .setAttachmentUrl(task.getResult().toString());

        sendMessage(message,fileType,task.getResult().toString());

//        lastChildRef.getRef().child("attachmentUrl").setValue(task.getResult().toString());
//        sendMessage(message,fileType,task.getResult().toString());

      } else {
        // Handle failures
        // ...
      }
    });


  }


  private void addFileMessageUploadListener(DatabaseReference attachmentRef,
                                            int index){

    attachmentRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {

        Log.d(TAG,"ATTACHMETN CAHNGED");

        if(snapshot.exists()){
          final String url = snapshot.getValue(String.class);
          if(url!=null){
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == Files.EXTERNAL_STORAGE_PERMISSION &&
            grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

      files.startImageFetchIntent();

      }else{

      files = null;

    }
  }


  private void showFullScreenFragment(Uri imageUri){

    pickerFrameLayout.setVisibility(View.VISIBLE);

    getSupportFragmentManager().beginTransaction().replace(pickerFrameLayout.getId(),
            new FullscreenFragment2(imageUri),"fullScreen").commit();

  }

  private void dismissFullScreenFragment(){

    if(pickerFrameLayout.getVisibility() == View.VISIBLE){
      pickerFrameLayout.setVisibility(View.GONE);

      getSupportFragmentManager().beginTransaction().remove(
              getSupportFragmentManager().findFragmentByTag("fullScreen")).commit();

    }
  }

  private void getMoreTopMessages(){

    currentMessagingRef
            .child("messages")
            .orderByKey()
            .limitToLast(MESSAGES_PAGE_SIZE)
            .endAt(String.valueOf(Integer.parseInt(firstKeyRef)-1))
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                final List<PrivateMessage> newMessages = new ArrayList<>();

                for(DataSnapshot child:snapshot.getChildren()){
                  newMessages.add(child.getValue(PrivateMessage.class));

                }

                privateMessages.addAll(0,newMessages);
                adapter.notifyItemRangeInserted(0,newMessages.size());


                firstKeyRef = String.valueOf(Integer.parseInt(lastKeyRef)
                        - privateMessages.size());

                messagesProgressBar.setVisibility(View.GONE);

                if(newMessages.size() < MESSAGES_PAGE_SIZE){
                  Log.d(TAG,"removing scorll lsitener");
                  privateMessagingRv.removeOnScrollListener(currentScrollListener);
                }

                isLoadingMessages = false;
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });
  }

  private class toTopScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

      int firstVisible = ((LinearLayoutManager)recyclerView.getLayoutManager())
              .findFirstCompletelyVisibleItemPosition();

      if (!isLoadingMessages &&  (firstVisible == 0 || firstVisible == -1) &&
              !recyclerView.canScrollVertically(-1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d(TAG,"is at top man");

          isLoadingMessages = true;
          messagesProgressBar.setVisibility(View.GONE);
          getMoreTopMessages();

      }
    }
  }

  @Override
  public void deleteMessage(PrivateMessage message, DialogInterface dialog) {

    final String id =
            String.valueOf(Integer.parseInt(firstKeyRef) + privateMessages.indexOf(message));

    currentMessagingRef.child("messages").child(id).child("deleted").setValue(true).
            addOnSuccessListener(v -> currentMessagingRef.child("lastDeleted").setValue(id)
                    .addOnSuccessListener(vo -> dialog.dismiss()).addOnFailureListener(e ->
                            dialog.dismiss())).addOnFailureListener(e -> {
      dialog.dismiss();

      Toast.makeText(this, "لقد فشل حذف الرسالة", Toast.LENGTH_SHORT).show();

      Log.d("ttt","failed: "+e.getMessage());
    });

  }


  private void scrollToBottom(){
    privateMessagingRv.post(() ->
            privateMessagingRv.smoothScrollToPosition(privateMessages.size()-1));
  }
  @Override
  public void onDestroy() {
    super.onDestroy();
//    sharedPreferences.edit()
//            .remove("isPaused")
//            .remove("currentMessagingUserId")
//            .remove("currentMessagingPromoId").apply();

    if(childEventListeners!=null && !childEventListeners.isEmpty()){
      for(DatabaseReference reference: childEventListeners.keySet()){
        reference.removeEventListener(Objects.requireNonNull(childEventListeners.get(reference)));
      }
    }
    if(valueEventListeners!=null && !valueEventListeners.isEmpty()){
      for(DatabaseReference reference: valueEventListeners.keySet()){
        reference.removeEventListener(Objects.requireNonNull(valueEventListeners.get(reference)));
      }
    }
//
//    if(promotionDeleteReceiver!=null){
//      unregisterReceiver(promotionDeleteReceiver);
//    }

  }

  @Override
  public void onBackPressed() {

    if(pickerFrameLayout.getVisibility() == View.VISIBLE){
      dismissFullScreenFragment();
    }else{
      super.onBackPressed();
    }

  }

}