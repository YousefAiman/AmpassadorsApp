package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Activities.CreatePollActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.PostActivity;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class PostsFragment extends Fragment implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener , View.OnClickListener ,
        PostAdapter.CommentsInterface{

  private static final int POSTS_LIMIT = 10;
  private Query query;
  private List<PostData> postData;
  private PostAdapter adapter;
  private RecyclerView post_list;
  private DocumentSnapshot lastDocSnap;
  private boolean isLoadingMessages;
  private SwipeRefreshLayout swipeRefresh;
  private PostsBottomScrollListener scrollListener;
  public PostsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    query = FirebaseFirestore.getInstance().collection("Posts")
            .orderBy("publishTime", Query.Direction.DESCENDING).limit(POSTS_LIMIT);
    postData = new ArrayList<>();

    adapter = new PostAdapter(postData, getContext(),this);

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

    return view;
  }


  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    FloatingActionButton floatingButton = view.findViewById(R.id.floatingButton);
    floatingButton.setOnClickListener(this);

    post_list.setAdapter(adapter);

    ReadPost(true);

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

  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.floatingButton){

      showPostOptionsBottomSheet();
    }
  }

  @Override
  public void showComments(String postId, int commentsCount) {

    CommentsFragment commentsFragment = new CommentsFragment(postId,commentsCount);
    commentsFragment.show(getChildFragmentManager(),"CommentsFragment");

  }

  private class PostsBottomScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingMessages &&
              !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {


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

          for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){

            if(snapshot.getLong("type") == PostData.TYPE_POLL){

              if(snapshot.getBoolean("pollEnded")){

                if(snapshot.getLong("totalVotes") > 0){
                  postData.add(snapshot.toObject(PostData.class));
                }

              }else{

                if(System.currentTimeMillis() >
                        snapshot.getLong("publishTime") +
                                snapshot.getLong("pollDuration")){

                  snapshot.getReference().update("pollEnded",true);

                  if(snapshot.getLong("totalVotes") > 0){

                    final PostData post = snapshot.toObject(PostData.class);
                    post.setPollEnded(true);
                    postData.add(post);

                  }

                }else{

                  postData.add(snapshot.toObject(PostData.class));

                }

              }

            }else{

              postData.add(snapshot.toObject(PostData.class));

            }


          }
//          postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));

        }

      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {


        if(isInitial){
          adapter.notifyDataSetChanged();

          if(postData.size() == POSTS_LIMIT){
            post_list.addOnScrollListener(scrollListener = new PostsBottomScrollListener());
          }
        }else{

          adapter.notifyItemRangeInserted((postData.size()-task.getResult().size())-1,
                  task.getResult().size());


          if(postData.size() < POSTS_LIMIT && scrollListener != null){
            post_list.removeOnScrollListener(scrollListener);
          }
        }



        swipeRefresh.setRefreshing(false);

      }
    });
  }


  private void showPostOptionsBottomSheet(){

    final BottomSheetDialog bsd = new BottomSheetDialog(getContext(), R.style.SheetDialog);
    final View parentView = getLayoutInflater().inflate(R.layout.post_options_bsd, null);
    parentView.setBackgroundColor(Color.TRANSPARENT);

    parentView.findViewById(R.id.new_post).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        bsd.dismiss();
        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
      }
    });

    parentView.findViewById(R.id.new_poll).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        bsd.dismiss();
        Intent intent = new Intent(getContext(), CreatePollActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
      }
    });

    bsd.setContentView(parentView);
    bsd.show();

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