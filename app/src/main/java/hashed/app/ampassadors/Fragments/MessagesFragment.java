package hashed.app.ampassadors.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Adapters.ChatsAdapter;
import hashed.app.ampassadors.Objects.ChatItem;
import hashed.app.ampassadors.Objects.PrivateMessagePreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class MessagesFragment extends Fragment{

  private static final int ADD_TYPE = 0,UPDATE_TYPE = 1,MOVE_TOP_FIRST_TYPE = 2,
  MESSAGE_PAGE_LIMIT = 8;

  //database
  private final DatabaseReference messagesRef =
          FirebaseDatabase.getInstance().getReference("PrivateMessages");
  private final CollectionReference messagesCollectionRef =
          FirebaseFirestore.getInstance().collection("PrivateMessages");
  private Query mainQuery;
  private int initialCount,previousSize;
  private boolean isLoadingMoreMessages;
  private DocumentSnapshot lastDocSnapshot;
  private ScrollListener scrollListener;

  //views
  private RecyclerView chatsRv;
  private ProgressBar progressBar;
  private TextView noMessagesTv;


  //chats
  private ArrayList<ChatItem> chatItems;
  private List<ListenerRegistration> listenerRegistrations;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;

  //adapter
  private String currentUid;
  private ChatsAdapter adapter;

  private int fetchedItems = 0;

  public MessagesFragment() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    chatItems = new ArrayList<>();
    adapter = new ChatsAdapter(chatItems, currentUid, getContext());
    valueEventListeners = new HashMap<>();
    listenerRegistrations = new ArrayList<>();

    mainQuery = messagesCollectionRef.whereArrayContains("users", currentUid)
            .whereLessThan("latestMessageTime", System.currentTimeMillis())
            .orderBy("latestMessageTime", Query.Direction.DESCENDING)
            .limit(MESSAGE_PAGE_LIMIT);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_messages, container, false);
    chatsRv = view.findViewById(R.id.childRv);
    noMessagesTv = view.findViewById(R.id.emptyTv);
    progressBar = view.findViewById(R.id.progressBar);


    chatsRv.setLayoutManager(new LinearLayoutManager(getContext(),
            RecyclerView.VERTICAL, false) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.21);
        return true;
      }


      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        if (chatItems.size() == 0) {
          noMessagesTv.setVisibility(View.VISIBLE);
          chatsRv.setVisibility(View.INVISIBLE);
        }
      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);

        if (chatsRv.getVisibility() == View.INVISIBLE) {
          noMessagesTv.setVisibility(View.GONE);
          chatsRv.setVisibility(View.VISIBLE);
        }
      }
    });

    chatsRv.setAdapter(adapter);

    return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getAllChats();

  }

  private void getAllChats() {

    isLoadingMoreMessages = true;
    if(progressBar.getVisibility() == View.GONE){
      progressBar.setVisibility(View.VISIBLE);
    }

    Query query = mainQuery;
    if(lastDocSnapshot !=null){
      query = query.startAfter(lastDocSnapshot);
    }

    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if(!snapshots.isEmpty()){


          initialCount = snapshots.size();
          Log.d("gettingMessages","queryCount: "+initialCount);
          for(DocumentSnapshot snapshot:snapshots){
            getMessageFromSnapshot(snapshot, false);
          }
          lastDocSnapshot = snapshots.getDocuments().get(initialCount-1);
        }else{

          if(lastDocSnapshot == null){
            noMessagesTv.setVisibility(View.VISIBLE);
          }

          if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
          }

          if(scrollListener!=null){
            chatsRv.removeOnScrollListener(scrollListener);
          }
        }
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        if(progressBar.getVisibility() == View.VISIBLE){
          progressBar.setVisibility(View.GONE);
        }

        if(lastDocSnapshot == null){
          noMessagesTv.setVisibility(View.VISIBLE);
        }

      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        addNewMessagesListener();
      }
    });

  }


  private void getMessageFromSnapshot(DocumentSnapshot snapshot, boolean addToFirst) {

    final ChatItem chatItem = new ChatItem();
    chatItem.setMessagingDocId(snapshot.getString("databaseRefId"));
    final String[] ids = chatItem.getMessagingDocId().split("-");

    final List<String> users = (List<String>) snapshot.get("users");

    if(users!=null && users.size() <= 2){
      chatItem.setMessagingUid(ids[0].equals(currentUid) ? ids[1] : ids[0]);
      chatItem.setGroupMessage(false);
    }else{
      chatItem.setMessagingUid(snapshot.getId());
      chatItem.setGroupMessage(true);
    }

    getLastMessage(chatItem, addToFirst);

    Log.d("gettingMessages","getMessageFromSnapshot: "+snapshot.getId());

  }

  private void getLastMessage(ChatItem chatItem, boolean addToFirst) {

    Log.d("gettingMessages","getLastMessage: "+chatItem.getMessagingDocId());

    final com.google.firebase.database.Query reference =
            messagesRef.child(chatItem.getMessagingDocId()).child("messages")
                    .orderByKey().limitToLast(1);

    valueEventListeners.put(reference.getRef(),
            reference.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                  Log.d("gettingMessages","onDataChange exists: "+
                          chatItem.getMessagingDocId());

                  final DataSnapshot ref = snapshot.getChildren().iterator().next();


                  if (chatItem.getMessage() == null) {

                    chatItem.setMessage(ref.getValue(PrivateMessagePreview.class));
                    chatItem.setMessageKey(Long.parseLong(ref.getKey()));

                    checkMessageSeenAndAddSeenListener(chatItem, addToFirst,
                            false);

                  } else {


                    int index = 0;
                    for (int i = 0; i < chatItems.size(); i++) {
                      if (chatItems.get(i).getMessagingDocId().equals(chatItem.getMessagingDocId())) {
                        index = i;
                        break;
                      }
                    }

                    final ChatItem chatItem1 = chatItems.get(index);

                    Log.d("ttt", "found at: " + index + " " +
                            "with messaging id: " + chatItem1.getMessagingDocId());

                    chatItem1.setMessage(ref.getValue(PrivateMessagePreview.class));

                    final DatabaseReference lastSeenRef =
                            messagesRef.child(chatItem1.getMessagingDocId()).
                                    child("UsersLastSeenMessages").
                                    child(currentUid);

                    final int finalIndex = index;
                    lastSeenRef.addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot snapshot) {

                        final String lastSeen = snapshot.getValue(String.class);

                          calculateUnseenCount(lastSeen,chatItem,
                                  finalIndex,
                                  finalIndex == 0?UPDATE_TYPE:MOVE_TOP_FIRST_TYPE,addToFirst);

                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError error) {

                      }
                    });


                  }
                }else{

                  Log.d("gettingMessages","onDataChange doesn't exist: "
                          +chatItem.getMessagingDocId());

                  chatItem.setSeen(true);
                  chatItem.setUnSeenCount(0);

                  messagesCollectionRef.document(chatItem.getMessagingDocId())
                          .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                      if(document.exists()){

                        final PrivateMessagePreview privateMessagePreview =
                                new PrivateMessagePreview();

                        final String creatorId = document.getString("creatorId");

                        privateMessagePreview.setSender(creatorId);

                        if(creatorId != null && creatorId.equals(currentUid)){

                          final List<String> users = (List<String>) document.get("users");

                          if(users!=null){
                            users.remove(currentUid);
                            privateMessagePreview.setContent("You added "+users.size()
                                    +" users to this group.");
                          }else{
                            privateMessagePreview.setContent("You added multiple" +
                                    " users to this group.");
                          }

//                                    if(users.size() == 2){
//
//                                      privateMessagePreview.setContent("You added ");
//
//                                      FirebaseFirestore.getInstance().collection("Users")
//                                              .whereIn("userId",users.size()>2?users.subList(0,2):users)
//                                              .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onSuccess(QuerySnapshot snapshots) {
//
//                                          if(!snapshots.isEmpty()){
//                                            for(DocumentSnapshot snapshot1:snapshots.getDocuments()){
//                                              privateMessagePreview.setContent(
//                                                      privateMessagePreview.getContent().concat(
//                                                              snapshot1.getString("username")+", "
//                                                      ));
//                                            }
//                                          }
//                                        }
//                                      }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                          privateMessagePreview.setContent(
//                                                  privateMessagePreview.getContent().concat(" and others to this group")
//                                          );
//                                        }
//                                      });
//
//                                    }else{
//
//                                      privateMessagePreview.setContent("You added "+users.size()
//                                      +" users to this group.");
//
//                                    }

                        }else{
                          privateMessagePreview.setContent("added you to this group");
                        }

                        privateMessagePreview.setTime(document.getLong("createdTime"));
                        privateMessagePreview.setDeleted(false);
                        privateMessagePreview.setType(Files.TEXT);
                        chatItem.setMessage(privateMessagePreview);

                        chatItem.setImageUrl(document.getString("imageUrl"));
                        chatItem.setUsername(document.getString("groupName"));
//                                  chatItems.add(chatItem);
//                                  adapter.notifyItemInserted(chatItems.size());

                        checkMessageSeenAndAddSeenListener(chatItem, addToFirst,
                                true);

                      }else{
                        Log.d("gettingMessages","database child doesn't exist: "
                        +chatItem.getMessagingDocId());
                      }
                    }
                  });
