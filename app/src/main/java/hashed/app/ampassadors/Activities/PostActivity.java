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

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

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
        reference = firebaseFirestore.collection("Post");
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
                if (postTexting.isEmpty()) {
                    Toast.makeText(PostActivity.this, "pales write your post", Toast.LENGTH_SHORT).show();
                } else {
                    PostData data = new PostData();
                    data.setDescription(postTexting);
                    data.setPostImage(filePath.toString());
                    data.setUsername("Ahmed");
                    data.setUesrid("2");
                    Task<DocumentReference> task = reference.add(data);
                    task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(PostActivity.this, "Posting now", Toast.LENGTH_SHORT).show();
                        }
                    });

                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, e.getMessage() + "", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        poll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(PostActivity.this, "poll", Toast.LENGTH_SHORT).show();
            }
        });
        image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(PostActivity.this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

                Intent pikPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pikPhoto, 2);

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
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploading = true;
            mProgressDialog.setMessage("جاري التحميل ......");
            mProgressDialog.show();
            filePath = data.getData();
            storageReference = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
            storageReference.putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        mProgressDialog.dismiss();
                        Picasso.get().load(filePath.toString()).fit().centerInside().into(postImage);



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