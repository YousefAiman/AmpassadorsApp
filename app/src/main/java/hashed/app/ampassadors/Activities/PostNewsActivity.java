package hashed.app.ampassadors.Activities;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;
import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FileDownloadUtil;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.FullScreenImagesUtil;
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
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    //data
    private PostData postData;
    private BroadcastReceiver downloadCompleteReceiver;

    //download
    private FileDownloadUtil fileDownloadUtil;
    private String fileName;

    private DocumentReference postRef;
    private boolean isForUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_news);

        setupToolbar();

        getViews();

        getPostData();

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

    }

    private void setClickListeners() {

        newsIv.setOnClickListener(this);
        userIv.setOnClickListener(this);
        likeTv.setOnClickListener(this);
        commentTv.setOnClickListener(this);

    }

    private void getPostData() {

        final Intent intent = getIntent();

        if(intent == null){
            finish();
            return;
        }

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(intent.hasExtra("postId")){

            if(getIntent().hasExtra("isForUser") && getIntent().getBooleanExtra("isForUser",false)){
                isForUser = true;

                postRef = firestore.collection("Users")
                        .document(getIntent().getStringExtra("publisherId"))
                        .collection("UserPosts")
                        .document(getIntent().getStringExtra("postId"));

            }else{
                postRef = firestore.collection("Posts")
                        .document(getIntent().getStringExtra("postId"));
            }

            fetchPostData(user);
        }else if(intent.hasExtra("notificationPostId")){


            final String postId = intent.getStringExtra("notificationPostId");

            if(getIntent().hasExtra("notificationType")){
                final String notificationType = getIntent().getStringExtra("notificationType");
                GlobalVariables.getMessagesNotificationMap().remove(postId + notificationType);
            }


            if(user!=null){

                firestore.collection("Users").document(
                        getIntent().hasExtra("notificationCreatorId")?getIntent().getStringExtra("notificationCreatorId"):user.getUid())
                        .collection("UserPosts").document(postId)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            postRef = documentSnapshot.getReference();
                            isForUser = true;
                        }else{
                            postRef = firestore.collection("Posts").document(postId);
                        }

                        fetchPostData(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        postRef = firestore.collection("Posts").document(postId);

                        fetchPostData(user);
                    }
                });

            }else{
                finish();
            }
        }else{
            finish();
        }

    }


    private void fetchPostData(FirebaseUser user){
        postRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    postData = documentSnapshot.toObject(PostData.class);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful() && postData!=null){

                    setClickListeners();

                    getUserInfo();

                    if (user != null && !user.isAnonymous()) {
                        if (postData.getPublisherId().equals(user.getUid())) {
                            toolbar.inflateMenu(R.menu.post_menu);

                        } else if (GlobalVariables.getInstance().getRole().equals("Admin")) {
                            toolbar.inflateMenu(R.menu.admin_menu);
                        } else {
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
                            GlobalVariables.getInstance().getLikesList().contains(postData.getPostId()) ? R.color.red :
                                    R.color.black));

                    getNewsType();

                }

            }
        });
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
                newsIv.setImageResource(R.drawable.document_icon);

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
                    Picasso.get().load(imageUrl).fit().centerCrop().into(userIv);
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
                    postData.getComments(),isForUser,postData.getPublisherId(),
                    PostData.TYPE_NEWS);

            commentsFragment.show(getSupportFragmentManager(), "CommentsFragment");

        } else if (id == newsIv.getId()) {

            if (postData.getAttachmentUrl() != null) {
                if (postData.getAttachmentType() == Files.IMAGE) {

//                    frameLayout.setVisibility(View.VISIBLE);
//                    getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
//                            new ImageFullScreenFragment(postData.getAttachmentUrl(),
//                                    getFileName()), "FullScreen")
//                            .commit();

                    FullScreenImagesUtil.showImageFullScreen(this,
                            postData.getAttachmentUrl(),
                            null,
                            getFileName());

//                    new ImageFullScreenFragment(postData.getAttachmentUrl(),
//                            getFileName()).show(getSupportFragmentManager(),
//                            "fullScreen");


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

//            DocumentReference documentReference;
//            if(getIntent().hasExtra("isForUser")){
//                documentReference = FirebaseFirestore.getInstance()
//                        .collection("Users")
//                        .document(postData.getPublisherId())
//                        .collection("UserPosts")
//                        .document(postData.getPostId());
//
//            }else{
//                documentReference = FirebaseFirestore.getInstance()
//                        .collection("Posts")
//                        .document(postData.getPostId());
//
//            }

            int type;
            if (GlobalVariables.getLikesList().contains(postData.getPostId())) {

                likeTv.setTextColor(getResources().getColor(R.color.black));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString()) - 1)));

                type = 2;

            } else {

                likeTv.setTextColor(getResources().getColor(R.color.red));

                likesTv.setText(String.valueOf(
                        (Integer.parseInt(likesTv.getText().toString()) + 1)));

                type = 1;

            }


            PostData.likePost(postData.getPostId(), postData.getTitle(), type,
                    postData.getPublisherId(), this,
                    getIntent().hasExtra("isForUser"),PostData.TYPE_NEWS,likeTv);

        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.edit) {
            Intent intent = new Intent(PostNewsActivity.this, PostNewActivity.class);
            intent.putExtra("postData", postData);
            intent.putExtra("isForEditing", true);
//            intent.putExtra("postID",postData.getPostId());
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                FirebaseFirestore.getInstance().collection("Users").
                        document(postData.getPublisherId()).collection("UserPosts")
                        .document(postData.getPostId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                       if (documentSnapshot.exists()){
                           intent.putExtra("justForUser", "justForUser");
                       }
                        startActivity(intent);
                        finish();
                    }
                });
        } else if (item.getItemId() == R.id.delete) {

            PostData.deletePost(this,postData);

    } else if (item.getItemId() == R.id.Reporting) {

            FirebaseFirestore.getInstance().collection("Posts").
                    document(postData.getPostId()).update("isReported", true).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        } else {
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