package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Fragments.AddCourseFragment;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UserMessageSearchActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener, UsersAdapter.UserAdapterClicker {

  private SearchView searchUserSearchView;
  private ArrayList<UserPreview> users;
  private RecyclerView userRv;
  private UsersAdapter usersAdapter;
  private CollectionReference usersRef;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_search);

    final Toolbar pickUserToolbar = findViewById(R.id.pickUserToolbar);
    userRv = findViewById(R.id.userRv);
    searchUserSearchView = findViewById(R.id.searchUserSearchView);
    progressBar = findViewById(R.id.progressBar);

    pickUserToolbar.setNavigationOnClickListener(v -> finish());

    searchUserSearchView.setOnClickListener(v -> searchUserSearchView.onActionViewCollapsed());

    searchUserSearchView.onActionViewExpanded();
    searchUserSearchView.setOnQueryTextListener(this);

    usersRef = FirebaseFirestore.getInstance().collection("Users");

    users = new ArrayList<>();
    usersAdapter = new UsersAdapter(users, R.layout.user_item_layout, this);
    userRv.setAdapter(usersAdapter);

    usersRef.orderBy("userId")
            .whereEqualTo("isEmailVerified",true)
            .orderBy("username",Query.Direction.ASCENDING)
            .limit(100).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
        if(!snapshots.isEmpty()){
          users.addAll(snapshots.toObjects(UserPreview.class));
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if(task.isSuccessful()){
          usersAdapter.notifyDataSetChanged();
        }
      }
    });


//    searchUserSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
//      @Override
//      public boolean onClose() {
//        if(!users.isEmpty()){
//          users.clear();
//          usersAdapter.notifyDataSetChanged();
//        }
//        return false;
//      }
//    });
  }

  @Override
  public boolean onQueryTextSubmit(String query) {

    searchUserSearchView.clearFocus();
    searchForQuery(query);

    return false;
  }

  private void searchForQuery(String query) {

    Log.d("ttt", "submit: " + query);
//    boolean alreadyExists = false;

//    for (UserPreview user : users) {
//      if (user.getUsername().equals(query)) {
//        alreadyExists = true;
//        break;
//      }
//    }
//    if (!alreadyExists) {

      progressBar.setVisibility(View.VISIBLE);

      usersRef.whereEqualTo("isEmailVerified",true)
              .whereEqualTo("username", query.trim())
              .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot snapshots) {
          if (!snapshots.isEmpty()) {
            users.addAll(snapshots.toObjects(UserPreview.class));
          }
        }
      }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
          if (task.isSuccessful()) {
            usersAdapter.notifyDataSetChanged();
//            usersAdapter.getFilter().filter(query);
          }
          progressBar.setVisibility(View.GONE);
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          progressBar.setVisibility(View.GONE);
        }
      });
    }

//  }

  @Override
  public boolean onQueryTextChange(String newText) {

    usersAdapter.getFilter().filter(newText);

    return true;
  }

  @Override
  public void clickUser(String userId) {

    if(getIntent().hasExtra("isForCourse")){
        setResult(AddCourseFragment.TUTOR_REQUEST
                ,getIntent().putExtra("userId",userId));
    }else{
      startActivity(new Intent(UserMessageSearchActivity.this,
              PrivateMessagingActivity.class)
              .putExtra("messagingUid", userId)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }
    finish();
  }
}