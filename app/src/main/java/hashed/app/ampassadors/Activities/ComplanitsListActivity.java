package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.ComplaintsAdapter;
import hashed.app.ampassadors.Adapters.SuggestionsAdapter;
import hashed.app.ampassadors.Objects.Complaints;
import hashed.app.ampassadors.Objects.Suggestions;
import hashed.app.ampassadors.R;

public class ComplanitsListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Task<QuerySnapshot> task;
    FirebaseFirestore firebaseFirestore;
    ComplaintsAdapter adapter;
    List<Complaints> complaints;
    Query   query ;
    CollectionReference collectionReference ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complanits_list);
        setUpComponte();
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        adapter = new ComplaintsAdapter(complaints,ComplanitsListActivity.this);
        recyclerView.setAdapter(adapter);
        showSuggestions(true);
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 3){
//            String id = data.getStringExtra("Deleted");
//            for (int i = 0 ; i<complaints.size();i++){
//                if (id.equals(i)){
//                   adapter.notifyItemRemoved(i);
//                    break;
//                }
//            }
//        }
//
//    }

    public void setUpComponte() {
        recyclerView = findViewById(R.id.list_com);
        firebaseFirestore = FirebaseFirestore.getInstance();
        complaints = new ArrayList<>();
        task = firebaseFirestore.collection("Complaints").get();
        collectionReference = firebaseFirestore.collection("Complaints");
        query = collectionReference.whereEqualTo("reviewed",false);

    }
    public void showSuggestions(boolean isInitial) {
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    for (QueryDocumentSnapshot documentSnapshot:task.getResult()){
                        Complaints  complaint= documentSnapshot.toObject(Complaints.class);
                        complaints.add(complaint);
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Error"+ e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

}