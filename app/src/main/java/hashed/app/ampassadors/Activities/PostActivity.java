package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Fragments.FilePickerPreviewFragment;
import hashed.app.ampassadors.Fragments.VideoPickerPreviewFragment;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PostActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    EditText post_text;
    Button posting;
    Button poll;
    Button image_btn;
    Button video_btn;
    Button pdf;
    ImageView postImage;
    CircleImageView user_image;
    TextView username;
    ProgressDialog mProgressDialog;
    FirebaseFirestore firebaseFirestore;
    CollectionReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;
    EditText postTitle;
    private Bitmap videoThumbnailBitmap = null;
    User user;
    String downloadUrl;
    private Uri filePath;
    boolean uploading = false;
    FirebaseStorage video;
    StorageReference videoRef;
    Uri vedioUrl;
    MediaController mediaController;
    Uri uri;

    int attachmentType = 0 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        setUpComponte();
        clickListiners();

    }

    // set up all ui design
    public void setUpComponte() {
        post_text = findViewById(R.id.posting_filed);
        posting = findViewById(R.id.create_post_btn);
        poll = findViewById(R.id.poll_btn_post);
        image_btn = findViewById(R.id.image_btn_post);
        video_btn = findViewById(R.id.vedio_btn_post);
        pdf = findViewById(R.id.pdf_btn_post);
        user_image = findViewById(R.id.image_create_post);
        username = findViewById(R.id.username_post_in_create_psot);
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Posts");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        postImage = findViewById(R.id.image_create_post);
        mProgressDialog = new ProgressDialog(this);
        mediaController = new MediaController(this);
        postTitle = findViewById(R.id.post_title);

    }

    public void clickListiners() {
        //upload text post
        posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String postTexting = post_text.getText().toString();
                String posttitle = postTitle.getText().toString();


                if (!postTexting.isEmpty() ||!posttitle.isEmpty()|| !downloadUrl.isEmpty()) {
                    mProgressDialog.setMessage("publishing form");
                    mProgressDialog.show();

                    HashMap<String, Object> dataMap = new HashMap<>();
                    String postId = UUID.randomUUID().toString();
                    dataMap.put("postId", postId);
                    dataMap.put("tilte",posttitle);
                    dataMap.put("publisherId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    dataMap.put("attachmentUrl", downloadUrl);
                    dataMap.put("attachmentType", attachmentType);
                    dataMap.put("publishTime", System.currentTimeMillis());
                    dataMap.put("likes", 0);
                    dataMap.put("comments", 0);
                    dataMap.put("description", postTexting);
                    dataMap.put("type", 1);

                    reference.document(postId).set(dataMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();

                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mProgressDialog.dismiss();

                            Toast.makeText(PostActivity.this, "Failed to post!" +
                                    " Please try again", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                }

            }
        });

        image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Files.startImageFetchIntent(PostActivity.this);
            }
        });
        pdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Files.startDocumentFetchIntent(PostActivity.this);

            }
        });
        video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Files.startVideoFetchIntent(PostActivity.this);
            }
        });

    }


    // Upload and download image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Files.PICK_IMAGE) {

            if (resultCode == RESULT_OK && data != null) {
                uri = data.getData();
                Picasso.get().load(data.getData()).fit().into(postImage);
            } else {
                //problem with image retrieving
            }
        } else if (requestCode == Files.PICK_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {
               uri = data.getData();
                 getVideo(uri);
            } else {
                //problem with image retrieving
            }
        } else if (requestCode == Files.PICK_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                if (Files.getFileSizeInMB(this, data.getData()) > Files.MAX_FILE_SIZE) {
                    Toast.makeText(this, "You can't send files bigger than "
                            + Files.MAX_FILE_SIZE + " MB!", Toast.LENGTH_SHORT).show();

                    uri = data.getData();

                } else {
                }

            } else {
                //problem with image retrieving

            }
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
    public void  getVideo(Uri uri){
        new Thread(new Runnable() {

            @Override
            public void run() {

                final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(PostActivity.this, uri);
                final long time = Long.parseLong(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                if(time > 0){

                    if(time>=1000){
                        videoThumbnailBitmap = retriever.getFrameAtTime(1000);
                    }else{
                        videoThumbnailBitmap = retriever.getFrameAtTime(time);
                    }

                }else{
                    //video stupid
                }

                if(videoThumbnailBitmap!=null){
                    postImage.post(new Runnable() {
                        @Override
                        public void run() {
                            postImage.setImageBitmap(videoThumbnailBitmap);
                        }
                    });
                }
            }
        }).start();
    }
}