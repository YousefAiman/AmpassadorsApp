package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.CommentsAdapter;
import hashed.app.ampassadors.Adapters.RepliesAdapter;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.Comment;
import hashed.app.ampassadors.Objects.CommentReply;
import hashed.app.ampassadors.R;


public class CommentsFragment extends BottomSheetDialogFragment implements View.OnClickListener ,
    CommentsAdapter.CommentsListener, RepliesAdapter.RepliesListener{

    private static final int COMMENTS_SIZE = 10;

    //database
        CollectionReference postsRef;
        private final String postId;
    private Query commentsQuery;


    //adapter
    CommentsAdapter commentsAdapter;
    private ChatsScrollListener chatsScrollListener;
    boolean isLoadingComments;
    List<Comment> comments;
    private DocumentSnapshot lastDocSnap;
    private Comment currentFocusedComment;


    //Views
    EditText commentEd;
    RecyclerView commentsRv;
    TextView commentCountTv;
    ImageView commentSubmitIv;
    FrameLayout repliesFrameLayout;
    int commentsCount;

    public CommentsFragment(String postId, int commentsCount){
        this.postId = postId;
        this.commentsCount = commentsCount;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Demo_BottomSheetDialog);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(comments,this,postId,getContext());
        postsRef = FirebaseFirestore.getInstance().collection("Posts");
        commentsQuery = postsRef.document(postId).collection("Comments")
                .limit(COMMENTS_SIZE).orderBy("likes", Query.Direction.DESCENDING);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commnet, container, false);

        commentEd = view.findViewById(R.id.commentEd);
        commentsRv = view.findViewById(R.id.commentsRv);
        commentSubmitIv = view.findViewById(R.id.commentSubmitIv);
        commentCountTv = view.findViewById(R.id.commentCountTv);
        repliesFrameLayout = view.findViewById(R.id.repliesFrameLayout);

        commentSubmitIv.setOnClickListener(this);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentCountTv.setText(String.valueOf(commentsCount));

        commentsRv.setAdapter(commentsAdapter);

        ReadComments(true);

    }


    private void ReadComments(boolean isInitial) {

        isLoadingComments = false;

        if(lastDocSnap!=null){
            commentsQuery = commentsQuery.startAfter(lastDocSnap);
        }

        commentsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if(!snapshots.isEmpty()){

                    lastDocSnap = snapshots.getDocuments().get(snapshots.size()-1);

                    if(isInitial){

                        comments.addAll(snapshots.toObjects(Comment.class));

                    }else{
                        comments.addAll(comments.size(),snapshots.toObjects(Comment.class));
                    }
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful() && task.getResult()!=null && !task.getResult().isEmpty()){
                    Log.d("ttt","comments: "+comments.size());
                    if(isInitial){

                        commentsAdapter.notifyDataSetChanged();

                        if(task.getResult().size() == COMMENTS_SIZE){
                            commentsRv.addOnScrollListener(chatsScrollListener = new ChatsScrollListener());
                        }

                    }else{

                        commentsAdapter.notifyItemRangeInserted(comments.size(),task.getResult().size());

                    }
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.commentSubmitIv){
                addComment();
        }
    }

    private void addComment(){

        commentSubmitIv.setClickable(false);
        Log.d("ttt","clicked");
        final String comment = commentEd.getText().toString();

        if(!comment.trim().isEmpty()){

            commentEd.setText("");

            Log.d("ttt","not empty");
            final String commentId = UUID.randomUUID().toString();

            final Map<String,Object> commentMap = new HashMap<>();
            commentMap.put("comment",comment);
            commentMap.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
            commentMap.put("time",System.currentTimeMillis());
            commentMap.put("replies",0);
            commentMap.put("commentId",commentId);
            commentMap.put("likes",0);

            postsRef.document(postId).collection("Comments").document(commentId)
                    .set(commentMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    comments.add(new Comment(commentMap));
                    commentsAdapter.notifyItemInserted(comments.size()-1);
                    commentsRv.scrollToPosition(comments.size()-1);
                    commentSubmitIv.setClickable(true);

                    postsRef.document(postId).update("comments", FieldValue.increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    commentCountTv.setText(String.valueOf(
                                            Integer.parseInt(commentCountTv.getText().toString())+1
                                    ));
                                }
                            });


                    sendCommentNotification(getResources().getString(R.string.commented_post));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    commentSubmitIv.setClickable(true);
                    Toast.makeText(getContext(),
                            "An error occurred while commenting! Please try again",
                            Toast.LENGTH_SHORT).show();
                    Log.d("ttt",e.getMessage());
                }
            });

        }

    }


    private void sendCommentNotification(String message){

        postsRef.document(postId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if(snapshot.exists()){

                    final String creatorId =snapshot.getString("publisherId");

                    if(!creatorId.
                            equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){


                        final DocumentReference userRef =
                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(FirebaseAuth.getInstance()
                                                .getCurrentUser().getUid());


                        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {
                                if(snapshot.exists()){
                                    final String username = snapshot.getString("username");

                                    FirestoreNotificationSender.sendFirestoreNotification(
                                            creatorId ,
                                            "postComment",
                                            username + message,
                                            username,
                                            postId
                                    );
                                }
                            }
                        });
                    }
                }
            }
        });


    }
    @Override
    public void showReplies(RecyclerView repliesRv,int position, boolean isReplying) {

        commentsRv.scrollToPosition(position);

        currentFocusedComment = comments.get(position);

        final ArrayList<CommentReply> commentReplies = new ArrayList<>();
        final RepliesAdapter adapter = new RepliesAdapter(commentReplies,this,
                currentFocusedComment.getCommentId(),
                postsRef.document(postId).collection("Comments")
                        .document(currentFocusedComment.getCommentId()),getContext());

        repliesRv.setAdapter(adapter);

        postsRef.document(postId).collection("Comments")
                .document(currentFocusedComment.getCommentId()).collection("Replies")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if(!snapshots.isEmpty()){
                    commentReplies.addAll(snapshots.toObjects(CommentReply.class));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if(task.isSuccessful() && !commentReplies.isEmpty()){
                    adapter.notifyDataSetChanged();
                    if(isReplying){

                        commentEd.setText("@"+comments.get(position).getUserName()+" ");
                        repliesRv.scrollToPosition(commentReplies.size()-1);
                        showKeyboard();

                    }else{

                        commentsRv.scrollToPosition(position);

                    }
                }else{

                    commentEd.setText("@"+comments.get(position).getUserName()+" ");
                    showKeyboard();

                }

            }
        });

        commentSubmitIv.setOnClickListener(v-> addReply(repliesRv,commentReplies,adapter,position));
    }

    @Override
    public void scrollToPosition(int position) {
        commentsRv.scrollToPosition(position);
    }

    private void addReply(RecyclerView rv,ArrayList<CommentReply> replies,RepliesAdapter adapter,
                          int commentPosition){

        commentSubmitIv.setClickable(false);
        final String reply = commentEd.getText().toString();

        if(!reply.trim().isEmpty()){

            commentEd.setText("");
            final String replyId = UUID.randomUUID().toString();

            final Map<String,Object> replyMap = new HashMap<>();
            replyMap.put("reply",reply);
            replyMap.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
            replyMap.put("time",System.currentTimeMillis());
            replyMap.put("replyId",replyId);
            replyMap.put("likes",0);

            postsRef.document(postId).collection("Comments").document(currentFocusedComment.getCommentId())
                    .collection("Replies").document(replyId).set(replyMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            postsRef.document(postId).collection("Comments")
                                    .document(comments.get(commentPosition).getCommentId())
                                    .update("replies",FieldValue.increment(1));


                            replies.add(new CommentReply(replyMap));
                            adapter.notifyItemInserted(replies.size()-1);
                            rv.smoothScrollToPosition(replies.size()-1);

                            sendCommentNotification(getResources().getString(R.string.replied_comment));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    commentSubmitIv.setClickable(true);
                    Toast.makeText(getContext(),
                            "An error occurred while replying! Please try again",
                            Toast.LENGTH_SHORT).show();
                    Log.d("ttt",e.getMessage());
                }
            });

        }
    }

    private void showKeyboard(){

        final Handler handler = new Handler();
        Runnable runnable;
        handler.post(
                runnable = new Runnable() {
                    public void run() {
                        commentEd.requestFocus();
                        InputMethodManager inputMethodManager =
                                (InputMethodManager)getContext()
                                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputMethodManager.toggleSoftInputFromWindow(
                                commentEd.getApplicationWindowToken(),
                                InputMethodManager.SHOW_FORCED, 0);

                        commentEd.requestFocus();
                    }
                });
        handler.removeCallbacks(runnable);

    }

    @Override
    public void likeComment(int position,TextView likesTv) {

        likesTv.setClickable(false);
        final Comment comment = comments.get(position);
        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DocumentReference commentRef =
                postsRef.document(postId).collection("Comments").document(
                        comment.getCommentId());

        if(comment.isLikedByUser()){

            //already liked needs to remove like

            if(comment.getLikes() > 0){

                comment.setLikes(comment.getLikes() - 1);

                likesTv.setText(getResources().getString(R.string.likes) +" "+ comment.getLikes());
            }

            likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.black,
                    null));

            commentRef.collection("CommentLikes").document(currentUid).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            comment.setLikedByUser(false);

                            commentRef.update("likes",FieldValue.increment(-1));

                            likesTv.setClickable(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    comment.setLikes(comment.getLikes() + 1);

                    likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.red,
                            null));

                    Toast.makeText(getContext(), R.string.failed_to_remove_like, Toast.LENGTH_SHORT).show();

                        likesTv.setText(getResources().getString(R.string.likes)+" "+
                                (comment.getLikes()));

                    likesTv.setClickable(true);

                }
            });


        }else{


            comment.setLikes(comment.getLikes() + 1);

            likesTv.setText(getResources().getString(R.string.likes)+" "+
                        (comment.getLikes()));

            likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.red,null));

            final Map<String, String> commentLike  = new HashMap<>();
            commentLike.put("userId",currentUid);
            commentRef.collection("CommentLikes").document(currentUid)
                    .set(commentLike)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            comment.setLikedByUser(true);

                            commentRef.update("likes",FieldValue.increment(1));

                            likesTv.setClickable(true);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    comment.setLikes(comment.getLikes() - 1);

                    likesTv.setTextColor(ResourcesCompat.getColor(getResources(),
                            R.color.black,null));

                        likesTv.setText(getResources().getString(R.string.likes)+" "+ (comment.getLikes()));

                    Toast.makeText(getContext(), R.string.failed_to_like,
                            Toast.LENGTH_SHORT).show();

                    likesTv.setClickable(true);
                }
            });

        }
    }

    @Override
    public void likeReply(CommentReply reply,TextView likesTv,String commentId) {

        likesTv.setClickable(false);
        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DocumentReference replyRef =
                postsRef.document(postId).collection("Comments").document(
                        commentId).collection("Replies").document(reply.getReplyId());

        if(reply.isLikedByUser()){

            //already liked needs to remove like

            if(reply.getLikes() > 0){

                reply.setLikes(reply.getLikes() - 1);

                likesTv.setText(getResources().getString(R.string.likes)+" "+ (reply.getLikes()));
            }

            likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.black,
                    null));

            replyRef.collection("ReplyLikes").document(currentUid).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            reply.setLikedByUser(false);

                            replyRef.update("likes",FieldValue.increment(-1));

                            likesTv.setClickable(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    reply.setLikes(reply.getLikes() + 1);
                    likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.red,null));

                    Toast.makeText(getContext(), R.string.failed_to_remove_like, Toast.LENGTH_SHORT).show();

                    likesTv.setText(getResources().getString(R.string.likes)+" "+
                            (reply.getLikes()));

                    likesTv.setClickable(true);

                }
            });


        }else{


            reply.setLikes(reply.getLikes() + 1);

            likesTv.setText(getResources().getString(R.string.likes)+" "+
                    (reply.getLikes()));

            likesTv.setTextColor(ResourcesCompat.getColor(getResources(),R.color.red,null));

            final Map<String, String> commentLike  = new HashMap<>();
            commentLike.put("userId",currentUid);
            replyRef.collection("ReplyLikes").document(currentUid)
                    .set(commentLike)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            reply.setLikedByUser(true);

                            replyRef.update("likes",FieldValue.increment(1));

                            likesTv.setClickable(true);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    reply.setLikes(reply.getLikes() - 1);

                    likesTv.setTextColor(ResourcesCompat.getColor(getResources(),
                            R.color.black,null));

                    likesTv.setText(getResources().getString(R.string.likes)+" "+
                            (reply.getLikes()));

                    Toast.makeText(getContext(), R.string.failed_to_like,
                            Toast.LENGTH_SHORT).show();

                    likesTv.setClickable(true);
                }
            });

        }

    }



    private class ChatsScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingComments &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                ReadComments(false);

            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(chatsScrollListener!=null){
            commentsRv.removeOnScrollListener(chatsScrollListener);
        }
    }


}
