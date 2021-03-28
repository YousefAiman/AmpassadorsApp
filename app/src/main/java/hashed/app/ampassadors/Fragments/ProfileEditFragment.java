package hashed.app.ampassadors.Fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

import hashed.app.ampassadors.Activities.sign_in;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class ProfileEditFragment extends Fragment {
  public static final int RESULT_OK = -1;
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
  EditText bio;
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);
    username = view.findViewById(R.id.input_username);
    email = view.findViewById(R.id.input_email);
    country = view.findViewById(R.id.input_country);
    city = view.findViewById(R.id.input_city);
    phone = view.findViewById(R.id.input_phone);
    save = view.findViewById(R.id.save);
    bio = view.findViewById(R.id.bio);
//
    imageView = view.findViewById(R.id.profile_picture);

    fAuth = FirebaseAuth.getInstance();
    userid = fAuth.getCurrentUser().getUid();

    fStore = FirebaseFirestore.getInstance();


    storage = FirebaseStorage.getInstance();
    sreference = storage.getReference();
    mProgressDialog = new ProgressDialog(getActivity());


    imageView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        SelectImage(getActivity());

      }
    });


    fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
          if (task.getResult().exists()) {
            String user_name = task.getResult().getString("username");
            String coun = task.getResult().getString("country");
            String cit = task.getResult().getString("city");
            String pho = task.getResult().getString("phone");
            String biotext = task.getResult().getString("Bio");

            username.setText(user_name);
            country.setText(coun);
            city.setText(cit);
            phone.setText(pho);
            bio.setText(biotext);

          }
        } else {
          Toast.makeText(getActivity(), "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
        }
      }
    });


    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
              Picasso.get().load(imageUrl).fit().into(imageView);
              Log.d("ttt", imageUrl);
            }
          });
          Toast.makeText(getActivity(), "انتهى التحميل...", Toast.LENGTH_SHORT).show();
        }
      });
    } else if (requestCode == CAMERA_REQUEST_CODE) {
      /// GALLERY
      uploading = true;
      mProgressDialog.setMessage("جاري التحميل ......");
      mProgressDialog.show();
      filePath = Uri.parse("file://" + cameraImageFilePath);
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
              Picasso.get().load(imageUrl).fit().into(imageView);
              Log.d("ttt", imageUrl);
              Toast.makeText(getActivity(), imageUrl, Toast.LENGTH_SHORT).show();
            }
          });
          Toast.makeText(getActivity(), "انتهى التحميل...", Toast.LENGTH_SHORT).show();
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
              Uri photoURI = FileProvider.getUriForFile(getActivity(),
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


  private void register(String username, String email, String country, String city, String phone) {

    userInfo = new UserInfo();
    // Picasso.get().load(userInfo.getImageUrl()).fit().into(circleImageView);

    FirebaseUser firebaseUser = fAuth.getCurrentUser();

    userInfo.setUsername(username);
    userInfo.setEmail(firebaseUser.getEmail());
    userInfo.setCountry(country);
    userInfo.setCity(city);
    userInfo.setPhone(phone);
    userInfo.setUserId(firebaseUser.getUid());
    userInfo.setStatus(true);


  }


  private void updatedata() {

    reference = fStore.collection("Users");

    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String txt_username = username.getText().toString();
        String txt_email = email.getText().toString();
        String txt_country = country.getText().toString();
        String txt_city = city.getText().toString();
        String txt_phone = phone.getText().toString();


        if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email)
                || TextUtils.isEmpty(txt_country) || TextUtils.isEmpty(txt_city) || TextUtils.isEmpty(txt_phone)) {
          Toast.makeText(getActivity(), "All field are required", Toast.LENGTH_SHORT).show();
        } else {
          register(txt_username, txt_email,
                  txt_country, txt_city, txt_phone);
          Intent intent = new Intent(getActivity(), sign_in.class);
          startActivity(intent);

        }
      }
    });

  }

  public PackageManager getPackageManager() {
    throw new RuntimeException("Stub!");
  }

  public File getExternalFilesDir(String type) {
    throw new RuntimeException("Stub!");
  }

}