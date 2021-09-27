package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int POLL_TYPE = 10;

  private static final CollectionReference postsCollectionRef =
          FirebaseFirestore.getInstance().collection("Posts");
  private final List<PostData> posts;
  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private final Context context;
  private boolean isForUser;
  public List<Integer> loadingItems = new ArrayList<>();

  public PostAdapter(List<PostData> posts, Context context) {
    this.posts = posts;
    this.context = context;
  }

  public PostAdapter(List<PostData> posts, Context context,boolean isForUser) {
    this.posts = posts;
    this.context = context;
    this.isForUser = isForUser;
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


      case POLL_TYPE:
        return new PollPreviewVh(LayoutInflater.from(context).inflate(
                R.layout.poll_preview_item_layout, parent, false));

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

      case POLL_TYPE:
        ((PollPreviewVh) holder).bind(posts.get(position));
        break;
    }

  }

  @Override
  public int getItemCount() {
    return posts.size();
  }


  @Override
  public int getItemViewType(int position) {

    final PostData postData = posts.get(position);

    if (postData.getType() == PostData.TYPE_POLL){
      return POLL_TYPE;
    }else{
      return postData.getAttachmentType();
    }

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

    private void bind(PostData postData) {

      if (postData.getAttachmentUrl() != null) {
        Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(newsIv);
      }
      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

      final Intent intent = new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getBindingAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if(isForUser){
        intent.putExtra("isForUser", true);
        intent.putExtra("publisherId",posts.get(getBindingAdapterPosition()).getPublisherId());
      }

      view.getContext().startActivity(intent);

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

    private void bind(PostData postData) {

      if (postData.getVideoThumbnailUrl() != null) {
        Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerCrop().into(newsIv);
      }

      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

      final Intent intent = new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getBindingAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if(isForUser){
        intent.putExtra("isForUser",isForUser);
        intent.putExtra("publisherId",posts.get(getBindingAdapterPosition()).getPublisherId());
      }

      view.getContext().startActivity(intent);

    }
  }

  public class NewsAttachmentVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final TextView newsTitleTv;

    public NewsAttachmentVh(@NonNull View itemView) {
      super(itemView);
      newsTitleTv = itemView.findViewById(R.id.newsTitleTv);
    }

    private void bind(PostData postData) {
      newsTitleTv.setText(postData.getTitle());
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

      final Intent intent = new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getBindingAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if(isForUser){
        intent.putExtra("isForUser",isForUser);
        intent.putExtra("publisherId",posts.get(getBindingAdapterPosition()).getPublisherId());
      }

      view.getContext().startActivity(intent);

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

    private void bind(PostData postData) {
      newsTitleTv.setText(postData.getTitle());
      newsDescriptionTv.setText(postData.getDescription());
      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

//      if()

      final Intent intent = new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getBindingAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if(isForUser){
        intent.putExtra("isForUser",isForUser);
        intent.putExtra("publisherId",posts.get(getBindingAdapterPosition()).getPublisherId());
      }

      view.getContext().startActivity(intent);
    }
  }


  public class PollPreviewVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final TextView questionTv;
    private final TextView showMoreTv;
    private final RecyclerView pollRv;

    public PollPreviewVh(@NonNull View itemView) {
      super(itemView);
      questionTv = itemView.findViewById(R.id.questionTv);
      pollRv = itemView.findViewById(R.id.pollRv);
      showMoreTv = itemView.findViewById(R.id.showMoreTv);
    }

    private void bind(PostData postData) {

      questionTv.setText(postData.getTitle());

      if(postData.getPollOptions() == null){
        postData.setChosenPollOption(-1);
      }

      if(postData.getChosenPollOption() == -1 &&
              postData.getPollOptions() == null){

        postsCollectionRef.document(postData.getPostId())
                .collection("UserVotes").document(currentUid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
          @Override
          public void onSuccess(DocumentSnapshot snapshot) {

            if (snapshot.exists()) {
              final int option = snapshot.get("voteOption", Integer.class);
              postData.setChosenPollOption(option);
            }

            checkShowProgress(postData);
          }
        });

      }else{

        checkShowProgress(postData);
      }

      itemView.setOnClickListener(this);

    }

    private void checkShowProgress(PostData postData){

      if (postData.isPollEnded()) {
        getPollRecycler(postData,true,true);
      } else {

        if (System.currentTimeMillis() >
                postData.getPublishTime() + postData.getPollDuration()) {

          postsCollectionRef.document(postData.getPostId())
                  .update("pollEnded", true);

          getPollRecycler(postData,true,true);

        } else {

          getPollRecycler(postData,false, postData.getChosenPollOption() != -1);

        }
      }

    }
    private void getPollRecycler(PostData postData,boolean hasEnded,boolean showProgress) {

      if(postData.getPollOptions() == null){
//        postData.setChosenPollOption(-1);
        postData.setPollOptions(new ArrayList<>());
      }

      final PollPostAdapter adapter = new PollPostAdapter(postData.getPollOptions()
              , postData.getPostId(), hasEnded, postData.getTotalVotes());

      adapter.setChosenOption(postData.getChosenPollOption());

      adapter.showProgress = showProgress;

      pollRv.setNestedScrollingEnabled(false);
      pollRv.setHasFixedSize(true);
      adapter.setHasStableIds(true);
      pollRv.setAdapter(adapter);

      if(!loadingItems.contains(getBindingAdapterPosition())){

        loadingItems.add(getBindingAdapterPosition());

        postsCollectionRef.document(postData.getPostId())
                .collection("Options")
                .orderBy("votes", Query.Direction.DESCENDING)
                .orderBy("option", Query.Direction.ASCENDING)
                .limit(6)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                  @Override
                  public void onSuccess(QuerySnapshot snapshots) {

                    if (!snapshots.isEmpty()) {
                      if(snapshots.size() == 6){
                        showMoreTv.setVisibility(View.VISIBLE);
                        for(int i=0;i<snapshots.size()-1;i++){
                          postData.getPollOptions().add(
                                  snapshots.getDocuments().get(i).toObject(PollOption.class));
                        }
                      }else{
                        showMoreTv.setVisibility(View.GONE);
                        postData.getPollOptions().addAll(snapshots.toObjects(PollOption.class));
                      }
                    }
                  }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
          @Override
          public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && !postData.getPollOptions().isEmpty()) {
              adapter.notifyDataSetChanged();
            }
          }
        });

      }
    }

    @Override
    public void onClick(View view) {

      final Intent intent = new Intent(view.getContext(), PostNewsActivity.class)
              .putExtra("postId", posts.get(getBindingAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if(isForUser){
        intent.putExtra("isForUser",isForUser);
        intent.putExtra("publisherId",posts.get(getBindingAdapterPosition()).getPublisherId());
      }

      view.getContext().startActivity(intent);

    }
  }
}


