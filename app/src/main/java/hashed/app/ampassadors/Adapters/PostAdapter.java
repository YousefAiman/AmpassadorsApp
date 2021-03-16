package hashed.app.ampassadors.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private static List<PostData> posts;
    Context context;
    private static final String dateFormat = "dd/MM/yyyy";

    private static final CollectionReference usersCollectionRef =
            FirebaseFirestore.getInstance().collection("Users"),
            postsCollectionRef = FirebaseFirestore.getInstance().collection("Posts");

  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private final CommentsInterface commentsInterface;
    private final ImageInterface imageInterface;

    public interface CommentsInterface{
      void showComments(String postId,int comments);
    }
  public interface ImageInterface{
    void showImage(String imageUrl);
  }



    public PostAdapter(List<PostData> posts , Context context,CommentsInterface commentsInterface,
                       ImageInterface imageInterface){
        PostAdapter.posts = posts;
        this.context = context;
        this.commentsInterface = commentsInterface;
        this.imageInterface = imageInterface;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      switch (viewType){

        case PostData.TYPE_NEWS:
          return new NewsVh(LayoutInflater.from(context).inflate(R.layout.home_post_news_item
                  , parent , false));

        case PostData.TYPE_POLL:
          return new PollVh(LayoutInflater.from(context).inflate(R.layout.poll_item_layout
                  , parent, false));

      }

    return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

      switch (holder.getItemViewType()){
        case PostData.TYPE_NEWS:
          ((NewsVh)holder).bind(posts.get(position));
          break;

        case PostData.TYPE_POLL:
          ((PollVh)holder).bind(posts.get(position));
          break;
      }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PollVh extends RecyclerView.ViewHolder implements View.OnClickListener{

      private final CircleImageView imageIv;
      private final TextView usernameTv;
      private final TextView dateTv;
      private final TextView questionTv;
      private final ImageView menuIv;
      private final RecyclerView pollRv;
      private final TextView likeTv;
      private final TextView commentTv;
      private final TextView votesTv;
      private final String voteRes,num_of_people;

      public PollVh(@NonNull View itemView) {
        super(itemView);
        imageIv = itemView.findViewById(R.id.imageIv);
        usernameTv = itemView.findViewById(R.id.usernameTv);
        dateTv = itemView.findViewById(R.id.dateTv);
        questionTv = itemView.findViewById(R.id.questionTv);
        menuIv = itemView.findViewById(R.id.menuIv);
        likeTv = itemView.findViewById(R.id.likeTv);
        commentTv = itemView.findViewById(R.id.commentTv);
        pollRv = itemView.findViewById(R.id.pollRv);
        votesTv = itemView.findViewById(R.id.votesTv);
        voteRes = itemView.getResources().getString(R.string.vote);
        num_of_people = itemView.getResources().getString(R.string.num_of_people);

      }


      @SuppressLint("SetTextI18n")
      private void bind(PostData postData){

          if(postData.getPollOptions() == null){

            if(postData.isPollEnded()){

              getPollRecycler(true);

            }else{

              if(System.currentTimeMillis() >
                      postData.getPublishTime() + postData.getPollDuration()){

                postsCollectionRef.document(postData.getPostId())
                        .update("pollEnded",true);

                getPollRecycler(true);

              }else{

                getPollRecycler(false);

              }

            }

        }

        if(postData.getPublisherName() == null){
          getUserInfo(postData,postData.getPublisherId(),imageIv,usernameTv);
        }else{
          if(postData.getPublisherImage()!=null){
            Picasso.get().load(postData.getPublisherImage()).fit().into(imageIv);
          }
          usernameTv.setText(postData.getPublisherName());
        }

        questionTv.setText(postData.getTitle());
        dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),dateFormat));

        if(GlobalVariables.getLikesList().contains(postData.getPostId())){

          likeTv.setTextColor(itemView.getContext()
                  .getResources().getColor(R.color.red));

        }else{

          likeTv.setTextColor(itemView.getContext()
                  .getResources().getColor(R.color.black));

        }
        likeTv.setOnClickListener(this);
        menuIv.setOnClickListener(this);
        commentTv.setOnClickListener(this);

        votesTv.setText(voteRes+" "+postData.getTotalVotes()+" "+num_of_people);


      }


      private void getPollRecycler(boolean hasEnded){

        final PostData postData = posts.get(getAdapterPosition());
        postData.setPollOptions(new ArrayList<>());

        final PollPostAdapter adapter = new PollPostAdapter(postData.getPollOptions()
                ,postData.getPostId(), hasEnded,postData.getTotalVotes());

        pollRv.setHasFixedSize(true);
        adapter.setHasStableIds(true);
        pollRv.setAdapter(adapter);

        postsCollectionRef.document(postData.getPostId())
                .collection("UserVotes").whereEqualTo("userId",currentUid)
        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
          int chosenOption = -1;
          @Override
          public void onSuccess(QuerySnapshot snapshots) {

            if(!snapshots.isEmpty()){
              chosenOption =  snapshots.getDocuments().get(0).get("voteOption",Integer.class);
            }

            postsCollectionRef.document(postData.getPostId())
                    .collection("Options").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                      @Override
                      public void onSuccess(QuerySnapshot snapshots) {
                        if(!snapshots.isEmpty()){
                          postData.getPollOptions().addAll(snapshots.toObjects(PollOption.class));
                        }
                      }
                    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
              @Override
              public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && !postData.getPollOptions().isEmpty()){

                  if(chosenOption != -1){
                    postData.getPollOptions().get(chosenOption).setChosen(true);
                    adapter.showProgress = true;
                  }

                  adapter.notifyDataSetChanged();
                }
              }
            });
          }
        });

      }

      @Override
      public void onClick(View view) {

        if(view.getId() == R.id.likeTv){

          if(likeTv.getCurrentTextColor() ==
                  itemView.getContext()
                          .getResources().getColor(R.color.red)){


            likeTv.setTextColor(itemView.getContext()
                    .getResources().getColor(R.color.black));

            PostData.likePost(posts.get(getAdapterPosition()).getPostId(),2);

          }else{

            likeTv.setTextColor(itemView.getContext()
                    .getResources().getColor(R.color.red));

            PostData.likePost(posts.get(getAdapterPosition()).getPostId(),1);

          }

        }else if(view.getId() == R.id.commentTv){

          final PostData postData = posts.get(getAdapterPosition());

          commentsInterface.showComments(postData.getPostId(),postData.getComments());

        }else if(view.getId() == R.id.menuIv){

        }

      }
    }

  public class NewsVh extends RecyclerView.ViewHolder implements View.OnClickListener {
      private final CircleImageView imageIv ;
        private final TextView usernameTv ;
        private final TextView dateTv ;
        private final TextView titleTv ;
        private final ImageView menuIv;
        private final ImageView postIv ;
        private final TextView likesTv ;
        private final TextView commentsTv ;
        private final TextView redMoreTv ;
        private final TextView likeTv;
        private final TextView commentTv;
        private final TextView descriptionTv;

        public NewsVh(@NonNull View itemView) {
            super(itemView);
            imageIv = itemView.findViewById(R.id.imageIv);
            usernameTv = itemView.findViewById(R.id.usernameTv);
            titleTv = itemView.findViewById(R.id.titleTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            postIv = itemView.findViewById(R.id.postIv);
            likesTv = itemView.findViewById(R.id.likesTv);
            commentsTv = itemView.findViewById(R.id.commentsTv);
            redMoreTv = itemView.findViewById(R.id.redMoreTv);
            likeTv = itemView.findViewById(R.id.likeTv);
            commentTv = itemView.findViewById(R.id.commentTv);
            descriptionTv = itemView.findViewById(R.id.descriptionTv);
            menuIv = itemView.findViewById(R.id.menuIv);
        }


        //        private void showPostOptionsBottomSheet() {
//            final View parentView = new CommentsFragment().getLayoutInflater
//                    ().inflate(R.layout.fragment_commnet, null);
//            parentView.setBackgroundColor(Color.TRANSPARENT);
//
//
//
//            bsd.setContentView(parentView);
//            bsd.show();
//
//        }

        private void bind(PostData postData){

            Picasso.get().load(postData.getImageUrl()).fit().into(postIv);

            if(postData.getImageUrl() == null){

                getUserInfo(postData,postData.getPublisherId(),imageIv,usernameTv);

            }else{

              if(postData.getPublisherImage()!=null
               && !postData.getPublisherImage().isEmpty()){

                Picasso.get().load(postData.getPublisherImage()).fit().into(imageIv);

              }

              usernameTv.setText(postData.getPublisherName());

            }

            titleTv.setText(postData.getTitle());
            dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),dateFormat));

            if(GlobalVariables.getLikesList().contains(postData.getPostId())){

              likeTv.setTextColor(itemView.getContext()
                      .getResources().getColor(R.color.red));

            }else{

              likeTv.setTextColor(itemView.getContext()
                      .getResources().getColor(R.color.black));

            }

            likesTv.setText(String.valueOf(postData.getLikes()));
            commentsTv.setText(String.valueOf(postData.getComments()));
            descriptionTv.setText(postData.getDescription());

            likeTv.setOnClickListener(this);
            redMoreTv.setOnClickListener(this);
            menuIv.setOnClickListener(this);
            postIv.setOnClickListener(this);
            commentTv.setOnClickListener(this);

        }



        @Override
        public void onClick(View view) {

            if(view.getId() == R.id.likeTv){

              if(likeTv.getCurrentTextColor() ==
                      itemView.getContext()
                              .getResources().getColor(R.color.red)
              ){


                likeTv.setTextColor(itemView.getContext()
                        .getResources().getColor(R.color.black));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString())-1)
                ));

                PostData.likePost(posts.get(getAdapterPosition()).getPostId(),2);


              }else{

                likeTv.setTextColor(itemView.getContext()
                        .getResources().getColor(R.color.red));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString())+1)
                ));

                PostData.likePost(posts.get(getAdapterPosition()).getPostId(),1);

              }

            }else if(view.getId() == R.id.commentTv){

              final PostData postData = posts.get(getAdapterPosition());

              commentsInterface.showComments(postData.getPostId(),postData.getComments());

            }else if(view.getId() == R.id.redMoreTv){

                if(descriptionTv.getVisibility()==View.VISIBLE){
                    redMoreTv.setText("read more");
                    descriptionTv.setVisibility(View.GONE);
                }else{
                    redMoreTv.setText("read less");
                    descriptionTv.setVisibility(View.VISIBLE);
                }


            }else if(view.getId() == R.id.menuIv){

            }else if(view.getId() == R.id.postIv){

              if(posts.get(getAdapterPosition()).getImageUrl()!=null){
                imageInterface.showImage(posts.get(getAdapterPosition()).getImageUrl());
              }


            }

        }
    }


  public static void getUserInfo(PostData postData,String userId, ImageView imageIv,
                                         TextView usernameTv){

    usersCollectionRef.document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
              if(documentSnapshot.exists()){
                postData.setPublisherImage(documentSnapshot.getString("imageUrl"));
                postData.setPublisherName(documentSnapshot.getString("username"));
              }
            }).addOnCompleteListener(task -> {

              if(postData.getPublisherImage() != null && !postData.getPublisherImage().isEmpty()){
                Picasso.get().load(postData.getPublisherImage()).into(imageIv);
              }

              usernameTv.setText(postData.getPublisherName());
            });

  }


  @Override
  public int getItemViewType(int position) {
    return posts.get(position).getType();
  }
}