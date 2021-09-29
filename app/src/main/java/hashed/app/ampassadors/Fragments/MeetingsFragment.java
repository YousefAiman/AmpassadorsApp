package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.NotificationsActivity;
import hashed.app.ampassadors.Activities.UsersPickerActivity;
import hashed.app.ampassadors.Adapters.MeetingsAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FirebaseDeleteUtil;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class MeetingsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {

  private static final int MEETING_LIMIT = 10;
  public static final int MEETING_RESULT = 5;
  
  //views
  private RecyclerView meetingsRv;
  private TextView noMessagesTv;
  private ArrayList<Meeting> meetings;
  private MeetingsAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private ScrollListener scrollListener;
  private FloatingActionButton floatingButton;
  private Button deleteMeetingsBtn;
  private Toolbar toolbar;


  private NotificationIndicatorReceiver notificationIndicatorReceiver;


  //database
  private String currentUid;
  private Query query;
  private DocumentSnapshot lastDocSnap;
  private boolean isLoading;
  private ListenerRegistration listenerRegistration;
  private TextView notificationCountTv;

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
            .whereEqualTo("hasEnded", false)
            .orderBy("startTime", Query.Direction.ASCENDING).limit(MEETING_LIMIT);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.meetings_fragment, container, false);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    meetingsRv = view.findViewById(R.id.childRv);
    noMessagesTv = view.findViewById(R.id.emptyTv);

    floatingButton = view.findViewById(R.id.floatingButton);
    deleteMeetingsBtn = view.findViewById(R.id.deleteMeetingsBtn);
    toolbar = view.findViewById(R.id.groupToolbar);

    noMessagesTv.setText(getResources().getString(R.string.no_current_meetings));


    meetingsRv.setAdapter(adapter);
    meetingsRv.setLayoutManager(new LinearLayoutManager(getContext(),
            RecyclerView.VERTICAL, false) {
      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        if (itemCount == 0) {
          noMessagesTv.setVisibility(View.VISIBLE);
          meetingsRv.setVisibility(View.INVISIBLE);
        }
      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        if (meetingsRv.getVisibility() == View.INVISIBLE) {
          noMessagesTv.setVisibility(View.GONE);
          meetingsRv.setVisibility(View.VISIBLE);
        }
      }
    });



    final Toolbar toolbar = view.findViewById(R.id.groupToolbar);
    toolbar.setNavigationOnClickListener(v -> ((Home_Activity) requireActivity()).showDrawer());
