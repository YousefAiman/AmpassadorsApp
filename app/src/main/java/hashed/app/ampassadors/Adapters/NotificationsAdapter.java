package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.CourseActivity;
import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.MeetingMessagingActivity;
import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.Notification;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsVh> {

  public static final int TYPE_NEW = 1, TYPE_OLD = 2;
  private static ArrayList<Notification> notifications;
  private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
  private final int type;
  private final NotificationDeleter notificationDeleter;
  private SharedPreferences sharedPreferences;

  public interface NotificationDeleter{
    void deleteNotification(Notification notification);
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

      case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
      case FirestoreNotificationSender.TYPE_POLL_LIKE:
      case FirestoreNotificationSender.TYPE_POST_LIKE:
      case FirestoreNotificationSender.TYPE_POST_COMMENT:
      case FirestoreNotificationSender.TYPE_POLL_COMMENT:
      case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:
      case FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE:
      case FirestoreNotificationSender.TYPE_POST_REPLY:
      case FirestoreNotificationSender.TYPE_POLL_REPLY:
        documentReference = firestore.collection("Users")
                .document(notification.getSenderId());
        break;

      case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
      case FirestoreNotificationSender.TYPE_MEETING_STARTED:
      case FirestoreNotificationSender.TYPE_MEETING_ADDED:
        documentReference = firestore.collection("Meetings")
                .document(notification.getSenderId());
        break;

      case FirestoreNotificationSender.TYPE_GROUP_ADDED:
      case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
        documentReference = firestore.collection("PrivateMessages")
                .document(notification.getSenderId());
        break;

//      case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
//      case FirestoreNotificationSender.TYPE_COURSE_STARTED:
//        documentReference = firestore.collection("Courses")
//                .document(notification.getSenderId());
//        break;

      default:
        return;
    }

    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists()) {

          final String imageUrl = documentSnapshot.getString("imageUrl");
          if(imageUrl!=null && !imageUrl.isEmpty()){
            notification.setImageUrl(imageUrl);
            Picasso.get().load(imageUrl).fit().centerCrop().into(imageView);
          }
        }
      }
    });
