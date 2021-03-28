package hashed.app.ampassadors.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Presentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.LocationRequester;
import hashed.app.ampassadors.Utils.EmojiUtil;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    private final static int CAMERA_REQUEST_CODE = 1;
    EditText username, password, confirm_pass, email, country, city, phone, Dob;
    TextView already_account;
    Button btn_register;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference reference;
    String imageUrl;
    String userid;
    Spinner counteryCode;
    ImageView iamge;
    ProgressDialog mProgressDialog;
    StorageReference sreference;
    boolean uploading = false;
    String cameraImageFilePath;
    FirebaseStorage storage;
    Spinner spinner;
    private Uri filePath;
    private ImageView locationIv;
    DatePicker dob;

    private static final int
            REQUEST_CHECK_SETTINGS = 100,
            REQUEST_LOCATION_PERMISSION = 10;

    private LocationRequester locationRequester;

    PhoneNumberUtil phoneNumberUtil;
    List<String> spinnerArray;
    String defaultCode;
    String defaultSpinnerChoice;
    String Role = "Ambassador";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        signUp();
        backSignIn();


        new Thread(() -> {

            phoneNumberUtil = PhoneNumberUtil.getInstance();

            List<String> supportedCountryCodes =
                    new ArrayList<>(phoneNumberUtil.getSupportedRegions());

            spinnerArray = new ArrayList<>(supportedCountryCodes.size());
            spinnerArray = new ArrayList<>(supportedCountryCodes.size());


            for (String code : supportedCountryCodes) {

                spinnerArray.add(EmojiUtil.countryCodeToEmoji(code)
                        + " +" + phoneNumberUtil.getCountryCodeForRegion(code));
            }

            supportedCountryCodes = null;

            Collections.sort(spinnerArray, new Comparator<String>() {
                @Override
                public int compare(String s, String t1) {
                    return extractCode(s) - extractCode(t1);
                }

                int extractCode(String s) {
                    return Integer.parseInt(s.split("\\+")[1]);
                }
            });


            Log.d("ttt", "list size: " + spinnerArray.size());
            if (this != null) {

                final ArrayAdapter<String> ad
                        = new ArrayAdapter<>(
                        sign_up.this,
                        R.layout.spinner_item_layout,
                        spinnerArray);

                ad.setDropDownViewResource(R.layout.spinner_item_layout);

                spinner.post(() -> {

                    spinner.setAdapter(ad);

                    selectDefaultPhoneCode(Locale.getDefault().getCountry().toUpperCase());

                });
            }
        }).start();


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

    private void signUp() {

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
                int year = dob.getYear();
                int month = dob.getMonth();
                int day = dob.getDayOfMonth();

                //                List<String> persons = new ArrayList<>();
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, persons);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinner.setAdapter(adapter);
//                reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                String name = document.getString("Role");
//                                persons.add(name);
//                            }
//                            adapter.notifyDataSetChanged();
//                        }
//                    }
//                });
                if (TextUtils.isEmpty(txt_username)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_SignUp_username, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_password, Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(txt_confrim_password)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_Confrim_password, Toast.LENGTH_LONG).show();

                } else if (!TextUtils.equals(txt_password, txt_confrim_password)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_Match_password, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_email)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_Email, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_country)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_Country, Toast.LENGTH_LONG).show();

                } else if (TextUtils.isEmpty(txt_city)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_City, Toast.LENGTH_LONG).show();

                } else if (TextUtils.isEmpty(txt_phone)) {
                    Toast.makeText(sign_up.this, R.string.Error_Message_Phone, Toast.LENGTH_LONG).show();

                } else if ((!checkPhoneNumber(txt_phone,
                        spinner.getSelectedItem().toString().split("\\+")[1]))) {

                    String phoneError =
                            getResources().getString(R.string.Invalied_Number_Message)
                                    + " " + getResources().getString(R.string.Auth_number_Message);

                    Toast.makeText(sign_up.this, phoneError, Toast.LENGTH_LONG).show();

                    return;
                } else {
                    register(txt_username, txt_password, txt_email, txt_country, txt_city, txt_phone, day ,year ,month);
//          Intent intent = new Intent(sign_up.this, sign_in.class);
//          startActivity(intent);
//          finish();

                }
            }
        });

    }

    private void register(String username, String passwrod, String email,
                          String country, String city, String phone  , int day  , int year , int month) {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.create_new_account));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String bio  = "";

        final Task<AuthResult> task = auth.createUserWithEmailAndPassword(email, passwrod);
        task.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                if (authResult == null || authResult.getUser() == null) {
                    return;
                }

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("username", username);
                hashMap.put("password", passwrod);
                hashMap.put("email", email);
                hashMap.put("country", country);
                hashMap.put("city", city);
             //   hashMap.put("approvement", false);
                hashMap.put("rejected", false);
                hashMap.put("phone", phone);
                hashMap.put("userId", authResult.getUser().getUid());
                hashMap.put("imageUrl", imageUrl);
                hashMap.put("status", true);
                hashMap.put("Year",year);
                hashMap.put("Month'", month);
                hashMap.put("Day", day);
                hashMap.put("Role", "Ambassador");
                hashMap.put("phoneCode", spinner.getSelectedItem());
                hashMap.put("Bio",bio);
                if (locationRequester != null && locationRequester.countryCode != null &&
                        !locationRequester.countryCode.isEmpty()) {
                    hashMap.put("countryCode", locationRequester.countryCode);
                }


                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        hashMap.put("token", s);

                        reference.document(authResult.getUser().getUid()).set(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        auth.signOut();
                                        authResult.getUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                progressDialog.dismiss();

                                                Intent intent = new Intent(sign_up.this, sign_in.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();

                                                Toast.makeText(sign_up.this, R.string.Email_Verfiy, Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(sign_up.this, R.string.Email_not_Sent,
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
//                        Toast.makeText(sign_up.this, R.string.SuccessfullMessage,
//                                Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth.signOut();
    }

    private void backSignIn() {
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
        spinner = findViewById(R.id.options);
        locationIv = findViewById(R.id.locationIv);
        locationIv.setOnClickListener(this);
        spinner = findViewById(R.id.phoneSpinner);
        dob = findViewById(R.id.dateofbirth);
        //
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAMERA
        if (requestCode == 2 && resultCode == RESULT_OK) {
            mProgressDialog.setMessage(getString(R.string.Download));
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
                            Log.d("ttt", imageUrl);
                        }
                    });
                    Toast.makeText(sign_up.this, R.string.Finish_message, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            /// GALLERY
            uploading = true;
            mProgressDialog.setMessage(getString(R.string.Download));
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
                            Picasso.get().load(imageUrl).fit().into(iamge);
                            Log.d("ttt", imageUrl);
                            Toast.makeText(sign_up.this, imageUrl, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Toast.makeText(sign_up.this, R.string.Finish_message, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode != Activity.RESULT_CANCELED) {
                locationRequester.getLastKnownLocation();
            }
        }

    }

    private void SelectImage(Context context) {
        //  CHOOSE WHERE WILL UPLOAD THE IMAGE
        final CharSequence[] options = {getString(R.string.CaptuerPhoto), getString(R.string.OpenGallray), getString(R.string.cancel)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.Title_AlretDialoge));
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take photo")) {
                    if (ActivityCompat.checkSelfPermission(sign_up.this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        Intent pikPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pikPhoto, 1);

                    }else {
                        ActivityCompat.requestPermissions(sign_up.this,new String[]{Manifest.permission.CAMERA},2);

                    }
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
                    if (ActivityCompat.checkSelfPermission(sign_up.this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                        Intent pikPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pikPhoto, 2);

                    }else {
                        ActivityCompat.requestPermissions(sign_up.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);

                    }
                } else if (options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });

        builder.show();
    }

    private File createImageFile() throws IOException {
        // CREATE URL FOR CAMERA IMAGE
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());

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

    @Override
    public void onClick(View view) {

        if (view.getId() == locationIv.getId()) {

            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, REQUEST_LOCATION_PERMISSION);
            } else {
                intilizeLocationRequester();
                locationIv.setClickable(false);
            }

        }

    }

    void intilizeLocationRequester() {
        locationRequester = new LocationRequester(this, this,
                country, city, locationIv);

        locationRequester.geCountryFromLocation();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intilizeLocationRequester();
                locationIv.setClickable(false);
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (locationRequester != null) {
            locationRequester.resumeLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationRequester != null) {
            locationRequester.stopLocationUpdates();
        }
    }


    boolean checkPhoneNumber(String number, String code) {

        final Phonenumber.PhoneNumber newNum = new Phonenumber.PhoneNumber();

        newNum.setCountryCode(Integer.parseInt(code)).setNationalNumber(Long.parseLong(number));

        return phoneNumberUtil.isValidNumber(newNum);
    }

    public void selectDefaultPhoneCode(String countryCode) {
        final String defaultSpinnerChoice = EmojiUtil.countryCodeToEmoji(countryCode)
                + " +" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryCode);

        spinner.setSelection(spinnerArray.indexOf(defaultSpinnerChoice));
    }


}
