package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hashed.app.ampassadors.Adapters.PollPostAdapter;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostPollActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

  //views
  private TextView usernameTv,dateTv,titleTv,likesTv,commentsTv,likeTv,commentTv,votesTv;
  private RecyclerView pollRv;
  private ImageView userIv;

  //data
  private PostData postData;

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


  private void setupToolbar(){

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v-> finish());
    toolbar.setOnMenuItemClickListener(this);

  }


  private void getViews(){
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


  private void setClickListeners(){
    userIv.setOnClickListener(this);
    likeTv.setOnClickListener(this);
    commentTv.setOnClickListener(this);
  }

  private void getPostData(){

    postData = (PostData) getIntent().getSerializableExtra("postData");
    titleTv.setText(postData.getTitle());

    votesTv.setText(getResources().getString(R.string.vote) +" "+ postData.getTotalVotes());
    likesTv.setText(getResources().getString(R.string.likes) +" "+ postData.getLikes());
    commentsTv.setText(getResources().getString(R.string.comments) +" "+ postData.getComments());


    commentsTv.setText(String.valueOf(postData.getComments()));
    dateTv.setText(TimeFormatter.formatTime(postData.getPublishTime()));

    likeTv.setTextColor(getResources().getColor(
            GlobalVariables.getLikesList().contains(postData.getPostId())?R.color.red:
                    R.color.black));

    getPollRecycler();

  }


  private void getUserInfo(){


    FirebaseFirestore.getInstance().collection("Users")
            .document(postData.getPublisherId()).get().addOnSuccessListener(snapshot -> {
      if(snapshot.exists()){

        final String imageUrl = snapshot.getString("imageUrl");

        if(imageUrl!=null && !imageUrl.isEmpty()){
          Picasso.get().load(imageUrl).fit().into(userIv);
        }
        usernameTv.setText(snapshot.getString("username"));

      }
    });


  }

  @Override
  public void onClick(View view) {

    final int id = view.getId();

    if(id == likeTv.getId()){
      likeOrDislike();
    }else if(id == commentTv.getId()){
      CommentsFragment commentsFragment = new CommentsFragment(postData.getPostId(),
              postData.getComments());
      commentsFragment.show(getSupportFragmentManager(),"CommentsFragment");
    }
  }


  private void likeOrDislike(){

    if(likeTv.getCurrentTextColor() == getResources().getColor(R.color.red)){

      likeTv.setTextColor(getResources().getColor(R.color.black));

      likesTv.setText(String.valueOf(
              (Integer.parseInt(likesTv.getText().toString())-1)));

      PostData.likePost(postData.getPostId(),2,postData.getPublisherId(),this);

    }else{

      likeTv.setTextColor(getResources().getColor(R.color.red));

      likesTv.setText(String.valueOf(
              (Integer.parseInt(likesTv.getText().toString())+1)));

      PostData.likePost(postData.getPostId(),1,postData.getPublisherId(),this);

    }
  }
  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }


  private void getPollRecycler(){

    final DocumentReference postRef =
            FirebaseFirestore.getInstance().collection("Posts")
                    .document(postData.getPostId());

    boolean hasEnded;
    if(System.currentTimeMillis() >
            postData.getPublishTime() + postData.getPollDuration()){
      postRef.update("pollEnded",true);
      hasEnded = true;
    }else{
      hasEnded = false;
    }

    final ArrayList<PollOption> pollOptions = new ArrayList<>();

    final PollPostAdapter adapter = new PollPostAdapter(pollOptions
            ,postData.getPostId(), hasEnded,postData.getTotalVotes());

    pollRv.setNestedScrollingEnabled(false);
    pollRv.setHasFixedSize(true);
    adapter.setHasStableIds(true);
    pollRv.setAdapter(adapter);

    postRef.collection("UserVotes")
            .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      int chosenOption = -1;
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        if(!snapshots.isEmpty()){
          chosenOption =  snapshots.getDocuments().get(0).get("voteOption",Integer.class);
        }

        postRef.collection("Options").get()
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



}