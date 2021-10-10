package hashed.app.ampassadors.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.CommentsAdapter;
import hashed.app.ampassadors.Adapters.RepliesAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.Comment;
import hashed.app.ampassadors.Objects.CommentReply;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.SigninUtil;


public class CommentsFragment extends BottomSheetDialogFragment implements View.OnClickListener,
        CommentsAdapter.CommentsListener, RepliesAdapter.RepliesListener {

  private static final int COMMENTS_SIZE = 10;
  private static final int TYPE_COMMENT_LIKE = 1,TYPE_REPLY_LIKE = 2;
  private final String postId;
  //database
  CollectionReference postsRef;
  //adapter
  CommentsAdapter commentsAdapter;
  boolean isLoadingComments;
  List<Comment> comments;
  //Views
  EditText commentEd;
  RecyclerView commentsRv;
  TextView commentCountTv;
  ImageView commentSubmitIv;
  int commentsCount;
  private Query commentsQuery;
  private ChatsScrollListener chatsScrollListener;
  private DocumentSnapshot lastDocSnap;
  private Comment currentFocusedComment;
  private final boolean isUserPost;
  private final String creatorId;
  private final int postType;

  public CommentsFragment(String postId, int commentsCount,boolean isUserPost,
                          String creatorId,int postType) {
    this.postId = postId;
    this.commentsCount = commentsCount;
    this.isUserPost = isUserPost;
    this.creatorId = creatorId;
    this.postType = postType;
  }


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext(), getTheme());
    bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
      @Override
      public void onShow(DialogInterface dialogInterface) {

        BottomSheetDialog dialogc = (BottomSheetDialog) dialogInterface;
        // When using AndroidX the resource can be found at com.google.android.material.R.id.design_bottom_sheet
        FrameLayout bottomSheet =  dialogc.findViewById(R.id.design_bottom_sheet);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(requireContext().getResources().getDisplayMetrics().heightPixels);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

      }
    });

    return bottomSheetDialog;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Demo_BottomSheetDialog);

    comments = new ArrayList<>();

    postsRef = FirebaseFirestore.getInstance().collection("Posts");

    DocumentReference documentReference;
    if(isUserPost){
      commentsAdapter = new CommentsAdapter(comments, this, postId, requireActivity(),creatorId);
      documentReference = FirebaseFirestore.getInstance().collection("Users")
              .document(creatorId)
              .collection("UserPosts")
              .document(postId);

    }else{
      commentsAdapter = new CommentsAdapter(comments, this, postId, requireActivity());
      documentReference = postsRef.document(postId);

    }

    commentsQuery = documentReference.collection("Comments")
            .limit(COMMENTS_SIZE).orderBy("likes", Query.Direction.DESCENDING);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_comment, container, false);

    commentEd = view.findViewById(R.id.commentEd);
    commentsRv = view.findViewById(R.id.commentsRv);
    commentSubmitIv = view.findViewById(R.id.commentSubmitIv);
    commentCountTv = view.findViewById(R.id.commentCountTv);

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

    if (lastDocSnap != null) {
      commentsQuery = commentsQuery.startAfter(lastDocSnap);
    }

    commentsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if (!snapshots.isEmpty()) {

          lastDocSnap = snapshots.getDocuments().get(snapshots.size() - 1);

          if (isInitial) {
            comments.addAll(snapshots.toObjects(Comment.class));
          } else {
            comments.addAll(comments.size(), snapshots.toObjects(Comment.class));
          }
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
          Log.d("ttt", "comments: " + comments.size());
          if (isInitial) {

            commentsAdapter.notifyDataSetChanged();

            if (task.getResult().size() == COMMENTS_SIZE) {
              commentsRv.addOnScrollListener(chatsScrollListener = new ChatsScrollListener());
            }

          } else {

            commentsAdapter.notifyItemRangeInserted(comments.size(), task.getResult().size());

          }
        }
      }
    });

  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.commentSubmitIv) {
      addComment();
    }
  }

  private void addComment() {

    if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

      SigninUtil.getInstance(getContext(),
              getActivity()).show();
    }else{

      commentSubmitIv.setClickable(false);
      Log.d("ttt", "clicked");
      final String comment = commentEd.getText().toString();

      if (!comment.trim().isEmpty()) {

        commentEd.setText("");

        Log.d("ttt", "not empty");
        final String commentId = UUID.randomUUID().toString();

        final Map<String, Object> commentMap = new HashMap<>();
        commentMap.put("comment", comment);
        commentMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        commentMap.put("time", System.currentTimeMillis());
        commentMap.put("replies", 0);
        commentMap.put("commentId", commentId);
        commentMap.put("likes", 0);

        DocumentReference documentReference;
        if(isUserPost){

          documentReference = FirebaseFirestore.getInstance().collection("Users")
                  .document(creatorId)
                  .collection("UserPosts")
                  .document(postId);

        }else{
          documentReference = postsRef.document(postId);
        }

        documentReference.collection("Comments").document(commentId)
                .set(commentMap).addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {

            comments.add(new Comment(commentMap));
            commentsAdapter.notifyItemInserted(comments.size() - 1);
            commentsRv.scrollToPosition(comments.size() - 1);
            commentSubmitIv.setClickable(true);

            documentReference.update("comments", FieldValue.increment(1))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                        commentCountTv.setText(String.valueOf(
                                Integer.parseInt(commentCountTv.getText().toString()) + 1
                        ));
                      }
                    });


            sendCommentNotification(comment);

          }
        }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            commentSubmitIv.setClickable(true);
            Toast.makeText(getContext(),
                    "An error occurred while commenting! Please try again",
                    Toast.LENGTH_SHORT).show();
            Log.d("ttt", e.getMessage());
          }
        });

      }
    }


  }

  private void sendReplyNotification(String message,String userId){

    final DocumentReference userRef =
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {
        if (snapshot.exists()) {
          final String username = snapshot.getString("username");
          final String imageUrl = snapshot.getString("imageUrl");

          final String currentUid =FirebaseAuth.getInstance().getCurrentUser().getUid();

          final String notificationType = postType == PostData.TYPE_NEWS? FirestoreNotificationSender.TYPE_POST_REPLY:FirestoreNotificationSender.TYPE_POLL_REPLY ;

          FirestoreNotificationSender.sendFirestoreNotification(
                  userId,
                  notificationType,
                  message,
                  username + " replied to your comment",
                  postId + "|" + creatorId);

          final Data data = new Data(
                  currentUid,
                  message,
                  username + " replied to your comment",
                  null,
                  "Post Comment",
                  notificationType,
                  postId + "|" + creatorId);

          if(imageUrl!=null && !imageUrl.isEmpty()){
            data.setSenderImageUrl(imageUrl);
          }

          CloudMessagingNotificationsSender.sendNotification(userId, data);
        }
      }
    });

  }


  private void sendCommentNotification(String message) {

    DocumentReference documentReference;
    if(isUserPost){

      documentReference = FirebaseFirestore.getInstance().collection("Users")
              .document(creatorId)
              .collection("UserPosts")
              .document(postId);

    }else{
      documentReference = postsRef.document(postId);
    }

    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {

        if (snapshot.exists()) {

          final String creatorId = snapshot.getString("publisherId");
          final String postTitle = snapshot.getString("title");

          if (!creatorId.
                  equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            final DocumentReference userRef =
                    FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                  final String username = snapshot.getString("username");
                  final String imageUrl = snapshot.getString("imageUrl");

                  final String currentUid =FirebaseAuth.getInstance().getCurrentUser().getUid();

                  FirestoreNotificationSender.sendFirestoreNotification(
                          creatorId,
                          postType == PostData.TYPE_NEWS? FirestoreNotificationSender.TYPE_POST_COMMENT:FirestoreNotificationSender.TYPE_POLL_COMMENT,
                          message,
                          username + " Commented on your post", postId);

                  final Data data = new Data(
                          currentUid,
                          message,
                          username + " Commented on your post",
                          null,
                          "Post Comment",
                          postType == PostData.TYPE_NEWS? FirestoreNotificationSender.TYPE_POST_COMMENT:FirestoreNotificationSender.TYPE_POLL_COMMENT,
                          postId);

                  if(imageUrl!=null && !imageUrl.isEmpty()){
                    data.setSenderImageUrl(imageUrl);
                  }

                  CloudMessagingNotificationsSender.sendNotification(creatorId, data);
                }
              }
            });
          }
        }
      }
    });


  }

  private void sendCommentLikeNotification(String userId,String body,int likeType) {

    DocumentReference documentReference;
    String destinationID;
    if(isUserPost){

      documentReference = FirebaseFirestore.getInstance().collection("Users")
              .document(creatorId)
              .collection("UserPosts")
              .document(postId);

      destinationID = postId + "|" + creatorId;

    }else{
      documentReference = postsRef.document(postId);

      destinationID = postId;
    }


    documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {

        if (snapshot.exists()) {

          if (!userId.
                  equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            final DocumentReference userRef =
                    FirebaseFirestore.getInstance().collection("Users")
                            .document(FirebaseAuth.getInstance()
                                    .getCurrentUser().getUid());

            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot snapshot) {
                if (snapshot.exists()) {
                  final String username = snapshot.getString("username");
                  final String imageUrl = snapshot.getString("imageUrl");

                  final String currentUid =FirebaseAuth.getInstance().getCurrentUser().getUid();

                  final String senderName =
                          username + (likeType == TYPE_COMMENT_LIKE?" Liked your comment":" Liked your reply");


                  final String notificationType =
                          postType == PostData.TYPE_NEWS
                                  ?FirestoreNotificationSender.TYPE_POST_COMMENT_LIKE
                                  :FirestoreNotificationSender.TYPE_POLL_COMMENT_LIKE;

                  FirestoreNotificationSender.sendFirestoreNotification(
                          userId, notificationType, body, senderName, destinationID);

                  final Data data = new Data(
                          currentUid,
                          body,
                          senderName,
                          null,
                          notificationType,
                          notificationType,
                          destinationID);

                  if(imageUrl!=null && !imageUrl.isEmpty()){
                    data.setSenderImageUrl(imageUrl);
                  }

                  CloudMessagingNotificationsSender.sendNotification(userId, data);
                }
              }
            });
          }
        }
      }
    });


  }


  @Override
  public void showReplies(RecyclerView repliesRv, int position, boolean isReplying) {

    commentsRv.scrollToPosition(position);

    currentFocusedComment = comments.get(position);

    final ArrayList<CommentReply> commentReplies = new ArrayList<>();

    DocumentReference documentReference;
    if(isUserPost){

      documentReference = FirebaseFirestore.getInstance().collection("Users")
              .document(creatorId)
              .collection("UserPosts")
              .document(postId);

    }else{
      documentReference = postsRef.document(postId);
    }




    final RepliesAdapter adapter = new RepliesAdapter(commentReplies, this,
            currentFocusedComment.getCommentId(),
            documentReference.collection("Comments")
                    .document(currentFocusedComment.getCommentId()), getContext());

    repliesRv.setAdapter(adapter);


    documentReference.collection("Comments")
            .document(currentFocusedComment.getCommentId()).collection("Replies")
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if (!snapshots.isEmpty()) {
          commentReplies.addAll(snapshots.toObjects(CommentReply.class));
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (task.isSuccessful() && !commentReplies.isEmpty()) {
          adapter.notifyDataSetChanged();
          if (isReplying) {

            commentEd.setText("@" + comments.get(position).getUserName() + " ");
            repliesRv.scrollToPosition(commentReplies.size() - 1);
            showKeyboard();

          } else {

            commentsRv.scrollToPosition(position);

          }
        } else {

          commentEd.setText("@" + comments.get(position).getUserName() + " ");
          showKeyboard();

        }

      }
    });

    commentSubmitIv.setOnClickListener(v -> addReply(repliesRv, commentReplies, adapter, position));
  }

  @Override
  public void scrollToPosition(int position) {
    commentsRv.scrollToPosition(position);
  }

  private void addReply(RecyclerView rv, ArrayList<CommentReply> replies, RepliesAdapter adapter,
                        int commentPosition) {


              if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(getContext(),
                      getActivity()).show();
          }else {

                commentSubmitIv.setClickable(false);
                final String reply = commentEd.getText().toString();

                if (!reply.trim().isEmpty()) {

                  commentEd.setText("");
                  final String replyId = UUID.randomUUID().toString();

                  final Map<String, Object> replyMap = new HashMap<>();
                  replyMap.put("reply", reply);
                  replyMap.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                  replyMap.put("time", System.currentTimeMillis());
                  replyMap.put("replyId", replyId);
                  replyMap.put("likes", 0);

                  DocumentReference documentReference;
                  if(isUserPost){

                    documentReference = FirebaseFirestore.getInstance().collection("Users")
                            .document(creatorId)
                            .collection("UserPosts")
                            .document(postId);

                  }else{
                    documentReference = postsRef.document(postId);
                  }


                  documentReference.collection("Comments").document(currentFocusedComment.getCommentId())
                          .collection("Replies").document(replyId).set(replyMap)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              documentReference.collection("Comments")
                                      .document(comments.get(commentPosition).getCommentId())
                                      .update("replies", FieldValue.increment(1));


                              replies.add(new CommentReply(replyMap));
                              adapter.notifyItemInserted(replies.size() - 1);
                              rv.smoothScrollToPosition(replies.size() - 1);

                              sendReplyNotification(getResources().getString(R.string.replied_comment),
                                      comments.get(commentPosition).getUserId());

//                              sendCommentNotification(getResources().getString(R.string.replied_comment));

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      commentSubmitIv.setClickable(true);
                      Toast.makeText(getContext(),
                              "An error occurred while replying! Please try again",
                              Toast.LENGTH_SHORT).show();
                      Log.d("ttt", e.getMessage());
                    }
                  });

                }
              }
  }
  private void showKeyboard() {

    final Handler handler = new Handler();
    Runnable runnable;
    handler.post(
            runnable = new Runnable() {
              public void run() {
                commentEd.requestFocus();
                InputMethodManager inputMethodManager =
                        (InputMethodManager) getContext()
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
  public void likeComment(int position, TextView likesTv) {

              if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(getContext(),
                      getActivity()).show();
          }else {
                likesTv.setClickable(false);
                final Comment comment = comments.get(position);
                final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                 DocumentReference documentReference;
                  if(isUserPost){

                    documentReference = FirebaseFirestore.getInstance().collection("Users")
                            .document(creatorId)
                            .collection("UserPosts")
                            .document(postId);

                  }else{
                    documentReference = postsRef.document(postId);
                  }

                final DocumentReference commentRef =
                        documentReference.collection("Comments").document(
                                comment.getCommentId());

                if (comment.isLikedByUser()) {

                  //already liked needs to remove like

                  if (comment.getLikes() > 0) {

                    comment.setLikes(comment.getLikes() - 1);

                    likesTv.setText(getResources().getString(R.string.likes) + " " + comment.getLikes());
                  }

                  likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black,
                          null));

                  commentRef.collection("CommentLikes").document(currentUid).delete()
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              comment.setLikedByUser(false);


                              commentRef.update("likes", FieldValue.increment(-1));

//                              FirebaseFirestore.getInstance().collection("Users")
//                                              .document(currentUid).update("Likes",FieldValue.arrayRemove(comment.getCommentId()));


                              likesTv.setClickable(true);

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                      comment.setLikes(comment.getLikes() + 1);

                      likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red,
                              null));

                      Toast.makeText(getContext(), R.string.failed_to_remove_like, Toast.LENGTH_SHORT).show();

                      likesTv.setText(getResources().getString(R.string.likes) + " " + (comment.getLikes()));

                      likesTv.setClickable(true);

                    }
                  });


                } else {


                  comment.setLikes(comment.getLikes() + 1);

                  likesTv.setText(getResources().getString(R.string.likes) + " " +
                          (comment.getLikes()));

                  likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));

                  final Map<String, String> commentLike = new HashMap<>();
                  commentLike.put("userId", currentUid);
                  commentRef.collection("CommentLikes").document(currentUid)
                          .set(commentLike)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              comment.setLikedByUser(true);

                              commentRef.update("likes", FieldValue.increment(1));