//    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//      @Override
//      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//        if (task.isSuccessful() && notification.getImageUrl() != null &&
//                !notification.getImageUrl().isEmpty()) {
//          Picasso.get().load(notification.getImageUrl()).fit().into(imageView);
//        }
//      }
//    });


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
      itemView.setOnClickListener(this);
    }

    private void bindChat(Notification notification) {

      nameTv.setText(notification.getSenderName());


      if (notification.getImageUrl() == null) {
        if (notification.getType().equals("zoomMeeting")) {
          Picasso.get().load(R.drawable.zoom_icon).fit().centerCrop().into(userIv);
        } else {
          getNotificationImage(notification, userIv);
        }

      } else {
        Picasso.get().load(notification.getImageUrl()).fit().centerCrop().into(userIv);
      }


      titleTv.setText(notification.getContent());

      timeTv.setText(TimeFormatter.formatTime(notification.getTimeCreated()));
    }

    @Override
    public void onClick(View view) {

      final Notification notification = notifications.get(getBindingAdapterPosition());

      notificationDeleter.deleteNotification(notification);
      Context context = view.getContext();

     context.startActivity(directToIntent(notification.getDestinationId(),notification.getType(),context));
//      switch (notification.getType()) {
//
//        case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
//          startMessagingActivity(new Intent(view.getContext(), PrivateMessagingActivity.class),
//                  notification.getDestinationId(),view.getContext(),
//                  notification.getType());
//
//          break;
//
//        case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
//          startMessagingActivity(new Intent(view.getContext(), MeetingMessagingActivity.class),
//                  notification.getDestinationId(),view.getContext(),
//                  notification.getType());
//          break;
//
//        case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
//          startMessagingActivity(new Intent(view.getContext(), CourseMessagingActivity.class),
//                  notification.getDestinationId(),view.getContext(),
//                  notification.getType());
//          break;
//
//        case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
//        case FirestoreNotificationSender.TYPE_GROUP_ADDED:
//          startMessagingActivity(new Intent(view.getContext(), GroupMessagingActivity.class),
//                  notification.getDestinationId(),view.getContext(),
//                  notification.getType());
//          break;
//
//
////        case FirestoreNotificationSender.TYPE_ZOOM:
//        case FirestoreNotificationSender.TYPE_ZOOM_COURSE:
//        case FirestoreNotificationSender.TYPE_ZOOM_MEETING:
//
//          final Intent intent = new Intent(view.getContext(),
//                  notification.getType().equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)?
//                          MeetingMessagingActivity.class:CourseMessagingActivity.class)
//                        .putExtra("messagingUid", notification.getDestinationId())
//                  .putExtra("type",notification.getType())
//                  .putExtra("notificationType",notification.getType())
//                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                itemView.getContext().startActivity(intent);
//          break;
//
//        case FirestoreNotificationSender.TYPE_MEETING_ADDED:
//        case FirestoreNotificationSender.TYPE_MEETING_STARTED:
//
//          final Intent meetingIntent = new Intent(view.getContext(), MeetingActivity.class)
//                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//          firestore.collection("Meetings").document(notification.getDestinationId())
//                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//              if (documentSnapshot.exists()) {
//
//                meetingIntent.putExtra("meeting",documentSnapshot.toObject(Meeting.class));
////                boolean hasEnded = documentSnapshot.getBoolean("hasEnded");
////                long startTime = documentSnapshot.getLong("startTime");
////
////                if (hasEnded || startTime < System.currentTimeMillis()) {
////
////                  String message;
////
////                  if (hasEnded) {
////                    message = "This meeting has ended";
////                  } else {
////                    message = "This meeting hasn't started yet";
////                  }
////
////
////                  Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
////                  notifications.remove(getAdapterPosition());
////                  notifyItemRemoved(getAdapterPosition());
////
////                }
//              }
//            }
//          }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//              if (task.isSuccessful() && notifications.contains(notification)) {
//
//                if(meetingIntent.hasExtra("meeting")){
//                  notificationDeleter.deleteNotification(notification);
//                  itemView.getContext().startActivity(meetingIntent);
//                }
////                Intent intent = new Intent(view.getContext(),
////                        MeetingMessagingActivity.class)
////                        .putExtra("messagingUid", notification.getDestinationId());
////
////                if(notification.getType().equals(FirestoreNotificationSender.TYPE_ZOOM)){
////                        intent.putExtra("type","zoomMeeting");
////                }
////
////                view.getContext().startActivity(intent);
//              }
//            }
//          });
//
//          break;
//
//        case FirestoreNotificationSender.TYPE_POST_LIKE:
//        case FirestoreNotificationSender.TYPE_POLL_LIKE:
//        case FirestoreNotificationSender.TYPE_POST_COMMENT:
//        case FirestoreNotificationSender.TYPE_POLL_COMMENT:
//        case FirestoreNotificationSender.TYPE_POST_REPLY:
//        case FirestoreNotificationSender.TYPE_POLL_REPLY:
//        case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:
//        case FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE:
//
//          notificationDeleter.deleteNotification(notification);
//
//
//
////          Intent postIntent = new Intent(view.getContext(), PostNewsActivity.class)
////                  .putExtra("postId", notification.getDestinationId())
////                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//          FirebaseFirestore.getInstance().collection("Users")
//                  .document(notification.getReceiverId())
//                  .collection("UserPosts")
//                  .document(notification.getDestinationId())
//                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
////
////              if(documentSnapshot.exists()){
////                postIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////                        .putExtra("postId", notification.getDestinationId());
////
////                itemView.getContext().startActivity(postIntent);
////              }
//
//              if(documentSnapshot.exists()){
//
//                notificationDeleter.deleteNotification(notification);
//
//                Intent postIntent = new Intent(view.getContext(),
//                          documentSnapshot.getLong("type") == PostData.TYPE_NEWS?
//                                  PostNewsActivity.class:PostPollActivity.class)
//                        .putExtra("isForUser",true).
//                putExtra("publisherId",notification.getReceiverId())
//                        .putExtra("postId", notification.getDestinationId())
//                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//
//
//                view.getContext().startActivity(postIntent);
//
//              }else{
//
//                firestore.collection("Posts").document(notification.getDestinationId())
//                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                  @Override
//                  public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                    if(documentSnapshot.exists()){
//
//                      notificationDeleter.deleteNotification(notification);
//
//                      Intent postIntent = new Intent(view.getContext(),
//                              documentSnapshot.getLong("type") == PostData.TYPE_NEWS?
//                                      PostNewsActivity.class:
//                                      PostPollActivity.class)
//                              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                              .putExtra("postId", notification.getDestinationId());
//
//                      itemView.getContext().startActivity(postIntent);
//
//                    }
//
//                  }
//                });
//              }
//            }
//          });
////          final ProgressDialog progressDialog = new ProgressDialog(itemView.getContext());
////          progressDialog.show();
////
////          final PostData[] postData = new PostData[1];
////          FirebaseFirestore.getInstance().collection("Posts")
////                  .document(notification.getDestinationId())
////                  .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
////            @Override
////            public void onSuccess(DocumentSnapshot snapshot) {
////              if(snapshot.exists()){
////                postData[0] = snapshot.toObject(PostData.class);
////              }
////            }
////          }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
////            @Override
////            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
////              if(task.isSuccessful() && postData[0] !=null){
////
////                progressDialog.dismiss();
////                notificationDeleter.deleteNotification(notification);
////                if(postData[0].getType() == PostData.TYPE_POLL){
////                  view.getContext().startActivity(new Intent(view.getContext(),
////                          PostPollActivity.class)
////                          .putExtra("postData", postData[0]));
////                }else{
////                  view.getContext().startActivity(new Intent(view.getContext(),
////                          PostNewsActivity.class)
////                          .putExtra("postData", postData[0]));
////                }
////              }
////            }
////          }).addOnFailureListener(new OnFailureListener() {
////            @Override
////            public void onFailure(@NonNull Exception e) {
////              Toast.makeText(itemView.getContext(),
////                      "An Error occurred! Please try again", Toast.LENGTH_SHORT).show();
////              progressDialog.dismiss();
////            }
////          });
//
//          break;
//
//
//        default:
//
//      }

    }

  }

  private Intent directToIntent(String sourceId,String sourceType,Context context){

    Intent destinationIntent = null;

    Log.d("ttt","AppIsRunning");
    if(sourceType.equals(FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE)

            || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_GROUP_ADDED)

            || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_COURSE_STARTED)
            || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)

            || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_MESSAGE)
            || sourceType.equals(FirestoreNotificationSender.TYPE_MEETING_STARTED)
            || sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING)) {

      Log.d("ttt","messaging type");

      switch (sourceType) {
        case FirestoreNotificationSender.TYPE_PRIVATE_MESSAGE:
          destinationIntent = new Intent(context, PrivateMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_GROUP_MESSAGE:
        case FirestoreNotificationSender.TYPE_GROUP_ADDED:
          destinationIntent = new Intent(context, GroupMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_COURSE_MESSAGE:
        case FirestoreNotificationSender.TYPE_COURSE_STARTED:
        case FirestoreNotificationSender.TYPE_ZOOM_COURSE:
          destinationIntent = new Intent(context, CourseMessagingActivity.class);
          break;

        case FirestoreNotificationSender.TYPE_MEETING_MESSAGE:
        case FirestoreNotificationSender.TYPE_MEETING_STARTED:
        case FirestoreNotificationSender.TYPE_ZOOM_MEETING:
          destinationIntent = new Intent(context, MeetingMessagingActivity.class);
          break;
      }

      Log.d("ttt","sourceId: "+sourceId);
      destinationIntent.putExtra("messagingUid", sourceId);

      if(sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_MEETING) ||
              sourceType.equals(FirestoreNotificationSender.TYPE_ZOOM_COURSE)){
        destinationIntent.putExtra("type",sourceType);
      }

      destinationIntent.setFlags(getIntentFlags(sourceId,context));
//          context.startActivity(destinationIntent);

    }else{

      switch (sourceType){
        case FirestoreNotificationSender.TYPE_MEETING_ADDED:
          destinationIntent = new Intent(context,MeetingActivity.class);
          destinationIntent.putExtra("meetingID",sourceId);

          Log.d("ttt","meeting source: "+sourceId);
//              fetchObjectAndStartIntent(MeetingActivity.class,context,"Meetings",
//                      Meeting.class,sourceId,"meeting");

          break;

        case FirestoreNotificationSender.TYPE_COURSE_ADDED:

          destinationIntent = new Intent(context, CourseActivity.class);
          destinationIntent.putExtra("courseID",sourceId);

          break;

        case FirestoreNotificationSender.TYPE_POST_LIKE:
        case FirestoreNotificationSender.TYPE_POST_COMMENT:

          destinationIntent = new Intent(context,PostNewsActivity.class)
                  .putExtra("notificationPostId",sourceId);

          break;

        case FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE:
        case FirestoreNotificationSender.TYPE_POST_REPLY:

          String[] arr = sourceId.split("\\|");

          if(arr.length>1){
            destinationIntent = new Intent(context,PostNewsActivity.class)
                    .putExtra("notificationPostId",arr[0])
                    .putExtra("notificationCreatorId",arr[1]);
          }else{
            destinationIntent = new Intent(context,PostNewsActivity.class)
                    .putExtra("notificationPostId",sourceId);
          }


          break;

        case FirestoreNotificationSender.TYPE_POLL_LIKE:
        case FirestoreNotificationSender.TYPE_POLL_COMMENT:
        case FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE:
        case FirestoreNotificationSender.TYPE_POLL_REPLY:

          destinationIntent =  new Intent(context, PostPollActivity.class)
                  .putExtra("postId",sourceId).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      }
    }

    if(destinationIntent!=null){
      destinationIntent.putExtra("notificationType",sourceType);
    }

    return destinationIntent;
  }

  private int getIntentFlags(String sourceId,Context context){

    Log.d("ttt","checkCurrentMessagingActivity");
    if (GlobalVariables.isAppIsRunning() && getSharedPreferences(context).contains("currentlyMessagingUid")) {
      if (sourceId.equals(getSharedPreferences(context).getString("currentlyMessagingUid", ""))) {
        Log.d("ttt", "this messaging activity is already open man");
        return Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      } else {
        Log.d("ttt", "current messaging is not this");
        return Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
      }
    } else {
      Log.d("ttt", "no current messaging in shared");
      return Intent.FLAG_ACTIVITY_NEW_TASK;
    }

  }

  public SharedPreferences getSharedPreferences(Context context) {
    if(sharedPreferences == null){
      sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.shared_name),
              Context.MODE_PRIVATE);
    }
    return sharedPreferences;
  }


  private void startMessagingActivity(Intent intent, String id, Context context,String sourceType){
    intent.putExtra("messagingUid",id)
            .putExtra("notificationType",sourceType)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

}
