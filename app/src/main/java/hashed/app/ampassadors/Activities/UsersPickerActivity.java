package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Fragments.MessagesFragment;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UsersPickerActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener ,UsersAdapter.UserClickListener,
        SearchView.OnQueryTextListener{
  private Query query;
  private DocumentSnapshot lastDocSnap;
  private static final int USERS_LIMIT = 15;
  private ArrayList<UserPreview> users;
  private RecyclerView userRv;
  private UsersAdapter usersAdapter;
  private scrollListener scrollListener;
  private boolean isLoading;
  private ArrayList<String> previousSelectedUserIdsList;
  private CollectionReference usersRef;
  private SearchView searchUserSearchView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users_picker);

    userRv = findViewById(R.id.userRv);
    Toolbar pickUserToolbar = findViewById(R.id.pickUserToolbar);
    pickUserToolbar.setNavigationOnClickListener(v-> finish());
    pickUserToolbar.setOnMenuItemClickListener(this);

    usersRef = FirebaseFirestore.getInstance().collection("Users");

    if(getIntent().hasExtra("selectedUserIdsList")){
      previousSelectedUserIdsList
              = getIntent().getStringArrayListExtra("selectedUserIdsList");
    }

    users = new ArrayList<>();

    userRv.setLayoutManager(new LinearLayoutManager(this,
            RecyclerView.VERTICAL, false) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.21);
        return true;
      }
    });

    if(previousSelectedUserIdsList!=null && !previousSelectedUserIdsList.isEmpty()){
      usersAdapter = new UsersAdapter(users,this,this,
              previousSelectedUserIdsList);
    }else{
      usersAdapter = new UsersAdapter(users,this,this);
    }

    userRv.setAdapter(usersAdapter);

//    query = FirebaseFirestore.getInstance().collection("Users")
//            .orderBy("username", Query.Direction.DESCENDING).limit(USERS_LIMIT);


    if(previousSelectedUserIdsList!=null && !previousSelectedUserIdsList.isEmpty()){

      getPreviousUsers();

    }
//    else{
//
//
////      getMoreUsers(true);
//
//    }

    searchUserSearchView = findViewById(R.id.searchUserSearchView);
    searchUserSearchView.setOnClickListener(v->
            searchUserSearchView.onActionViewCollapsed());

    searchUserSearchView.onActionViewExpanded();
    searchUserSearchView.setOnQueryTextListener(this);


  }

  private void getPreviousUsers(){

    for(String id:previousSelectedUserIdsList){

      FirebaseFirestore.getInstance().collection("Users")
              .document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {

          users.add(documentSnapshot.toObject(UserPreview.class));

        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          usersAdapter.notifyItemInserted(users.size()-1);
        }
      });
    }
  }

  private void getMoreUsers(boolean isInitial){


    if(lastDocSnap!=null){
      query = query.startAfter(lastDocSnap);
    }

    query.get().addOnSuccessListener(snapshots -> {
      if(!snapshots.isEmpty()){
        if(isInitial){
          users.addAll(snapshots.toObjects(UserPreview.class));
        }else{
          users.addAll(users.size()-1,snapshots.toObjects(UserPreview.class));
        }
      }
    }).addOnCompleteListener(task -> {

      if(task.isSuccessful() && task.getResult() != null) {
        if (isInitial) {
          usersAdapter.notifyDataSetChanged();
          if(task.getResult().size() == USERS_LIMIT){
//            userRv.addOnScrollListener(scrollListener = new scrollListener());
          }
        } else {
          if (!task.getResult().isEmpty() && task.getResult().size() < USERS_LIMIT){
//            userRv.removeOnScrollListener(scrollListener);
          }
          usersAdapter.notifyItemRangeInserted(
                  (users.size() - task.getResult().size())-1, task.getResult().size());
        }
      }
    });

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {

    final ArrayList<String> selectedUserIds = usersAdapter.selectedUserIds;

    if(!selectedUserIds.isEmpty() && selectedUserIds.size() >= 2){

      Intent output = new Intent();
      output.putStringArrayListExtra("selectedUserIds", selectedUserIds);
      setResult(3, output);
      finish();

    }else{

      Toast.makeText(this, "You need to choose at least 2 people to create a group",
              Toast.LENGTH_SHORT).show();

    }

    return false;
  }

  @Override
  public void clickUser(String userId,int position) {

    if(usersAdapter.selectedUserIds.contains(userId)){
      usersAdapter.selectedUserIds.remove(userId);
    }else{
      usersAdapter.selectedUserIds.add(userId);
    }

    usersAdapter.notifyItemChanged(position);

  }

  @Override
  public boolean onQueryTextSubmit(String query) {

    searchUserSearchView.clearFocus();

    Log.d("ttt","submit: "+query);
    boolean alreadyExists = false;

    for(UserPreview user : users){
      if(user.getUsername().equals(query)){
        alreadyExists = true;
        break;
      }
    }

    if(!alreadyExists){

      usersRef.whereEqualTo("username",query.trim())
              .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
            usersAdapter.getFilter().filter(query);
          }
        }
      });
    }

    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {

    usersAdapter.getFilter().filter(newText);

    return true;
  }


  private class scrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

      if (!isLoading && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getMoreUsers(false);

      }
    }
  }

}