package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.NotificationUtil.BadgeUtil;
import hashed.app.ampassadors.Objects.Notification;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;
import hashed.app.ampassadors.Utils.WifiUtil;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsVh>{

  public static final int TYPE_NEW = 1,TYPE_OLD = 2;
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final ArrayList<Notification> notifications;
  private final int type;

  public NotificationsAdapter(ArrayList<Notification> notifications, int type){
    this.notifications = notifications;
    this.type = type;
  }


  @NonNull
  @Override
  public NotificationsVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    Log.d("ttt","creating notificaiton  view hol;der");
      return new NotificationsVh(LayoutInflater.from(parent.getContext())
              .inflate(R.layout.notifications_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull NotificationsVh holder, int position) {

    if(type == TYPE_OLD){
      holder.itemView.setBackgroundColor(ResourcesCompat.getColor(
              holder.itemView.getContext().getResources(),
              R.color.white_grey,null));
    }

    holder.bindChat(notifications.get(position));

  }

  public class NotificationsVh extends RecyclerView.ViewHolder implements View.OnClickListener{

    private final CircleImageView userIv;
    private final TextView nameTv,titleTv,timeTv;

     public NotificationsVh(@NonNull View itemView) {
       super(itemView);
       userIv = itemView.findViewById(R.id.userIv);
       nameTv = itemView.findViewById(R.id.nameTv);
       titleTv = itemView.findViewById(R.id.titleTv);
       timeTv = itemView.findViewById(R.id.timeTv);
     }

      private void bindChat(Notification notification){

        nameTv.setText(notification.getSenderName());


       if(notification.getImageUrl() == null){
         if(notification.getType().equals("zoomMeeting")) {
           Picasso.get().load(R.drawable.zoom_icon).fit().into(userIv);
         }else{
           getNotificationImage(notification,userIv);
         }

       }else{
         Picasso.get().load(notification.getImageUrl()).fit().into(userIv);
       }


        titleTv.setText(notification.getContent());

        timeTv.setText(TimeFormatter.formatTime(notification.getTimeCreated()));

        itemView.setOnClickListener(this);

     }

     @Override
     public void onClick(View view) {

      final Notification notification = notifications.get(getAdapterPosition());


      switch (notification.getType()){

        case "privateMessaging":

          view.getContext().startActivity(new Intent(view.getContext(),
                  PrivateMessagingActivity.class)
                  .putExtra("messagingUid",notification.getDestinationId()));

          break;

        case "groupMessaging":


          firestore.collection("Meetings").document(notification.getDestinationId())
                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
             if(documentSnapshot.exists()){

               boolean hasEnded = documentSnapshot.getBoolean("hasEnded");
               long startTime = documentSnapshot.getLong("startTime");

               if(hasEnded || startTime < System.currentTimeMillis()){

                 String message;

                 if(hasEnded){
                   message = "This meeting has ended";
                 }else{
                   message = "This meeting hasn't started yet";
                 }

                 Toast.makeText(view.getContext(),message, Toast.LENGTH_SHORT).show();
                 notifications.remove(getAdapterPosition());
                 notifyItemRemoved(getAdapterPosition());

               }
             }
            }
          }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful() && notifications.contains(notification)){

                view.getContext().startActivity(new Intent(view.getContext(),
                        GroupMessagingActivity.class)
                        .putExtra("messagingUid",notification.getDestinationId()));

              }
            }
          });

          break;


        default:

      }



     }

   }


   private void getNotificationImage(Notification notification,ImageView imageView){

    DocumentReference documentReference;

    switch (notification.getType()){

      case "privateMessaging":
        documentReference = firestore.collection("Users")
                .document(notification.getSenderId());
        break;

      case "groupMessaging":
        documentReference = firestore.collection("Meetings")
                .document(notification.getSenderId());
        break;

      default:
        return;
    }


     documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
       @Override
       public void onSuccess(DocumentSnapshot documentSnapshot) {
        
         if(documentSnapshot.exists()){
           notification.setImageUrl(documentSnapshot.getString("imageUrl"));
         }
         
       }
     }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
       @Override
       public void onComplete(@NonNull Task<DocumentSnapshot> task) {
         if(task.isSuccessful() && notification.getImageUrl()!=null){
           Picasso.get().load(notification.getImageUrl()).fit().into(imageView);
         }
       }
     });


   }

  @Override
  public int getItemCount() {
    return notifications.size();
  }


  public static class SwipeToDeleteNotificationCallback extends ItemTouchHelper.SimpleCallback {
    private final NotificationsAdapter mAdapter;

    public SwipeToDeleteNotificationCallback(NotificationsAdapter adapter) {
      super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
      mAdapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
      return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

      if(WifiUtil.checkWifiConnection(viewHolder.itemView.getContext())) {

        deleteNotification(mAdapter.notifications.get(viewHolder.getAdapterPosition()),
                viewHolder.itemView.getContext());

      }
    }

  }


  private static void deleteNotification(Notification n, Context context) {
    Log.d("ttt", "deleting notif");

    final String notificationPath = FirebaseAuth.getInstance().getCurrentUser().getUid()
            + "_" + n.getDestinationId() + "_" + n.getType();

    FirebaseFirestore.getInstance().collection("Notifications")
            .document(notificationPath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void aVoid) {

        if (Build.VERSION.SDK_INT < 26) {
          BadgeUtil.decrementBadgeNum(context);
        }

      }
    });

  }


}
