package hashed.app.ampassadors.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class sign_up extends AppCompatActivity {

    EditText username, password, confirm_pass, email, country, city, phone;
    TextView  already_account;
    Button btn_register;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference reference;
    UserInfo userInfo;

    String imageUrl;
    String userid ;
    ImageView iamge;
    ProgressDialog mProgressDialog;
    StorageReference sreference;
    private final static int CAMERA_REQUEST_CODE = 1;
    boolean uploading = false;
    private Uri filePath;
    String cameraImageFilePath;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        init();
        signUp();
        backSignIn();

        iamge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ActivityCompat.requestPermissions(sign_up.this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                SelectImage(sign_up.this);

            }
        });
    }

    private void signUp(){

        reference = firebaseFirestore.collection("Users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = username.getText().toString();
                String txt_password = password.getText().toString();
                String txt_confrim_password = confirm_pass.getText().toString();
                String txt_email = email.getText().toString();
                String txt_country = country.getText().toString();
                String txt_city = city.getText().toString();
                String txt_phone = phone.getText().toString();

                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_password)||TextUtils.isEmpty(txt_email)
                        || TextUtils.isEmpty(txt_country) || TextUtils.isEmpty(txt_city ) || TextUtils.isEmpty(txt_phone)) {
                    Toast.makeText(sign_up.this, "All field are required", Toast.LENGTH_SHORT).show();
                }else if (!txt_password.equals(txt_confrim_password)) {
                    Toast.makeText(sign_up.this, "Password must match and confirm password", Toast.LENGTH_LONG).show();
                }else {
                    register(txt_username, txt_password, txt_email,
                            txt_country, txt_city,txt_phone);
                    Intent intent = new Intent(sign_up.this, sign_in.class);
                    startActivity(intent);
                    finish();

                }
            }
        });

    }

    private void register(String username,  String passwrod ,String email, String country,  String city, String phone) {
        final Task<AuthResult> task = auth.createUserWithEmailAndPassword(email , passwrod);
        task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                userInfo = new UserInfo();
               // Picasso.get().load(userInfo.getImageUrl()).fit().into(circleImageView);

                FirebaseUser firebaseUser = auth.getCurrentUser();

                userInfo.setUsername(username);
                userInfo.setPassword(passwrod);
                userInfo.setEmail(firebaseUser.getEmail());
                userInfo.setCountry(country);
                userInfo.setCity(city);
                userInfo.setPhone(phone);
                userInfo.setUserid(firebaseUser.getUid());
                userInfo.setImageUrl(imageUrl);


               reference.document(firebaseUser.getUid()).set(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()){
                           Toast.makeText(sign_up.this, "Added successfully", Toast.LENGTH_LONG).show();

                       }
                   }
               })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void backSignIn(){
        already_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sign_up.this, sign_in.class);
                startActivity(intent);

            }
        });
    }

    public void init() {
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        already_account = findViewById(R.id.back_sign_in);
        username = findViewById(R.id.input_username);
        password = findViewById(R.id.imput_password);
        confirm_pass = findViewById(R.id.imput_repassword);
        email = findViewById(R.id.input_email);
        country = findViewById(R.id.input_country);
        city = findViewById(R.id.input_city);
        phone = findViewById(R.id.input_phone);
        btn_register = findViewById(R.id.sign_up_btn);
        iamge = findViewById(R.id.profile_picture);
        storage = FirebaseStorage.getInstance();
        sreference = storage.getReference();
        mProgressDialog = new ProgressDialog(this);
        userid = auth.getUid();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAMERA
        if (requestCode == 2 && resultCode == RESULT_OK) {
            mProgressDialog.setMessage("جاري التحميل...");
            mProgressDialog.show();
            Uri uri = data.getData();
            StorageReference filepath = sreference.child("Profile img").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();


                    //   Uri download = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();

                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                             imageUrl = uri.toString();
                            Picasso.get().load(imageUrl).fit().into(iamge);
                            Log.d("ttt",imageUrl);
                        }
                    });
                    Toast.makeText(sign_up.this, "انتهى التحميل...", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (requestCode == CAMERA_REQUEST_CODE) {
            /// GALLERY
            uploading = true;
            mProgressDialog.setMessage("جاري التحميل ......");
            mProgressDialog.show();
            filePath = Uri.parse("file://"+cameraImageFilePath);
            sreference = FirebaseStorage.getInstance().getReference().child("Profile img/" + UUID.randomUUID().toString());
            sreference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();
                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageUrl = uri.toString();
                            Picasso.get().load(imageUrl).fit().into(iamge);
                            Log.d("ttt",imageUrl);
                            Toast.makeText(sign_up.this, imageUrl, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(sign_up.this, "انتهى التحميل...", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void SelectImage(Context context) {
        //  CHOOSE WHERE WILL UPLOAD THE IMAGE
        final CharSequence[] options = {"Take photo", "open Gallery", "Cancel"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose you profile photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take photo")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {

                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(sign_up.this,
                                    "hashed.app.ampassadors.provider", photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                        }
                    }
                } else if (options[i].equals("open Gallery")) {
                    Intent pikPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pikPhoto, 2);

                } else if (options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });

        builder.show();
    }
    private File createImageFile() throws IOException {
        // CREATE URL FOR CAMERA IMAGE
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        );

        // Save a file: path for use with ACTION_VIEW intents
        cameraImageFilePath = image.getAbsolutePath();
        return image;
    }
}