//                              FirebaseFirestore.getInstance().collection("Users")
//                                      .document(currentUid).update("Likes",FieldValue.arrayUnion(comment.getCommentId()));
//

                              likesTv.setClickable(true);

                              if(!comment.getUserId().equals(currentUid)){
                                sendCommentLikeNotification(comment.getUserId(),
                                        comment.getComment(), TYPE_COMMENT_LIKE);
                              }

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                      comment.setLikes(comment.getLikes() - 1);

                      likesTv.setTextColor(ResourcesCompat.getColor(getResources(),
                              R.color.black, null));

                      likesTv.setText(getResources().getString(R.string.likes) + " " + (comment.getLikes()));

                      Toast.makeText(getContext(), R.string.failed_to_like,
                              Toast.LENGTH_SHORT).show();

                      likesTv.setClickable(true);
                    }
                  });

                }

              }
  }

  @Override
  public void likeReply(CommentReply reply, TextView likesTv, String commentId) {


         if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(getContext(),
                      getActivity()).show();
          }else {

                likesTv.setClickable(false);
                final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                 DocumentReference documentReference;
                  if(isUserPost){

                    documentReference = FirebaseFirestore.getInstance().collection("Users")
                            .document(creatorId)
                            .collection("UserPosts")
                            .document(postId);

                  }else{
                    documentReference = postsRef.document(postId);
                  }


                final DocumentReference replyRef =
                        documentReference.collection("Comments").document(
                                commentId).collection("Replies").document(reply.getReplyId());

                if (reply.isLikedByUser()) {

                  //already liked needs to remove like

                  if (reply.getLikes() > 0) {

                    reply.setLikes(reply.getLikes() - 1);

                    likesTv.setText(getResources().getString(R.string.likes) + " " + (reply.getLikes()));
                  }

                  likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black,
                          null));

                  replyRef.collection("ReplyLikes").document(currentUid).delete()
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              reply.setLikedByUser(false);

                              replyRef.update("likes", FieldValue.increment(-1));

                              likesTv.setClickable(true);

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      reply.setLikes(reply.getLikes() + 1);
                      likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));

                      Toast.makeText(getContext(), R.string.failed_to_remove_like, Toast.LENGTH_SHORT).show();

                      likesTv.setText(getResources().getString(R.string.likes) + " " + (reply.getLikes()));

                      likesTv.setClickable(true);

                    }
                  });


                } else {


                  reply.setLikes(reply.getLikes() + 1);

                  likesTv.setText(getResources().getString(R.string.likes) + " " +
                          (reply.getLikes()));

                  likesTv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));

                  final Map<String, String> commentLike = new HashMap<>();
                  commentLike.put("userId", currentUid);
                  replyRef.collection("ReplyLikes").document(currentUid)
                          .set(commentLike)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              reply.setLikedByUser(true);

                              replyRef.update("likes", FieldValue.increment(1));

                              likesTv.setClickable(true);

                              if(!reply.getUserId().equals(currentUid)){
                                sendCommentLikeNotification(reply.getUserId(),reply.getReply(),
                                        TYPE_REPLY_LIKE);
                              }

                            }
                          }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                      reply.setLikes(reply.getLikes() - 1);

                      likesTv.setTextColor(ResourcesCompat.getColor(getResources(),
                              R.color.black, null));

                      likesTv.setText(getResources().getString(R.string.likes) + " " +
                              (reply.getLikes()));

                      Toast.makeText(getContext(), R.string.failed_to_like,
                              Toast.LENGTH_SHORT).show();

                      likesTv.setClickable(true);
                    }
                  });

                }

              }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (chatsScrollListener != null) {
      commentsRv.removeOnScrollListener(chatsScrollListener);
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


}
