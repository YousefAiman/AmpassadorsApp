package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.UsersPickerActivity;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class OnlineUsersFragment extends Fragment implements UsersAdapter.UserClickListener {


  Query query;
  DocumentSnapshot lastDocSnap;

  private static final int USERS_LIMIT = 15;
  ArrayList<UserPreview> users;
  RecyclerView userRv;
  UsersAdapter usersAdapter;
  private scrollListener scrollListener;
  boolean isLoading;


  public OnlineUsersFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    users = new ArrayList<>();
    usersAdapter = new UsersAdapter(users,getContext(),this);

    query = FirebaseFirestore.getInstance().collection("Users")
            .whereEqualTo("online",true)
            .orderBy("username", Query.Direction.DESCENDING).limit(USERS_LIMIT);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view =  inflater.inflate(R.layout.fragment_recycler_child, container, false);
    userRv = view.findViewById(R.id.childRv);
    TextView noUsersTv = view.findViewById(R.id.emptyTv);
    noUsersTv.setText(R.string.no_online_users);

    userRv.setLayoutManager(new LinearLayoutManager(getContext(),
            RecyclerView.VERTICAL, false) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.21);
        return true;
      }

      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        if (itemCount == 0){
          noUsersTv.setVisibility(View.VISIBLE);
          userRv.setVisibility(View.INVISIBLE);
        }
      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);

        if(userRv.getVisibility() == View.INVISIBLE){

          noUsersTv.setVisibility(View.GONE);
          userRv.setVisibility(View.VISIBLE);

        }
      }


    });

    userRv.setAdapter(usersAdapter);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getMoreUsers(true);


  }

  private void getMoreUsers(boolean isInitial){

    if(lastDocSnap!=null){
      query = query.startAfter(lastDocSnap);
    }

    query.get().addOnSuccessListener(snapshots -> {

      if(!snapshots.isEmpty()){

        Log.d("ttt","online users: "+snapshots.size());
        if(isInitial){
          userRv.setVisibility(View.VISIBLE);
          users.addAll(snapshots.toObjects(UserPreview.class));
        }else{
          users.addAll(users.size()-1,snapshots.toObjects(UserPreview.class));
        }
      }

    }).addOnCompleteListener(task -> {

      if(task.isSuccessful() && task.getResult()!=null) {

        if (isInitial) {

          if(!users.isEmpty()){
            userRv.setVisibility(View.VISIBLE);
            usersAdapter.notifyDataSetChanged();

            if(users.size() == USERS_LIMIT){
              userRv.addOnScrollListener(scrollListener = new scrollListener());
            }

          }
        } else {

          if (!task.getResult().isEmpty() && task.getResult().size() < USERS_LIMIT){
            userRv.removeOnScrollListener(scrollListener);
          }

          usersAdapter.notifyItemRangeInserted(
                  (users.size() - task.getResult().size())-1, task.getResult().size());


        }



      }

    });

  }

  @Override
  public void clickUser(String userId,int position) {

    startActivity(new Intent(getContext(), PrivateMessagingActivity.class)
            .putExtra("messagingUid",userId));

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