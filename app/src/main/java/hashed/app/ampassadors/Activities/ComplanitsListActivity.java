package hashed.app.ampassadors.Activities;

import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

import hashed.app.ampassadors.Adapters.ComplaintsAdapter;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Objects.Complaints;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class ComplanitsListActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener{
  private static final int COMPLAINT_LIMIT = 15;
  RecyclerView recyclerView;
  Task<QuerySnapshot> task;
  FirebaseFirestore firebaseFirestore;
  ComplaintsAdapter adapter;
  List<Complaints> complaints;
  Query query;
  CollectionReference collectionReference;
  ScrollListener scrollListener;
  DocumentSnapshot lastDocSnap;
  boolean isLoading;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_complanits_list);
    setUpComponte();
    RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(manager);
    adapter = new ComplaintsAdapter(complaints, ComplanitsListActivity.this);
    recyclerView.setAdapter(adapter);
    showSuggestions(true);
    setUpToolBarAndActions();

  }


  public void setUpComponte() {
    recyclerView = findViewById(R.id.list_com);
    firebaseFirestore = FirebaseFirestore.getInstance();
    complaints = new ArrayList<>();
    task = firebaseFirestore.collection("Complaints").get();
    collectionReference = firebaseFirestore.collection("Complaints");
    query = collectionReference.whereEqualTo("reviewed", false).limit(COMPLAINT_LIMIT);

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
          complaints.addAll(queryDocumentSnapshots.toObjects(Complaints.class));
        }else{
          complaints.addAll(complaints.size(),queryDocumentSnapshots.toObjects(Complaints.class));
        }
      }
    }).addOnCompleteListener(task -> {
      if (isInitial) {
        adapter.notifyDataSetChanged();

        if (task.getResult().size() == COMPLAINT_LIMIT && scrollListener == null) {
          recyclerView.addOnScrollListener(scrollListener = new ScrollListener());
        }

        collectionReference.whereEqualTo("reviewed", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
            if(value!=null){
              for(DocumentChange dc:value.getDocumentChanges()){
                if(dc.getType() == DocumentChange.Type.REMOVED){
                  for(Complaints complaint:complaints){
                    if(complaint.getComplaintsId().equals(dc.getDocument().getId())){
                      final int index = complaints.indexOf(complaint);
                      complaints.remove(index);
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

        adapter.notifyItemRangeInserted(complaints.size() - resultSize,resultSize);
        if (resultSize < COMPLAINT_LIMIT && scrollListener != null) {
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