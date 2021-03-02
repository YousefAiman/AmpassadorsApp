package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.Comments;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    Context context;
    List<Comments> comments;

    public CommentAdapter(Context context, List<Comments> comments) {
        this.context = context;
        this.comments = comments;

    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);

        return new CommentAdapter.CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Comments comment = comments.get(position);
        holder.commentstext.setText(comment.getCommentstext());
        holder.replynum.setText(comment.getRepliesnumber());
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.like.setTextColor(Color.RED);
            }
        });
        holder.username.setText(comment.getUserid());
        holder.time.setText(comment.getTime()+"");
        Picasso.get().load(comment.getUserimagecomment()).into(holder.useriamge);
//        if ()
    }

    @Override
    public int getItemCount() {
        return comments.size();

    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        TextView username;
        CircleImageView useriamge ;
        TextView commentstext;
        TextView like;
        TextView comment;
        TextView time;
        TextView replynum;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_comments);
            commentstext = itemView.findViewById(R.id.comment_text_ed);
            like = itemView.findViewById(R.id.comments_likes_count);
            comment = itemView.findViewById(R.id.reply_comments);
            time = itemView.findViewById(R.id.timecoments);
            replynum = itemView.findViewById(R.id.likesnumberincomment);
            useriamge = itemView.findViewById(R.id.image_uesr_in_reply);


        }

    }
    public  void bind(PostData postData){

    }
}
