package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Authentication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.PostData;
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
    User user;
    String downloadUrl;
    private Uri filePath;
    boolean uploading = false;
    private String cameraImageFilePath;

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
        postImage = findViewById(R.id.image_create_post);
        mProgressDialog = new ProgressDialog(this);
    }

    public void clickListiners() {
        //upload text post
        posting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String postTexting = post_text.getText().toString();

                if(!postTexting.isEmpty() && !downloadUrl.isEmpty()){
                    mProgressDialog.setMessage("publishing form");
                    mProgressDialog.show();

                    HashMap<String, Object> dataMap = new HashMap<>();
                    String postId = UUID.randomUUID().toString();
                    dataMap.put("postId",postId);
                    dataMap.put("publisherId",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    dataMap.put("imageUrl",downloadUrl);
                    dataMap.put("publishTime",System.currentTimeMillis());
                    dataMap.put("likes",0);
                    dataMap.put("comments",0);
                    dataMap.put("description",postTexting);
                    dataMap.put("type",1);

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

                }else{

                    Toast.makeText(PostActivity.this, "Please Fill in the post form!"
                            , Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostActivity.this, "PDF", Toast.LENGTH_SHORT).show();

            }
        });
        video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "Video", Toast.LENGTH_SHORT).show();
            }
        });

    }


    // Upload and download image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Files.PICK_IMAGE  &&
                resultCode == RESULT_OK && data != null) {
            uploading = true;
            mProgressDialog.setMessage("جاري التحميل ......");
            mProgressDialog.show();
            filePath = data.getData();
            storageReference = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
            storageReference.putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Picasso.get().load(filePath.toString()).fit().centerInside().into(postImage);

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mProgressDialog.dismiss();
                            downloadUrl = uri.toString();
                        }
                    });


                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}