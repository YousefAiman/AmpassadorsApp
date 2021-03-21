package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class Profile extends AppCompatActivity {
    TextView username, password, email, country, city, phone;
    Button edit_profile;
    ImageView imageView;


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

        FirebaseFirestore.getInstance().collection("Users").document(
                FirebaseAuth.getInstance().getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if(documentSnapshot.exists()){
                            userInfo[0] = documentSnapshot.toObject(UserInfo.class);
                        }

                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    username.setText(userInfo[0].getUsername());
                    password.setText(userInfo[0].getPassword());
                    email.setText(userInfo[0].getEmail());
                    country.setText(userInfo[0].getCountry());
                    city.setText(userInfo[0].getCity());
                    phone.setText(userInfo[0].getPhone());
                    Picasso.get().load(userInfo[0].getImageUrl()).fit().into(imageView);
                }else {
                    Toast.makeText(Profile.this, "Error"+task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, profile_edit.class);
                startActivity(intent);
            }
        });


    }
}