package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Objects.PostNewsPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final List<PostNewsPreview> posts;
  private final Context context;
  public List<Integer> loadingItems = new ArrayList<>();

  public NewsAdapter(List<PostNewsPreview> posts, Context context) {
    this.posts = posts;
    this.context = context;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    switch (viewType) {

      case Files.IMAGE:
        return new NewsImageVh(LayoutInflater.from(context).inflate(R.layout.news_image_item_layout
                , parent, false));

      case Files.VIDEO:
        return new NewsVideosVh(LayoutInflater.from(context).inflate(R.layout.news_video_item_layout
                , parent, false));

      case Files.DOCUMENT:
        return new NewsAttachmentVh(LayoutInflater.from(context).inflate(
                R.layout.news_attachment_item_layout
                , parent, false));

      case Files.TEXT:
        return new NewsTextVh(LayoutInflater.from(context).inflate(R.layout.news_text_item_layout
                , parent, false));

    }

    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    switch (holder.getItemViewType()) {
      case Files.IMAGE:
        ((NewsImageVh) holder).bind(posts.get(position));
        break;

      case Files.VIDEO:
        ((NewsVideosVh) holder).bind(posts.get(position));
        break;

      case Files.DOCUMENT:
        ((NewsAttachmentVh) holder).bind(posts.get(position));
        break;

      case Files.TEXT:
        ((NewsTextVh) holder).bind(posts.get(position));
        break;
    }

  }

  @Override
  public int getItemCount() {
    return posts.size();
  }

  @Override
  public int getItemViewType(int position) {
    return posts.get(position).getAttachmentType();
  }

  public class NewsImageVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final ImageView newsIv;
    private final TextView newsTitleTv;

    public NewsImageVh(@NonNull View itemView) {
      super(itemView);
      newsIv = itemView.findViewById(R.id.newsIv);
      newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
    }

    private void bind(PostNewsPreview postData) {

      if (postData.getAttachmentUrl() != null) {
        Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(newsIv);
      }
      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

      view.getContext().startActivity(new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
  }

  public class NewsVideosVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final ImageView newsIv;
    private final TextView newsTitleTv;

    public NewsVideosVh(@NonNull View itemView) {
      super(itemView);
      newsIv = itemView.findViewById(R.id.newsIv);
      newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
    }

    private void bind(PostNewsPreview postData) {

      if (postData.getVideoThumbnailUrl() != null) {
        Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerCrop().into(newsIv);
      }

      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

      view.getContext().startActivity(new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));


    }
  }

  public class NewsAttachmentVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final TextView newsTitleTv;

    public NewsAttachmentVh(@NonNull View itemView) {
      super(itemView);
      newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
    }

    private void bind(PostNewsPreview postData) {
      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {


      view.getContext().startActivity(new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
  }


  public class NewsTextVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final TextView newsTitleTv,newsDescriptionTv;

    public NewsTextVh(@NonNull View itemView) {
      super(itemView);
      newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
      newsDescriptionTv = itemView.findViewById(R.id.newsDescriptionTv);
    }

    private void bind(PostNewsPreview postData) {
      newsTitleTv.setText(postData.getTitle());
      newsDescriptionTv.setText(postData.getDescription());
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

      view.getContext().startActivity(new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
  }

}

