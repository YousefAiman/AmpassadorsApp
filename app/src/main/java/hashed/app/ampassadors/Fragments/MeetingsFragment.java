package hashed.app.ampassadors.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import hashed.app.ampassadors.Adapters.MeetingsAdapter;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class MeetingsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

  //views
  private RecyclerView meetingsRv;
  private TextView noMessagesTv;
  private ArrayList<Meeting> meetings;
  private MeetingsAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;

  private ScrollListener scrollListener;

  //database
  private String currentUid;
  private Query query;
  private DocumentSnapshot lastDocSnap;
  private static final int MEETING_LIMIT = 8;
  private boolean isLoading;
  private ListenerRegistration listenerRegistration;
  public MeetingsFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //adapter
    currentUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    meetings = new ArrayList<>();
    adapter = new MeetingsAdapter(meetings);

    query = FirebaseFirestore.getInstance().collection("Meetings")
            .whereArrayContains("members", currentUid)
            .whereEqualTo("hasEnded",false)
            .orderBy("startTime", Query.Direction.ASCENDING).limit(MEETING_LIMIT);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.online_users_fragment, container, false);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    meetingsRv = view.findViewById(R.id.childRv);
    noMessagesTv = view.findViewById(R.id.emptyTv);
    noMessagesTv.setText(R.string.no_current_meetings);


    meetingsRv.setAdapter(adapter);
    meetingsRv.setLayoutManager(new LinearLayoutManager(getContext(),
            RecyclerView.VERTICAL, false) {
      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        if (itemCount == 0){
          noMessagesTv.setVisibility(View.VISIBLE);
          meetingsRv.setVisibility(View.INVISIBLE);
        }
      }
      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        if(meetingsRv.getVisibility() == View.INVISIBLE){
          noMessagesTv.setVisibility(View.GONE);
          meetingsRv.setVisibility(View.VISIBLE);
        }
      }
    });

    swipeRefreshLayout.setOnRefreshListener(this);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    getMoreMeetings(true);

  }

  private void getMoreMeetings(boolean isInitial){

    isLoading = true;

    if(lastDocSnap!=null){
      query = query.startAfter(lastDocSnap);
    }

    query.get().addOnSuccessListener(snapshots -> {

      if(!snapshots.isEmpty()){

        Log.d("ttt","online users: "+snapshots.size());
        if(isInitial){
          meetings.addAll(snapshots.toObjects(Meeting.class));
        }else{
          meetings.addAll(meetings.size()-1, snapshots.toObjects(Meeting.class));
        }
      }

    }).addOnCompleteListener(task -> {

      if(task.isSuccessful() && task.getResult()!=null && !task.getResult().isEmpty()) {

        lastDocSnap = task.getResult().getDocuments().get(
                task.getResult().size()-1
        );


        if (isInitial) {

          if(!meetings.isEmpty()){
            meetingsRv.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();

            if(meetings.size() == MEETING_LIMIT){
              meetingsRv.addOnScrollListener(scrollListener = new ScrollListener());
            }

            if(listenerRegistration == null){
              addMeetingEndedListener();
            }

          }
        } else {

          if (!task.getResult().isEmpty() && task.getResult().size() < MEETING_LIMIT){
            meetingsRv.removeOnScrollListener(scrollListener);
          }

          adapter.notifyItemRangeInserted(
                  (meetings.size() - task.getResult().size())-1,
                  task.getResult().size());

        }
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);

      }

    });

  }

  @Override
  public void onRefresh() {

    meetings.clear();
    adapter.notifyDataSetChanged();
    lastDocSnap = null;
    getMoreMeetings(true);

  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

      if (!isLoading && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getMoreMeetings(false);

      }
    }
  }


  private void addMeetingEndedListener(){
    listenerRegistration =
            FirebaseFirestore.getInstance().collection("Meetings")
                    .whereArrayContains("members", currentUid)
                    .whereEqualTo("hasEnded",true).addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value!=null && !value.getDocumentChanges().isEmpty()){

                  for(DocumentChange dc:value.getDocumentChanges()){
                    if(dc.getType() == DocumentChange.Type.ADDED){


                      if(meetings == null || meetings.isEmpty())
                        return;

                      final String meetingId = dc.getDocument().getString("meetingId");

                      if(meetingId == null)
                        return;

                      for(int i=0;i<meetings.size();i++){
                        if(meetings.get(i).getMeetingId()
                                .equals(meetingId)){
                          meetings.get(i).setHasEnded(true);
                          break;
                        }
                      }
                    }
                  }
                }

              }
            });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if(meetingsRv != null && scrollListener!=null){
      meetingsRv.removeOnScrollListener(scrollListener);
    }
    if(listenerRegistration!=null){
      listenerRegistration.remove();
    }
  }
}