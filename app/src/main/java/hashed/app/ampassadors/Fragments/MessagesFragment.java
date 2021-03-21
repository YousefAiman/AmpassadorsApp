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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Adapters.ChatsAdapter;
import hashed.app.ampassadors.Objects.ChatItem;
import hashed.app.ampassadors.Objects.PrivateMessagePreview;
import hashed.app.ampassadors.R;

public class MessagesFragment extends Fragment {

  //database
  private static final DatabaseReference messagesRef =
          FirebaseDatabase.getInstance().getReference("PrivateMessages");
  private static final CollectionReference messagesCollectionRef =
          FirebaseFirestore.getInstance().collection("PrivateMessages");
  //views
  private RecyclerView chatsRv;
  private ProgressBar progressBar;
//  private boolean isLoadingMessages = false;
//  private static final int MESSAGE_PAGE = 10;
//  private DocumentSnapshot lastDocumentSnapshot;
//  private DocumentSnapshot firstDocumentSnapshot;
//  private ChatsScrollListener scrollListener;
  private TextView noMessagesTv;
  //chats
  private ArrayList<ChatItem> chatItems;
  private List<ListenerRegistration> listenerRegistrations;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;

  //adapter
  private String currentUid;
  private ChatsAdapter adapter;


  public MessagesFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    chatItems = new ArrayList<>();
    adapter = new ChatsAdapter(chatItems, currentUid, getContext());

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_recycler_child, container, false);
    chatsRv = view.findViewById(R.id.childRv);
    noMessagesTv = view.findViewById(R.id.emptyTv);
    progressBar = view.findViewById(R.id.progressBar);
    noMessagesTv.setText(R.string.no_current_messages);

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
        if (itemCount == 0) {
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

//    isLoadingMessages = true;
    Query query = messagesCollectionRef.whereArrayContains("users", currentUid)
            .orderBy("latestMessageTime", Query.Direction.DESCENDING);

//    if(lastDocumentSnapshot!=null){
//      query = query.startAfter(lastDocumentSnapshot);
//    }

//    final Query finalQuery = query;
    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        valueEventListeners = new HashMap<>();
//        if(isInitial){
//          valueEventListeners = new HashMap<>();
//        }

        final List<DocumentSnapshot> docs = snapshots.getDocuments();

        if (docs.isEmpty()) {
          return;
        }


//        lastDocumentSnapshot = docs.get(0);
//        firstDocumentSnapshot = docs.get(docs.size()-1);

        for (int i = 0; i < docs.size(); i++) {

          if (!docs.get(i).contains("databaseRefId")) {
            return;
          }

          getMessageFromSnapshot(docs.get(i), false);

          Log.d("ttt", "snapshot: " + docs.get(i).getId());


        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {



        if (task.isSuccessful() && task.getResult() != null) {

          addNewMessagesListener();

//          if(task.getResult().size() == MESSAGE_PAGE){
//            chatsRv.addOnScrollListener(scrollListener = new ChatsScrollListener());
//          }else if(scrollListener!=null){
//
//            chatsRv.removeOnScrollListener(scrollListener);
//          }
//
//
//          if(isInitial){
//            addNewMessagesListener();
//
//            if(task.getResult().size() == MESSAGE_PAGE){
//              chatsRv.addOnScrollListener(scrollListener = new ChatsScrollListener());
//            }
//
//          }else if(task.getResult().size() < MESSAGE_PAGE){
//
//            chatsRv.removeOnScrollListener(scrollListener);
//
//          }


        }



//        if(task.isSuccessful() && task.getResult()!=null){
//
//          if(isInitial){
//
//
//          }
//
//
//
//        }else if(scrollListener!=null){
//        }

      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        progressBar.setVisibility(View.GONE);
      }
    });

  }


  private void getMessageFromSnapshot(DocumentSnapshot snapshot, boolean addToFirst) {

    final ChatItem chatItem = new ChatItem();
    chatItem.setMessagingDocId(snapshot.getString("databaseRefId"));
    final String[] ids = chatItem.getMessagingDocId().split("-");
    chatItem.setMessagingUid(ids[0].equals(currentUid) ? ids[1] : ids[0]);
    getLastMessage(chatItem, addToFirst);

  }

  private void getLastMessage(ChatItem chatItem, boolean addToFirst) {

    final com.google.firebase.database.Query reference =
            messagesRef.child(chatItem.getMessagingDocId()).child("messages")
                    .orderByKey().limitToLast(1);

    valueEventListeners.put(reference.getRef(),
            reference.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {

                  Log.d("ttt", "last message value changed: " + snapshot.getKey()
                          + " child count: " + snapshot.getChildrenCount());

                  final DataSnapshot ref = snapshot.getChildren().iterator().next();

                  Log.d("ttt", "ref.key: " + ref.getKey());

                  if (chatItem.getMessage() == null) {

                    Log.d("ttt", "no message so adding it");

                    chatItem.setMessageKey(Long.parseLong(ref.getKey()));

                    checkMessageSeenAndAddSeenListener(ref, chatItem, addToFirst);

                  } else {

                    Log.d("ttt", "looking for chat item");
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

                    if (index == 0) {

                      adapter.notifyItemChanged(index);

                    } else {

//                      Collections.swap(chatItems,index,0);
//                      adapter.notifyItemMoved(index,0);

                      chatItems.remove(index);
                      adapter.notifyItemRemoved(index);
                      chatItems.add(0, chatItem1);
                      adapter.notifyItemInserted(0);

                    }

//                    Log.d("ttt","has message so updating");
//
////            if(chatItem.getMessage().getDeleted()){
////              return;
////            }
//                    if(ref.child("deleted").getValue(Boolean.class)){
//
//                      Log.d("ttt","has message so updating");
//
//                      chatItems.get(index).getMessage().setDeleted(true);
//                      adapter.notifyItemChanged(index);
//
//                    }else{
//
//
//                      chatItems.get(index).setMessage(
//                              ref.getValue(PrivateMessagePreview.class));
//
//                      if(index == 0){
//                        adapter.notifyItemChanged(index);
//                      }else{
//
//                        Collections.swap(chatItems,index,0);
//                        adapter.notifyItemMoved(index,0);
//
//                      }
//                    }
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
            }));

//    messagesRef.child(chatItem.getMessagingDocId()).child("messages").orderByKey()
//            .limitToLast(1).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
//      @Override
//      public void onSuccess(DataSnapshot snapshot) {
//
//        if(!snapshot.exists() || snapshot.getChildrenCount() == 0){
//          return;
//        }
//
//        final DataSnapshot message = snapshot.getChildren().iterator().next();
//
//
//        final DatabaseReference lastSeenRef=
//                messagesRef.child(chatItem.getMessagingDocId()).child("LastSeenMessage:"+currentUid);
//
//        ValueEventListener listener;
//        lastSeenRef.addValueEventListener(listener = new ValueEventListener() {
//                  @Override
//                  public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    if(snapshot.exists()){
//                      chatItem.setSeen(message.getKey() == null ||
//                              Long.parseLong(snapshot.getValue(String.class))
//                                      <= Long.parseLong(message.getKey()));
//                    }
//
//                  }
//
//                  @Override
//                  public void onCancelled(@NonNull DatabaseError error) {
//
//                  }
//                });
//
//        valueEventListeners.put(lastSeenRef,listener);
//
//
//        Log.d("ttt","snapshot: "+snapshot.getKey());
//        chatItem.setMessage(message.getValue(PrivateMessagePreview.class));
//      }
//    }).addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//      @Override
//      public void onComplete(@NonNull Task<DataSnapshot> task) {
//
//
//        if(task.isSuccessful()){
//          if(forUpdate){
//
//            if(index == 0){
//              adapter.notifyItemChanged(index);
//            }else{
//              Collections.swap(chatItems,index,0);
//              adapter.notifyItemMoved(index,0);
//            }
//
//          }else{
//            chatItems.add(chatItem);
//            adapter.notifyItemInserted(chatItems.size());
//          }
//
//        }
//
//      }
//    });

  }

  private void checkMessageSeenAndAddSeenListener(DataSnapshot messageSnapshot,
                                                  ChatItem chatItem,
                                                  boolean addToFirst) {

    DatabaseReference lastSeenRef =
            messagesRef.child(chatItem.getMessagingDocId()).child("LastSeenMessage:" + currentUid);

    ValueEventListener listener;

    lastSeenRef.addValueEventListener(listener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {

          Log.d("ttt", "last seen changed");
          if (!chatItems.contains(chatItem)) {

            Log.d("ttt", "!chatItems.contains(chatItem)");
            chatItem.setSeen(Long.parseLong(snapshot.getValue(String.class)) >=
                    chatItem.getMessageKey());

            Log.d("ttt", "chatItem.getseen: " +
                    chatItem.isSeen());

            chatItem.setMessage(messageSnapshot.
                    getValue(PrivateMessagePreview.class));

            if (addToFirst) {

              chatItems.add(0, chatItem);

              adapter.notifyItemInserted(0);


            } else {
              chatItems.add(chatItem);

              adapter.notifyItemInserted(chatItems.size());
            }


            if(progressBar.getVisibility() == View.VISIBLE){
              progressBar.setVisibility(View.GONE);
            }

          } else {

            Log.d("ttt", "chatItems.contains(chatItem)");

            int index = 0;
            for (int i = 0; i < chatItems.size(); i++) {
              if (chatItems.get(i).getMessagingDocId().equals(chatItem.getMessagingDocId())) {
                index = i;
                break;
              }
            }

            ChatItem chatItem1 = chatItems.get(index);

            boolean wasSeen = chatItem1.isSeen();

            chatItem1.setSeen(Long.parseLong(snapshot.getValue(String.class)) >=
                    chatItem1.getMessageKey());

            Log.d("ttt", "chatItems.get(position),getseen: " +
                    chatItem1.isSeen());

            if ((chatItem1.isSeen() && !wasSeen)
                    || (!chatItem1.isSeen() && wasSeen)) {

              adapter.notifyItemChanged(index);

            }

          }

        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
      }
    });

    valueEventListeners.put(lastSeenRef, listener);

  }


  private void addMessageChangeListener(Query query) {

    if (listenerRegistrations == null) {
      listenerRegistrations = new ArrayList<>();
    }

//    query = query.whereGreaterThan("latestMessageTime",
//            System.currentTimeMillis());

    listenerRegistrations.add(
            query.addSnapshotListener((value, error) -> {

              Log.d("ttt", "something changed");
              if (value != null) {
                for (DocumentChange dc : value.getDocumentChanges()) {
                  Log.d("ttt", "doc id: " + dc.getDocument().getId());
                  if (dc.getType() == DocumentChange.Type.MODIFIED
//                  ||
//          dc.getType() == DocumentChange.Type.ADDED
                  ) {
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


//    messagesCollectionRef.document(documentRef).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//      @Override
//      public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//        if(value!=null && value.exists()){
//          final long newLatestMessageTime = value.getLong("latestMessageTime");
//          if(newLatestMessageTime > chatItems.get(index).getTime()){
//           getLastMessage(chatItems.get(index),documentRef,true,index);
//          }
//        }
//      }
//    });

  }

  private void addNewMessagesListener() {

    if (listenerRegistrations == null) {
      listenerRegistrations = new ArrayList<>();
    }

    final Query query = messagesCollectionRef
            .whereArrayContains("users", currentUid)
            .orderBy("latestMessageTime", Query.Direction.DESCENDING)
            .whereGreaterThan("latestMessageTime", System.currentTimeMillis());

    listenerRegistrations.add(
            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value,
                                  @Nullable FirebaseFirestoreException error) {

                if (value != null) {

                  for (DocumentChange dc : value.getDocumentChanges()) {

                    if (dc.getType() == DocumentChange.Type.ADDED) {


                      for (int i = 0; i < chatItems.size(); i++) {

                        if (chatItems.get(i).getMessagingDocId().equals(dc.getDocument()
                                .getString("databaseRefId"))) {
                          return;
                        }

                        if (i == chatItems.size() - 1) {

                          getMessageFromSnapshot(dc.getDocument(), true);

                        }
                      }

                      Log.d("ttt", "added a message after time: " +
                              dc.getDocument().getId());

//              getMessageFromSnapshot(dc.getDocument());

                    }
                  }
                }
              }
            }));

  }

//  private class ChatsScrollListener extends RecyclerView.OnScrollListener {
//    @Override
//    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//      super.onScrollStateChanged(recyclerView, newState);
//
////      int firstVisible = ((LinearLayoutManager)recyclerView.getLayoutManager())
////              .findFirstCompletelyVisibleItemPosition();
//
//      if (!isLoadingMessages &&
//              !recyclerView.canScrollVertically(1) &&
//              newState == RecyclerView.SCROLL_STATE_IDLE) {
//
//
//        getAllChats(false);
//
//
//      }
//    }
//  }


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
}