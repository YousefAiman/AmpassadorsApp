package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class ReprotedAdapter extends RecyclerView.Adapter<ReprotedAdapter.ReportedHolder> {
    Context context ;
    List<PostData>postData;

    public ReprotedAdapter(Context context ,List<PostData>postData){
        this.context = context ;
        this.postData = postData;

    }
    @NonNull
    @Override
    public ReportedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_image_item_layout,parent,false);

        return new ReprotedAdapter.ReportedHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportedHolder holder, int position) {
        PostData post = postData.get(position);
        holder.title.setText(post.getTitle());
        Picasso.get().load(post.getAttachmentUrl()).fit().into(holder.postImage);


    }

    @Override
    public int getItemCount() {
        return postData.size();
    }

    public class ReportedHolder extends RecyclerView.ViewHolder {

        ImageView postImage ;
        TextView title;

        public ReportedHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.newsIv);
            title = itemView.findViewById(R.id.newsTitleTv);
        }
    }
}
