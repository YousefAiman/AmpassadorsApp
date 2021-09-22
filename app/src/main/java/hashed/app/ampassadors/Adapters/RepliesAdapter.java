package hashed.app.ampassadors.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import hashed.app.ampassadors.Activities.ProfileActiv;
import hashed.app.ampassadors.Objects.CommentReply;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesVh> {

    private final ArrayList<CommentReply> replies;

    private final CollectionReference usersRef =
            FirebaseFirestore.getInstance().collection("Users");
    private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final RepliesListener repliesListener;
    private final String commentId;
    private final CollectionReference repliesRef;
    private final int redColor
//            ,blackColor
            ;

    public RepliesAdapter(ArrayList<CommentReply> replies, RepliesListener repliesListener
            , String commentId, DocumentReference commentRef, Context context) {

        this.replies = replies;
        this.repliesListener = repliesListener;
        this.commentId = commentId;
        repliesRef = commentRef.collection("Replies");
        redColor = context.getResources().getColor(R.color.red);
//        blackColor = context.getResources().getColor(R.color.black);
    }

    @NonNull
    @Override
    public RepliesVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RepliesAdapter.RepliesVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reply_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RepliesVh holder, int position) {

        holder.bind(replies.get(position));

    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    private void checkUserLikedReply(int position, TextView likesTv) {

        repliesRef.document(replies.get(position).getReplyId())
                .collection("ReplyLikes").document(currentUid)
                .get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                replies.get(position).setHasBeenCheckedForUserLike(true);
                replies.get(position).setLikedByUser(true);
                likesTv.setTextColor(redColor);

            }
        });

    }

    public interface RepliesListener {
        void likeReply(CommentReply commentReply, TextView likeTv, String commentId);
    }

    public class RepliesVh extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CircleImageView imageIv;
        private final TextView usernameTv;
        private final TextView replyTv;
        private final TextView timeTv;
        private final TextView likesTv;

        public RepliesVh(@NonNull View itemView) {
            super(itemView);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            imageIv = itemView.findViewById(R.id.imageIv);
            replyTv = itemView.findViewById(R.id.replyTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likesTv = itemView.findViewById(R.id.likesTv);
        }

        @SuppressLint("SetTextI18n")
        private void bind(CommentReply commentReply) {
            imageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageIv.getContext().startActivity(new Intent(itemView.getContext(),
                            ProfileActiv.class).putExtra("userId", replies.get(getAdapterPosition())
                            .getUserId()).putExtra("ImageUrl",
                            replies.get(getAdapterPosition()).getUserImage())
                            .putExtra("username", replies.get(getAdapterPosition()).getUserName()));

                }
            });


            replyTv.setText(commentReply.getReply());

            likesTv.setText(itemView.getResources().getString(R.string.likes) + " "
                    + commentReply.getLikes());


            if (commentReply.isHasBeenCheckedForUserLike()) {

                if (commentReply.isLikedByUser()) {
                    likesTv.setTextColor(redColor);
                }

            } else {
                checkUserLikedReply(getAdapterPosition(), likesTv);
            }


            if (commentReply.getUserName() == null) {

                getUserData(commentReply.getUserId(), commentReply);
            } else {


                if (commentReply.getUserImage() != null && !commentReply.getUserImage().isEmpty()) {
                    Picasso.get().load(commentReply.getUserImage()).fit().centerCrop().into(imageIv);
                }


                usernameTv.setText(commentReply.getUserName());
            }

            timeTv.setText(TimeFormatter.formatTime(commentReply.getTime()));

            likesTv.setOnClickListener(this);
        }

        private void getUserData(String userId, CommentReply commentReply) {

            usersRef.document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {

                                commentReply.setUserImage(documentSnapshot.getString("imageUrl"));
                                commentReply.setUserName(documentSnapshot.getString("username"));

                            }
                        }
                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (commentReply.getUserImage() != null && !commentReply.getUserImage().isEmpty()) {
                        Picasso.get().load(commentReply.getUserImage()).fit()
                                .centerCrop()
                                .into(imageIv);
                    }

                    usernameTv.setText(commentReply.getUserName());

                }
            });

        }

        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.likesTv) {
                repliesListener.likeReply(replies.get(getAdapterPosition()), likesTv, commentId);
            }

        }


    }

}
