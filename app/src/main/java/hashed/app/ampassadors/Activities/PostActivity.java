package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

import static hashed.app.ampassadors.Utils.Files.DOCUMENT;
import static hashed.app.ampassadors.Utils.Files.IMAGE;
import static hashed.app.ampassadors.Utils.Files.VIDEO;
import static hashed.app.ampassadors.Utils.Files.getFileInfo;
import static hashed.app.ampassadors.Utils.Files.getFileLaunchIntentFromUri;
import static hashed.app.ampassadors.Utils.Files.getFileSizeInMB;

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
    int attachmentType = 0;
    String postTexting;
    String posttitle;
    TextView title;

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
        user_image = findViewById(R.id.image_user_posting_in_create_post);
        username = findViewById(R.id.username_post_in_create_psot);
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Posts");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        postImage = findViewById(R.id.postIv);
        mProgressDialog = new ProgressDialog(this);
        mediaController = new MediaController(this);
        postTitle = findViewById(R.id.post_title);
    }

    public void clickListiners() {
        //upload text post
        posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postTexting = post_text.getText().toString();
                posttitle = postTitle.getText().toString();


                if (!postTexting.isEmpty() && !posttitle.isEmpty() || !downloadUrl.isEmpty()) {
                    mProgressDialog.setMessage("publishing form");
                    mProgressDialog.show();

                    Upload(uri);
                } else if (posttitle.isEmpty()) {
                    Toast.makeText(PostActivity.this, "You have to fill the Title", Toast.LENGTH_SHORT).show();
                } else if (postTexting.isEmpty()) {
                    Toast.makeText(PostActivity.this, "You have to fill the Post", Toast.LENGTH_SHORT).show();
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
                Picasso.get().load(uri).fit().into(postImage);
                attachmentType = IMAGE;
            } else {
                //problem with image retrieving
            }
        } else if (requestCode == Files.PICK_VIDEO) {
            if (resultCode == RESULT_OK && data != null) {
                uri = data.getData();
                getVideo(uri);

                attachmentType = VIDEO;

            } else {
                //problem with image retrieving
            }
        } else if (requestCode == Files.PICK_FILE) {
            if (resultCode == RESULT_OK && data != null) {
                uri = data.getData();

                getFileInfo(PostActivity.this, uri);
                attachmentType = DOCUMENT;
                if (Files.getFileSizeInMB(this, data.getData()) > Files.MAX_FILE_SIZE) {
                    getFileSizeInMB(PostActivity.this, uri);

                    Toast.makeText(this, "You can't send files bigger than "
                            + Files.MAX_FILE_SIZE + " MB!", Toast.LENGTH_SHORT).show();


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

    public void getVideo(Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(PostActivity.this, uri);
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

    public void AddPost() {

        HashMap<String, Object> dataMap = new HashMap<>();
        String postId = UUID.randomUUID().toString();
        dataMap.put("postId", postId);
        dataMap.put("title", posttitle);
        dataMap.put("publisherId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        dataMap.put("imageUrl", downloadUrl);
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
                        dataMap.put("attachmentUrl", downloadUrl);
                        dataMap.put("attachmentType", IMAGE);
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


    }

    public void Upload(Uri uri) {
        String fileName = "";
        if (attachmentType == IMAGE) {
            fileName = "img";
            StorageReference storageReference = storage.getReference().child(fileName).child(uri.getLastPathSegment());
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl = uri.toString();
                    AddPost();
                    Toast.makeText(PostActivity.this, "Successfully Add ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();


                }
            });
        } else if (attachmentType == VIDEO) {
            fileName = "videoPost";
            StorageReference storageReference = storage.getReference().child(fileName).child(uri.getLastPathSegment());
            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl = uri.toString();
                    AddPost();
                    Toast.makeText(PostActivity.this, "Successfully Add ", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else if (attachmentType == DOCUMENT) {
            fileName = "DocumentsPost";
            StorageReference reference = storage.getReference().child(fileName).child(uri.getLastPathSegment());
            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    downloadUrl = uri.toString();
                    AddPost();
                    Toast.makeText(PostActivity.this, "Successfully Add ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

//
//    private void cancelUploadTasks(){
//
//        if(uploadTasks!=null && !uploadTasks.isEmpty()){
//            for(UploadTask uploadTask:uploadTasks.keySet()){
//
//                if(uploadTask.isComplete()){
//
//
//                }else{
//
//                    Log.d("ttt","task not complete so adding new listener, " +
//                            "and trying to cancel: "+uploadTask.cancel());
//
//                    if(uploadTasks.containsKey(uploadTask)){
//
//                        uploadTask.removeOnSuccessListener(
//                                (OnSuccessListener<? super UploadTask.TaskSnapshot>) uploadTasks.get(uploadTask));
//
//                    }
//
//                    uploadTask.addOnSuccessListener(taskSnapshot -> uploadTask.getSnapshot().getStorage().delete().addOnSuccessListener(aVoid -> Log.d("ttt", "ref delete sucess")).addOnFailureListener(e -> Log.d("ttt", "ref delete failed: " + e.getMessage())));
//
//                }
//            }
//        }




}