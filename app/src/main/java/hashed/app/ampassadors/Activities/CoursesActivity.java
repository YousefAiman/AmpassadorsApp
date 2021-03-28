package hashed.app.ampassadors.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
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

import hashed.app.ampassadors.Adapters.CoursesAdapter;
import hashed.app.ampassadors.Adapters.HomeNewsHeaderViewPagerAdapter;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Fragments.AddCourseFragment;
import hashed.app.ampassadors.Fragments.MeetingsFragment;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class CoursesActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

  private static final int COURSES_LIMIT = 15;
  private Query query;
  private DocumentSnapshot lastDocSnap;
  private ArrayList<Course> courses;
  private CoursesAdapter coursesAdapter;
  private ScrollListener scrollListener;
  private boolean isLoadingCourses;
  private ListenerRegistration listenerRegistration;
//  private String currentUid;

  //views
  private FloatingActionButton addCourseBtn;
  private SwipeRefreshLayout swipeRefreshLayout;
  private RecyclerView coursesRv;
  private TextView emptyTv;
  private DialogFragment dialogFragment;

  private NotificationIndicatorReceiver notificationIndicatorReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_courses);

    setupToolbar();

    getViews();

    initializeObjects();

    getMoreCourses(true);


  }

  private void setupToolbar(){

    final Toolbar courseToolbar = findViewById(R.id.courseToolbar);
    courseToolbar.setNavigationOnClickListener(v-> finish());
    courseToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_notifications) {
          startActivity(new Intent(CoursesActivity.this,
                  NotificationsActivity.class)
                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        return false;
      }
    });

    courseToolbar.getMenu().findItem(R.id.action_notifications)
            .setIcon(GlobalVariables.getNotificationsCount() > 0 ?
                    R.drawable.notification_indicator_icon :
                    R.drawable.notification_icon);

    setupNotificationReceiver(courseToolbar);

  }

  private void getViews(){

    addCourseBtn = findViewById(R.id.addCourseBtn);
    swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    coursesRv = findViewById(R.id.coursesRv);
    emptyTv = findViewById(R.id.emptyTv);

    if(!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
      if (GlobalVariables.getRole().equals("Admin") ||
              GlobalVariables.getRole().equals("Coordinator")){
        addCourseBtn.setVisibility(View.VISIBLE);
        addCourseBtn.setOnClickListener(this);
      }
    }

    swipeRefreshLayout.setOnRefreshListener(this);

    coursesRv.setLayoutManager(new LinearLayoutManager(this,
            RecyclerView.VERTICAL, false) {
      @Override
      public void onItemsRemoved(@NonNull RecyclerView recyclerView,
                                 int positionStart, int itemCount) {
        if (itemCount == 0) {
          emptyTv.setVisibility(View.VISIBLE);
          recyclerView.setVisibility(View.INVISIBLE);
        }
      }

      @Override
      public void onItemsAdded(@NonNull RecyclerView recyclerView,
                               int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        if (recyclerView.getVisibility() == View.INVISIBLE) {
          emptyTv.setVisibility(View.GONE);
          recyclerView.setVisibility(View.VISIBLE);
        }
      }
    });

  }

  private void initializeObjects(){

//    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    query = FirebaseFirestore.getInstance().collection("Courses")
            .orderBy("startTime", Query.Direction.ASCENDING)
            .whereEqualTo("hasEnded",false)
            .limit(COURSES_LIMIT);

    courses = new ArrayList<>();
    coursesAdapter = new CoursesAdapter(courses);
    coursesRv.setAdapter(coursesAdapter);

  }


  private void getMoreCourses(boolean isInitial) {

    isLoadingCourses = true;
    swipeRefreshLayout.setRefreshing(true);

    if (lastDocSnap != null) {
      query = query.startAfter(lastDocSnap);
    }

    query.get().addOnSuccessListener(snapshots -> {

      if (!snapshots.isEmpty()) {
        if (isInitial) {
          courses.addAll(snapshots.toObjects(Course.class));
        } else {
          courses.addAll(courses.size() - 1, snapshots.toObjects(Course.class));
        }
      }

    }).addOnCompleteListener(task -> {

      if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

        lastDocSnap = task.getResult().getDocuments().get(task.getResult().size() - 1);


        if (isInitial) {

          if (!courses.isEmpty()) {
            coursesRv.setVisibility(View.VISIBLE);
            coursesAdapter.notifyDataSetChanged();

            if (courses.size() == COURSES_LIMIT && scrollListener == null) {
              coursesRv.addOnScrollListener(scrollListener = new ScrollListener());
            }

            if (listenerRegistration == null) {
              addCourseEndedListener();
            }

          }else{
            emptyTv.setVisibility(View.VISIBLE);
          }
        } else {

          if (!task.getResult().isEmpty() && task.getResult().size() < COURSES_LIMIT) {
            coursesRv.removeOnScrollListener(scrollListener);
          }

          coursesAdapter.notifyItemRangeInserted((courses.size() - task.getResult().size()),
                  task.getResult().size());

        }


      }

      swipeRefreshLayout.setRefreshing(false);
      isLoadingCourses = false;
    });

  }

  private void addCourseEndedListener() {

    listenerRegistration =
            FirebaseFirestore.getInstance().collection("Courses")
                    .whereEqualTo("hasEnded", true)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
              @Override
              public void onEvent(@Nullable QuerySnapshot value,
                                  @Nullable FirebaseFirestoreException error) {

                if (value != null && !value.getDocumentChanges().isEmpty()) {

                  for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {

                      if (courses == null || courses.isEmpty())
                        return;

                      final String courseId = dc.getDocument().getString("courseId");

                      if (courseId == null)
                        return;

                      new Thread(new Runnable() {
                        @Override
                        public void run() {
                          for (int i = 0; i < courses.size(); i++) {
                            if (courses.get(i).getCourseId().equals(courseId)) {
                              courses.get(i).setHasEnded(true);
                              break;
                            }
                          }
                        }
                      }).start();

                    }
                  }
                }

              }
            });
  }

  @Override
  public void onRefresh() {
    courses.clear();
    coursesAdapter.notifyDataSetChanged();
    lastDocSnap = null;
    getMoreCourses(true);
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == addCourseBtn.getId()) {

      dialogFragment = new AddCourseFragment();
      dialogFragment.show(getSupportFragmentManager(),"AddCourseFragment");
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (notificationIndicatorReceiver != null) {
      unregisterReceiver(notificationIndicatorReceiver);
    }

    if(coursesRv != null && scrollListener != null){
      coursesRv.removeOnScrollListener(scrollListener);
    }
  }

  public void addCourseToList(Course course){
    if(coursesRv.getVisibility() == View.INVISIBLE){
      coursesRv.setVisibility(View.VISIBLE);
    }

    new Thread(new Runnable() {
      @Override
      public void run() {
        boolean wasAdded = false;
        for(Course course1:courses){
          if(course.getStartTime() <= course1.getStartTime()){
            final int index = courses.indexOf(course1);
            courses.add(index,course);
            coursesAdapter.notifyItemInserted(index);
            wasAdded = true;
            break;
          }
        }

        if(!wasAdded){
          courses.add(course);
          coursesAdapter.notifyItemInserted(courses.size());
        }
      }
    });
  }

  private void setupNotificationReceiver(Toolbar courseToolbar) {

    notificationIndicatorReceiver =
            new NotificationIndicatorReceiver() {
              @Override
              public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("showIndicator")) {
                  final MenuItem item = courseToolbar.getMenu().findItem(R.id.action_notifications);
                  if (intent.getBooleanExtra("showIndicator", false)) {
                    item.setIcon(R.drawable.notification_indicator_icon);
                  } else {
                    item.setIcon(R.drawable.notification_icon);
                  }
                }
              }
            };

    registerReceiver(notificationIndicatorReceiver,
            new IntentFilter(BuildConfig.APPLICATION_ID + ".notificationIndicator"));

  }

  private class ScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingCourses && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getMoreCourses(false);

      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d("ttt","on acitivyt result");
    dialogFragment.onActivityResult(requestCode,resultCode,data);
  }

}