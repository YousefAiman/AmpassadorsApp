package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hashed.app.ampassadors.Adapters.NotificationsAdapter;
import hashed.app.ampassadors.Objects.Notification;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class NotificationsActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener{

  private ArrayList<Notification> newerNotifications;
//  private ArrayList<Notification> olderNotifications;
  private NotificationsAdapter newerAdapter;
//  private NotificationsAdapter olderAdapter;


  //views
//  private TextView newestNotificationsTv,oldestNotificationsTv;
//  private RecyclerView newestNotificationsRv,oldestNotificationsRv;
  private RecyclerView newestNotificationsRv;
//  private SwipeRefreshLayout swipeRefreshLayout;


  private ListenerRegistration listener;
  private String currentUserId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notifications);


    currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    setupToolbar();

//    NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
//    nestedScrollView.setNestedScrollingEnabled(false);

//    nestedScrollView.getViewTreeObserver().addon
    getViews();


    newestNotificationsRv = findViewById(R.id.newestNotificationsRv);


    newerNotifications = new ArrayList<>();
//    olderNotifications = new ArrayList<>();

    newerAdapter = new NotificationsAdapter(newerNotifications,
            NotificationsAdapter.TYPE_NEW);

//    olderAdapter = new NotificationsAdapter(olderNotifications,
//            NotificationsAdapter.TYPE_OLD);


    final ItemTouchHelper itemTouchHelper = new
            ItemTouchHelper(new NotificationsAdapter.SwipeToDeleteNotificationCallback(newerAdapter));
    itemTouchHelper.attachToRecyclerView(newestNotificationsRv);

//    final ItemTouchHelper itemTouchHelper2 = new
//            ItemTouchHelper(new NotificationsAdapter.SwipeToDeleteNotificationCallback(olderAdapter));
//    itemTouchHelper2.attachToRecyclerView(oldestNotificationsRv);

    newestNotificationsRv.setAdapter(newerAdapter);

//    oldestNotificationsRv.setAdapter(newerAdapter);

    getNotifications();
  }

  private void getViews(){

//    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
//    newestNotificationsTv = findViewById(R.id.newestNotificationsTv);
//    oldestNotificationsTv = findViewById(R.id.oldestNotificationsTv);

//    oldestNotificationsRv = findViewById(R.id.oldestNotificationsRv);
//    swipeRefreshLayout.setOnRefreshListener(this);
  }

  private void getNotifications(){

//    swipeRefreshLayout.setRefreshing(true);

    final Query query = FirebaseFirestore.getInstance().collection("Notifications")
            .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid());

