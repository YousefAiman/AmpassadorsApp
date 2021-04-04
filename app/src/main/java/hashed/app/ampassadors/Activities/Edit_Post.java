package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultRenderersFactory;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class Edit_Post extends AppCompatActivity implements View.OnClickListener,  Toolbar.OnMenuItemClickListener {
    CircleImageView userimage;
    EditText title;
    EditText desvEd;
    Button edit;
    TextView attachmentTv, usernameTv;
    ImageView videoPlayIv, video, image, doc, attachmentIv;
    FirebaseFirestore firebaseFirestore;
    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    FirebaseStorage storage;
    String imageUrl;
    int attachmentType;
    private Uri attachmentUri;
    private Bitmap videoThumbnailBitmap;
    private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap;
    StorageReference sreference;
    private String documentName;
    private double documentSize;
    String postid;
    String videoThumbnailUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__post);
        setupToolbar();
        setupElemnts();
        setClickListeners();
        getUserInfo();

        final String[] titlepost = new String[1];
        final String[] text_desc = new String[1];
        final String[] attachment = new String[1];
        Intent intent = getIntent();
        postid = intent.getStringExtra("postID");
        if (intent.hasExtra("postID")) {
            firebaseFirestore.collection("Posts").document(postid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        titlepost[0] = documentSnapshot.getString("title");
                        text_desc[0] = documentSnapshot.getString("description");
                        attachment[0] = documentSnapshot.getString("attachmentUrl");

                    }
                }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    title.setText(titlepost[0]);
                    desvEd.setText(text_desc[0]);
                    if (attachment[0] != null && !attachment[0].isEmpty()) {
                        Picasso.get().load(attachment[0]).fit().into(attachmentIv);
                    }
                }
                }
            });
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedata();
                }
            });
        }


    }

    public void setupElemnts() {
        userimage = findViewById(R.id.userIv);
        title = findViewById(R.id.titleEd);
        desvEd = findViewById(R.id.descriptionEd);
        edit = findViewById(R.id.edit_btn);
        attachmentTv = findViewById(R.id.attachmentTv);
        attachmentIv = findViewById(R.id.attachmentIv);
        usernameTv = findViewById(R.id.usernameTv);
        video = findViewById(R.id.videoIv);
        image = findViewById(R.id.imageIv);
        doc = findViewById(R.id.documentIv);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        sreference = storage.getReference();
        videoPlayIv = findViewById(R.id.videoPlayIv);
    }

    private void UpdatePost(String txt_title, String txt_desc, String attachment, String videoThumbnailUrl, int attachmentType,ProgressDialog progressDialog) {
        final DocumentReference df = FirebaseFirestore.getInstance().collection("Posts")
                .document(postid);
        df.update("description", txt_desc,
                "title", txt_title,
                "attachmentUrl", attachment , "attachmentType",attachmentType).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (attachment != null && !attachment.isEmpty()) {

                    df.update("attachmentUrl", attachment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            edit.setClickable(true);
                            Intent intent = new Intent(Edit_Post.this, Profile.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    edit.setClickable(true);
                    Intent intent = new Intent(Edit_Post.this, Profile.class);
                    startActivity(intent);
                    finish();
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Edit_Post.this, R.string.Error_UpdateFail+"  "+e.getMessage()
                        , Toast.LENGTH_SHORT).show();
                Log.d("eee",e.getMessage());
                progressDialog.dismiss();
                edit.setClickable(true);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {

            attachmentUri = data.getData();
//      attachmentLinear.setVisibility(View.GONE);

            switch (requestCode) {
                case Files.PICK_IMAGE:
                    attachmentType = Files.IMAGE;
                    Picasso.get().load(attachmentUri).fit().centerCrop().into(attachmentIv);
                    break;

                case Files.PICK_VIDEO:
                    attachmentType = Files.VIDEO;
                    getVideoThumbnail();
                    videoPlayIv.setVisibility(View.VISIBLE);

                    attachmentTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            attachmentIv.setOnClickListener(null);
                            attachmentIv.setImageBitmap(null);
                            attachmentIv.setVisibility(View.GONE);
                            videoPlayIv.setVisibility(View.GONE);
                            playerView.setVisibility(View.VISIBLE);
                            playerView.setPlayer(SetupPlayer());
                        }
                    });

                    break;

                case Files.PICK_FILE:

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
                retriever.setDataSource(Edit_Post.this, attachmentUri);
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
                    attachmentIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    attachmentIv.post(new Runnable() {
                        @Override
                        public void run() {
                            attachmentIv.setImageBitmap(videoThumbnailBitmap);
                        }
                    });
                }
            }
        }).start();
    }


    private SimpleExoPlayer SetupPlayer() {

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
                .createMediaSource(fileDataSource.getUri());

        simpleExoPlayer.prepare(firstSource, true, true);

        simpleExoPlayer.setPlayWhenReady(true);

        return simpleExoPlayer;
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

        cancelUploadTasks();

        if (simpleExoPlayer != null) {
            playerView.setPlayer(null);
            playerView = null;
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == edit.getId()) {

            final String titletxt = title.getText().toString().trim();
            final String description = desvEd.getText().toString().trim();

            if (titletxt.isEmpty()) {
                Toast.makeText(this, R.string.Message_Fill_title
                        , Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, R.string.Message_Fill_descr
                        , Toast.LENGTH_SHORT).show();
                return;
            }

//      if (attachmentUri == null) {
//        Toast.makeText(this, R.string.Message_Attchent_post
//                , Toast.LENGTH_SHORT).show();
//        return;
//      }

            edit.setClickable(false);

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
                    uploadImage(titletxt, description, progressDialog);
                    break;

                case Files.VIDEO:
                    uploadVideo(titletxt, description, progressDialog);
                    break;

                case Files.DOCUMENT:
                    uploadDocument(titletxt, description, progressDialog);
                    break;

                case Files.TEXT:
                    uploadDocument(titletxt, description, progressDialog);
                    break;

            }


        } else if (view.getId() == doc.getId()) {

            Files.startDocumentFetchIntent(this);

        } else if (view.getId() == video.getId()) {

            Files.startVideoFetchIntent(this);

        } else if (view.getId() == image.getId()) {

            Files.startImageFetchIntent(this);

        }
    }


    private void setClickListeners() {

        edit.setOnClickListener(this);
        doc.setOnClickListener(this);
        video.setOnClickListener(this);
        image.setOnClickListener(this);

    }

    private void getUserInfo() {

        Intent intent = getIntent();
        String userid = intent.getStringExtra("userid");
        FirebaseFirestore.getInstance().collection("Users")
                .document(userid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {

                final String imageUrl = snapshot.getString("imageUrl");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).fit().centerCrop().into(userimage);
                }
                usernameTv.setText(snapshot.getString("username"));

            }
        });
    }

    private void cancelUploadTasks() {

        if (uploadTaskMap != null && !uploadTaskMap.isEmpty()) {
            for (UploadTask uploadTask : uploadTaskMap.keySet()) {
                if (uploadTaskMap.containsKey(uploadTask)) {

                    uploadTask.removeOnSuccessListener(
                            (OnSuccessListener<? super UploadTask.TaskSnapshot>)
                                    uploadTaskMap.get(uploadTask));

                }

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadTask.getSnapshot().getStorage().delete();
                    }
                });
            }
        }


    }


    private void uploadImage(String txt_title, String txt_desc, ProgressDialog progressDialog) {

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child(Files.POST_IMAGE_REF).child(UUID.randomUUID().toString() + "-" +
                        System.currentTimeMillis());

        final UploadTask uploadTask = reference.putFile(attachmentUri);

        final StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    uploadTaskMap.remove(uploadTask);
                    reference.getDownloadUrl().addOnSuccessListener(uri1 -> {

                        final String attachmentUrl = uri1.toString();

                        UpdatePost(txt_title, txt_desc, attachmentUrl, null,attachmentType, progressDialog);

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Edit_Post.this,
                                    R.string.post_publish_error, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
                }).addOnCompleteListener(task ->
                        new File(attachmentUri.getPath()).delete())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(Edit_Post.this,
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

                        UpdatePost(title, description, attachmentUrl, null,attachmentType, progressDialog);

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Edit_Post.this,
                                    R.string.post_publish_error, Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                        }
                    });
                }).addOnCompleteListener(task ->
                        new File(attachmentUri.getPath()).delete()).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Edit_Post.this,
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

                                       videoThumbnailUrl = uri.toString();

                                        Log.d("ttt", "videoThumbnailUrl: " + videoThumbnailUrl);

                                        UpdatePost(title, description, attachmentUrl,
                                                videoThumbnailUrl,attachmentType, progressDialog);

                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Edit_Post.this,
                                                    R.string.post_publish_error,
                                                    Toast.LENGTH_LONG).show();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Edit_Post.this,
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
                                Toast.makeText(Edit_Post.this,
                                        R.string.post_publish_error,
                                        Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });

        uploadTaskMap.put(uploadTask, onSuccessListener);

    }


    private void setupToolbar() {

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(this);


    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    private void updatedata() {

        String texttitle = title.getText().toString();
        String textdesc = desvEd.getText().toString();
//        Picasso.get().load(attachmentUri).fit().into(attachmentIv);

        if (TextUtils.isEmpty(texttitle)
                || TextUtils.isEmpty(texttitle) || TextUtils.isEmpty(textdesc)) {
            Toast.makeText(Edit_Post.this, "All field are required", Toast.LENGTH_SHORT).show();
        } else {

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.Update_Message));
            progressDialog.setCancelable(false);
            progressDialog.show();
            edit.setClickable(false);

            if (attachmentUri != null) {

                StorageReference filepath = sreference.child("attachmentUrl")
                        .child(attachmentUri.getLastPathSegment());

                filepath.putFile(attachmentUri).
                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        imageUrl = uri.toString();
                                        UpdatePost(texttitle, textdesc,imageUrl ,null,attachmentType, progressDialog);
                                    }
                                });
                            }
                        });

            } else {
                UpdatePost(texttitle, textdesc,imageUrl,null,attachmentType, progressDialog);;
            }

        }
    }

}