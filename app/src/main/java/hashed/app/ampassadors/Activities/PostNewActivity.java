package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Fragments.VideoFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.PostNewsPreview;
import hashed.app.ampassadors.Objects.UserPostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FileDownloadUtil;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;
import hashed.app.ampassadors.Utils.UploadTaskUtil;


public class PostNewActivity extends AppCompatActivity implements View.OnClickListener {

  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private ImageView userIv, pdfIv, videoIv, imageIv, attachmentIv, videoPlayIv;
  private Button publishBtn;
  private TextView usernameTv, attachmentTv;
  private EditText titleEd, descriptionEd;
  private LinearLayout attachmentLinear;
  private PlayerView playerView;
  private Uri attachmentUri;
  private Bitmap videoThumbnailBitmap;
  private SimpleExoPlayer simpleExoPlayer;
  private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap;
  private int attachmentType = Files.TEXT;
  private String documentName;
  private double documentSize;
  private CheckBox checkBox;

  private UserPostData userPostData;
  //editing
  private PostData postData;
  private boolean isForEditing;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_new);

    isForEditing = getIntent().hasExtra("isForEditing");

    setupToolbar();

    getViews();

    if (isForEditing) {
      fillPostData();
    } else {
      getUserInfo(currentUid);
    }

    setClickListeners();
    if (getIntent().hasExtra("justForUser")) {
      checkBox.setVisibility(View.GONE);
    } else {
      checkBox.setVisibility(View.VISIBLE);
    }

  }

  private void setupToolbar() {
    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    if (isForEditing) {
      toolbar.setTitle(getResources().getString(R.string.edit_post));
    }
  }


  private void getViews() {

    userIv = findViewById(R.id.userIv);
    pdfIv = findViewById(R.id.pdfIv);
    videoIv = findViewById(R.id.videoIv);
    imageIv = findViewById(R.id.imageIv);
    attachmentIv = findViewById(R.id.attachmentIv);
    publishBtn = findViewById(R.id.edit_btn);
    usernameTv = findViewById(R.id.usernameTv);
    titleEd = findViewById(R.id.titleEd);
    descriptionEd = findViewById(R.id.descriptionEd);
    attachmentLinear = findViewById(R.id.attachmentLinear);
    videoPlayIv = findViewById(R.id.videoPlayIv);
    playerView = findViewById(R.id.playerView);
    attachmentTv = findViewById(R.id.attachmentTv);
    checkBox = findViewById(R.id.checkbox);

    if (isForEditing) {
      publishBtn.setText(getResources().getString(R.string.edit));
    }
  }

  private void setClickListeners() {


    publishBtn.setOnClickListener(this);
    pdfIv.setOnClickListener(this);
    videoIv.setOnClickListener(this);
    imageIv.setOnClickListener(this);

  }

  private void fillPostData() {

    postData = (PostData) getIntent().getSerializableExtra("postData");

    getUserInfo(postData.getPublisherId());

    attachmentType = postData.getAttachmentType();
    checkBox.setChecked(postData.isImportant());

//    if (postData.getAttachmentType() == Files.IMAGE) {
//      Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(attachmentIv);
//    } else if (postData.getAttachmentType() == Files.VIDEO) {
//      Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerInside().into(attachmentIv);
//    } else if (postData.getAttachmentType() == Files.VIDEO) {
//      Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerInside().into(attachmentIv);
//    }

    titleEd.setText(postData.getTitle());

    descriptionEd.setText(postData.getDescription());

    switch (postData.getAttachmentType()) {

      case Files.IMAGE:
        Picasso.get().load(postData.getAttachmentUrl()).fit().centerCrop().into(attachmentIv);
        break;

      case Files.VIDEO:

        Picasso.get().load(postData.getVideoThumbnailUrl()).fit().centerInside().into(attachmentIv);

        videoPlayIv.setVisibility(View.VISIBLE);

        attachmentIv.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            attachmentIv.setOnClickListener(null);
            attachmentIv.setImageBitmap(null);
            attachmentIv.setVisibility(View.GONE);
            videoPlayIv.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            playerView.setPlayer(SetupPlayer(postData.getAttachmentUrl()));
          }
        });

        break;

      case Files.DOCUMENT:

        attachmentTv.setVisibility(View.VISIBLE);
        attachmentIv.setImageResource(R.drawable.document_icon);
        attachmentIv.setScaleType(ImageView.ScaleType.CENTER);

