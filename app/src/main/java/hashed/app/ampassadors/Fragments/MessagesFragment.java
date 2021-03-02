package hashed.app.ampassadors.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.FirebaseFirestore;
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

public class MessagesFragment extends Fragment{

  //views
  private RecyclerView chatsRv;
  private TextView noMessagesTv;

  //chats
  private ArrayList<ChatItem> chatItems;
  private boolean isLoadingMessages = false;
  private static final int MESSAGE_PAGE = 10;
  private DocumentSnapshot lastDocumentSnapshot;
  private ChatsScrollListener scrollListener;

  //database
  private static final DatabaseReference messagesRef =
          FirebaseDatabase.getInstance().getReference("PrivateMessages");

  private static final CollectionReference messagesCollectionRef =
          FirebaseFirestore.getInstance().collection("PrivateMessages");

  private List<ListenerRegistration> snapshots;
  private Map<DatabaseReference, ValueEventListener> valueEventListeners;

  //adapter
  private String currentUid;
  private ChatsAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    chatItems = new ArrayList<>();
    adapter = new ChatsAdapter(chatItems,currentUid,getContext());

  }

  public MessagesFragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view  =  inflater.inflate(R.layout.fragment_recycler_child, container, false);
    chatsRv = view.findViewById(R.id.childRv);
    noMessagesTv = view.findViewById(R.id.emptyTv);
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
        if (itemCount == 0){
          noMessagesTv.setVisibility(View.VISIBLE);
          chatsRv.setVisibility(View.INVISIBLE);
        }
      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);

        if(chatsRv.getVisibility() == View.INVISIBLE){

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

    getMoreChats(true);

  }


  private void getMoreChats(boolean isInitial){

    isLoadingMessages = true;
    Query query = messagesCollectionRef.whereArrayContains("users",currentUid)
            .orderBy("latestMessageTime", Query.Direction.DESCENDING).limit(MESSAGE_PAGE);

    if(lastDocumentSnapshot!=null){
      query = query.startAfter(lastDocumentSnapshot);
    }

    final Query finalQuery = query;
    query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        if(!snapshots.getDocuments().isEmpty()){
          lastDocumentSnapshot = snapshots.getDocuments().get(snapshots.size()-1);
        }


        if(isInitial){
          valueEventListeners = new HashMap<>();
        }

        for(DocumentSnapshot snapshot:snapshots){


          if(snapshot == null || !snapshot.contains("databaseRefId")){
            return;
          }

          Log.d("ttt","snapshot: "+snapshot.getId());

          ChatItem chatItem = new ChatItem();
          chatItem.setMessagingDocId(snapshot.getString("databaseRefId"));

          final String[] ids = chatItem.getMessagingDocId().split("-");


          chatItem.setMessagingUid(ids[0].equals(currentUid)?ids[1]:ids[0]);

          getLastMessage(chatItem,false,0);

        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {


        addMessageChangeListener(finalQuery);


        if(task.isSuccessful() && task.getResult()!=null &&
                task.getResult().size() == MESSAGE_PAGE){


          if(isInitial){

            chatsRv.addOnScrollListener(scrollListener = new ChatsScrollListener());
          }

        }else{

          if(scrollListener!=null){
            chatsRv.removeOnScrollListener(scrollListener);
          }

        }

      }
    });

  }

  private void getLastMessage(ChatItem chatItem,boolean forUpdate,int index){

    messagesRef.child(chatItem.getMessagingDocId()).child("messages").orderByKey().limitToLast(1).get().
            addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
      @Override
      public void onSuccess(DataSnapshot snapshot) {

        if(snapshot.getChildrenCount() < 0){
          return;
        }

        final DataSnapshot message = snapshot.getChildren().iterator().next();



        final DatabaseReference lastSeenRef=
                messagesRef.child(chatItem.getMessagingDocId()).child("LastSeenMessage:"+currentUid);

        ValueEventListener listener;
        lastSeenRef.addValueEventListener(listener = new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if(snapshot.exists()){
                      chatItem.setSeen(message.getKey() == null ||
                              Long.parseLong(snapshot.getValue(String.class))
                                      <= Long.parseLong(message.getKey()));
                    }

                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {

                  }
                });

        valueEventListeners.put(lastSeenRef,listener);


        Log.d("ttt","snapshot: "+snapshot.getKey());
        chatItem.setMessage(message.getValue(PrivateMessagePreview.class));
      }
    }).addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DataSnapshot> task) {


        if(task.isSuccessful()){
          if(forUpdate){

            if(index == 0){
              adapter.notifyItemChanged(index);
            }else{
              Collections.swap(chatItems,index,0);
              adapter.notifyItemMoved(index,0);
            }

          }else{
            chatItems.add(chatItem);
            adapter.notifyItemInserted(chatItems.size());
          }

        }

      }
    });


  }

  private void addMessageChangeListener(Query query){

      if(snapshots == null){
        snapshots = new ArrayList<>();
      }

    query = query.whereGreaterThan("latestMessageTime",
            System.currentTimeMillis()/1000);

    snapshots.add(
    query.addSnapshotListener((value, error) -> {

      Log.d("ttt","something changed");
      if(value!=null){
        for(DocumentChange dc:value.getDocumentChanges()){
          Log.d("ttt","doc id: "+dc.getDocument().getId());
          if(dc.getType() == DocumentChange.Type.MODIFIED ||
          dc.getType() == DocumentChange.Type.ADDED){
            Log.d("ttt","MODIFIED");
            final DocumentSnapshot ds = dc.getDocument();

            for(int i=0;i<chatItems.size();i++){

              Log.d("ttt","chatItems.get(i).getMessagingDocId(): "+
                      chatItems.get(i).getMessagingDocId());

              Log.d("ttt"," ds.getId(): "+ds.getId());

              if(chatItems.get(i).getMessagingDocId().equals(ds.getId())){

                Log.d("ttt","found at: "+i);

                getLastMessage(chatItems.get(i),true,i);

//                  final long newLatestMessageTime = ds.getLong("latestMessageTime");
//
//                  if(newLatestMessageTime > chatItems.get(i).getTime()){
//                    getLastMessage(chatItems.get(i),true,i);
//                  }

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

  private class ChatsScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

//      int firstVisible = ((LinearLayoutManager)recyclerView.getLayoutManager())
//              .findFirstCompletelyVisibleItemPosition();

      if (!isLoadingMessages &&
              !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d("ttt","is at bottom man");

        getMoreChats(false);


      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if(snapshots!=null && !snapshots.isEmpty()){
      for(ListenerRegistration listener:snapshots){
        listener.remove();
      }
    }

    if(valueEventListeners!=null && !valueEventListeners.isEmpty()){
      for(DatabaseReference reference: valueEventListeners.keySet()){
        reference.removeEventListener(Objects.requireNonNull(valueEventListeners.get(reference)));
      }
    }

  }
}