//                  int index = listenerRegistrations.size();
//                  listenerRegistrations.add(messagesCollectionRef.document(chatItem.getMessagingDocId())
//                          .addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                            @Override
//                            public void onEvent(@Nullable DocumentSnapshot value,
//                                                @Nullable FirebaseFirestoreException error) {
//                              if(value!=null){
//
//                                if(chatItem.getMessage() == null){
//
//                                }
////                                else if(value.getLong("latestMessageTime")
////                                        > chatItem.getMessage().getTime()){
////                                    listenerRegistrations.get(index).remove();
////                                    listenerRegistrations.remove(index);
////
////                                  getMessageFromSnapshot(value,true);
////
////                                }
//                              }
//
//                            }
//                          }));
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

                Log.d("gettingMessages","onCancelled: "+chatItem.getMessagingDocId()
                +" error: "+error.getMessage());

              }
            }));

  }

  private void checkMessageSeenAndAddSeenListener(ChatItem chatItem, boolean addToFirst,
                                                  boolean isAnEmptyGroupMessage) {

    Log.d("gettingMessages","checkMessageSeenAndAddSeenListener: "
            +chatItem.getMessagingDocId());

    DatabaseReference lastSeenRef =
            messagesRef.child(chatItem.getMessagingDocId())
                    .child("UsersLastSeenMessages")
                    .child(currentUid);

    ValueEventListener listener;

    lastSeenRef.addValueEventListener(listener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {

          if (!chatItems.contains(chatItem)) {

            if(isAnEmptyGroupMessage){

              if(addToFirst){
                boolean isAtTop = isAtTop();
                chatItems.add(0,chatItem);
                adapter.notifyItemInserted(0);
                if(isAtTop){
                  chatsRv.smoothScrollToPosition(0);
                }
              }else{
                chatItems.add(chatItem);
//                if(isLastMessage){
                  orderAndShowMessages();
//                }

              }
//              final int index = chatItems.size();
//              adapter.notifyItemInserted(index);

            }else{

              final String lastSeen = snapshot.getValue(String.class);

              if (addToFirst) {
                calculateUnseenCount(lastSeen,chatItem,0,
                        ADD_TYPE,true);
              } else {

                  calculateUnseenCount(lastSeen,chatItem,chatItems.size(),
                          ADD_TYPE,false);

              }
            }

            if(progressBar.getVisibility() == View.VISIBLE){
              progressBar.setVisibility(View.GONE);
            }

          } else {

            final int index = chatItems.indexOf(chatItem);
            final ChatItem chatItem1 = chatItems.get(index);
            final String lastSeen = snapshot.getValue(String.class);
            calculateUnseenCount(lastSeen,chatItem1,index,UPDATE_TYPE,false);

          }
        }else{
          initialCount--;
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        initialCount--;
      }
    });

    valueEventListeners.put(lastSeenRef, listener);

  }


  private void calculateUnseenCount(String lastSeen,ChatItem chatItem,int index, int updateType,
                                    boolean addToFirst){

    messagesRef.child(chatItem.getMessagingDocId()).child("messages").orderByKey()
            .startAt(lastSeen+1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){
                  chatItem.setUnSeenCount(snapshot.getChildrenCount());
                }else{
                  chatItem.setUnSeenCount(0);
                }
//                  final boolean wasSeen = chatItem.isSeen();

                  chatItem.setSeen(Long.parseLong(lastSeen) ==
                          chatItem.getMessageKey());

                  Log.d("ttt", "chatItems.get(position),getseen: " +
                          chatItem.isSeen());

                  if(updateType == UPDATE_TYPE){

                  adapter.notifyItemChanged(index);

                  }else if(updateType == MOVE_TOP_FIRST_TYPE){

                    boolean scrollToTop = isAtTop();

                    chatItems.remove(index);
                    adapter.notifyItemRemoved(index);
                    chatItems.add(0, chatItem);
                    adapter.notifyItemInserted(0);

                    if(scrollToTop){
                      chatsRv.smoothScrollToPosition(0);
                    }

                  }else{
//                    if(index == 0){
//                      chatItems.add(index,chatItem);
//                      adapter.notifyItemInserted(index);
//                    }else{
                    if(addToFirst){

                      boolean isAtTop = isAtTop();
                      chatItems.add(0,chatItem);
                      adapter.notifyItemInserted(0);

                      if(isAtTop){
                        chatsRv.smoothScrollToPosition(0);
                      }

                    }else{
                      chatItems.add(chatItem);
//                      if(isLastMessage){
                        orderAndShowMessages();
//                      }
                    }
//                      adapter.notifyItemInserted(chatItems.size());
//                    }

                  }

              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            });

  }
  private void addMessageChangeListener(Query query) {

    if (listenerRegistrations == null) {
      listenerRegistrations = new ArrayList<>();
    }


    listenerRegistrations.add(
            query.addSnapshotListener((value, error) -> {

              Log.d("ttt", "something changed");
              if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                  Log.d("ttt", "doc id: " + dc.getDocument().getId());
                  if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    Log.d("ttt", "MODIFIED");
                    final DocumentSnapshot ds = dc.getDocument();

                    for (int i = 0; i < chatItems.size(); i++) {

                      Log.d("ttt", "chatItems.get(i).getMessagingDocId(): " +
                              chatItems.get(i).getMessagingDocId());

                      Log.d("ttt", " ds.getId(): " + ds.getId());

                      if (chatItems.get(i).getMessagingDocId().equals(ds.getId())) {

                        Log.d("ttt", "found at: " + i);

                        if (chatItems.get(i).getMessage().getDeleted()) {
                          getLastMessage(chatItems.get(i), false);
                        } else if (ds.contains("lastMessageDeleted")) {
                          chatItems.get(i).getMessage().setDeleted(true);
                          adapter.notifyItemChanged(i);
                        }
                        break;
                      }
                    }
                  }
                }
              }
            }));

  }

  private void addNewMessagesListener() {

//    if (listenerRegistrations == null) {
//      listenerRegistrations = new ArrayList<>();
//    }

    final Query query = messagesCollectionRef
            .whereArrayContains("users", currentUid)
            .orderBy("latestMessageTime")
            .whereGreaterThan("latestMessageTime", System.currentTimeMillis());

    listenerRegistrations.add(
            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value,
                                  @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                  for (DocumentChange dc : value.getDocumentChanges()) {

                    if (dc.getType() == DocumentChange.Type.ADDED) {
                      Log.d("ttt","added to latest to users array: "+
                              dc.getDocument().getId());

                      if (!chatItems.isEmpty()) {
                        final String documentId = dc.getDocument()
                                .getString("databaseRefId");
                        for (int i = 0; i < chatItems.size(); i++) {
                          if (chatItems.get(i).getMessagingDocId()
                                  .equals(documentId)) {
                            Log.d("ttt", "this message exists so not adding");
                            return;
                          }
                        }
                        Log.d("ttt", "message doesn't exists so adding it!");
                      }
                      getMessageFromSnapshot(dc.getDocument(), true);


                    }else if(dc.getType() == DocumentChange.Type.REMOVED){
                      removeMessage(dc);
                    }
                  }
                }
              }
            }));

  }


  private void removeMessage(DocumentChange dc){
    for (int i = 0; i < chatItems.size(); i++) {

      if (chatItems.get(i).getMessagingDocId().equals(dc.getDocument()
              .getString("databaseRefId"))) {

        final ChatItem chatItem = chatItems.get(i);

        chatItems.remove(i);
        adapter.notifyItemRemoved(i);

        final com.google.firebase.database.Query reference =
                messagesRef.child(chatItem.getMessagingDocId())
                        .child("messages").orderByKey().limitToLast(1);

        reference.removeEventListener(
                Objects.requireNonNull(valueEventListeners.get(
                        reference.getRef())));

        valueEventListeners.remove(reference.getRef());

        DatabaseReference lastSeenRef =
                messagesRef.child(chatItem.getMessagingDocId())
                        .child("UsersLastSeenMessages").child(currentUid);

        lastSeenRef.removeEventListener(
                Objects.requireNonNull(valueEventListeners.get(lastSeenRef.getRef())));

        valueEventListeners.remove(lastSeenRef.getRef());

        if(chatItem.isGroupMessage()){
          int listenerIndex = listenerRegistrations.size();
          listenerRegistrations.add(

                  messagesCollectionRef.whereEqualTo("groupId",chatItem.getMessagingDocId())
                  .whereArrayContains("users",currentUid)
                  .limit(1)
                  .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                      if(value!=null && !value.getDocumentChanges().isEmpty()){
                        if(value.getDocumentChanges().get(0).getType().equals(DocumentChange.Type.ADDED)){

                                final long latestMessageTime =
                                        dc.getDocument().getLong("latestMessageTime");

                                List<Long> chatItemsTimesList = new ArrayList<>();

                                for(ChatItem chatItem1:chatItems){
                                  chatItemsTimesList.add(chatItem1.getTime());
                                }

                            chatItemsTimesList.add(latestMessageTime);

                                Collections.sort(chatItemsTimesList,(c1,c2) ->
                                        Long.compare(c2,c1));

                                int index = chatItemsTimesList.indexOf(latestMessageTime);

                                Log.d("ttt","needs to be added at: "+index);

                                getMessageFromSnapshot(dc.getDocument(),false);

                          listenerRegistrations.get(listenerIndex).remove();
                          listenerRegistrations.remove(listenerIndex);

                        }
                      }
                    }
                  })
          );
        }


        break;
      }
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (listenerRegistrations != null && !listenerRegistrations.isEmpty()) {
      for (ListenerRegistration listener : listenerRegistrations) {
        listener.remove();
      }
    }

    if (valueEventListeners != null && !valueEventListeners.isEmpty()) {
      for (DatabaseReference reference : valueEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(valueEventListeners.get(reference)));
      }
    }

  }


  private void orderAndShowMessages(){

    if(chatItems.size() == initialCount){

      chatsRv.setVisibility(View.VISIBLE);

      Collections.sort(chatItems, (c1, c2) ->
              Long.compare(c1.getTime(), c2.getTime()));

      adapter.notifyDataSetChanged();


      addDeletionListener();

      chatsRv.addOnScrollListener(scrollListener = new ScrollListener());

    }else if(chatItems.size() == (previousSize + initialCount)-1){

      Log.d("ttt","need to update adapter");

      Collections.sort(chatItems.subList(previousSize,chatItems.size()), (c1, c2) ->
              Long.compare(c1.getTime(), c2.getTime()));

      Log.d("ttt","item range inserted: "
      +previousSize + "-" + (chatItems.size() - previousSize));

      adapter.notifyItemRangeInserted(previousSize,chatItems.size() - previousSize);
    }else{
      Log.d("ttt","ordering messages: "+chatItems.size()
              + "-" + (previousSize + initialCount));
    }

    if(progressBar.getVisibility() == View.VISIBLE){
      progressBar.setVisibility(View.GONE);
    }

    previousSize = chatItems.size();
    isLoadingMoreMessages = false;
  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingMoreMessages && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {
        Log.d("ttt","scrolled to bottom");
        getAllChats();
      }
    }
  }

//  private int getIndexOrderedByTime(long time){
//
//    for(ChatItem chatItem1:chatItems){
//      if(time > chatItem1.getTime()){
//        return chatItems.indexOf(chatItem1);
//      }
//    }
//
//    return chatItems.size();
//  }
//
//

  private void addDeletionListener(){
    listenerRegistrations.add(
            messagesCollectionRef.whereArrayContains("users", currentUid)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                      @Override
                      public void onEvent(@Nullable QuerySnapshot value,
                                          @Nullable FirebaseFirestoreException error) {
                        if(value!=null){

                          for(DocumentChange dc:value.getDocumentChanges()){
                            if(dc.getType() == DocumentChange.Type.REMOVED){
                              removeMessage(dc);
                            }
                          }
                        }
                      }
                    })
    );

  }

  private boolean isAtTop(){
    if(chatsRv.getLayoutManager()!=null){
      return ((LinearLayoutManager) chatsRv.getLayoutManager())
              .findFirstCompletelyVisibleItemPosition() == 0;
    }
    return false;
  }
}