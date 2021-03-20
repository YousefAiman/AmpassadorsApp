package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import hashed.app.ampassadors.Adapters.UsersPickerAdapter;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UserSearchActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener {

  private SearchView searchUserSearchView;
  private ArrayList<UserPreview> users;
  private RecyclerView userRv;
  private UsersPickerAdapter pickerAdapter;
  private ArrayList<String> previousSelectedUserIdsList;
  private CollectionReference usersRef;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_search);

    Toolbar pickUserToolbar = findViewById(R.id.pickUserToolbar);
    userRv = findViewById(R.id.userRv);
    searchUserSearchView = findViewById(R.id.searchUserSearchView);


    pickUserToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (pickerAdapter.selectedUserIds != null && !pickerAdapter.selectedUserIds.isEmpty()) {
          setResult(3, new Intent().putExtra("previousSelectedUserIdsList"
                  , pickerAdapter.selectedUserIds));
        }
        finish();

      }
    });


    searchUserSearchView.setOnClickListener(v -> searchUserSearchView.onActionViewCollapsed());

    searchUserSearchView.onActionViewExpanded();
    searchUserSearchView.setOnQueryTextListener(this);


    usersRef = FirebaseFirestore.getInstance().collection("Users");


    if (getIntent().hasExtra("selectedUserIds")) {
      previousSelectedUserIdsList
              = getIntent().getStringArrayListExtra("selectedUserIds");
    }

    users = new ArrayList<>();

    usersRef.orderBy("username")
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
          pickerAdapter = new UsersPickerAdapter(users, previousSelectedUserIdsList,
                  true);
          userRv.setAdapter(pickerAdapter);
        }
      }
    });
  }
  @Override
  public boolean onQueryTextSubmit(String query) {

    searchUserSearchView.clearFocus();
    searchForQuery(query);

    return false;
  }

  private void searchForQuery(String query) {

    Log.d("ttt", "submit: " + query);
    boolean alreadyExists = false;

    for (UserPreview user : users) {
      if (user.getUsername().equals(query)) {
        alreadyExists = true;
        break;
      }
    }

    if (!alreadyExists) {

      usersRef.whereEqualTo("username", query.trim())
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
            pickerAdapter.notifyDataSetChanged();
            pickerAdapter.getFilter().filter(query);
          }
        }
      });
    }

  }

  @Override
  public boolean onQueryTextChange(String newText) {

    pickerAdapter.getFilter().filter(newText);

    return true;
  }

}