package hashed.app.ampassadors.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.SuggestionsAdapter;
import hashed.app.ampassadors.Objects.Suggestions;
import hashed.app.ampassadors.R;

public class List_Sug_Activity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener{
  RecyclerView recyclerView;
  Task<QuerySnapshot> task;
  FirebaseFirestore firebaseFirestore;
  SuggestionsAdapter adapter;
  List<Suggestions> suggestions;
  Query query;
  CollectionReference collectionReference;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list__sug__com);
    setUpComponte();
    RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(manager);
    adapter = new SuggestionsAdapter(List_Sug_Activity.this, suggestions);
    recyclerView.setAdapter(adapter);
    showSuggestions(true);
    setUpToolBarAndActions();
  }

  public void setUpComponte() {
    recyclerView = findViewById(R.id.list_com_sug);
    firebaseFirestore = FirebaseFirestore.getInstance();
    suggestions = new ArrayList<>();
    collectionReference = firebaseFirestore.collection("Suggestions");
    query = collectionReference.whereEqualTo("reviewed", false);
    task = firebaseFirestore.collection("Suggestions").get();

  }

  public void showSuggestions(boolean isInitial) {

    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
          for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
            Suggestions suggestion = documentSnapshot.toObject(Suggestions.class);
            suggestions.add(suggestion);
          }
          adapter.notifyDataSetChanged();
        }
      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Toast.makeText(List_Sug_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    });
  }
  private void setUpToolBarAndActions() {

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
    toolbar.setOnMenuItemClickListener(this);


  }
  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }
}