package hashed.app.ampassadors.Objects;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.DocumentViewChangeSet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.R;

@IgnoreExtraProperties
public class PostData implements Serializable {

    public static final int TYPE_NEWS = 1,TYPE_POLL = 2;

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
    @Exclude
    private String publisherName;
    @Exclude
    private String publisherImage;
    @Exclude
    private int chosenPollOption;
    @Exclude
    private ArrayList<PollOption> pollOptions;


    public PostData(){
    }

    public PostData(Map<String,Object> postMap){

        this.postId = (String) postMap.get("postId");
        this.title = (String) postMap.get("title");
        this.description = (String) postMap.get("description");
        this.publisherId = (String) postMap.get("publisherId");
        this.attachmentType = (int) postMap.get("attachmentType");

        if(videoThumbnailUrl!=null){
            this.videoThumbnailUrl = (String) postMap.get("videoThumbnailUrl");
        }

        if(documentName!=null){
            this.documentName = (String) postMap.get("documentName");
        }

        if(documentSize != 0){
            this.documentSize = (long) postMap.get("documentSize");
        }

        this.publishTime = (long) postMap.get("publishTime");
        this.likes = (long) postMap.get("likes");
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


    public static void likePost(String postId, int type, String creatorId, Context context){

        if(type == 1){

            HashMap<String, Object> likedMap = new HashMap<>();
            likedMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

            FirebaseFirestore.getInstance().collection("Posts")
                    .document(postId)
                    .collection("Likes")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .set(likedMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void v) {

                            FirebaseFirestore.getInstance().collection("Posts")
                                    .document(postId).update("likes",FieldValue.increment(1));

                            final DocumentReference userRef =
                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance()
                                                    .getCurrentUser().getUid());
                            userRef .update("Likes",FieldValue.arrayUnion(postId));

                            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot) {
                                    if(snapshot.exists()){
                                        final String username = snapshot.getString("username");

                                        FirestoreNotificationSender.sendFirestoreNotification(
                                                creatorId,"postLike",
                                                username +
                                                        context.getResources()
                                                                .getString(R.string.liked_post),
                                                    username,
                                                    postId
                                                );
                                    }
                                }
                            });

                        }
                    });

        }else {


            final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection("Posts")
                    .document(postId)
                    .collection("Likes")
                    .document()
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FirebaseFirestore.getInstance().collection("Posts")
                                    .document(postId).update("likes",FieldValue.increment(-1));

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(currentUid)
                                    .update("Likes",FieldValue.arrayRemove(postId));

                            FirestoreNotificationSender.deleteFirestoreNotification(
                                    postId,
                                    "postLike"
                            );
                        }
                    });

        }

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
}