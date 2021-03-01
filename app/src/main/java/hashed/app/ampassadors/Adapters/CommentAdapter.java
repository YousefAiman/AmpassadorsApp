package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Comment;

import java.util.List;

import hashed.app.ampassadors.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    Context context ;
    List<Comment>comments ;

    public CommentAdapter(Context context , List<Comment>comments){
        this.context = context ;
        this.comments = comments;

    }
    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_edit_design , parent ,false);

        return new CommentAdapter.CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
    Comment comment = comments.get(position);

    }

    @Override
    public int getItemCount() {
        return comments.size();

    }

    public class CommentHolder extends RecyclerView.ViewHolder {
        TextView username ;
        TextView commentstext;
        TextView like ;
        TextView comment ;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username_comments);
            commentstext = itemView.findViewById(R.id.comment_text_ed);
            like = itemView.findViewById(R.id.comments_likes_count);
            comment = itemView.findViewById(R.id.reply_comments);
        }

    }
}