//    listener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
//      @Override
//      public void onEvent(@Nullable QuerySnapshot value,
//                          @Nullable FirebaseFirestoreException error) {
//
//        for(DocumentChange dc:value.getDocumentChanges()){
//
//          switch (dc.getType()){
//
//
//            case ADDED:
//
//
//              if (newerNotifications.size() == 0) {
//                newerNotifications.add(n);
//                adapter.notifyItemInserted(0);
//              } else {
//                if (n.getTimeCreated() > notifications.get(0).getTimeCreated()) {
//                  notifications.add(0, n);
//                  adapter.notifyItemInserted(0);
//                } else {
//                  notifications.add(n);
//                  adapter.notifyItemInserted(notifications.size() - 1);
//                }
//              }
//
//              break;
//
//
//            case REMOVED:
//
//              Notification notification;
//
//              notification = findNotification(newerNotifications,
//                      dc.getDocument().getId());
//
//              if(notification == null){
//                notification = findNotification(olderNotifications,
//                        dc.getDocument().getId());
//              }
//
//
//              if(notification == null){
//                return;
//              }
//
//              final String identifierTitle =
//                      notification.getDestinationId() + notification.getType();
//
//              if (GlobalVariables.getMessagesNotificationMap() != null) {
//                if (GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {
//
//
//                  ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
//                          .cancel(GlobalVariables
//                                  .getMessagesNotificationMap().get(identifierTitle));
//                  GlobalVariables.getMessagesNotificationMap().remove(identifierTitle);
//
//                }
//              }
//
//              break;
//
//            case MODIFIED:
//
//              Notification modifiedNotification;
//
//              modifiedNotification = findNotification(newerNotifications,
//                      dc.getDocument().getId());
//
//              if(modifiedNotification == null){
//                modifiedNotification = findNotification(olderNotifications,
//                        dc.getDocument().getId());
//              }
//
//
//              if(modifiedNotification == null){
//                return;
//              }
//
//
//              modifiedNotification.setTimeCreated(dc.getDocument().getLong("timeCreated"));
//
//              int index;
//
//              if(newerNotifications.contains(modifiedNotification)){
//                index = newerNotifications.indexOf(modifiedNotification);
//
//                newerAdapter.notifyItemChanged(index);
//
//                Collections.swap(newerNotifications, index, 0);
//                newerAdapter.notifyItemMoved(index, 0);
//
//              }else{
//
//                index = olderNotifications.indexOf(modifiedNotification);
//
//                olderAdapter.notifyItemChanged(index);
//
//                Collections.swap(olderNotifications, index, 0);
//                olderAdapter.notifyItemMoved(index, 0);
//              }
//
//              break;
//
//          }
//
//
//        }
//      }
//    });

    final Query initialQuery = query.orderBy("timeCreated", Query.Direction.DESCENDING)
            .limit(10);

    initialQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        if(!snapshots.getDocuments().isEmpty()){

//          newestNotificationsTv.setVisibility(View.VISIBLE);
//          newestNotificationsRv.setVisibility(View.VISIBLE);

          newerNotifications.addAll(snapshots.toObjects(Notification.class));

//          final List<DocumentSnapshot> documentSnapshots = snapshots.getDocuments();
//
//          if(documentSnapshots.size() > 3){
//
//            oldestNotificationsTv.setVisibility(View.VISIBLE);
//            oldestNotificationsRv.setVisibility(View.VISIBLE);
//
//            for(DocumentSnapshot snapshot:documentSnapshots.subList(0,3)){
//              newerNotifications.add(snapshot.toObject(Notification.class));
//            }
//
//            for(DocumentSnapshot snapshot:documentSnapshots.subList(3,snapshots.size())){
//              olderNotifications.add(snapshot.toObject(Notification.class));
//            }
//
//          }else{
//            newerNotifications = (ArrayList<Notification>) snapshots.toObjects(Notification.class);
//          }
        }

      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if(task.isSuccessful()){

          if(!newerNotifications.isEmpty()){
            Log.d("ttt","!newerNotifications.isEmpty()");
            newerAdapter.notifyDataSetChanged();
          }else{
            Log.d("ttt","newerNotifications.isEmpty()");
          }

//          if(!olderNotifications.isEmpty()){
//            olderAdapter.notifyDataSetChanged();
//          }

        }

      listener = query.whereGreaterThan("timeCreated",System.currentTimeMillis())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
      @Override
      public void onEvent(@Nullable QuerySnapshot value,
                          @Nullable FirebaseFirestoreException error) {

        if(value == null)
          return;


        for(DocumentChange dc:value.getDocumentChanges()){


          switch (dc.getType()){

            case ADDED:


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

              break;

            case REMOVED:

              Notification notification;

              notification = findNotification(newerNotifications,
                      dc.getDocument().getId());

//              if(notification == null){
//                notification = findNotification(olderNotifications,
//                        dc.getDocument().getId());
//              }


              if(notification == null){
                return;
              }

              final String identifierTitle =
                      notification.getDestinationId() + notification.getType();

              if (GlobalVariables.getMessagesNotificationMap() != null) {
                if (GlobalVariables.getMessagesNotificationMap().containsKey(identifierTitle)) {


                  ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
                          .cancel(GlobalVariables
                                  .getMessagesNotificationMap().get(identifierTitle));
                  GlobalVariables.getMessagesNotificationMap().remove(identifierTitle);

                }
              }

              break;

            case MODIFIED:

              Notification modifiedNotification;

              modifiedNotification = findNotification(newerNotifications,
                      dc.getDocument().getId());

//              if(modifiedNotification == null){
//                modifiedNotification = findNotification(olderNotifications,
//                        dc.getDocument().getId());
//              }


              if(modifiedNotification == null){
                return;
              }


              modifiedNotification.setTimeCreated(dc.getDocument().getLong("timeCreated"));

              int index;

              if(newerNotifications.contains(modifiedNotification)){
                index = newerNotifications.indexOf(modifiedNotification);

                newerAdapter.notifyItemChanged(index);

                Collections.swap(newerNotifications, index, 0);
                newerAdapter.notifyItemMoved(index, 0);

              }else{

//                index = olderNotifications.indexOf(modifiedNotification);
//
//                olderAdapter.notifyItemChanged(index);
//
//                Collections.swap(olderNotifications, index, 0);
//                olderAdapter.notifyItemMoved(index, 0);
              }

              break;

          }


        }
      }
    });

//        swipeRefreshLayout.setRefreshing(false);

      }
    });

  }

  private Notification findNotification(ArrayList<Notification> notifications, String documentId){
    for(Notification notification: notifications){
      final String notificationPath = currentUserId + "_" +
              notification.getDestinationId()+ "_" + notification.getType();
      if(documentId.equals(notificationPath)){
        return notification;
      }
    }
    return null;
  }

  private void setupToolbar(){

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v->finish());

  }

  @Override
  public void onRefresh() {
//
//    newerNotifications.clear();
//    olderNotifications.clear();
//
//
//    newerAdapter.notifyDataSetChanged();
//    olderAdapter.notifyDataSetChanged();
//
//    getNotifications();

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (listener != null) {
      listener.remove();
    }
  }
}