package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostNewsActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

  //views
  private CardView cardView;
  private TextView usernameTv, dateTv, titleTv, descriptionTv, likesTv, commentsTv, likeTv, commentTv, newsTitleTv;
  private ImageView newsIv, userIv, attachmentImage,playIv;
  private FrameLayout frameLayout;


  //data
  private PostData postData;
  private BroadcastReceiver downloadCompleteReceiver;

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
  }

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


  private void setupToolbar() {

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());
    toolbar.setOnMenuItemClickListener(this);

  }


  private void getViews() {

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
    newsTitleTv = findViewById(R.id.newsTitleTv);
    playIv = findViewById(R.id.playIv);

  }

  private void setClickListeners() {

    newsIv.setOnClickListener(this);
    userIv.setOnClickListener(this);
    likeTv.setOnClickListener(this);
    commentTv.setOnClickListener(this);

  }

  private void getPostData() {

    postData = (PostData) getIntent().getSerializableExtra("postData");

    if (postData.getAttachmentType() == Files.IMAGE) {
      Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(newsIv);
    }else if(postData.getAttachmentType() == Files.VIDEO){
      Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerInside().into(newsIv);
    }

    titleTv.setText(postData.getTitle());
    descriptionTv.setText(postData.getDescription());
    newsTitleTv.setText(postData.getDocumentName());
    likesTv.setText(String.valueOf(postData.getLikes()));
    commentsTv.setText(String.valueOf(postData.getComments()));
    dateTv.setText(TimeFormatter.formatWithPattern(postData.getPublishTime(),
            TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

    likeTv.setTextColor(getResources().getColor(
            GlobalVariables.getLikesList().contains(postData.getPostId()) ? R.color.red :
                    R.color.black));

    getNewsType();

  }

  private void getNewsType() {

    switch (postData.getAttachmentType()) {

      case Files.VIDEO:

        playIv.setVisibility(View.VISIBLE);
        newsIv.setOnClickListener(this);
        break;

      case Files.DOCUMENT:

        attachmentImage = new ImageView(this);
        attachmentImage.setImageResource(R.drawable.download_icon);

        final LayoutParams lp2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.gravity = Gravity.TOP;
        final float density = getResources().getDisplayMetrics().density;
        lp2.setMargins((int) (4 * density), (int) (4 * density), 0, 0);


        newsIv.setScaleType(ImageView.ScaleType.CENTER);
        newsIv.setImageResource(R.drawable.pdf_icon);

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
    } else if (id == newsIv.getId()) {

      if (postData.getAttachmentUrl() != null) {
        if (postData.getAttachmentType() == Files.IMAGE) {

          frameLayout.setVisibility(View.VISIBLE);

          getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
                  new ImageFullScreenFragment(postData.getAttachmentUrl()),"FullScreen")
                  .commit();

        } else if (postData.getAttachmentType() == Files.VIDEO) {

          frameLayout.setVisibility(View.VISIBLE);

          getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
                  new VideoFullScreenFragment(postData.getAttachmentUrl())).commit();

        }
      }
    }
  }

  private void likeOrDislike() {


    if (likeTv.getCurrentTextColor() == getResources().getColor(R.color.red)) {

      likeTv.setTextColor(getResources().getColor(R.color.black));

      likesTv.setText(String.valueOf(
              (Integer.parseInt(likesTv.getText().toString()) - 1)));

      PostData.likePost(postData.getPostId(), 2, postData.getPublisherId(), this);

    } else {

      likeTv.setTextColor(getResources().getColor(R.color.red));

      likesTv.setText(String.valueOf(
              (Integer.parseInt(likesTv.getText().toString()) + 1)));

      PostData.likePost(postData.getPostId(), 1, postData.getPublisherId(), this);

    }
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }

  private void showDownloadAlert() {
    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
    alertDialogBuilder.setMessage(getString(R.string.DownLoad_Asking));

    alertDialogBuilder.setPositiveButton(R.string.YES, (dialogInterface, i) -> {
      downloadFile(postData.getAttachmentUrl(), postData.getDocumentName());
    });

    alertDialogBuilder.setNegativeButton(R.string.No, (dialogInterface, i) -> {
      dialogInterface.dismiss();
    });

    alertDialogBuilder.show();

  }

  @Override
  public void onBackPressed() {

    if (frameLayout.getVisibility() == View.VISIBLE) {
      getSupportFragmentManager().beginTransaction().remove(
              getSupportFragmentManager().getFragments().get(0)).commit();
      frameLayout.setVisibility(View.GONE);
    } else {
      super.onBackPressed();
    }

  }

  private void downloadFile(String url, String fileName) {

    DownloadManager.Request request;

    request = new DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription(getString(R.string.Download))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      request.setRequiresCharging(false);
    }

    DownloadManager downloadManager = (DownloadManager)
            this.getSystemService(Context.DOWNLOAD_SERVICE);


    request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,
            fileName);

    long downloadId = downloadManager.enqueue(request);
    attachmentImage.setImageResource(R.drawable.cancel_icon);
    attachmentImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        final DownloadManager downloadManager = (DownloadManager)
                getSystemService(Context.DOWNLOAD_SERVICE);

        downloadManager.remove(downloadId);
        attachmentImage.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            showDownloadAlert();
          }
        });

      }
    });

    if (downloadCompleteReceiver == null) {
      setUpDownloadReceiver();
    }


  }

  private void setUpDownloadReceiver() {

    downloadCompleteReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        if (id != -1) {

//          openDownloadedFile(id);

          attachmentImage.setImageResource(R.drawable.download_icon);
          attachmentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showDownloadAlert();
            }
          });

        }

      }
    };

    registerReceiver(downloadCompleteReceiver,
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (downloadCompleteReceiver != null) {
      unregisterReceiver(downloadCompleteReceiver);
    }
  }
}