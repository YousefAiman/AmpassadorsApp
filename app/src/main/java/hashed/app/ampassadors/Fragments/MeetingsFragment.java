//package hashed.app.ampassadors.Fragments;
//
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.google.firebase.auth.FirebaseAuth;
//
//import hashed.app.ampassadors.Objects.UserPreview;
//import hashed.app.ampassadors.R;
//
//public class MeetingsFragment extends Fragment {
//
//  //views
//  private RecyclerView meetingsRv;
//  private TextView noMessagesTv;
//
//  //adapter
//  private String currentUid;
//
//
//
//  public MeetingsFragment() {
//    // Required empty public constructor
//  }
//
//  @Override
//  public void onCreate(@Nullable Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//
//    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//  }
//
//
//
//  @Override
//  public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                           Bundle savedInstanceState) {
//    // Inflate the layout for this fragment
//    View view =  inflater.inflate(R.layout.fragment_recycler_child, container, false);
//    meetingsRv = view.findViewById(R.id.childRv);
//    noMessagesTv = view.findViewById(R.id.emptyTv);
//    noMessagesTv.setText(R.string.no_current_meetings);
//
//    meetingsRv.setLayoutManager(new LinearLayoutManager(getContext(),
//            RecyclerView.VERTICAL, false) {
//      @Override
//      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
//        lp.height = (int) (getWidth() * 0.32);
//        return true;
//      }
//
//      @Override
//      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
//                                 int positionStart, int itemCount) {
//        if (itemCount == 0){
//          noMessagesTv.setVisibility(View.VISIBLE);
//          meetingsRv.setVisibility(View.INVISIBLE);
//        }
//      }
//
//      @Override
//      public void onItemsAdded(@NonNull RecyclerView recyclerView,
//                               int positionStart, int itemCount) {
//        super.onItemsAdded(recyclerView, positionStart, itemCount);
//
//        if(meetingsRv.getVisibility() == View.INVISIBLE){
//
//          noMessagesTv.setVisibility(View.GONE);
//          meetingsRv.setVisibility(View.VISIBLE);
//
//        }
//      }
//    });
//
//
//
//    return view;
//  }
//
//  @Override
//  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//    super.onViewCreated(view, savedInstanceState);
//
//
//
//  }
//
//
//  private void getMoreUsers(boolean isInitial){
//
//    if(lastDocSnap!=null){
//      query = query.startAfter(lastDocSnap);
//    }
//
//    query.get().addOnSuccessListener(snapshots -> {
//
//      if(!snapshots.isEmpty()){
//
//        Log.d("ttt","online users: "+snapshots.size());
//        if(isInitial){
//          users.addAll(snapshots.toObjects(UserPreview.class));
//        }else{
//          users.addAll(users.size()-1,snapshots.toObjects(UserPreview.class));
//        }
//      }
//
//    }).addOnCompleteListener(task -> {
//
//      if(task.isSuccessful() && task.getResult()!=null) {
//
//        if (isInitial) {
//
//          if(!users.isEmpty()){
//            userRv.setVisibility(View.VISIBLE);
//            usersAdapter.notifyDataSetChanged();
//
//            if(users.size() == USERS_LIMIT){
//              userRv.addOnScrollListener(scrollListener = new OnlineUsersFragment.scrollListener());
//            }
//
//          }
//        } else {
//
//          if (!task.getResult().isEmpty() && task.getResult().size() < USERS_LIMIT){
//            userRv.removeOnScrollListener(scrollListener);
//          }
//
//          usersAdapter.notifyItemRangeInserted(
//                  (users.size() - task.getResult().size())-1, task.getResult().size());
//
//
//        }
//
//
//
//      }
//
//    });
//
//  }
//
//
//  private class scrollListener extends RecyclerView.OnScrollListener {
//    @Override
//    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//      super.onScrollStateChanged(recyclerView, newState);
//
//      if (!isLoading && !recyclerView.canScrollVertically(1) &&
//              newState == RecyclerView.SCROLL_STATE_IDLE) {
//
//        getMoreUsers(false);
//
//      }
//    }
//  }
//}