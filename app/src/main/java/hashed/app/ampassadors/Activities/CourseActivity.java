package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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

import hashed.app.ampassadors.Activities.MessagingActivities.CourseMessagingActivity;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class CourseActivity extends AppCompatActivity implements View.OnClickListener {

  private static final int USER_LIMIT = 20;
  //views
  private TextView meetingTitleTv,meetingDateTv,meetingTimeTv,contributorsTv;
  private RecyclerView contributorsRv;
  private Button joinBtn;

  //contributors
  private ArrayList<UserPreview> userPreviews;
  private UsersAdapter usersAdapter;
  private List<String> meetingUserIds;
  private CollectionReference usersRef;
  private boolean isLoadingUsers = false;
  private ScrollListener scrollListener;

  private Query attendeeQuery;
  //meeting info
  private Course course;
  private DocumentSnapshot lastDocSnap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_meeting);


    getViews();

    setUpAdapter();

    final Intent intent = getIntent();

    if(intent == null){
      finish();
      return;
    }

    if(intent.hasExtra("course")){

      course = (Course) getIntent().getSerializableExtra("course");

      setUpToolbar();

      fillMeetingInfo();

    }else if(intent.hasExtra("courseID")){

      FirebaseFirestore.getInstance().collection("Courses")
              .document(intent.getStringExtra("courseID"))
              .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {

          if(documentSnapshot!=null && documentSnapshot.exists()){
            course = documentSnapshot.toObject(Course.class);

            setUpToolbar();
            fillMeetingInfo();

          }else{
            finish();
          }

        }
      });

    }else{
      finish();
    }




  }

  private void setUpToolbar(){
    final Toolbar meetingToolbar = findViewById(R.id.meetingToolbar);
    meetingToolbar.setNavigationOnClickListener(v->finish());

    meetingToolbar.setTitle(course.getTitle());

  }

  private void getViews(){
    meetingTitleTv = findViewById(R.id.meetingTitleTv);
    meetingDateTv = findViewById(R.id.meetingDateTv);
    meetingTimeTv = findViewById(R.id.meetingTimeTv);
    contributorsTv = findViewById(R.id.contributorsTv);
    joinBtn = findViewById(R.id.joinBtn);
    contributorsRv = findViewById(R.id.contributorsRv);

    contributorsTv.setText(getResources().getString(R.string.attendees));
    joinBtn.setOnClickListener(this);
  }

  private void setUpAdapter(){

    userPreviews = new ArrayList<>();

    usersAdapter = new UsersAdapter(userPreviews, R.layout.meeting_contributor_details_item);

    contributorsRv.setAdapter(usersAdapter);

    usersRef = FirebaseFirestore.getInstance().collection("Users");

  }

  private void fillMeetingInfo(){

    meetingTitleTv.setText(course.getTitle());

    meetingDateTv.setText(getResources().getString(R.string.start_time) + " " +
            TimeFormatter.formatWithPattern(course.getStartTime(),
                    TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

    if(course.getTutorNames().size() > 1){
      String tutorNames = getResources().getString(R.string.course_tutors)+" ";

      for(String name:course.getTutorNames()){
        String concat = "";
        if(course.getTutorNames().indexOf(name) != course.getTutorNames().size()-1){
          concat = name+", ";
        }else{
          concat = name;
        }
        tutorNames = tutorNames.concat(concat);
      }

      meetingTimeTv.setText(tutorNames);
    }else{
      meetingTimeTv.setText(getResources().getString(R.string.tutor_name)+" "+
              course.getTutorNames().get(0));
    }


    FirebaseFirestore.getInstance().collection("Courses")
            .document(course.getCourseId()).collection("Attendees")
    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
              @Override
              public void onSuccess(QuerySnapshot snapshots) {
                if(!snapshots.isEmpty()){
                  meetingUserIds = new ArrayList<>();
                  for(DocumentSnapshot snapshot:snapshots){
                    meetingUserIds.add(snapshot.getId());
                  }
                }else{
                  contributorsTv.setText(getResources().getString(R.string.no_current_attendees));
                }
              }
            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
              @Override
              public void onComplete(@NonNull Task<QuerySnapshot> task) {
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



//    getMoreUsers(true);
  }

//  private void getMoreUsers(boolean isInitial) {
//
//    isLoadingUsers = true;
//    Query currentQuery = attendeeQuery;
//    if (lastDocSnap != null) {
//      currentQuery = attendeeQuery.startAfter(lastDocSnap);
//    }
//
//    currentQuery.get().addOnSuccessListener(snapshots -> {
//
//      if (!snapshots.isEmpty()) {
//        lastDocSnap = snapshots.getDocuments().get(snapshots.size() - 1);
//
//        Log.d("ttt", "online users: " + snapshots.size());
//        if (isInitial) {
//          userPreviews.addAll(snapshots.toObjects(UserPreview.class));
//        } else {
//          userPreviews.addAll(userPreviews.size() - 1, snapshots.toObjects(UserPreview.class));
//        }
//      }
//
//    }).addOnCompleteListener(task -> {
//
//      if (task.isSuccessful() && task.getResult() != null) {
//
//        if (isInitial) {
//          if (!userPreviews.isEmpty()) {
//            usersAdapter.notifyDataSetChanged();
//            if (userPreviews.size() == USER_LIMIT) {
//              contributorsRv.addOnScrollListener(scrollListener = new ScrollListener());
//            }
//
//          }
//        } else {
//
//          if (!task.getResult().isEmpty()) {
//            int size = task.getResult().size();
//
//            usersAdapter.notifyItemRangeInserted(
//                    userPreviews.size() - size,size);
//
//            if (task.getResult().size() < USER_LIMIT && scrollListener != null) {
//              contributorsRv.removeOnScrollListener(scrollListener);
//            }
//          }
//        }
//      }
//      isLoadingUsers = false;
//    });
//
//  }

  @Override
  public void onClick(View view) {
    if(view.getId() == joinBtn.getId()){

     startActivity(new Intent(CourseActivity.this, CourseMessagingActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                      .putExtra("messagingUid", course.getCourseId()));

     finish();
    }
  }
  private void getUsers(List<String> userIdsList) {

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