//        FirebaseStorage.getInstance().getReferenceFromUrl(postData.getAttachmentUrl())
//                .getFile()
        documentName = postData.getDocumentName();
//        documentSize = (double) fileMap.get("fileSize");
        attachmentTv.setText(documentName);

        break;

    }


  }

  private void getUserInfo(String id) {

    FirebaseFirestore.getInstance().collection("Users")
            .document(id).get().addOnSuccessListener(snapshot -> {
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

    if (view.getId() == publishBtn.getId()) {

      if (isForEditing) {
        updatePost();
      } else {
        startPostPublishing();
      }


    } else if (view.getId() == pdfIv.getId()) {

      Files.startDocumentFetchIntent(this);

    } else if (view.getId() == videoIv.getId()) {

      Files.startVideoFetchIntent(this);

    } else if (view.getId() == imageIv.getId()) {

      Files.startImageFetchIntent(this);

    }
  }

  private void updatePost() {

    final String title = titleEd.getText().toString().trim();
    final String description = descriptionEd.getText().toString().trim();


    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage(getString(R.string.EditingMessage));
    progressDialog.setCancelable(false);
    progressDialog.show();


    if (attachmentUri != null) {

      uploadTaskMap = new HashMap<>();

      switch (attachmentType) {
        case Files.IMAGE:

          uploadImage(title, description, progressDialog);

          break;

        case Files.VIDEO:

          uploadVideo(title, description, progressDialog);

          break;

        case Files.DOCUMENT:

          uploadDocument(title, description, progressDialog);

          break;

        case Files.TEXT:
          publishPostUpdate(title, description,null,null,progressDialog);
          break;
      }

    }else{
      publishPostUpdate(title, description,null,null,progressDialog);
    }

  }

  private void publishPostUpdate(String title, String description, String attachmentUrl,
                                 String videoThumbnailUrl, ProgressDialog progressDialog){

    Map<String, Object> updateMap = new HashMap<>();

    if(!title.equals(postData.getTitle())){
      updateMap.put("title",title);
    }

    if(!description.equals(postData.getDescription())){
      updateMap.put("description",description);
    }

    if(attachmentType != postData.getAttachmentType()){
      updateMap.put("attachmentType",attachmentType);
    }

    if(attachmentUri!=null && attachmentType == Files.DOCUMENT){
      updateMap.put("documentName",documentName);
      updateMap.put("documentSize",documentSize);
    }

    if(checkBox.isChecked() != postData.isImportant()){
      updateMap.put("important",checkBox.isChecked());
    }

    if(attachmentUrl!=null){
      updateMap.put("attachmentUrl",attachmentUrl);
    }

    if(videoThumbnailUrl!=null){
      updateMap.put("videoThumbnailUrl",videoThumbnailUrl);
    }

     DocumentReference documentReference ;
    if (getIntent().hasExtra("justForUser")) {

      documentReference = FirebaseFirestore.getInstance().collection("Users")
              .document(postData.getPublisherId())
              .collection("UserPosts")
              .document(postData.getPostId());
    }else {

        documentReference =
              FirebaseFirestore.getInstance().collection("Posts")
                      .document(postData.getPostId());

    }
      if(postData.getAttachmentUrl()!=null){

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReferenceFromUrl(postData.getAttachmentUrl()).delete();

        if(postData.getAttachmentType() == Files.VIDEO){

          documentReference.update("videoThumbnailUrl", FieldValue.delete());

          storage.getReferenceFromUrl(postData.getVideoThumbnailUrl()).delete();

        }else if(postData.getAttachmentType() == Files.DOCUMENT){

          documentReference.update("documentName", FieldValue.delete(),
                  "documentSize",FieldValue.delete());

        }



      documentReference.update(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          Toast.makeText(PostNewActivity.this, "Successfully updated post",
                  Toast.LENGTH_SHORT).show();
          progressDialog.dismiss();

          finish();

        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

          Toast.makeText(PostNewActivity.this, R.string.Error_UpdateFail,
                  Toast.LENGTH_SHORT).show();
          progressDialog.dismiss();
          Log.d("qqq",e.getMessage());
        }
      });

    }

  }

  private void startPostPublishing() {
    final String title = titleEd.getText().toString().trim();
    final String description = descriptionEd.getText().toString().trim();

    if (title.isEmpty()) {
      Toast.makeText(this, R.string.Message_Fill_title
              , Toast.LENGTH_SHORT).show();
      return;
    }

    if (description.isEmpty()) {
      Toast.makeText(this, R.string.Message_Fill_descr
              , Toast.LENGTH_SHORT).show();
      return;
    }


    if (attachmentUri == null && !getIntent().hasExtra("justForUser")) {
      Toast.makeText(this, R.string.add_an_attachment_warning
              , Toast.LENGTH_SHORT).show();
      return;
    }

    publishBtn.setClickable(false);

    if (simpleExoPlayer != null && simpleExoPlayer.getPlayWhenReady()) {
      simpleExoPlayer.setPlayWhenReady(false);
    }

    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage(getString(R.string.Publish));
    progressDialog.setCancelable(false);
    progressDialog.show();

    uploadTaskMap = new HashMap<>();


    switch (attachmentType) {
      case Files.IMAGE:
        uploadImage(title, description, progressDialog);
        break;

      case Files.VIDEO:
        uploadVideo(title, description, progressDialog);
        break;

      case Files.DOCUMENT:
        uploadDocument(title, description, progressDialog);
        break;

        case Files.TEXT:
          publishPost(title,description,null,null,progressDialog);
          break;
      }



  }

  private void publishPost(String title, String description, String attachmentUrl,
                           String videoThumbnailUrl, ProgressDialog progressDialog) {

    final HashMap<String, Object> dataMap = new HashMap<>();
    final String postId = UUID.randomUUID().toString();
    dataMap.put("postId", postId);
    dataMap.put("title", title);
    dataMap.put("description", description);
    dataMap.put("publisherId", currentUid);
    dataMap.put("important", checkBox.isChecked());
    dataMap.put("isReported", false);
    dataMap.put("attachmentType", attachmentType);
    if (attachmentUrl != null) {
      dataMap.put("attachmentUrl", attachmentUrl);
    }

    dataMap.put("keyWords", Arrays.asList(title.toLowerCase().trim().split(" ")));

    if (videoThumbnailUrl != null) {
      dataMap.put("videoThumbnailUrl", videoThumbnailUrl);
    }

    if (documentName != null) {
      dataMap.put("documentName", documentName);
    }

    if (documentSize != 0) {
      dataMap.put("documentSize", documentSize);
    }
    dataMap.put("publishTime", System.currentTimeMillis());
    dataMap.put("likes", 0);
    dataMap.put("comments", 0);
    dataMap.put("type", 1);


    if (getIntent().hasExtra("justForUser")) {

      DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
              .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
              .collection("UserPosts").document(postId);

      reference.set(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          progressDialog.dismiss();

          setResult(3, new Intent().putExtra("postData",
                  new PostData(dataMap)));

          finish();
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Toast.makeText(PostNewActivity.this,
                  R.string.post_publish_error, Toast.LENGTH_LONG).show();
          progressDialog.dismiss();
        }
      });



    } else {

      DocumentReference reference = FirebaseFirestore.getInstance().collection("Posts")
              .document(postId);

      reference.set(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
          progressDialog.dismiss();

          setResult(3, new Intent().putExtra("postNewsPreview",
                  new PostNewsPreview(dataMap)));
          finish();
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Toast.makeText(PostNewActivity.this,
                  R.string.post_publish_error, Toast.LENGTH_LONG).show();
          progressDialog.dismiss();
        }
      });

    }
  }

  private void uploadImage(String title, String description, ProgressDialog progressDialog) {

    final StorageReference reference = FirebaseStorage.getInstance().getReference()
            .child(Files.POST_IMAGE_REF).child(UUID.randomUUID().toString() + "-" +
                    System.currentTimeMillis());

    final UploadTask uploadTask = reference.putFile(attachmentUri);

    final StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
            uploadTask.addOnSuccessListener(taskSnapshot -> {
              uploadTaskMap.remove(uploadTask);
              reference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                final String attachmentUrl = uri1.toString();

                if (isForEditing) {
                  publishPostUpdate(title, description, attachmentUrl, null, progressDialog);
                } else {
                  publishPost(title, description, attachmentUrl, null, progressDialog);
                }
              }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Toast.makeText(PostNewActivity.this,
                          R.string.post_publish_error, Toast.LENGTH_LONG).show();
                  progressDialog.dismiss();
                }
              });
            }).addOnCompleteListener(task ->
                    new File(attachmentUri.getPath()).delete())
                    .addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {

                        Toast.makeText(PostNewActivity.this,
                                R.string.post_publish_error, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                      }
                    });

    uploadTaskMap.put(uploadTask, onSuccessListener);

  }


  private void uploadDocument(String title, String description, ProgressDialog progressDialog) {

    final StorageReference reference = FirebaseStorage.getInstance().getReference()
            .child(Files.POST_DOCUMENT_REF).child(UUID.randomUUID().toString() + "-" +
                    System.currentTimeMillis());

    final UploadTask uploadTask = reference.putFile(attachmentUri);

    final StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
            uploadTask.addOnSuccessListener(taskSnapshot -> {
              uploadTaskMap.remove(uploadTask);
              reference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                final String attachmentUrl = uri1.toString();

                if (isForEditing) {
                  publishPostUpdate(title, description, attachmentUrl, null, progressDialog);
                } else {
                  publishPost(title, description, attachmentUrl, null, progressDialog);
                }

              }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  Toast.makeText(PostNewActivity.this,
                          R.string.post_publish_error, Toast.LENGTH_LONG).show();
                  progressDialog.dismiss();
                }
              });
            }).addOnCompleteListener(task ->
                    new File(attachmentUri.getPath()).delete()).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostNewActivity.this,
                        R.string.post_publish_error, Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
              }
            });

    uploadTaskMap.put(uploadTask, onSuccessListener);

  }

  private void uploadVideo(String title, String description, ProgressDialog progressDialog) {

    final StorageReference reference = FirebaseStorage.getInstance().getReference()
            .child(Files.POST_VIDEO_REF).child(UUID.randomUUID().toString() + "-" +
                    System.currentTimeMillis());

    final UploadTask uploadTask = reference.putFile(attachmentUri);

    final StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
            uploadTask.addOnSuccessListener(taskSnapshot -> {
              uploadTaskMap.remove(uploadTask);
              reference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                final String attachmentUrl = uri1.toString();

                final StorageReference thumbnailReference =
                        FirebaseStorage.getInstance().getReference()
                                .child(Files.POST_THUMBNAIL_REF).child(UUID.randomUUID().toString()
                                + "-" + System.currentTimeMillis());

                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                videoThumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                final UploadTask thumbnailUploadTask =
                        thumbnailReference.putBytes(baos.toByteArray());

                StorageTask<UploadTask.TaskSnapshot> onSuccessListener2 =
                        thumbnailUploadTask.addOnSuccessListener(taskSnapshot2 -> {
                          uploadTaskMap.remove(thumbnailUploadTask);
                          thumbnailReference.getDownloadUrl().addOnSuccessListener(uri -> {

                            final String videoThumbnailUrl = uri.toString();

                            Log.d("ttt", "videoThumbnailUrl: " + videoThumbnailUrl);

                            if (isForEditing) {
                              publishPostUpdate(title, description, attachmentUrl,
                                      videoThumbnailUrl, progressDialog);
                            } else {
                              publishPost(title, description, attachmentUrl,
                                      videoThumbnailUrl, progressDialog);
                            }



                          }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                              Toast.makeText(PostNewActivity.this,
                                      R.string.post_publish_error,
                                      Toast.LENGTH_LONG).show();
                              progressDialog.dismiss();
                            }
                          });
                        }).addOnFailureListener(new OnFailureListener() {
                          @Override
                          public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostNewActivity.this,
                                    R.string.post_publish_error,
                                    Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                          }
                        });

                uploadTaskMap.put(thumbnailUploadTask, onSuccessListener2);

              });
            }).addOnCompleteListener(task ->
                    new File(attachmentUri.getPath()).delete()).addOnFailureListener(
                    new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PostNewActivity.this,
                                R.string.post_publish_error,
                                Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                      }
                    });

    uploadTaskMap.put(uploadTask, onSuccessListener);

  }


  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      switch (requestCode) {
        case Files.PICK_FILE:
          Files.startDocumentFetchIntent(this);
          break;
        case Files.PICK_IMAGE:
          Files.startImageFetchIntent(this);
          break;
        case Files.PICK_VIDEO:
          Files.startVideoFetchIntent(this);
          break;
      }
    } else {

      //permission denied
    }


  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK && data != null && data.getData() != null) {

      attachmentUri = data.getData();
//      attachmentLinear.setVisibility(View.GONE);

      switch (requestCode) {

        case Files.PICK_IMAGE:

          attachmentType = Files.IMAGE;

          videoPlayIv.setVisibility(View.GONE);
          attachmentIv.setOnClickListener(null);

          attachmentTv.setVisibility(View.GONE);
          attachmentTv.setText("");

          Picasso.get().load(attachmentUri).fit().centerCrop().into(attachmentIv);
          break;

        case Files.PICK_VIDEO:
          attachmentType = Files.VIDEO;
          getVideoThumbnail();

          attachmentTv.setVisibility(View.GONE);
          attachmentTv.setText("");

          videoPlayIv.setVisibility(View.VISIBLE);

          attachmentIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              attachmentIv.setOnClickListener(null);
              attachmentIv.setImageBitmap(null);
              attachmentIv.setVisibility(View.GONE);
              videoPlayIv.setVisibility(View.GONE);
              playerView.setVisibility(View.VISIBLE);
              playerView.setPlayer(SetupPlayer(null));
            }
          });

          break;

        case Files.PICK_FILE:


          videoPlayIv.setVisibility(View.GONE);
          attachmentIv.setOnClickListener(null);

          attachmentType = Files.DOCUMENT;
          attachmentTv.setVisibility(View.VISIBLE);
          attachmentIv.setImageResource(R.drawable.document_icon);
          attachmentIv.setScaleType(ImageView.ScaleType.CENTER);

          final Map<String, Object> fileMap = Files.getFileInfo(this, attachmentUri);

          documentName = (String) fileMap.get("fileName");
          documentSize = (double) fileMap.get("fileSize");
          attachmentTv.setText(documentName + " - " + documentSize + " MB");

          break;

      }
    }
  }



  private void getVideoThumbnail() {

    new Thread(new Runnable() {

      @Override
      public void run() {

        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(PostNewActivity.this, attachmentUri);
        final long time = Long.parseLong(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        if (time > 0) {

          if (time >= 1000) {
            videoThumbnailBitmap = retriever.getFrameAtTime(1000);
          } else {
            videoThumbnailBitmap = retriever.getFrameAtTime(time);
          }

        } else {
          //video stupid
        }

        if (videoThumbnailBitmap != null) {
          attachmentIv.post(new Runnable() {
            @Override
            public void run() {
              attachmentIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
              attachmentIv.setImageBitmap(videoThumbnailBitmap);
            }
          });
        }
      }
    }).start();
  }


  private SimpleExoPlayer SetupPlayer(String url) {

    if (url != null) {
      new VideoFullScreenFragment(url).show(getSupportFragmentManager(), "video");
    } else {
      simpleExoPlayer = new SimpleExoPlayer.Builder(this,
              new DefaultRenderersFactory(this)).build();

      DataSpec dataSpec = new DataSpec(attachmentUri);
      final FileDataSource fileDataSource = new FileDataSource();
      try {
        fileDataSource.open(dataSpec);
      } catch (FileDataSource.FileDataSourceException e) {
        e.printStackTrace();
      }


      DataSource.Factory dataSourceFactory =
              new DefaultDataSourceFactory(this, Util.getUserAgent(this,
                      "simpleExoPlayer"));

      MediaSource firstSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
              .createMediaSource(MediaItem.fromUri(fileDataSource.getUri()));

      simpleExoPlayer.prepare(firstSource, true, true);

      simpleExoPlayer.setPlayWhenReady(true);
      return simpleExoPlayer;
    }

    return null;

  }

  @Override
  public void onBackPressed() {

    if (!descriptionEd.getText().toString().trim().isEmpty()
            || !titleEd.getText().toString().trim().isEmpty()
            || attachmentUri != null) {

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Do you want to leave without creating this post?");
      alert.setMessage("Leaving will discard this post");

      alert.setPositiveButton("Leave", (dialogInterface, i) -> {
        UploadTaskUtil.cancelUploadTasks(uploadTaskMap);
        dialogInterface.dismiss();
        finish();
      });

      alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
      alert.create().show();

    } else {
      super.onBackPressed();
    }

  }


  @Override
  protected void onPause() {
    super.onPause();
    if (simpleExoPlayer != null) {
      simpleExoPlayer.setPlayWhenReady(false);
    }
  }


  @Override
  protected void onResume() {
    super.onResume();
    if (simpleExoPlayer != null) {
      simpleExoPlayer.setPlayWhenReady(true);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    UploadTaskUtil.cancelUploadTasks(uploadTaskMap);

    if (simpleExoPlayer != null) {
      playerView.setPlayer(null);
      playerView = null;
      simpleExoPlayer.release();
      simpleExoPlayer = null;
    }

  }

}