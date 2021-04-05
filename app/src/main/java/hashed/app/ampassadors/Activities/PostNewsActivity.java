package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FileDownloadUtil;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.SigninUtil;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class PostNewsActivity extends AppCompatActivity implements View.OnClickListener,
            Toolbar.OnMenuItemClickListener {

    //views
    private CardView cardView;
    private TextView usernameTv, dateTv, titleTv, descriptionTv, likesTv,
            commentsTv, likeTv, commentTv, newsTitleTv;
    private ImageView newsIv, userIv, attachmentImage, playIv;
    private FrameLayout frameLayout;
    ProgressDialog progressDialog;
    Toolbar toolbar;
    //data
    private PostData postData;
    private BroadcastReceiver downloadCompleteReceiver;

    //download
    private FileDownloadUtil fileDownloadUtil;
    private String fileName;

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
        toolbar = findViewById(R.id.toolbar);
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
        progressDialog = new ProgressDialog(this);
    }

    private void setClickListeners() {

        newsIv.setOnClickListener(this);
        userIv.setOnClickListener(this);
        likeTv.setOnClickListener(this);
        commentTv.setOnClickListener(this);

    }

    private void getPostData() {

        postData = (PostData) getIntent().getSerializableExtra("postData");

        if(!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
            if (postData.getPublisherId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                toolbar.inflateMenu(R.menu.post_menu);

            }else if (GlobalVariables.getRole().equals("Admin")){
                toolbar.inflateMenu(R.menu.admin_menu);
            }else {
                toolbar.inflateMenu(R.menu.users_post_menu);

            }
        }


        if (postData.getAttachmentType() == Files.IMAGE) {
            Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(newsIv);
        } else if (postData.getAttachmentType() == Files.VIDEO) {
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
        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userIv.getContext().startActivity(new Intent(PostNewsActivity.this,
                        ProfileActiv.class).putExtra("userId", postData.getPublisherId())
                        .putExtra("ImageUrl", postData.getPublisherImage())
                        .putExtra("username", postData.getPublisherName()));
            }
        });

        switch (postData.getAttachmentType()) {

            case Files.VIDEO:

                playIv.setVisibility(View.VISIBLE);
                newsIv.setOnClickListener(this);
                break;

            case Files.DOCUMENT:

                attachmentImage = new ImageView(this);
                attachmentImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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

                        if (fileDownloadUtil == null) {
                            fileDownloadUtil = new FileDownloadUtil(PostNewsActivity.this, postData.getAttachmentUrl(),
                                    postData.getDocumentName(), attachmentImage);
                        }

                        fileDownloadUtil.showDownloadAlert();

                        if (downloadCompleteReceiver == null) {
                            setUpDownloadReceiver();
                        }

                    }
                });

                cardView.addView(attachmentImage);

                break;

            case Files.TEXT:
                cardView.setVisibility(View.GONE);
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
                            new ImageFullScreenFragment(postData.getAttachmentUrl(),
                                    getFileName()), "FullScreen")
                            .commit();

                } else if (postData.getAttachmentType() == Files.VIDEO) {

                    frameLayout.setVisibility(View.VISIBLE);

                    getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
                            new VideoFullScreenFragment(postData.getAttachmentUrl(),
                                    getFileName())).commit();

                }
            }
        }
    }

    private String getFileName() {
        if (fileName == null) {
            fileName = FirebaseStorage.getInstance().getReferenceFromUrl(
                            postData.getAttachmentUrl()).getName();
        }
        return fileName;
    }

    private void likeOrDislike() {

        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

            SigninUtil.getInstance(PostNewsActivity.this,
                    PostNewsActivity.this).show();
        } else {

            if (likeTv.getCurrentTextColor() == getResources().getColor(R.color.red)) {

                likeTv.setTextColor(getResources().getColor(R.color.black));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString()) - 1)));

                PostData.likePost(postData.getPostId(), postData.getTitle(), 2, postData.getPublisherId(), this);

            } else {

                likeTv.setTextColor(getResources().getColor(R.color.red));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString()) + 1)));

                PostData.likePost(postData.getPostId(), postData.getTitle(), 1, postData.getPublisherId(), this);

            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.edit) {

            Intent intent = new Intent(PostNewsActivity.this, PostNewActivity.class);
//            intent.putExtra("postID",postData.getPostId());
            intent.putExtra("postData",postData);
            intent.putExtra("isForEditing",true);
//            intent.putExtra("publisherimage",postData.getPublisherImage());
//            intent.putExtra("userid",postData.getPublisherId());
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.delete) {
            
            progressDialog.setMessage(getString(R.string.Dleteing));
            progressDialog.show();
            FirebaseFirestore.getInstance().collection("Posts")
                    .document(postData.getPostId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                    Toast.makeText(PostNewsActivity.this, R.string.Delete_success, Toast.LENGTH_SHORT).show();
                }
            });

        } else if (item.getItemId() == R.id.Reporting) {

            FirebaseFirestore.getInstance().collection("Posts").
                    document(postData.getPostId()).update("isReported",true).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(PostNewsActivity.this, R.string.Repored, Toast.LENGTH_SHORT).show();

                }
            });

        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (frameLayout.getVisibility() == View.VISIBLE) {
            getSupportFragmentManager().beginTransaction().remove(
                    getSupportFragmentManager().getFragments().get(0)).commit();
            frameLayout.setVisibility(View.GONE);

        }else {
            super.onBackPressed();
        }

    }

    private void setUpDownloadReceiver() {

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (id != -1) {
                    attachmentImage.setImageResource(R.drawable.download_icon);
                    attachmentImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fileDownloadUtil.showDownloadAlert();
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