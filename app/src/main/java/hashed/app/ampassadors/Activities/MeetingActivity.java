package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class MeetingActivity extends AppCompatActivity implements View.OnClickListener {

  //views
  private TextView meetingTitleTv,meetingDateTv,meetingTimeTv;
  private RecyclerView contributorsRv;
  private Button joinBtn;

  //contributors
  private ArrayList<UserPreview> userPreviews;
  private UsersAdapter usersAdapter;
  private List<String> meetingUserIds;
  private CollectionReference usersRef;
  private boolean isLoadingUsers = false;
  private ScrollListener scrollListener;

  //meeting info
  private Meeting meeting;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_meeting);

    meeting = (Meeting) getIntent().getSerializableExtra("meeting");

    setUpToolbar();

    getViews();

    setUpAdapter();

    fillMeetingInfo();

  }

  private void setUpToolbar(){
    final Toolbar meetingToolbar = findViewById(R.id.meetingToolbar);
    meetingToolbar.setNavigationOnClickListener(v->finish());
  }

  private void getViews(){
    meetingTitleTv = findViewById(R.id.meetingTitleTv);
    meetingDateTv = findViewById(R.id.meetingDateTv);
    meetingTimeTv = findViewById(R.id.meetingTimeTv);
    joinBtn = findViewById(R.id.joinBtn);
    contributorsRv = findViewById(R.id.contributorsRv);

    joinBtn.setOnClickListener(this);
  }

  private void setUpAdapter(){

    userPreviews = new ArrayList<>();

    usersAdapter = new UsersAdapter(userPreviews, R.layout.meeting_contributor_details_item);

    contributorsRv.setAdapter(usersAdapter);

    usersRef = FirebaseFirestore.getInstance().collection("Users");

  }

  private void fillMeetingInfo(){

    meetingTitleTv.setText(meeting.getTitle());

    meetingDateTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
            TimeFormatter.MONTH_DAY_YEAR));

    meetingTimeTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
            TimeFormatter.HOUR_MINUTE));


    FirebaseFirestore.getInstance().collection("Meetings")
            .document(meeting.getMeetingId())
            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {
        if(snapshot.exists()){
          meetingUserIds = (List<String>) snapshot.get("members");
        }
      }
    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
        if(task.isSuccessful() && meetingUserIds!=null && !meetingUserIds.isEmpty()){
          if (meetingUserIds.size() > 10) {

            getUsers(meetingUserIds.subList(0, 10));

            contributorsRv.addOnScrollListener(scrollListener = new ScrollListener());

          }else{
            getUsers(meetingUserIds);
          }

        }
      }
    });

  }


  void getUsers(List<String> userIdsList) {

    isLoadingUsers = true;
    final int previousSize = userPreviews.size();

    usersRef.whereIn("userId", userIdsList).get()
            .addOnSuccessListener(snapshots -> {
              if(!snapshots.isEmpty()){
               userPreviews.addAll(snapshots.toObjects(UserPreview.class));
              }
            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (previousSize == 0) {
          usersAdapter.notifyDataSetChanged();
        } else {
          usersAdapter.notifyItemRangeInserted(previousSize, userPreviews.size()
                  - previousSize);
        }

        isLoadingUsers = false;
      }
    });

  }

  @Override
  public void onClick(View view) {
    if(view.getId() == joinBtn.getId()){

     startActivity(new Intent(MeetingActivity.this, GroupMessagingActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
                      meeting.getMeetingId()));

     finish();
    }
  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingUsers && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {


        if (meetingUserIds.size() >= userPreviews.size() + 10) {
          getUsers(meetingUserIds.subList(
                  userPreviews.size(), userPreviews.size() + 10));
        } else {

          contributorsRv.removeOnScrollListener(scrollListener);

          if (meetingUserIds.size() > userPreviews.size()) {
            getUsers(meetingUserIds.subList(userPreviews.size(), meetingUserIds.size()));
          }


        }

      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if(contributorsRv!=null && scrollListener!=null){
      contributorsRv.removeOnScrollListener(scrollListener);
    }
  }
}