//    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//      @Override
//      public boolean onMenuItemClick(MenuItem item) {
//
//        if (item.getItemId() == R.id.action_notifications) {
//          startActivity(new Intent(getContext(), NotificationsActivity.class)
//                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
//
//        return false;
//      }
//    });

    final View notificationActionView = toolbar.getMenu()
            .findItem(R.id.action_notifications).getActionView();
    notificationCountTv = notificationActionView.findViewById(R.id.notificationCountTv);

    notificationActionView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(new Intent(getContext(), NotificationsActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
    });
    swipeRefreshLayout.setOnRefreshListener(this);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


    if(GlobalVariables.getNotificationsCount() > 0){
      if(notificationCountTv.getVisibility() == View.GONE){
        notificationCountTv.setVisibility(View.VISIBLE);
      }
      notificationCountTv.setText(GlobalVariables.getNotificationsCount() > 99?"99+":
              String.valueOf(GlobalVariables.getNotificationsCount()));

    }else if(notificationCountTv.getVisibility() == View.VISIBLE){
      notificationCountTv.setVisibility(View.GONE);
    }
    setupNotificationReceiver();

    getMoreMeetings(true);



    if(!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
      if(GlobalVariables.getRole()!=null){
        if (GlobalVariables.getRole().equals("Admin") ||
                GlobalVariables.getRole().equals("Coordinator")){
          floatingButton.setVisibility(View.VISIBLE);
          floatingButton.setOnClickListener(this);
        }
      }
    }



  }

  private void getMoreMeetings(boolean isInitial) {

    swipeRefreshLayout.setRefreshing(true);
    isLoading = true;

    Query newQuery = query;
    if (lastDocSnap != null) {
      newQuery = newQuery.startAfter(lastDocSnap);
    }

    newQuery.get().addOnSuccessListener(snapshots -> {

      if (!snapshots.isEmpty()) {

        Log.d("ttt", "online users: " + snapshots.size());
        if (isInitial) {
          meetings.addAll(snapshots.toObjects(Meeting.class));
        } else {
          meetings.addAll(meetings.size() - 1, snapshots.toObjects(Meeting.class));
        }
      }

    }).addOnCompleteListener(task -> {

      if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

        lastDocSnap = task.getResult().getDocuments().get(
                task.getResult().size() - 1
        );


        if (isInitial) {

          if (!meetings.isEmpty()) {
            meetingsRv.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();

            if (meetings.size() == MEETING_LIMIT && scrollListener == null) {
              meetingsRv.addOnScrollListener(scrollListener = new ScrollListener());
            }

            if (listenerRegistration == null) {
              addMeetingEndedListener();
            }

          }
        } else {

          if (!task.getResult().isEmpty() && task.getResult().size() < MEETING_LIMIT) {
            meetingsRv.removeOnScrollListener(scrollListener);
          }

          adapter.notifyItemRangeInserted(
                  (meetings.size() - task.getResult().size()),
                  task.getResult().size());

        }
        isLoading = false;

      }
      swipeRefreshLayout.setRefreshing(false);
    });

  }

  @Override
  public void onRefresh() {

    meetings.clear();
    adapter.notifyDataSetChanged();
    lastDocSnap = null;
    getMoreMeetings(true);

  }

  private void addMeetingEndedListener() {
    listenerRegistration =
            FirebaseFirestore.getInstance().collection("Meetings")
                    .whereArrayContains("members", currentUid)
                    .whereEqualTo("hasEnded", true).addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value != null && !value.getDocumentChanges().isEmpty()) {

                  for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {


                      if (meetings == null || meetings.isEmpty())
                        return;

                      final String meetingId = dc.getDocument().getString("meetingId");

                      if (meetingId == null)
                        return;

                      for (int i = 0; i < meetings.size(); i++) {
                        if (meetings.get(i).getMeetingId()
                                .equals(meetingId)) {
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

    if (meetingsRv != null && scrollListener != null) {
      meetingsRv.removeOnScrollListener(scrollListener);
    }
    if (listenerRegistration != null) {
      listenerRegistration.remove();
    }

    if (notificationIndicatorReceiver != null) {
      requireContext().unregisterReceiver(notificationIndicatorReceiver);
    }


  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.floatingButton) {
      startActivityForResult(new Intent(getContext(), UsersPickerActivity.class), MEETING_RESULT);
    }else if (view.getId() == R.id.deleteMeetingsBtn) {
      FirebaseDeleteUtil.deleteAllMeetingsAndMessages();
    }
  }

  private void setupNotificationReceiver() {

    notificationIndicatorReceiver =
            new NotificationIndicatorReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                if(GlobalVariables.getNotificationsCount() > 0){
                  if(notificationCountTv.getVisibility() == View.GONE){
                    notificationCountTv.setVisibility(View.VISIBLE);
                  }
                  notificationCountTv.setText(GlobalVariables.getNotificationsCount() > 99?
                          "99+":String.valueOf(GlobalVariables.getNotificationsCount()));

                }else if(notificationCountTv.getVisibility() == View.VISIBLE){
                  notificationCountTv.setVisibility(View.GONE);
                }
              }
            };

    requireContext().registerReceiver(notificationIndicatorReceiver,
            new IntentFilter(BuildConfig.APPLICATION_ID + ".notificationIndicator"));

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

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

    if(requestCode == MEETING_RESULT && data!=null && data.hasExtra("meeting")){

     final Meeting meeting = (Meeting) data.getSerializableExtra("meeting");

     if(meeting!=null){
       meetings.add(0,meeting);
       adapter.notifyItemInserted(0);
       meetingsRv.scrollToPosition(0);
     }
    }

  }
}