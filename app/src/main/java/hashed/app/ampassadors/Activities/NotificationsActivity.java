package hashed.app.ampassadors.Activities;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.List;

import hashed.app.ampassadors.Adapters.NotificationsAdapter;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.Objects.Notification;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.WifiUtil;

public class NotificationsActivity extends AppCompatActivity implements
        NotificationsAdapter.NotificationDeleter ,SwipeRefreshLayout.OnRefreshListener{

  private final int NOTIFICATIONS_LIMIT = 10;
  private ArrayList<Notification> newerNotifications;
  private NotificationsAdapter newerAdapter;
//  private NotificationsAdapter olderAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

  private List<ListenerRegistration> listenerRegistrationList;
  private String currentUserId;
  private Query mainQuery;
  private DocumentSnapshot lastDocSnapshot;
  private RecyclerView newestNotificationsRv;
  private ScrollListener scrollListener;
  private boolean isLoadingNotifications;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notifications);

    currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    setupToolbar();

    newestNotificationsRv = findViewById(R.id.newestNotificationsRv);
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

    swipeRefreshLayout.setOnRefreshListener(this);
    listenerRegistrationList = new ArrayList<>();

    newerNotifications = new ArrayList<>();

    newerAdapter = new NotificationsAdapter(newerNotifications,
            NotificationsAdapter.TYPE_NEW,this);

    final ItemTouchHelper itemTouchHelper = new
            ItemTouchHelper(new SwipeToDeleteNotificationCallback());
    itemTouchHelper.attachToRecyclerView(newestNotificationsRv);


    newestNotificationsRv.setAdapter(newerAdapter);


    mainQuery = FirebaseFirestore.getInstance().collection("Notifications")
            .orderBy("timeCreated", Query.Direction.DESCENDING)
            .whereLessThan("timeCreated",System.currentTimeMillis())
            .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid())
    .limit(NOTIFICATIONS_LIMIT);



    getNotifications(true);
  }


  private void getNotifications(boolean isInitial) {

    isLoadingNotifications = true;
    swipeRefreshLayout.setRefreshing(true);

    Query query = mainQuery;
    if(!isInitial && lastDocSnapshot!=null){
      query = mainQuery.startAfter(lastDocSnapshot);
    }

    query.get().addOnSuccessListener(queryDocumentSnapshots -> {
      if (!queryDocumentSnapshots.isEmpty()) {

        lastDocSnapshot = queryDocumentSnapshots.getDocuments().get(
                queryDocumentSnapshots.size() - 1
        );

        if(isInitial){
          newerNotifications.addAll(queryDocumentSnapshots.toObjects(Notification.class));
        }else{
          newerNotifications.addAll(newerNotifications.size(),
                  queryDocumentSnapshots.toObjects(Notification.class));
        }
      }
    }).addOnCompleteListener(task -> {
      if (isInitial) {
        newerAdapter.notifyDataSetChanged();

        if (task.getResult().size() == NOTIFICATIONS_LIMIT && scrollListener == null) {
          newestNotificationsRv.addOnScrollListener(scrollListener = new ScrollListener());
        }

      } else {

        final int resultSize = task.getResult().size();

        newerAdapter.notifyItemRangeInserted(newerNotifications.size() -
                resultSize,resultSize);
        if (resultSize < NOTIFICATIONS_LIMIT && scrollListener != null) {
          newestNotificationsRv.removeOnScrollListener(scrollListener);
        }
      }

      swipeRefreshLayout.setRefreshing(false);

      isLoadingNotifications = false;
    });

    if(isInitial){

      addNotificationChangeAndRemoveListener();

      Query newNotifsQuery = FirebaseFirestore.getInstance().collection("Notifications")
              .whereGreaterThan("timeCreated",System.currentTimeMillis())
              .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid());
      addNotificationsListenerForQuery(newNotifsQuery);

//      newestNotificationsRv.addOnScrollListener(scrollListener = new ScrollListener());
    }

