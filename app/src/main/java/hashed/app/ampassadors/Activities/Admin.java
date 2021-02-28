package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.AdminAdapter;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class Admin extends AppCompatActivity {
    FirebaseFirestore firebaseFirestore ;
    RecyclerView list_users;
    List<UserInfo> data;
    AdminAdapter adapter ;
    Task<QuerySnapshot> task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        firebaseFirestore = FirebaseFirestore.getInstance();
        list_users = findViewById(R.id.users_list);
        list_users.setHasFixedSize(true);
        list_users.setLayoutManager(new LinearLayoutManager(this));
        data = new ArrayList<>();

        task =firebaseFirestore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (DocumentSnapshot qs : task.getResult().getDocuments()){
                        String email = qs.getString("email");
                        String pass = qs.getString("password");


                        UserInfo info = new UserInfo();
                        info.setEmail("Email : "+email);
                        info.setPassword("Password : "+pass);

                        data.add(info);
                    }
                }
                adapter = new AdminAdapter(Admin.this, data);
                list_users.setAdapter(adapter);
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Admin.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}