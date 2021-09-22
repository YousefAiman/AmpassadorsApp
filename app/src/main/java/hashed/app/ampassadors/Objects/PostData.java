package hashed.app.ampassadors.Objects;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

@IgnoreExtraProperties
public class PostData implements Serializable {

  public static final int TYPE_NEWS = 1, TYPE_POLL = 2;

  @PropertyName("postId")
  private String postId;
  @PropertyName("title")
  private String title;
  @PropertyName("publisherId")
  private String publisherId;
  @PropertyName("attachmentUrl")
  private String attachmentUrl;
  @PropertyName("videoThumbnailUrl")
  private String videoThumbnailUrl;
  @PropertyName("attachmentType")
  private int attachmentType;
  @PropertyName("documentName")
  private String documentName;
  @PropertyName("documentSize")
  private long documentSize;
  @PropertyName("publishTime")
  private long publishTime;
  @PropertyName("likes")
  private long likes;
  @PropertyName("comments")
  private int comments;
  @PropertyName("description")
  private String description;
  @PropertyName("type")
  private int type;
  @PropertyName("duration")
  private long pollDuration;
  @PropertyName("totalVotes")
  private long totalVotes;
  @PropertyName("pollEnded")
  private boolean pollEnded;
  @PropertyName("isReported")
  boolean isReported;
  @PropertyName("important")
  boolean important ;
  @Exclude
  private String publisherName;
  @Exclude
  private String publisherImage;
  @Exclude
  private int chosenPollOption;
  @Exclude
  private ArrayList<PollOption> pollOptions;


  public PostData() {
  }

  public PostData(Map<String, Object> postMap) {

    this.postId = (String) postMap.get("postId");
    this.title = (String) postMap.get("title");
    this.description = (String) postMap.get("description");
    this.publisherId = (String) postMap.get("publisherId");

    if(postMap.containsKey("attachmentType")){
      this.attachmentType = (int) postMap.get("attachmentType");
    }

    if(postMap.containsKey("attachmentUrl")){
      this.attachmentUrl = (String) postMap.get("attachmentUrl");
    }


    if (videoThumbnailUrl != null) {
      this.videoThumbnailUrl = (String) postMap.get("videoThumbnailUrl");
    }

    if (documentName != null) {
      this.documentName = (String) postMap.get("documentName");
    }

    if (documentSize != 0) {
      this.documentSize = (long) postMap.get("documentSize");
    }

    this.publishTime = (long) postMap.get("publishTime");
    this.likes = (int) postMap.get("likes");
    this.comments = (int) postMap.get("comments");
    this.type = (int) postMap.get("type");

  }

  public PostData(String title, String publisherId,
                  long publishTime, int likes, int comments, String description, int type) {
    this.title = title;
    this.publisherId = publisherId;
    this.publishTime = publishTime;
    this.likes = likes;
    this.comments = comments;
    this.description = description;
    this.type = type;
  }

