package hashed.app.ampassadors.Objects;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.DocumentViewChangeSet;

import java.io.File;
import java.util.HashMap;

@IgnoreExtraProperties
public class UserPostData {

    @PropertyName("postId")
    private String postId;
    @PropertyName("title")
    private String title;
    @PropertyName("publisherId")
    private String publisherId ;
    @PropertyName("publisherName")
    private String publisherName ;
    @PropertyName("publisherImage")
    private String publisherImage ;
    @PropertyName("imageUrl")
    private String imageUrl;
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

    public UserPostData(){

    }
    public UserPostData(String title, String publisherId, String imageUrl,
                    long publishTime, int likes, int comments, String description, int type) {
        this.title = title;
        this.publisherId = publisherId;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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


    public static void likePost(String postId,int type){


        if(type == 1){

            HashMap<String, Object> likedMap = new HashMap<>();
            likedMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());


            FirebaseFirestore.getInstance().collection("Users")
                    .document(postId)
                    .collection("Likes")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .set(likedMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(postId).update("likes",FieldValue.increment(1));


                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update("Likes",FieldValue.arrayUnion(postId));

                        }
                    });


        }else {


            FirebaseFirestore.getInstance().collection("Users")
                    .document(postId)
                    .collection("Likes")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(postId).update("likes",FieldValue.increment(-1));

                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .update("Likes",FieldValue.arrayRemove(postId));

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
}