//    initialQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//      @Override
//      public void onSuccess(QuerySnapshot snapshots) {
//
//        if (!snapshots.getDocuments().isEmpty()) {
//          newerNotifications.addAll(snapshots.toObjects(Notification.class));
//
//        }
//
//      }
//    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//      @Override
//      public void onComplete(@NonNull Task<QuerySnapshot> task) {
//        if (task.isSuccessful()) {
//
//          if (!newerNotifications.isEmpty()) {
//            Log.d("ttt", "!newerNotifications.isEmpty()");
//            newerAdapter.notifyDataSetChanged();
//          } else {
//            Log.d("ttt", "newerNotifications.isEmpty()");
//          }
//
//        }
//
//        listener = query.whereGreaterThan("timeCreated", System.currentTimeMillis())
//                .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                  @Override
//                  public void onEvent(@Nullable QuerySnapshot value,
//                                      @Nullable FirebaseFirestoreException error) {
//
//                    if (value == null)
//                      return;
//
//
//                    for (DocumentChange dc : value.getDocumentChanges()) {
//
//
//                      switch (dc.getType()) {
//
//                        case ADDED:
//
//
//                          final Notification n = dc.getDocument().toObject(Notification.class);
//
//                          if (newerNotifications.size() == 0) {
//
//                            newerNotifications.add(n);
//                            newerAdapter.notifyItemInserted(0);
//
//                          } else {
//                            if (n.getTimeCreated() > newerNotifications.get(0).getTimeCreated()) {
//                              newerNotifications.add(0, n);
//                              newerAdapter.notifyItemInserted(0);
//                            } else {
//                              newerNotifications.add(n);
//                              newerAdapter.notifyItemInserted(newerNotifications.size() - 1);
//                            }
//                          }
//
//                          break;
//
//                        case REMOVED:
//
//
//
//                          break;
//
//                        case MODIFIED:
//
//                          Notification modifiedNotification;
//
//                          modifiedNotification = findNotification(newerNotifications,
//                                  dc.getDocument().getId());
//
////              if(modifiedNotification == null){
////                modifiedNotification = findNotification(olderNotifications,
////                        dc.getDocument().getId());
////              }
//
//
//                          if (modifiedNotification == null) {
//                            return;
//                          }
//
//
//                          modifiedNotification.setTimeCreated(dc.getDocument().getLong("timeCreated"));
//
//                          int index;
//
//                          if (newerNotifications.contains(modifiedNotification)) {
//                            index = newerNotifications.indexOf(modifiedNotification);
//
//                            newerAdapter.notifyItemChanged(index);
//
//                            Collections.swap(newerNotifications, index, 0);
//                            newerAdapter.notifyItemMoved(index, 0);
//
//                          } else {
//
////                index = olderNotifications.indexOf(modifiedNotification);
////
////                olderAdapter.notifyItemChanged(index);
////
////                Collections.swap(olderNotifications, index, 0);
////                olderAdapter.notifyItemMoved(index, 0);
//                          }
//
//                          break;
//
//                      }
//
//
//                    }
//                  }
//                });
//
////        swipeRefreshLayout.setRefreshing(false);
//
//      }
//    });

  }

  private Notification findNotification(ArrayList<Notification> notifications, String documentId) {
    for (Notification notification : notifications) {
      final String notificationPath = notification.getSenderId() + "_" +
              notification.getDestinationId() + "_" + notification.getType();
      if (documentId.equals(notificationPath)) {
        return notification;
      }
    }
    return null;
  }

  private void setupToolbar() {

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());

  }


  @Override
  public void onDestroy() {
    super.onDestroy();
    if (listenerRegistrationList != null && !listenerRegistrationList.isEmpty()) {
      for(ListenerRegistration listenerRegistration:listenerRegistrationList){
        listenerRegistration.remove();
      }
    }
    if(scrollListener!=null && newestNotificationsRv!=null){
      newestNotificationsRv.removeOnScrollListener(scrollListener);
    }
  }

  private void deleteNotif(Notification n) {
    Log.d("ttt", "deleting notif");

    final String notificationPath = n.getSenderId()
            + "_" + n.getDestinationId() + "_" + n.getType();

    FirebaseFirestore.getInstance().collection("Notifications")
            .document(notificationPath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void aVoid) {
        if(newerNotifications.contains(n)){
          int index = newerNotifications.indexOf(n);
          newerNotifications.remove(index);
          newerAdapter.notifyItemRemoved(index);

          if (Build.VERSION.SDK_INT < 26) {
            BadgeUtil.decrementBadgeNum(NotificationsActivity.this);
          }
        }
      }
    });

  }

  @Override
  public void deleteNotification(Notification notification) {
    deleteNotif(notification);
  }

  @Override
  public void onRefresh() {

    if (listenerRegistrationList != null && !listenerRegistrationList.isEmpty()) {
      for(ListenerRegistration listenerRegistration:listenerRegistrationList){
        listenerRegistration.remove();
      }
    }

    newerNotifications.clear();
    newerAdapter.notifyDataSetChanged();
    lastDocSnapshot = null;
    getNotifications(true);


  }

  private class SwipeToDeleteNotificationCallback extends ItemTouchHelper.SimpleCallback {
    public SwipeToDeleteNotificationCallback() {
      super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
      return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

      if (WifiUtil.checkWifiConnection(viewHolder.itemView.getContext())) {

        deleteNotif(newerNotifications.get(viewHolder.getAdapterPosition()));

      }
    }

  }


  private void addNotification(DocumentChange dc){
    final Notification n = dc.getDocument().toObject(Notification.class);

    if (newerNotifications.size() == 0) {

      newerNotifications.add(n);
      newerAdapter.notifyItemInserted(0);

    } else {
      if (n.getTimeCreated() > newerNotifications.get(0).getTimeCreated()) {
        newerNotifications.add(0, n);
        newerAdapter.notifyItemInserted(0);
      } else {
        newerNotifications.add(n);
        newerAdapter.notifyItemInserted(newerNotifications.size() - 1);
      }
    }

  }

  private void deleteNotificationFromFirestore(DocumentChange dc){

    Notification notification = findNotification(newerNotifications,
            dc.getDocument().getId());

    if (notification == null) {
      return;
    }

    deleteNotification(notification);

    final String identifierTitle =
            notification.getDestinationId() + notification.getType();

    if (GlobalVariables.getMessagesNotificationMap() != null) {
      if (GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {


        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(GlobalVariables
                        .getMessagesNotificationMap().get(identifierTitle));
        GlobalVariables.getMessagesNotificationMap().remove(identifierTitle);

      }
    }

  }

  private void addNotificationChangeAndRemoveListener(){

    Query query = FirebaseFirestore.getInstance().collection("Notifications")
            .orderBy("timeCreated", Query.Direction.DESCENDING)
            .whereLessThan("timeCreated",System.currentTimeMillis())
            .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid());


    listenerRegistrationList.add(
            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value,
                                  @Nullable FirebaseFirestoreException error) {

                if (value == null)
                  return;

                for (DocumentChange dc : value.getDocumentChanges()) {

                  switch (dc.getType()) {

                    case REMOVED:
                      deleteNotificationFromFirestore(dc);
                      break;

                    case MODIFIED:

                      final Notification modifiedNotification = findNotification(newerNotifications,
                              dc.getDocument().getId());

                      if (modifiedNotification == null) {
                        return;
                      }

                      int index = newerNotifications.indexOf(modifiedNotification);

                      modifiedNotification.setTimeCreated(dc.getDocument().getLong("timeCreated"));
                      modifiedNotification.setContent(dc.getDocument().getString("content"));
                      if(index == 0){
                        newerAdapter.notifyItemChanged(index);
                      }else{

                        newerNotifications.remove(index);
                        newerAdapter.notifyItemRemoved(index);
                        newerNotifications.add(0, modifiedNotification);
                        newerAdapter.notifyItemInserted(0);
                      }

                      break;
                  }


                }


              }
            }));

  }


  private void addNotificationsListenerForQuery(Query query){

    listenerRegistrationList.add(
    query.addSnapshotListener(new EventListener<QuerySnapshot>() {
      @Override
      public void onEvent(@Nullable QuerySnapshot value,
                          @Nullable FirebaseFirestoreException error) {

        if (value == null)
          return;

        for (DocumentChange dc : value.getDocumentChanges()) {

          switch (dc.getType()) {

            case ADDED:
              lastDocSnapshot = dc.getDocument();
              addNotification(dc);
              break;

            case REMOVED:
              deleteNotificationFromFirestore(dc);
              break;

            case MODIFIED:

              final Notification modifiedNotification = findNotification(newerNotifications,
                      dc.getDocument().getId());

              if (modifiedNotification == null) {
                return;
              }

              int index = newerNotifications.indexOf(modifiedNotification);

              modifiedNotification.setTimeCreated(dc.getDocument().getLong("timeCreated"));
              modifiedNotification.setContent(dc.getDocument().getString("content"));
              if(index == 0){
                newerAdapter.notifyItemChanged(index);
              }else{

                newerNotifications.remove(index);
                newerAdapter.notifyItemRemoved(index);
                newerNotifications.add(0, modifiedNotification);
                newerAdapter.notifyItemInserted(0);

//                Collections.swap(newerNotifications, index, 0);
//                newerAdapter.notifyItemMoved(index, 0);
              }

              break;
          }


        }


      }
    }));

  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingNotifications && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d("ttt", "is at bottom");

        getNotifications(false);

      }
    }
  }

}