//  public class PollVh extends RecyclerView.ViewHolder implements View.OnClickListener{
//
//      private final CircleImageView imageIv;
//      private final TextView usernameTv;
//      private final TextView dateTv;
//      private final TextView questionTv;
//      private final ImageView menuIv;
//      private final RecyclerView pollRv;
//      private final TextView likeTv;
//      private final TextView commentTv;
//      private final TextView votesTv;
//      private final String voteRes,num_of_people;
//
//      public PollVh(@NonNull View itemView) {
//        super(itemView);
//        imageIv = itemView.findViewById(R.id.imageIv);
//        usernameTv = itemView.findViewById(R.id.usernameTv);
//        dateTv = itemView.findViewById(R.id.dateTv);
//        questionTv = itemView.findViewById(R.id.questionTv);
//        menuIv = itemView.findViewById(R.id.menuIv);
//        likeTv = itemView.findViewById(R.id.likeTv);
//        commentTv = itemView.findViewById(R.id.commentTv);
//        pollRv = itemView.findViewById(R.id.pollRv);
//        votesTv = itemView.findViewById(R.id.votesTv);
//        voteRes = itemView.getResources().getString(R.string.vote);
//        num_of_people = itemView.getResources().getString(R.string.num_of_people);
//
//      }
//
//
//      @SuppressLint("SetTextI18n")
//      private void bind(PostData postData){
//
//
//
//          if(postData.getPollOptions() == null){
//
//            if(postData.isPollEnded()){
//
//              if(postData.getTitle().equals("quetion")){
//                Log.d("ttt","total votes for quetion: "+postData.getTotalVotes());
//              }
//
//              getPollRecycler(true);
//
//            }else{
//
//              if(System.currentTimeMillis() >
//                      postData.getPublishTime() + postData.getPollDuration()){
//
//                postsCollectionRef.document(postData.getPostId())
//                        .update("pollEnded",true);
//
//                getPollRecycler(true);
//
//              }else{
//
//                getPollRecycler(false);
//
//              }
//
//            }
//
//        }
//
//        if(postData.getPublisherName() == null){
//          getUserInfo(postData,postData.getPublisherId(),imageIv,usernameTv);
//        }else{
//          if(postData.getPublisherImage()!=null){
//            Picasso.get().load(postData.getPublisherImage()).fit().into(imageIv);
//          }
//          usernameTv.setText(postData.getPublisherName());
//        }
//
//        questionTv.setText(postData.getTitle());
//        dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),dateFormat));
//
//        if(GlobalVariables.getLikesList().contains(postData.getPostId())){
//
//          likeTv.setTextColor(itemView.getContext()
//                  .getResources().getColor(R.color.red));
//
//        }else{
//
//          likeTv.setTextColor(itemView.getContext()
//                  .getResources().getColor(R.color.black));
//
//        }
//        likeTv.setOnClickListener(this);
//        menuIv.setOnClickListener(this);
//        commentTv.setOnClickListener(this);
//
//        itemView.setOnClickListener(new View.OnClickListener() {
//          @Override
//          public void onClick(View view) {
//
//            view.getContext().startActivity(new Intent(view.getContext(),
//                    PostPollActivity.class).putExtra("postData",(Serializable)
//                    posts.get(getBindingAdapterPosition())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//
//          }
//        });
//        votesTv.setText(voteRes+" "+postData.getTotalVotes()+" "+num_of_people);
//      }
//
//      private void getPollRecycler(boolean hasEnded){
//
//        final PostData postData = posts.get(getBindingAdapterPosition());
//        postData.setPollOptions(new ArrayList<>());
//
//        final PollPostAdapter adapter = new PollPostAdapter(postData.getPollOptions()
//                ,postData.getPostId(), hasEnded,postData.getTotalVotes());
//
//        pollRv.setNestedScrollingEnabled(false);
//        pollRv.setHasFixedSize(true);
//        adapter.setHasStableIds(true);
//        pollRv.setAdapter(adapter);
//
//        postsCollectionRef.document(postData.getPostId())
//                .collection("UserVotes").whereEqualTo("userId",currentUid)
//        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//          int chosenOption = -1;
//          @Override
//          public void onSuccess(QuerySnapshot snapshots) {
//
//            if(!snapshots.isEmpty()){
//              chosenOption =  snapshots.getDocuments().get(0).get("voteOption",Integer.class);
//            }
//
//            postsCollectionRef.document(postData.getPostId())
//                    .collection("Options").get()
//                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                      @Override
//                      public void onSuccess(QuerySnapshot snapshots) {
//                        if(!snapshots.isEmpty()){
//                          postData.getPollOptions().addAll(snapshots.toObjects(PollOption.class));
//                        }
//                      }
//                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//              @Override
//              public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful() && !postData.getPollOptions().isEmpty()){
//
//                  if(chosenOption != -1){
//                    postData.getPollOptions().get(chosenOption).setChosen(true);
//                    adapter.showProgress = true;
//                  }
//
//                  adapter.notifyDataSetChanged();
//                }
//              }
//            });
//          }
//        });
//
//      }
//
//      @Override
//      public void onClick(View view) {
//
//        if(view.getId() == R.id.likeTv){
//
//          if(likeTv.getCurrentTextColor() ==
//                  itemView.getContext()
//                          .getResources().getColor(R.color.red)){
//
//
//            likeTv.setTextColor(itemView.getContext()
//                    .getResources().getColor(R.color.black));
//
//            PostData.likePost(posts.get(getBindingAdapterPosition()).getPostId(),2
//                    ,posts.get(getBindingAdapterPosition()).getPublisherId(),view.getContext());
//
//          }else{
//
//            likeTv.setTextColor(itemView.getContext()
//                    .getResources().getColor(R.color.red));
//
//            PostData.likePost(posts.get(getBindingAdapterPosition()).getPostId(),1,
//                    posts.get(getBindingAdapterPosition()).getPublisherId(),view.getContext());
//
//          }
//
//        }else if(view.getId() == R.id.commentTv){
//
//          final PostData postData = posts.get(getBindingAdapterPosition());
//
//          commentsInterface.showComments(postData.getPostId(),postData.getComments());
//
//        }else if(view.getId() == R.id.menuIv){
//
//        }
//
//      }
//    }


