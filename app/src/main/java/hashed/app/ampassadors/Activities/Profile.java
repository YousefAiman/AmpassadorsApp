package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class Profile extends AppCompatActivity {

    public static final int EDIT_CODE = 2;
    TextView username, password, email, country, city, phone;
    Button edit_profile;
    ImageView imageView;
    private ListenerRegistration listenerRegistration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        edit_profile = findViewById(R.id.edit_data);
        username = findViewById(R.id.in_username);
        password = findViewById(R.id.in_password);
        email = findViewById(R.id.in_email);
        country = findViewById(R.id.in_country);
        city = findViewById(R.id.in_city);
        phone = findViewById(R.id.in_phone);
        imageView = findViewById(R.id.profile_picture);

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
                            password.setText(userInfo[0].getPassword());
                            email.setText(userInfo[0].getEmail());
                            country.setText(userInfo[0].getCountry());
                            city.setText(userInfo[0].getCity());
                            phone.setText(userInfo[0].getPhone());
                            Picasso.get().load(userInfo[0].getImageUrl()).fit().into(imageView);

                        }else{

                            username.setText(value.getString("username"));
                            country.setText(value.getString("country"));
                            city.setText(value.getString("city"));
                            phone.setText(value.getString("phone"));
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

//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        Log.d("ttt","activity result");
//        if(resultCode == EDIT_CODE && data != null && data.hasExtra("editBundle")){
//            Log.d("ttt","data is not null");
//            final Bundle editBundle = data.getBundleExtra("editBundle");
//            username.setText(editBundle.getString("username"));
//            country.setText(editBundle.getString("country"));
//            city.setText(editBundle.getString("city"));
//            phone.setText(editBundle.getString("phone"));
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }
    }
}