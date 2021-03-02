package hashed.app.ampassadors.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class PostsFragment extends Fragment implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {

  FirebaseFirestore firebaseFirestore;
  Query query;
  List<PostData> postData;
  PostAdapter adapter;
  RecyclerView post_list;
  DocumentSnapshot lastDocSnap;
  boolean isLoadingMessages;
  SwipeRefreshLayout swipeRefresh;
  public PostsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    firebaseFirestore = FirebaseFirestore.getInstance();
    query = firebaseFirestore.collection("Posts").orderBy("publishTime",
            Query.Direction.DESCENDING).limit(10);
    postData = new ArrayList<>();

    adapter = new PostAdapter(postData, getContext());

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view =  inflater.inflate(R.layout.fragment_posts, container, false);
    post_list = view.findViewById(R.id.home_list);
    swipeRefresh = view.findViewById(R.id.swipeRefresh);
    swipeRefresh.setOnRefreshListener(this);

    Toolbar toolbar = view.findViewById(R.id.home_activity_toolbar);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ((Home_Activity)getActivity())
                .showDrawer();
      }
    });



//    post_list.setLayoutManager(new LinearLayoutManager(getContext(),
//            RecyclerView.VERTICAL, false) {
//      @Override
//      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
//        lp.height = (int) (getWidth() * 2.2);
//        return true;
//      }
//    });
    return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    post_list.setAdapter(adapter);

    ReadPost(true);


    post_list.addOnScrollListener(new ChatsScrollListener());
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    return false;
  }

  @Override
  public void onRefresh() {

    postData.clear();
    adapter.notifyDataSetChanged();
    lastDocSnap = null;
    ReadPost(true);

  }

  private class ChatsScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingMessages &&
              !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d("ttt","is at bottom man");

        ReadPost(false);

      }
    }
  }


  private void ReadPost(boolean isInitial) {

    swipeRefresh.setRefreshing(true);
    isLoadingMessages = false;

    Query updatedQuery = query;

    if(lastDocSnap!=null){

      updatedQuery = query.startAfter(lastDocSnap);

    }

    updatedQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
        if (!queryDocumentSnapshots.isEmpty()) {

          lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                  queryDocumentSnapshots.size()-1
          );

          postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));

        }

      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {



        if(isInitial){
          adapter.notifyDataSetChanged();

        }else{
          adapter.notifyItemRangeInserted((postData.size()-task.getResult().size())-1,
                  task.getResult().size());
        }


        swipeRefresh.setRefreshing(false);

      }
    });
  }


  private void setUpToolBarAndActions() {

    final Toolbar toolbar = getView().findViewById(R.id.home_activity_toolbar);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(getContext(), "tool", Toast.LENGTH_SHORT).show();
      }
    });
    toolbar.setOnMenuItemClickListener(this);

  }
}