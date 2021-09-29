package hashed.app.ampassadors.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.Adapters.AdminAdapter;
import hashed.app.ampassadors.Objects.UserApprovment;
import hashed.app.ampassadors.R;

public class Admin extends AppCompatActivity {

  private static final int USER_LIMIT = 15;
  FirebaseFirestore firebaseFirestore;
  RecyclerView list_users;
  List<UserApprovment> data;
  AdminAdapter adapter;
  Task<QuerySnapshot> task;
  String userid;
  FirebaseAuth fAuth;
  Spinner spinner;
  private ScrollListener scrollListener;
  private boolean isLoadingUsers;
  private Query query;
  private DocumentSnapshot lastDocSnap;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_admin);
    final Toolbar toolbar = findViewById(R.id.admin_toolbar);
    toolbar.setNavigationOnClickListener(v -> {
      onBackPressed();
    });
    fAuth = FirebaseAuth.getInstance();
    userid = fAuth.getCurrentUser().getUid();
    spinner = findViewById(R.id.options);


    firebaseFirestore = FirebaseFirestore.getInstance();
    list_users = findViewById(R.id.users_list);
    list_users.setLayoutManager(new LinearLayoutManager(this));
    data = new ArrayList<>();

    adapter = new AdminAdapter(Admin.this, data);
    list_users.setAdapter(adapter);

     query = firebaseFirestore.collection("Users")
            .whereEqualTo("rejected",false)
            .limit(USER_LIMIT);
    getUsers(true);
  }
  private void getUsers(boolean isInitial) {
    isLoadingUsers = true;
    final AtomicInteger addedCount = new AtomicInteger();
    Query updatedQuery = query;
    if (lastDocSnap != null) {
      updatedQuery = query.startAfter(lastDocSnap);
    }
    updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
      if (!queryDocumentSnapshots.isEmpty()) {

        lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                queryDocumentSnapshots.size() - 1
        );

        data.addAll(queryDocumentSnapshots.toObjects(UserApprovment.class));
      }
    }).addOnCompleteListener(task -> {
      if (isInitial) {
        adapter.notifyDataSetChanged();

        if (data.size() == USER_LIMIT) {
          list_users.addOnScrollListener(scrollListener =
                  new ScrollListener());
        }
      } else {
        Log.d("ttt","Added count: "+addedCount.get());

        adapter.notifyItemRangeInserted(data.size() - addedCount.get(),
                addedCount.get());

        if (addedCount.get() < USER_LIMIT && scrollListener != null) {
          list_users.removeOnScrollListener(scrollListener);
        }
      }
      isLoadingUsers = false;
    });
  }
  @Override
  public void onDestroy() {
    super.onDestroy();
    if(list_users!=null && scrollListener!=null){
      list_users.removeOnScrollListener(scrollListener);
    }
  }
  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingUsers &&
              !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getUsers(false);
      }
    }
  }
}