//  public class NewsVh extends RecyclerView.ViewHolder implements View.OnClickListener {
//      private final CircleImageView imageIv ;
//        private final TextView usernameTv ;
//        private final TextView dateTv ;
//        private final TextView titleTv ;
//        private final ImageView menuIv;
//        private final ImageView postIv ;
//        private final TextView likesTv ;
//        private final TextView commentsTv ;
//        private final TextView redMoreTv ;
//        private final TextView likeTv;
//        private final TextView commentTv;
//        private final TextView descriptionTv;
//
//        public NewsVh(@NonNull View itemView) {
//            super(itemView);
//            imageIv = itemView.findViewById(R.id.imageIv);
//            usernameTv = itemView.findViewById(R.id.usernameTv);
//            titleTv = itemView.findViewById(R.id.titleTv);
//            dateTv = itemView.findViewById(R.id.dateTv);
//            postIv = itemView.findViewById(R.id.postIv);
//            likesTv = itemView.findViewById(R.id.likesTv);
//            commentsTv = itemView.findViewById(R.id.commentsTv);
//            redMoreTv = itemView.findViewById(R.id.redMoreTv);
//            likeTv = itemView.findViewById(R.id.likeTv);
//            commentTv = itemView.findViewById(R.id.commentTv);
//            descriptionTv = itemView.findViewById(R.id.descriptionTv);
//            menuIv = itemView.findViewById(R.id.menuIv);
//        }
//
//
//        //        private void showPostOptionsBottomSheet() {
////            final View parentView = new CommentsFragment().getLayoutInflater
////                    ().inflate(R.layout.fragment_commnet, null);
////            parentView.setBackgroundColor(Color.TRANSPARENT);
////
////
////
////            bsd.setContentView(parentView);
////            bsd.show();
////
////        }
//
//        private void bind(PostData postData){
//
//            Picasso.get().load(postData.getImageUrl()).fit().into(postIv);
//
//            if(postData.getImageUrl() == null){
//
//                getUserInfo(postData,postData.getPublisherId(),imageIv,usernameTv);
//
//            }else{
//
//              if(postData.getPublisherImage()!=null
//               && !postData.getPublisherImage().isEmpty()){
//
//                Picasso.get().load(postData.getPublisherImage()).fit().into(imageIv);
//
//              }
//
//              usernameTv.setText(postData.getPublisherName());
//
//            }
//
//            titleTv.setText(postData.getTitle());
//            dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),dateFormat));
//
//            if(GlobalVariables.getLikesList().contains(postData.getPostId())){
//
//              likeTv.setTextColor(itemView.getContext()
//                      .getResources().getColor(R.color.red));
//
//            }else{
//
//              likeTv.setTextColor(itemView.getContext()
//                      .getResources().getColor(R.color.black));
//
//            }
//
//            likesTv.setText(String.valueOf(postData.getLikes()));
//            commentsTv.setText(String.valueOf(postData.getComments()));
//            descriptionTv.setText(postData.getDescription());
//
//            likeTv.setOnClickListener(this);
//            redMoreTv.setOnClickListener(this);
//            menuIv.setOnClickListener(this);
//            postIv.setOnClickListener(this);
//            commentTv.setOnClickListener(this);
//
//        }
//
//
//
//        @Override
//        public void onClick(View view) {
//
//            if(view.getId() == R.id.likeTv){
//
//              if(likeTv.getCurrentTextColor() ==
//                      itemView.getContext()
//                              .getResources().getColor(R.color.red)
//              ){
//
//
//                likeTv.setTextColor(itemView.getContext()
//                        .getResources().getColor(R.color.black));
//
//                likesTv.setText(String.valueOf(
//                        (Integer.parseInt(likesTv.getText().toString())-1)
//                ));
//
//                PostData.likePost(posts.get(getBindingAdapterPosition()).getPostId(),2,
//                        posts.get(getBindingAdapterPosition()).getPublisherId(),view.getContext());
//
//
//              }else{
//
//                likeTv.setTextColor(itemView.getContext()
//                        .getResources().getColor(R.color.red));
//
//                likesTv.setText(String.valueOf(
//                        (Integer.parseInt(likesTv.getText().toString())+1)
//                ));
//
//                PostData.likePost(posts.get(getBindingAdapterPosition()).getPostId(),1,
//                        posts.get(getBindingAdapterPosition()).getPublisherId(),view.getContext());
//
//              }
//
//            }else if(view.getId() == R.id.commentTv){
//
//              final PostData postData = posts.get(getBindingAdapterPosition());
//
//              commentsInterface.showComments(postData.getPostId(),postData.getComments());
//
//            }else if(view.getId() == R.id.redMoreTv){
//
//                if(descriptionTv.getVisibility()==View.VISIBLE){
//                    redMoreTv.setText("read more");
//                    descriptionTv.setVisibility(View.GONE);
//                }else{
//                    redMoreTv.setText("read less");
//                    descriptionTv.setVisibility(View.VISIBLE);
//                }
//
//
//            }else if(view.getId() == R.id.menuIv){
//
//            }else if(view.getId() == R.id.postIv){
//
//              if(posts.get(getBindingAdapterPosition()).getImageUrl()!=null){
//                imageInterface.showImage(posts.get(getBindingAdapterPosition()).getImageUrl());
//              }
//
//
//            }
//
//        }
//    }

