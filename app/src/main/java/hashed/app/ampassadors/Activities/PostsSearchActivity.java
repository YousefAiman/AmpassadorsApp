package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Adapters.UsersPickerAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class PostsSearchActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener {

  private SearchView searchView;
  private ArrayList<PostData> posts;
  private RecyclerView postRv;
  private PostAdapter postAdapter;
  private CollectionReference postsRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_search);

    setUpToolbar();

    getViews();

    initializeObjects();

  }

  private void setUpToolbar(){

    final Toolbar pickUserToolbar = findViewById(R.id.pickUserToolbar);
    pickUserToolbar.setNavigationOnClickListener(v->finish());

  }

  private void getViews(){

    postRv = findViewById(R.id.userRv);
    searchView = findViewById(R.id.searchUserSearchView);

    searchView.setOnClickListener(v -> searchView.onActionViewCollapsed());
    searchView.onActionViewExpanded();
    searchView.setOnQueryTextListener(this);


  }

  private void initializeObjects(){

    postsRef = FirebaseFirestore.getInstance().collection("Posts");

    posts = new ArrayList<>();
    postAdapter = new PostAdapter(posts,this);
    postRv.setAdapter(postAdapter);

  }

  @Override
  public boolean onQueryTextSubmit(String query) {

    searchView.clearFocus();
    searchForPost(query);

    return false;
  }

  private void searchForPost(String query) {

    Query searchQuery;

    searchQuery = postsRef
            .orderBy("publishTime", Query.Direction.DESCENDING);

    final String[] splitArr = query.split(" ");

    if(splitArr.length == 0){

      searchQuery = postsRef.whereArrayContains("keyWords", query);

    }else if(splitArr.length <= 10){

      searchQuery = searchQuery.whereArrayContainsAny("keyWords",
              Arrays.asList(splitArr));

    }else {

      searchQuery = searchQuery.whereArrayContainsAny("keyWords",
              Arrays.asList(splitArr).subList(0,10));

    }

    final AtomicInteger itemsAdded = new AtomicInteger();
    searchQuery.get().addOnSuccessListener(snapshots -> {

      if (!snapshots.isEmpty()) {
        posts.clear();
        posts.addAll(snapshots.toObjects(PostData.class));
      }

    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if(task.isSuccessful() && !posts.isEmpty()){
          postAdapter.notifyDataSetChanged();
        }
      }
    });
  }

  @Override
  public boolean onQueryTextChange(String newText) {
//    pickerAdapter.getFilter().filter(newText);
    return true;
  }



}