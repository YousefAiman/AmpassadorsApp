package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FileDownloadUtil;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.FullScreenImagesUtil;

public class Profile extends AppCompatActivity {

    public static final int EDIT_CODE = 2;
    TextView username, email, country, city, phone, bio;
    Button edit_profile;
    ImageView imageView;
    private FrameLayout frameLayout;
    private BroadcastReceiver downloadCompleteReceiver;
    private FileDownloadUtil fileDownloadUtil;
    private ListenerRegistration listenerRegistration;
    String userid = FirebaseAuth.getInstance().getUid();
    UserInfo userInfo ;
    CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        frameLayout = findViewById(R.id.frameLayout);
        edit_profile = findViewById(R.id.edit_data);
        username = findViewById(R.id.in_username);
        email = findViewById(R.id.in_email);
        country = findViewById(R.id.in_country);
        city = findViewById(R.id.in_city);
        phone = findViewById(R.id.in_phone);
        imageView = findViewById(R.id.profile_picture);
        bio = findViewById(R.id.bio_text);
        userInfo = new UserInfo();
        userInfo.setUserId(userid);

        collectionReference.document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {


                if(documentSnapshot.contains("imageUrl") && documentSnapshot.getString("imageUrl")!=null){

                    final String image = documentSnapshot.getString("imageUrl");

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            edit_profile.setVisibility(View.GONE);
                            toolbar.setVisibility(View.GONE);
                            frameLayout.setVisibility(View.VISIBLE);

                            FullScreenImagesUtil.showImageFullScreen(Profile.this,
                                    image,null,null)
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {

                                    edit_profile.setVisibility(View.VISIBLE);
                                    toolbar.setVisibility(View.VISIBLE);
                                    frameLayout.setVisibility(View.GONE);

                                }
                            });

                        }
                    });

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Profile.this, "Empty image", Toast.LENGTH_SHORT).show();
            }
        });




        final UserInfo[] userInfo = new UserInfo[1];
        listenerRegistration =  FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        Log.d("ttt","value change");

                        if(userInfo[0] == null){

                            userInfo[0] = value.toObject(UserInfo.class);
                            username.setText(userInfo[0].getUsername());
                            email.setText(userInfo[0].getEmail());
                            country.setText(userInfo[0].getCountry());
                            city.setText(userInfo[0].getCity());

                            phone.setText(userInfo[0].getPhone());
                            Picasso.get().load(userInfo[0].getImageUrl()).fit().into(imageView);
                            bio.setText(userInfo[0].getBio());
                            Log.d("tttt",userInfo[0].getBio() + "bio;");

                        }else{

                            username.setText(value.getString("username"));
                            country.setText(value.getString("country"));
                            city.setText(value.getString("city"));
                            phone.setText(value.getString("phone"));
                            bio.setText(value.getString("Bio"));

                            if(value.contains("imageUrl")){
                                Picasso.get().load(value.getString("imageUrl")).
                                        fit().into(imageView);
                            }

                        }

                    }
                });
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, profile_edit.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,EDIT_CODE);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }
    }
    private void setUpDownloadReceiver() {

        downloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (id != -1) {
                    imageView.setImageResource(R.drawable.download_icon);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fileDownloadUtil.showDownloadAlert();
                        }
                    });
                }

            }
        };

        registerReceiver(downloadCompleteReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }
}