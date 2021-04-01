package hashed.app.ampassadors.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.Notification;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsVh> {

  public static final int TYPE_NEW = 1, TYPE_OLD = 2;
  private static ArrayList<Notification> notifications;
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final int type;
  private final NotificationDeleter notificationDeleter;

  public interface NotificationDeleter{
    public void deleteNotification(Notification notification);
  }

  public NotificationsAdapter(ArrayList<Notification> notifications, int type,
                              NotificationDeleter notificationDeleter) {
    NotificationsAdapter.notifications = notifications;
    this.type = type;
    this.notificationDeleter = notificationDeleter;
  }


  @NonNull
  @Override
  public NotificationsVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    Log.d("ttt", "creating notificaiton  view hol;der");
    return new NotificationsVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.notifications_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull NotificationsVh holder, int position) {

    if (type == TYPE_OLD) {
      holder.itemView.setBackgroundColor(ResourcesCompat.getColor(
              holder.itemView.getContext().getResources(),
              R.color.white_grey, null));
    }

    holder.bindChat(notifications.get(position));

  }

  private void getNotificationImage(Notification notification, ImageView imageView) {

    DocumentReference documentReference;

    switch (notification.getType()) {

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

        if (documentSnapshot.exists()) {
          notification.setImageUrl(documentSnapshot.getString("imageUrl"));
        }

      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful() && notification.getImageUrl() != null) {
          Picasso.get().load(notification.getImageUrl()).fit().into(imageView);
        }
      }
    });


  }

  @Override
  public int getItemCount() {
    return notifications.size();
  }

  public class NotificationsVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final CircleImageView userIv;
    private final TextView nameTv, titleTv, timeTv;

    public NotificationsVh(@NonNull View itemView) {
      super(itemView);
      userIv = itemView.findViewById(R.id.userIv);
      nameTv = itemView.findViewById(R.id.nameTv);
      titleTv = itemView.findViewById(R.id.titleTv);
      timeTv = itemView.findViewById(R.id.timeTv);
    }

    private void bindChat(Notification notification) {

      nameTv.setText(notification.getSenderName());


      if (notification.getImageUrl() == null) {
        if (notification.getType().equals("zoomMeeting")) {
          Picasso.get().load(R.drawable.zoom_icon).fit().into(userIv);
        } else {
          getNotificationImage(notification, userIv);
        }

      } else {
        Picasso.get().load(notification.getImageUrl()).fit().into(userIv);
      }


      titleTv.setText(notification.getContent());

      timeTv.setText(TimeFormatter.formatTime(notification.getTimeCreated()));

      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

      final Notification notification = notifications.get(getAdapterPosition());


      switch (notification.getType()) {

        case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:

          notificationDeleter.deleteNotification(notification);

          view.getContext().startActivity(new Intent(view.getContext(),
                  PrivateMessagingActivity.class)
                  .putExtra("messagingUid", notification.getDestinationId()));

          break;

        case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
        case FirestoreNotificationSender.TYPE_MEETING_ADDED:
        case FirestoreNotificationSender.TYPE_ZOOM:
        case FirestoreNotificationSender.TYPE_MEETING_STARTED:

          firestore.collection("Meetings").document(notification.getDestinationId())
                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
              if (documentSnapshot.exists()) {

                boolean hasEnded = documentSnapshot.getBoolean("hasEnded");
                long startTime = documentSnapshot.getLong("startTime");

                if (hasEnded || startTime < System.currentTimeMillis()) {

                  String message;

                  if (hasEnded) {
                    message = "This meeting has ended";
                  } else {
                    message = "This meeting hasn't started yet";
                  }


                  Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
                  notifications.remove(getAdapterPosition());
                  notifyItemRemoved(getAdapterPosition());

                }
              }
            }
          }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if (task.isSuccessful() && notifications.contains(notification)) {

                notificationDeleter.deleteNotification(notification);
                if(notification.getType().equals(FirestoreNotificationSender.TYPE_ZOOM)){
                  final Bundle destinationBundle = new Bundle();
                  destinationBundle.putString("sourceId",notification.getDestinationId());
                  destinationBundle.putString("sourceType",notification.getType());

                  view.getContext().startActivity(new Intent(view.getContext(),
                          GroupMessagingActivity.class)
                          .putExtra("destinationBundle",destinationBundle));

                }else{

                  view.getContext().startActivity(new Intent(view.getContext(),
                          GroupMessagingActivity.class)
                          .putExtra("messagingUid", notification.getDestinationId()));
                }

              }
            }
          });

          break;

        case FirestoreNotificationSender.TYPE_LIKE:
        case FirestoreNotificationSender.TYPE_COMMENT:

          final ProgressDialog progressDialog = new ProgressDialog(itemView.getContext());
          progressDialog.show();

          final PostData[] postData = new PostData[1];
          FirebaseFirestore.getInstance().collection("Posts")
                  .document(notification.getDestinationId())
                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
              if(snapshot.exists()){
                postData[0] = snapshot.toObject(PostData.class);
              }
            }
          }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful() && postData[0] !=null){

                progressDialog.dismiss();
                notificationDeleter.deleteNotification(notification);
                if(postData[0].getType() == PostData.TYPE_POLL){
                  view.getContext().startActivity(new Intent(view.getContext(),
                          PostPollActivity.class)
                          .putExtra("postData", postData[0]));
                }else{
                  view.getContext().startActivity(new Intent(view.getContext(),
                          PostNewsActivity.class)
                          .putExtra("postData", postData[0]));
                }
              }
            }
          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Toast.makeText(itemView.getContext(),
                      "An Error occurred! Please try again", Toast.LENGTH_SHORT).show();
              progressDialog.dismiss();
            }
          });

          break;


        default:

      }


    }

  }


}
