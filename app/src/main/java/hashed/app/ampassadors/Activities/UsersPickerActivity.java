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

public class UsersPickerActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

  Query query;
  DocumentSnapshot lastDocSnap;

  private static final int USERS_LIMIT = 15;
  ArrayList<UserPreview> users;
  RecyclerView userRv;
  UsersAdapter usersAdapter;
  private scrollListener scrollListener;
  boolean isLoading;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users_picker);

    userRv = findViewById(R.id.userRv);
    Toolbar pickUserToolbar = findViewById(R.id.pickUserToolbar);
    pickUserToolbar.setOnMenuItemClickListener(this);


    users = new ArrayList<>();

    userRv.setLayoutManager(new LinearLayoutManager(this,
            RecyclerView.VERTICAL, false) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.21);
        return true;
      }
    });


    usersAdapter = new UsersAdapter(users,this,false);
    userRv.setAdapter(usersAdapter);


    query = FirebaseFirestore.getInstance().collection("Users")
            .orderBy("username", Query.Direction.DESCENDING).limit(USERS_LIMIT);


    getMoreUsers(true);

    EditText searchUserEd = findViewById(R.id.searchUserEd);

    searchUserEd.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (usersAdapter != null) {
          usersAdapter.getFilter().filter(charSequence.toString());
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });


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

      if(task.isSuccessful() && task.getResult()!=null) {

        if (isInitial) {

          usersAdapter.notifyDataSetChanged();
//          userRv.addOnScrollListener(scrollListener = new scrollListener());

        } else {

          usersAdapter.notifyItemRangeInserted(
                  (users.size() - task.getResult().size())-1, task.getResult().size());


        }

        if (!task.getResult().isEmpty() && task.getResult().size() < USERS_LIMIT){
//          userRv.removeOnScrollListener(scrollListener);
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