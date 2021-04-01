package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hashed.app.ampassadors.Adapters.PollPostAdapter;
import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.SigninUtil;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostPollActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

  private static final int POLL_LIMIT = 15;
  //views
  private TextView usernameTv, dateTv, titleTv, likesTv, commentsTv, likeTv, commentTv, votesTv;
  private RecyclerView pollRv;
  private ImageView userIv;

  //data
  private PostData postData;
  private ScrollListener scrollListener;
  private boolean isLoadingOptions;
  private DocumentReference postRef;
  private ArrayList<PollOption> pollOptions;
  private PollPostAdapter adapter;
  private int chosenOption = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_poll);

    setupToolbar();

    getViews();

    setClickListeners();

    getPostData();

    getUserInfo();

  }


  private void setupToolbar() {

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());
    toolbar.setOnMenuItemClickListener(this);

  }


  private void getViews() {
    userIv = findViewById(R.id.userIv);
    usernameTv = findViewById(R.id.usernameTv);
    dateTv = findViewById(R.id.dateTv);
    titleTv = findViewById(R.id.titleTv);
    pollRv = findViewById(R.id.pollRv);
    votesTv = findViewById(R.id.votesTv);
    likesTv = findViewById(R.id.likesTv);
    commentsTv = findViewById(R.id.commentsTv);
    likeTv = findViewById(R.id.likeTv);
    commentTv = findViewById(R.id.commentTv);
  }


  private void setClickListeners() {
    userIv.setOnClickListener(this);
    likeTv.setOnClickListener(this);
    commentTv.setOnClickListener(this);
  }
  private void getPostData() {
        userIv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        userIv.getContext().startActivity(new Intent(PostPollActivity.this,
                ProfileActiv.class).putExtra("userId",postData.getPublisherId())
                .putExtra("ImageUrl", postData.getPublisherImage())
                .putExtra("username",postData.getPublisherName()));
      }
    });
    postData = (PostData) getIntent().getSerializableExtra("postData");
    titleTv.setText(postData.getTitle());

    votesTv.setText(String.valueOf(postData.getTotalVotes()));
    likesTv.setText(String.valueOf(postData.getLikes()));
    commentsTv.setText(String.valueOf(postData.getComments()));
    commentsTv.setText(String.valueOf(postData.getComments()));
    dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),
            TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

    likeTv.setTextColor(getResources().getColor(
            GlobalVariables.getLikesList().contains(postData.getPostId()) ? R.color.red :
                    R.color.black));

    getPollRecycler();

  }


  private void getUserInfo() {


    FirebaseFirestore.getInstance().collection("Users")
            .document(postData.getPublisherId()).get().addOnSuccessListener(snapshot -> {
      if (snapshot.exists()) {

        final String imageUrl = snapshot.getString("imageUrl");

        if (imageUrl != null && !imageUrl.isEmpty()) {
          Picasso.get().load(imageUrl).fit().into(userIv);
        }
        usernameTv.setText(snapshot.getString("username"));

      }
    });


  }

  @Override
  public void onClick(View view) {

    final int id = view.getId();

    if (id == likeTv.getId()) {
      likeOrDislike();
    } else if (id == commentTv.getId()) {
      CommentsFragment commentsFragment = new CommentsFragment(postData.getPostId(),
              postData.getComments());
      commentsFragment.show(getSupportFragmentManager(), "CommentsFragment");
    }
  }


  private void likeOrDislike() {
        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

      SigninUtil.getInstance(PostPollActivity.this,
              PostPollActivity.this).show();
    }else {
          if (likeTv.getCurrentTextColor() == getResources().getColor(R.color.red)) {

            likeTv.setTextColor(getResources().getColor(R.color.black));

            likesTv.setText(String.valueOf(
                    (Integer.parseInt(likesTv.getText().toString()) - 1)));

            PostData.likePost(postData.getPostId(),postData.getTitle(), 2, postData.getPublisherId(), this);
          } else {
            likeTv.setTextColor(getResources().getColor(R.color.red));

            likesTv.setText(String.valueOf(
                    (Integer.parseInt(likesTv.getText().toString()) + 1)));

            PostData.likePost(postData.getPostId(),postData.getTitle(), 1, postData.getPublisherId(), this);

          }
        }


  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }


  private void getPollRecycler() {


    pollRv.setNestedScrollingEnabled(false);
    pollRv.setHasFixedSize(true);

    postRef = FirebaseFirestore.getInstance().collection("Posts")
                    .document(postData.getPostId());

    postRef.collection("UserVotes")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {

        if (snapshot.exists()) {
          chosenOption = snapshot.get("voteOption", Integer.class);
          postData.setChosenPollOption(chosenOption);
        }


        boolean hasEnded;

        if (postData.isPollEnded()) {
          hasEnded = true;
        } else {

          if (System.currentTimeMillis() >
                  postData.getPublishTime() + postData.getPollDuration()) {

            postRef.update("pollEnded", true);
            hasEnded = true;

          } else {
            hasEnded = false;
          }
        }

        pollOptions = new ArrayList<>();

        adapter = new PollPostAdapter(pollOptions
                , postData.getPostId(), hasEnded, postData.getTotalVotes());


        if(postData.getChosenPollOption()!=-1){
          adapter.setChosenOption(postData.getChosenPollOption());
          adapter.showProgress = true;
        }else{
          adapter.showProgress = hasEnded;
        }

        adapter.setHasStableIds(true);
        pollRv.setAdapter(adapter);

        getPollOptions(true);

      }
    });

  }

  private void getPollOptions(boolean initial){

    isLoadingOptions = true;

    postRef.collection("Options").orderBy("votes", Query.Direction.DESCENDING)
            .limit(POLL_LIMIT).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if (!snapshots.isEmpty()) {
          if(initial){
            pollOptions.addAll(snapshots.toObjects(PollOption.class));
          }else{
            pollOptions.addAll(pollOptions.size(),snapshots.toObjects(PollOption.class));
          }
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful() && !postData.getPollOptions().isEmpty()) {

          if (initial) {
            adapter.notifyDataSetChanged();

            if (pollOptions.size() == POLL_LIMIT) {
              pollRv.addOnScrollListener(scrollListener = new ScrollListener());
            }

          } else {

            final int addedSize =  task.getResult().size();

            adapter.notifyItemRangeInserted(pollOptions.size() - addedSize,addedSize);

            if (addedSize < POLL_LIMIT && scrollListener != null) {
              pollRv.removeOnScrollListener(scrollListener);
            }
          }

          isLoadingOptions = false;
        }
      }
    });


  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingOptions &&
              !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getPollOptions(false);
      }
    }
  }


}