//  public static void getUserInfo(PostData postData,String userId, ImageView imageIv,
//                                         TextView usernameTv){
//
//    usersCollectionRef.document(userId).get()
//            .addOnSuccessListener(documentSnapshot -> {
//              if(documentSnapshot.exists()){
//                postData.setPublisherImage(documentSnapshot.getString("imageUrl"));
//                postData.setPublisherName(documentSnapshot.getString("username"));
//              }
//            }).addOnCompleteListener(task -> {
//
//              if(postData.getPublisherImage() != null && !postData.getPublisherImage().isEmpty()){
//                Picasso.get().load(postData.getPublisherImage()).into(imageIv);
//              }
//
//              usernameTv.setText(postData.getPublisherName());
//            });
//
//  }
//    if (postData.getType() == PostData.TYPE_NEWS) {
//
//
////        switch (postData.getAttachmentType()) {
////          case Files.IMAGE:
////            return IMAGE_NEWS;
////
////          case Files.DOCUMENT:
////            return ATTACHMENT_NEWS;
////
////          case Files.VIDEO:
////            return VIDEO_NEWS;
////
////          case Files.VIDEO:
////            return VIDEO_NEWS;
////
////          return TEXT_NEWS;
////          default:
////            return IMAGE_NEWS;
////        }
//
//    } else  {
//
//    } else {
//      return PostData.TYPE_NEWS;
//    }
