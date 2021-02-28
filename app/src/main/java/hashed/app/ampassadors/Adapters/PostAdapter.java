package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    List<PostData>data;
    Context context ;
    public PostAdapter(List<PostData>data , Context context ){
        this.data = data ;
        this.context = context;
    }
    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_item_post , parent , false);


        return new PostAdapter.PostHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        PostData postData = data.get(position);
        holder.description.setText(postData.getDescription());
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.likesnum.setText(postData.getLikes()+1+"");
                holder.likes.setTextColor(Color.RED);

            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PostHolder extends RecyclerView.ViewHolder {
      CircleImageView userImage ;
      TextView username ;
      TextView description ;
      TextView likesnum ;
      TextView commentsnum ;
      Button comment;
      Button likes ;



        public PostHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.user_image_post_item);
            username = itemView.findViewById(R.id.user_name_in_post_item);
            description = itemView.findViewById(R.id.description_post_item);
            likesnum = itemView.findViewById(R.id.count_likes_post_item);
            commentsnum = itemView.findViewById(R.id.count_comment_post_item);
            comment = itemView.findViewById(R.id.comment_btn_post_item);
            likes = itemView.findViewById(R.id.like_btn_post_item);

        }
    }
}
