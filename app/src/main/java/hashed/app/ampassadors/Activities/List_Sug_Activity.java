package hashed.app.ampassadors.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.SuggestionsAdapter;
import hashed.app.ampassadors.Fragments.MeetingsFragment;
import hashed.app.ampassadors.Objects.Complaints;
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
  private static final int SUGGESTS_LIMIT = 15;
  boolean isLoading;
  DocumentSnapshot lastDocSnap;
  ScrollListener scrollListener;
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

  private void showSuggestions(boolean isInitial) {

    isLoading = true;

    Query updatedQuery = query;
    if (lastDocSnap != null) {
      updatedQuery = query.startAfter(lastDocSnap);
    }

    updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
      if (!queryDocumentSnapshots.isEmpty()) {

          lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                  queryDocumentSnapshots.size() - 1
        );

        if(isInitial){
          suggestions.addAll(queryDocumentSnapshots.toObjects(Suggestions.class));
        }else{
          suggestions.addAll(suggestions.size(),queryDocumentSnapshots.toObjects(Suggestions.class));
        }
      }
    }).addOnCompleteListener(task -> {
      if (isInitial) {
        adapter.notifyDataSetChanged();

        if (task.getResult().size() == SUGGESTS_LIMIT && scrollListener == null) {
          recyclerView.addOnScrollListener(scrollListener = new ScrollListener());
        }

        collectionReference.whereEqualTo("reviewed", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                  @Override
                  public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if(value!=null){
                      for(DocumentChange dc:value.getDocumentChanges()){
                        if(dc.getType() == DocumentChange.Type.REMOVED){
                          for(Suggestions suggestion:suggestions){
                            if(suggestion.getSuggestionId().equals(dc.getDocument().getId())){
                              final int index = suggestions.indexOf(suggestion);
                              suggestions.remove(index);
                              adapter.notifyItemRemoved(index);
                              break;
                            }
                          }
                        }
                      }
                    }
                  }
                });

      } else {

        final int resultSize = task.getResult().size();

        adapter.notifyItemRangeInserted(suggestions.size() - resultSize,resultSize);
        if (resultSize < SUGGESTS_LIMIT && scrollListener != null) {
          recyclerView.removeOnScrollListener(scrollListener);
        }
      }


      isLoading = false;
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
  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoading && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {
        showSuggestions(false);
      }
    }
  }

}