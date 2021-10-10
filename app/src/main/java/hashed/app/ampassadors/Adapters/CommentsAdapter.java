package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.ProfileActiv;
import hashed.app.ampassadors.Objects.Comment;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentHolder> {

  private final List<Comment> comments;
  private final CollectionReference usersRef =
          FirebaseFirestore.getInstance().collection("Users");
  private final String currentUid;
  private final CommentsListener commentsListener;
  private final CollectionReference commentsRef;
  private final int redColor,blackColor;

  public CommentsAdapter(List<Comment> comments, CommentsListener commentsListener, String postId,
                         Context context) {
    this.comments = comments;
    this.commentsListener = commentsListener;
      commentsRef = FirebaseFirestore.getInstance().collection("Posts")
              .document(postId).collection("Comments");


    redColor = context.getResources().getColor(R.color.red);
        blackColor = context.getResources().getColor(R.color.black);
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

  }


  public CommentsAdapter(List<Comment> comments, CommentsListener commentsListener, String postId,
                         Context context,String creatorId) {
    this.comments = comments;
    this.commentsListener = commentsListener;
    commentsRef = FirebaseFirestore.getInstance().collection("Users")
            .document(creatorId).collection("UserPosts")
            .document(postId).collection("Comments");


    redColor = context.getResources().getColor(R.color.red);
    blackColor = context.getResources().getColor(R.color.black);
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  }

  @NonNull
  @Override
  public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new CommentsAdapter.CommentHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.comment_item_layout, parent, false));


  }

  @Override
  public void onBindViewHolder(@NonNull CommentHolder holder, int position) {

    holder.bind(comments.get(position));

  }

  @Override
  public int getItemCount() {
    return comments.size();

  }

  private void checkUserLikedComment(String commentId, TextView likesTv, Comment comment) {

    commentsRef.document(commentId).collection("CommentLikes")
            .document(currentUid).get().addOnSuccessListener(documentSnapshot -> {
      comment.setHasBeenCheckedForUserLike(documentSnapshot.exists());
      comment.setLikedByUser(documentSnapshot.exists());

      if (documentSnapshot.exists()) {
        likesTv.setTextColor(redColor);
      }else{
        likesTv.setTextColor(blackColor);
      }
    });

  }

  public interface CommentsListener {
    void showReplies(RecyclerView repliesRv, int position, boolean isReplying);

    void scrollToPosition(int position);

    void likeComment(int position, TextView likesTv);
  }

  public class CommentHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,
          View.OnLongClickListener {

    private final TextView usernameTv;
    private final CircleImageView imageIv;
    private final TextView commentTv;
    private final TextView timeTv;
    private final TextView likesTv;
    private final TextView showRepliesTv;
    private final TextView addCommentTv;
    private final RecyclerView repliesRv;

    public CommentHolder(@NonNull View itemView) {
      super(itemView);
      usernameTv = itemView.findViewById(R.id.usernameTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      commentTv = itemView.findViewById(R.id.commentTv);
      showRepliesTv = itemView.findViewById(R.id.showRepliesTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      likesTv = itemView.findViewById(R.id.likesTv);
      addCommentTv = itemView.findViewById(R.id.addCommentTv);
      repliesRv = itemView.findViewById(R.id.repliesRv);


      addCommentTv.setOnClickListener(this);
      likesTv.setOnClickListener(this);


    }

    private void bind(Comment comment) {

      if(!comment.getUserId().equals(currentUid)){
        itemView.setOnLongClickListener(this);
      }else{
        itemView.setOnLongClickListener(null);
      }


      imageIv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          imageIv.getContext().startActivity(new Intent(itemView.getContext(),
                  ProfileActiv.class).putExtra("userId",comments.get(getAdapterPosition())
                  .getUserId()).putExtra("ImageUrl",
                  comments.get(getAdapterPosition()).getUserImage())
                  .putExtra("username",comments.get(getAdapterPosition()).getUserName()));
        }
      });
      commentTv.setText(comment.getComment());

      likesTv.setText(itemView.getResources()
              .getString(R.string.likes) + " " + comment.getLikes());

      if (comment.isHasBeenCheckedForUserLike()) {
        if (comment.isLikedByUser()) {
          likesTv.setTextColor(redColor);
        }else{
          likesTv.setTextColor(blackColor);
        }
      } else {
        checkUserLikedComment(comment.getCommentId(), likesTv, comment);
      }

      if (comment.getReplies() > 0) {
        showRepliesTv.setVisibility(View.VISIBLE);

        showRepliesTv.setText(itemView.getResources().getString(R.string.show)
                +" "+comment.getReplies()+" "+(comment.getReplies()>1?
                itemView.getResources().getString(R.string.replies):
                itemView.getResources().getString(R.string.reply)));

        showRepliesTv.setOnClickListener(this);
      } else {
        showRepliesTv.setVisibility(View.GONE);
      }

      if (comment.getUserName() == null) {

        getUserData(comment.getUserId(), comment);

      } else {
        Picasso.get().load(comment.getUserImage()).fit().centerCrop().into(imageIv);
        usernameTv.setText(comment.getUserName());
      }

      timeTv.setText(TimeFormatter.formatTime(comment.getTime()));



    }


    private void getUserData(String userId, Comment comment) {

      usersRef.document(userId).get()
              .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                  if(documentSnapshot!=null && (!documentSnapshot.contains("rejected") || !documentSnapshot.getBoolean("rejected"))){
                    comment.setUserImage(documentSnapshot.getString("imageUrl"));
                    comment.setUserName(documentSnapshot.getString("username"));
                  }

                }
              }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

          if (comment.getUserImage() != null && !comment.getUserImage().isEmpty()) {
            Picasso.get().load(comment.getUserImage()).fit()
                    .centerCrop()
                    .into(imageIv);
          }
          usernameTv.setText(comment.getUserName());

        }
      });

    }


    @Override
    public void onClick(View view) {



      if (view.getId() == R.id.showRepliesTv) {

        showRepliesTv.setVisibility(View.GONE);
        repliesRv.setVisibility(View.VISIBLE);
        commentsListener.showReplies(repliesRv, getAdapterPosition(), false);

      } else if (view.getId() == R.id.addCommentTv) {

        if (repliesRv.getVisibility() == View.VISIBLE) {
          commentsListener.scrollToPosition(getAdapterPosition());
        } else {
          repliesRv.setVisibility(View.VISIBLE);
          commentsListener.showReplies(repliesRv, getAdapterPosition(), true);
        }

        if (showRepliesTv.getVisibility() == View.VISIBLE) {
          showRepliesTv.setVisibility(View.GONE);
        }

      } else if (view.getId() == R.id.likesTv) {

        commentsListener.likeComment(getAdapterPosition(), likesTv);

      }
    }

    @Override
    public boolean onLongClick(View view) {

      final BottomSheetDialog bsd = new BottomSheetDialog(view.getContext(), R.style.SheetDialog);
      final View parentView = LayoutInflater.from(view.getContext()).inflate(R.layout.comment_options_bsd, null);
      parentView.setBackgroundColor(Color.TRANSPARENT);

      final TextView tvReport = parentView.findViewById(R.id.tvReport);

      tvReport.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          tvReport.setClickable(false);

          final HashMap<String,Object> reportMap = new HashMap<>();
          reportMap.put("userId",currentUid);
          reportMap.put("time",System.currentTimeMillis());

          final DocumentReference commentRef = commentsRef.document(comments.get(getBindingAdapterPosition()).getCommentId());

          final CollectionReference reportsRef = commentRef.collection("Reports");

          reportsRef.document(currentUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

              if(task.getResult() == null || !task.getResult().exists()){

                reportsRef.document(currentUid).update(reportMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void unused) {

                    commentRef.update("reports", FieldValue.increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void unused) {

                                commentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                  @Override
                                  public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    if(documentSnapshot!= null && documentSnapshot.contains("reports") && documentSnapshot.getLong("reports") >= 5){

                                      commentRef.update("isDeleted",true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                          comments.remove(getBindingAdapterPosition());
                                          notifyItemRemoved(getBindingAdapterPosition());
                                        }
                                      });

                                    }

                                  }
                                });

                              }
                            });

                  }
                });
              }

            }
          });

          bsd.cancel();

        }
      });

      bsd.setContentView(parentView);
      bsd.show();

      return false;
    }
  }

}
