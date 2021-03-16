package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostNewsActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

  //views
  private CardView cardView;
  private TextView usernameTv,dateTv,titleTv,descriptionTv,likesTv,commentsTv,likeTv
          ,commentTv;
  private ImageView newsIv, userIv,attachmentImage;
  private FrameLayout frameLayout;

  //data
  private PostData postData;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_news);


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

    cardView = findViewById(R.id.cardView);
    newsIv = findViewById(R.id.newsIv);
    userIv = findViewById(R.id.userIv);
    usernameTv = findViewById(R.id.usernameTv);
    dateTv = findViewById(R.id.dateTv);
    titleTv = findViewById(R.id.titleTv);
    descriptionTv = findViewById(R.id.descriptionTv);
    likesTv = findViewById(R.id.likesTv);
    commentsTv = findViewById(R.id.commentsTv);
    likeTv = findViewById(R.id.likeTv);
    commentTv = findViewById(R.id.commentTv);
    frameLayout = findViewById(R.id.frameLayout);

  }


  private void setClickListeners(){

    newsIv.setOnClickListener(this);
    userIv.setOnClickListener(this);
    likeTv.setOnClickListener(this);
    commentTv.setOnClickListener(this);

  }
  private void getPostData(){

    postData = (PostData) getIntent().getSerializableExtra("postData");

    if(postData.getAttachmentType() == Files.IMAGE){
      Picasso.get().load(postData.getAttachmentUrl()).fit().into(newsIv);
    }

    titleTv.setText(postData.getTitle());
    descriptionTv.setText(postData.getDescription());
    likesTv.setText(String.valueOf(postData.getLikes()));
    commentsTv.setText(String.valueOf(postData.getComments()));
    dateTv.setText(TimeFormatter.formatTime(postData.getPublishTime()));

    likeTv.setTextColor(getResources().getColor(
              GlobalVariables.getLikesList().contains(postData.getPostId())?R.color.red:
                      R.color.black));

    getNewsType();

  }

  private void getNewsType(){

    switch (postData.getAttachmentType()){

      case Files.VIDEO:

        final ImageView playImage = new ImageView(this);
        playImage.setImageResource(R.drawable.video_icon_circle);

        final LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity= Gravity.CENTER;
        playImage.setLayoutParams(lp);

        playImage.setOnClickListener(this);

        cardView.addView(playImage);

        break;

      case Files.DOCUMENT:

        attachmentImage = new ImageView(this);
        attachmentImage.setImageResource(R.drawable.download_icon);

        final LayoutParams lp2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.gravity= Gravity.TOP;
        final float density = getResources().getDisplayMetrics().density;
        lp2.setMargins((int) (4*density),(int) (4*density),0,0);

        attachmentImage.setLayoutParams(lp2);

        attachmentImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            showDownloadAlert();
          }
        });

        cardView.addView(attachmentImage);

        break;

    }
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
    }else if(id == newsIv.getId()){

      if(postData.getAttachmentUrl()!=null){
        if(postData.getAttachmentType() == Files.IMAGE){
          new ImageFullScreenFragment(postData.getAttachmentUrl())
                  .show(getSupportFragmentManager(),"FullScreen");
        }else if(postData.getAttachmentType() == Files.VIDEO){
          frameLayout.setVisibility(View.VISIBLE);
          getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
                  new VideoFullScreenFragment(postData.getAttachmentUrl())).commit();
        }
      }
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

  private void showDownloadAlert(){
    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setMessage("Do you want to download file?");

    alertDialogBuilder.setPositiveButton("Download",(dialogInterface, i) -> {
//        downloadFile(position,url,fileName);
    });

    alertDialogBuilder.setNegativeButton("Cancel",(dialogInterface, i) -> {
      dialogInterface.dismiss();
    });

    alertDialogBuilder.show();

  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    if(frameLayout.getVisibility() == View.VISIBLE){

      getSupportFragmentManager().beginTransaction().remove(
              getSupportFragmentManager().getFragments().get(0)).commit();

      frameLayout.setVisibility(View.GONE);

    }else{
      super.onBackPressed();
    }

  }
}