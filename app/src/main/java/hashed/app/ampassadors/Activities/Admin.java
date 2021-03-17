package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
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
    Button delete, approve;
    UserInfo info ;
    String userid;
    boolean approvment;
    FirebaseAuth fAuth;
    Spinner spinner;

    CollectionReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        delete = findViewById(R.id.delete_account);
        approve = findViewById(R.id.approve_account);

        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();
        spinner = findViewById(R.id.options);


        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> persons = new ArrayList<>();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, persons);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = document.getString("Role");
                                persons.add(name);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                DocumentReference reference = firebaseFirestore.collection("Users").document(userid);

                    if (approvment){
                            reference.update("approvement", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    approvment = true;
                                }
                            });
                    }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference reference = firebaseFirestore.collection("Users").document(userid);

                if (approvment){
                    reference.update("approvement", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            approvment = false;
                        }
                    });
                }
            }
        });


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

        Spinner spinner = (Spinner) findViewById(R.id.options);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.persons_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }
}