  public static void likePost(String postId, String postTitle, int type,
                              String creatorId, Context context, boolean isUserPost,
                              int postType,View likeView) {

    likeView.setClickable(false);

    if (type == 1) {

      HashMap<String, Object> likedMap = new HashMap<>();
      likedMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

      DocumentReference documentReference;

      if(isUserPost){
        documentReference = FirebaseFirestore.getInstance().collection("Users")
                .document(creatorId)
                .collection("UserPosts")
                .document(postId);
      }else{
        documentReference = FirebaseFirestore.getInstance().collection("Posts")
                .document(postId);
      }

      documentReference.collection("Likes")
              .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
              .set(likedMap)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void v) {

                  final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                  final DocumentReference userRef =
                          FirebaseFirestore.getInstance().collection("Users")
                                  .document(currentUid);

                  userRef.update("Likes", FieldValue.arrayUnion(postId))
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                      documentReference.update("likes", FieldValue.increment(1))
                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                          if(!creatorId.equals(currentUid)) {

                            final String notificationPath = creatorId + "_" + postId + "_" +
                                    (type == TYPE_NEWS?FirestoreNotificationSender.TYPE_POST_LIKE:FirestoreNotificationSender.TYPE_POLL_LIKE);

                            FirebaseFirestore.getInstance().collection("Notifications")
                                    .document(notificationPath)
                                    .get().addOnSuccessListener(documentSnapshot -> {
                              if (!documentSnapshot.exists()) {

                                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                  @Override
                                  public void onSuccess(DocumentSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                      final String username = snapshot.getString("username");
                                      final String imageUrl = snapshot.getString("imageUrl");

                                      final String message = username + " " + context.getResources()
                                              .getString(R.string.liked_post);

                                      final String notificationType = postType == PostData.TYPE_NEWS?FirestoreNotificationSender.TYPE_POST_LIKE:FirestoreNotificationSender.TYPE_POLL_LIKE;

                                      FirestoreNotificationSender.sendFirestoreNotification(
                                              creatorId,
                                              notificationType,
                                              message, username, postId);

                                      final Data data = new Data(
                                              currentUid,
                                              message,
                                              postTitle,
                                              null,
                                              "Post Like",
                                              notificationType,
                                              postId);

                                      if (imageUrl != null && !imageUrl.isEmpty()) {
                                        data.setSenderImageUrl(imageUrl);
                                      }

                                      CloudMessagingNotificationsSender.sendNotification(creatorId, data);

                                    }
                                  }
                                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                  @Override
                                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    likeView.setClickable(true);
                                  }
                                });
                              }else{
                                likeView.setClickable(true);
                              }
                            }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                likeView.setClickable(true);
                              }
                            });

                          }else{
                            likeView.setClickable(true);
                          }

                        }
                      }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          likeView.setClickable(true);
                        }
                      });

                    }
                  }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      likeView.setClickable(true);
                    }
                  });


                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          likeView.setClickable(true);
        }
      });

    } else {


      final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

      DocumentReference documentReference;

      if(isUserPost){
        documentReference = FirebaseFirestore.getInstance().collection("Users")
                .document(creatorId)
                .collection("UserPosts")
                .document(postId);
      }else{
        documentReference = FirebaseFirestore.getInstance().collection("Posts")
                .document(postId);
      }

      documentReference
              .collection("Likes")
              .document(currentUid)
              .delete()
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                  FirebaseFirestore.getInstance().collection("Users")
                          .document(currentUid)
                          .update("Likes", FieldValue.arrayRemove(postId))
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                              documentReference.update("likes", FieldValue.increment(-1))
                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                  likeView.setClickable(true);
                                }
                              }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                  likeView.setClickable(true);
                                }
                              });
                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      likeView.setClickable(true);
                    }
                  });

                  FirestoreNotificationSender.deleteFirestoreNotification(
                          postId,
                          "postLike"
                  );
                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          likeView.setClickable(true);
        }
      });

    }

  }

  public static void deletePost(Activity activity,PostData postData){

    ProgressDialog progressDialog = new ProgressDialog(activity);
    progressDialog.setMessage(activity.getString(R.string.Dleteing));
    progressDialog.show();
    FirebaseFirestore.getInstance().collection("Users").
            document(postData.getPublisherId()).collection("UserPosts")
            .document(postData.getPostId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists()){
          deletePostRelatedData(documentSnapshot.getReference(),activity,progressDialog,
                  postData);
        }else {
          deletePostRelatedData(FirebaseFirestore.getInstance().collection("Posts")
                          .document(postData.getPostId())
                  ,activity,progressDialog, postData);
        }

      }
    });

  }

  private static void deletePostRelatedData(DocumentReference postRef, Activity activity,
                                            ProgressDialog progressDialog,PostData postData){

    postRef.update("deleting",true).addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void aVoid) {

        postRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            progressDialog.dismiss();
            activity.finish();
            Toast.makeText(activity, R.string.Delete_success, Toast.LENGTH_SHORT).show();
          }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            if(task.isSuccessful()){
              deleteCollection("Likes",postRef);
              deleteCollection("Comments",postRef);

              if(postData.getType() == PostData.TYPE_POLL){
                deleteCollection("Options",postRef);
                deleteCollection("UserVotes",postRef);
              }

              final FirebaseStorage storage = FirebaseStorage.getInstance();

              if(postData.getAttachmentUrl()!=null){
                storage.getReferenceFromUrl(postData.getAttachmentUrl()).delete();
              }

              if(postData.getAttachmentType() == Files.VIDEO && postData.getVideoThumbnailUrl()!=null){
                storage.getReferenceFromUrl(postData.getVideoThumbnailUrl()).delete();
              }

              FirebaseFirestore.getInstance().collection("Notifications")
                      .whereEqualTo("destinationId",postData.getPostId())
                      .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot snapshots) {
                  for(DocumentSnapshot documentSnapshot:snapshots){
                    documentSnapshot.getReference().delete();
                  }
                }
              });
            }
          }
        });

      }
    });

  }

  private static void deleteCollection(String collectionName,DocumentReference postRef){
    postRef.collection(collectionName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if(snapshots != null){
          for(DocumentSnapshot documentSnapshot:snapshots){
            documentSnapshot.getReference().delete();
          }
        }
      }
    });
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getLikes() {
    return likes;
  }

  public void setLikes(long likes) {
    this.likes = likes;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getPublisherId() {
    return publisherId;
  }

  public void setPublisherId(String publisherId) {
    this.publisherId = publisherId;
  }

  public long getPublishTime() {
    return publishTime;
  }

  public void setPublishTime(long publishTime) {
    this.publishTime = publishTime;
  }

  public int getComments() {
    return comments;
  }

  public void setComments(int comments) {
    this.comments = comments;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public String getPublisherImage() {
    return publisherImage;
  }

  public void setPublisherImage(String publisherImage) {
    this.publisherImage = publisherImage;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public long getPollDuration() {
    return pollDuration;
  }

  public void setPollDuration(long pollDuration) {
    this.pollDuration = pollDuration;
  }

  public int getChosenPollOption() {
    return chosenPollOption;
  }

  public void setChosenPollOption(int chosenPollOption) {
    this.chosenPollOption = chosenPollOption;
  }

  public long getTotalVotes() {
    return totalVotes;
  }

  public void setTotalVotes(long totalVotes) {
    this.totalVotes = totalVotes;
  }

  public ArrayList<PollOption> getPollOptions() {
    return pollOptions;
  }

  public void setPollOptions(ArrayList<PollOption> pollOptions) {
    this.pollOptions = pollOptions;
  }

  public boolean isPollEnded() {
    return pollEnded;
  }

  public void setPollEnded(boolean pollEnded) {
    this.pollEnded = pollEnded;
  }

  public int getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(int attachmentType) {
    this.attachmentType = attachmentType;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }

  public String getVideoThumbnailUrl() {
    return videoThumbnailUrl;
  }

  public void setVideoThumbnailUrl(String videoThumbnailUrl) {
    this.videoThumbnailUrl = videoThumbnailUrl;
  }

  public String getDocumentName() {
    return documentName;
  }

  public void setDocumentName(String documentName) {
    this.documentName = documentName;
  }

  public long getDocumentSize() {
    return documentSize;
  }

  public void setDocumentSize(long documentSize) {
    this.documentSize = documentSize;
  }

  public boolean isReported() {
    return isReported;
  }

  public void setReported(boolean reported) {
    isReported = reported;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }
}