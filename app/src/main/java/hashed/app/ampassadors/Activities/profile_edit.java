package hashed.app.ampassadors.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class profile_edit extends AppCompatActivity {

  private final static int CAMERA_REQUEST_CODE = 1;
  EditText username, email, country, city, phone;
  Button save;
  FirebaseAuth fAuth;
  FirebaseFirestore fStore;
  String userid;
  ImageView imageView;
  CollectionReference reference;
  UserInfo userInfo;
  ProgressDialog mProgressDialog;
  String imageUrl;
  StorageReference sreference;
  boolean uploading = false;
  String cameraImageFilePath;
  FirebaseStorage storage;
  private Uri filePath;
  private Uri imageUri;
  private ImageView updateImageIV;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile_edit);
  //  updatedata();
    init();
// toolbar
    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

    updateImageIV = findViewById(R.id.updateImageIV);


    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ActivityCompat.requestPermissions(profile_edit.this,
                new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        SelectImage(profile_edit.this);

      }
    });


    final String[] usernameString = new String[1];
    final String[] countryString = new String[1];
    final String[] cityString = new String[1];
    final String[] phoneString = new String[1];
    final String[] imageUrl = new String[1];

    fStore.collection("Users").document(userid).get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot snapshot) {

                if(snapshot.exists()){

                  usernameString[0] = snapshot.getString("username");
                  countryString[0] = snapshot.getString("country");
                  cityString[0] = snapshot.getString("city");
                  phoneString[0] = snapshot.getString("phone");
                  imageUrl[0] = snapshot.getString("imageUrl");

                }
              }
            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful()){
          username.setText(usernameString[0]);
          country.setText(countryString[0]);
          city.setText(cityString[0]);
          phone.setText(phoneString[0]);

          if(imageUrl[0] !=null && !imageUrl[0].isEmpty()) {
            Picasso.get().load(imageUrl[0]).fit().into(imageView);
          }
        }
      }
    });

    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        updatedata();
      }
    });

    updateImageIV.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        SelectImage(profile_edit.this);
      }
    });


  }

  //    private void drawer(){
//        final DrawerLayout drawerLayout_b = findViewById(R.id.drawer_layout_b);
//        findViewById(R.id.image_menu_b).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                drawerLayout_b.openDrawer(GravityCompat.START);
//            }
//        });
//    }
  private void init() {
    username = findViewById(R.id.input_username);
    email = findViewById(R.id.input_email);
    country = findViewById(R.id.input_country);
    city = findViewById(R.id.input_city);
    phone = findViewById(R.id.input_phone);
    save = findViewById(R.id.save);
//
    imageView = findViewById(R.id.profile_picture);

    fAuth = FirebaseAuth.getInstance();
    userid = fAuth.getCurrentUser().getUid();

    fStore = FirebaseFirestore.getInstance();


    storage = FirebaseStorage.getInstance();
    sreference = storage.getReference();
    mProgressDialog = new ProgressDialog(this);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // CAMERA
    if (requestCode == 2 && resultCode == RESULT_OK) {
//      mProgressDialog.setMessage(getString(R.string.Download));
//      mProgressDialog.show();
      Uri uri = data.getData();
      imageUri = uri;

      Picasso.get().load(imageUri).fit().into(imageView);

//      StorageReference filepath = sreference.child("Profile img").child(uri.getLastPathSegment());
//      filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//        @Override
//        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//          mProgressDialog.dismiss();
//
//          //   Uri download = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();
//          Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
//          result.addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//              imageUrl = uri.toString();
//              Picasso.get().load(imageUrl).fit().into(imageView);
//              Log.d("ttt", imageUrl);
//            }
//          });
//          Toast.makeText(profile_edit.this, R.string.Finish_message, Toast.LENGTH_SHORT).show();
//        }
//      });

    } else if (requestCode == CAMERA_REQUEST_CODE) {
      /// GALLERY
      uploading = true;
      mProgressDialog.setMessage(getString(R.string.Download));
      mProgressDialog.show();
      filePath = Uri.parse("file://" + cameraImageFilePath);

      imageUri = filePath;


      Picasso.get().load(imageUri).fit().into(imageView);
//      sreference = FirebaseStorage.getInstance().getReference().child("Profile img/" + UUID.randomUUID().toString());
//      sreference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//        @Override
//        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//          mProgressDialog.dismiss();
//          Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
//          result.addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//              String imageUrl = uri.toString();
//              Picasso.get().load(imageUrl).fit().into(imageView);
//              Log.d("ttt", imageUrl);
//              Toast.makeText(profile_edit.this, imageUrl, Toast.LENGTH_SHORT).show();
//            }
//          });
//          Toast.makeText(profile_edit.this, R.string.Finish_message, Toast.LENGTH_SHORT).show();
//        }
//      }).addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//          Toast.makeText(profile_edit.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//      });
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case CAMERA_REQUEST_CODE:
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
          try {
            createImageFile();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
          break;
    }
  }

  private void SelectImage(Context context) {
    //  CHOOSE WHERE WILL UPLOAD THE IMAGE
    final CharSequence[] options = {getString(R.string.CaptuerPhoto),
            getString(R.string.OpenGallray), getString(R.string.Cansle)};
    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(getString(R.string.Title_AlretDialoge));
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
              Uri photoURI = FileProvider.getUriForFile(profile_edit.this,
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

  private void updatedata() {

        String txt_username = username.getText().toString();
        String txt_country = country.getText().toString();
        String txt_city = city.getText().toString();
        String txt_phone = phone.getText().toString();

        if (TextUtils.isEmpty(txt_username)
                || TextUtils.isEmpty(txt_country) || TextUtils.isEmpty(txt_city) || TextUtils.isEmpty(txt_phone)) {
          Toast.makeText(profile_edit.this, "All field are required", Toast.LENGTH_SHORT).show();
        } else {

          ProgressDialog progressDialog = new ProgressDialog(this);
          progressDialog.setMessage("Updating info");
          progressDialog.setCancelable(false);
          progressDialog.show();
          save.setClickable(false);

          if(imageUri!=null) {

            StorageReference filepath = sreference.child("Profile img")
                    .child(imageUri.getLastPathSegment());

            filepath.putFile(imageUri).
                    addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                  public void onSuccess(Uri uri) {
                    imageUrl = uri.toString();
                    updateData(txt_username,txt_country,txt_city,txt_phone,progressDialog);
                  }
                });
              }
            });

          }else {
            updateData(txt_username,txt_country,txt_city,txt_phone,progressDialog);
          }

        }
      }


      private void updateData(String txt_username,String txt_country,
                              String txt_city,String txt_phone,ProgressDialog progressDialog){


    final DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users")
            .document(fAuth.getCurrentUser().getUid());

        userRef.update("username",txt_username,
                        "country",txt_country,
                        "city",txt_city,
                        "phone",txt_phone)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {

                    if(imageUrl!=null && !imageUrl.isEmpty()){

                      userRef.update("imageUrl",imageUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          progressDialog.dismiss();
                          save.setClickable(true);
                        }
                      });
                    }else{
                      progressDialog.dismiss();
                      save.setClickable(true);
                    }
                  }
                }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Toast.makeText(profile_edit.this, "Info update error"
                    , Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            save.setClickable(true);
          }
        });

      }
}