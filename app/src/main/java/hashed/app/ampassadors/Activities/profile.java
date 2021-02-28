package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.AdminAdapter;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class profile extends AppCompatActivity {
    TextView username, password, email, country, city, phone;
    Button edit_profile;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userid ;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        goToEditprofile();
        drawer();



     fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
               if (task.isSuccessful()){
                   if (task.getResult().exists()){
                       String user_name = task.getResult().getString("usernam");
                       username.setText(user_name);
                   }
               }else {
                   Toast.makeText(profile.this, "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
               }
           }
       });


//        DocumentReference documentReference = fStore.collection("users").document(userid);
//
//        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
//                if (documentSnapshot.exists()) {
//                    username.setText(documentSnapshot.getString("usernam"));
//                    phone.setText(documentSnapshot.getString("phone"));
//                    password.setText(documentSnapshot.getString("password"));
//                    email.setText(documentSnapshot.getString("email"));
//                    country.setText(documentSnapshot.getString("country"));
//                    city.setText(documentSnapshot.getString("city"));
//                }else {
//                    Log.d("ttt", "On Event do not exists");
//                }
//            }
//        });
    }

    private void init(){
        edit_profile = findViewById(R.id.edit_data);
        username = findViewById(R.id.in_username);
        password = findViewById(R.id.in_password);
        email = findViewById(R.id.in_email);
        country = findViewById(R.id.in_country);
        city = findViewById(R.id.in_city);
        phone = findViewById(R.id.in_phone);
//
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();

        fStore = FirebaseFirestore.getInstance();
    }

    private void goToEditprofile(){
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(profile.this, profile_edit.class);
                startActivity(intent);

            }
        });
    }

    private void drawer(){
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.image_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }
}