package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class CreateMeetingActivity extends AppCompatActivity implements View.OnClickListener,
        UsersAdapter.UserClickListener {


  //time
  private DateFormat todayYearMonthDayFormat;
  long startMillis;

  //database
  private CollectionReference meetingsRef;
  private CollectionReference usersRef;


  //seleceted users
  private static final int USERS_LIMIT = 10;
  private String currentUid;
  private ArrayList<String> selectedUserIdsList;
  private ArrayList<UserPreview> selectedUsers;
  private boolean isLoadingUsers;
  private scrollListener scrollListener;


  //views
  private RecyclerView selectedUserRv;
  private TextView meetingTimeTv,usersEd,showSelectedUsersTv;
  private EditText titleEd,descreptionEd;
  private Button doneButton;
  private Toolbar toolbar;

  UsersAdapter selectedUsersAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_meeting);

    initViews();

    initValues();

    setViewClickers();


  }

  private void initViews(){

    meetingTimeTv = findViewById(R.id.meetingTimeTv);
    selectedUserRv = findViewById(R.id.selectedUserRv);
    titleEd = findViewById(R.id.titleEd);
    descreptionEd = findViewById(R.id.descreptionEd);
    doneButton = findViewById(R.id.doneButton);
    usersEd = findViewById(R.id.usersEd);
    toolbar = findViewById(R.id.toolbar);
    showSelectedUsersTv = findViewById(R.id.showSelectedUsersTv);
    NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);

    nestedScrollView.setNestedScrollingEnabled(false);

  }

  private void initValues(){

    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());

    meetingsRef = FirebaseFirestore.getInstance().collection("Meetings");

  }


  private void setViewClickers(){

    toolbar.setNavigationOnClickListener(v->finish());
    usersEd.setOnClickListener(this);
    meetingTimeTv.setOnClickListener(this);
    doneButton.setOnClickListener(this);
    showSelectedUsersTv.setOnClickListener(this);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == 3 && data!=null){

      if(data.hasExtra("selectedUserIds")){


        if(selectedUsersAdapter!=null){
          selectedUsers.clear();
          selectedUsersAdapter.notifyDataSetChanged();
          selectedUsersAdapter = null;
        }

        selectedUserIdsList = data.getStringArrayListExtra("selectedUserIds");

        showSelectedUsersTv.setVisibility(View.VISIBLE);


        Log.d("ttt","selectedUserIdsList: "+ selectedUserIdsList.size());
      }

    }
  }

  private void pickTime() {

    Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());
    DatePickerDialog StartTime = new DatePickerDialog(this,
            new DatePickerDialog.OnDateSetListener() {
      String date;

      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar newDate = Calendar.getInstance(Locale.getDefault());
        newDate.set(year, monthOfYear, dayOfMonth);
        date = todayYearMonthDayFormat.format(newDate.getTime());
        Calendar mcurrentTime = Calendar.getInstance(Locale.getDefault());
        TimePickerDialog mTimePicker = new TimePickerDialog(
                CreateMeetingActivity.this, (timePicker, selectedHour, selectedMinute) -> {
          final Calendar calendar = Calendar.getInstance(Locale.getDefault());
          final long currentTime = calendar.getTimeInMillis();
          calendar.set(Calendar.YEAR, year);
          calendar.set(Calendar.MONTH, monthOfYear);
          calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
          calendar.set(Calendar.HOUR, selectedHour);
//          calendar.set(Calendar.AM, selectedHour);
          calendar.set(Calendar.MINUTE, selectedMinute);
          calendar.set(Calendar.SECOND, 0);
          calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() >= currentTime) {
              startMillis = calendar.getTimeInMillis();
              date = date.concat(" " + selectedHour + ":" + selectedMinute);
              meetingTimeTv.setText(date);
            } else {
              Toast.makeText(CreateMeetingActivity.this,
                      "Meeting time can't be at selected time!", Toast.LENGTH_SHORT).show();
            }

        }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
                true);
        mTimePicker.setTitle("Select Meeting Time");
        mTimePicker.show();
      }

    }, mcurrentDate.get(Calendar.YEAR), mcurrentDate.get(Calendar.MONTH),
            mcurrentDate.get(Calendar.DAY_OF_MONTH));
    StartTime.show();

  }

  private void publishMeeting(){

    final String title = titleEd.getText().toString();
    final String description = descreptionEd.getText().toString();

    if(!title.isEmpty() && !description.isEmpty() && startMillis!=0 &&
            selectedUserIdsList != null && !selectedUserIdsList.isEmpty() &&
            selectedUserIdsList.size() > 1){

      ProgressDialog progressDialog = new ProgressDialog(CreateMeetingActivity.this);
      progressDialog.setTitle("Publishing Meeting!");
      progressDialog.setCancelable(false);
      progressDialog.show();

      String meetingId = UUID.randomUUID().toString();

      selectedUserIdsList.add(currentUid);
      Meeting meeting = new Meeting(
              currentUid,
              title,
              description,
              startMillis,
              System.currentTimeMillis(),
              selectedUserIdsList,
              meetingId,
              false
      );

      meetingsRef.document(meetingId).set(meeting)
             .addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful()){

                   final Map<String, Object> meetingMap = new HashMap<>();
                   meetingMap.put("Moderator",currentUid);
                   meetingMap.put("groupId",meetingId);

//                   meetingMap.put("Messages",new ArrayList<>());


                   FirebaseDatabase.getInstance().getReference()
                           .child("GroupMessages").child(meetingId)
                           .setValue(meetingMap)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                               progressDialog.dismiss();
                               finish();
                             }
                           });

                 }
               }
             }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {

          Toast.makeText(CreateMeetingActivity.this,
                  "Failed to create meeting please try again!", Toast.LENGTH_SHORT).show();
          progressDialog.dismiss();

        }
      });

    }

  }

  private void createSelectedUserAdapter(){

    usersRef = FirebaseFirestore.getInstance().collection("Users");

    selectedUserRv.setLayoutManager(new LinearLayoutManager(this,
            RecyclerView.VERTICAL, false) {
      @Override
      public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        lp.height = (int) (getWidth() * 0.21);
        return true;
      }
    });

    selectedUsers = new ArrayList<>();

    selectedUsersAdapter = new UsersAdapter(selectedUsers,this, this);
    selectedUserRv.setAdapter(selectedUsersAdapter);

    getUsersPage(true,0,
            selectedUserIdsList.size() > USERS_LIMIT? USERS_LIMIT - 1:
                    selectedUserIdsList.size());

  }

  private void getUsersPage(boolean isInitial,int startAt,int endAt){

    isLoadingUsers = true;
    for(String id:selectedUserIdsList.subList(startAt,endAt)){

      usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {

            selectedUsers.add(documentSnapshot.toObject(UserPreview.class));

        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

          selectedUsersAdapter.notifyItemInserted(selectedUsers.size()-1);

          if(selectedUserIdsList.indexOf(id) == endAt){
            isLoadingUsers = false;
          }

        }
      });


    }

    if(isInitial && (selectedUserIdsList.size() - 10) > USERS_LIMIT){
      selectedUserRv.addOnScrollListener(scrollListener = new scrollListener());
    }else if((selectedUserIdsList.size() - 10) <= USERS_LIMIT){

      selectedUserRv.removeOnScrollListener(scrollListener);

    }

  }

  @Override
  public void onClick(View view) {

    if(view.getId() == R.id.meetingTimeTv){

      pickTime();

    }else if(view.getId() == R.id.usersEd){

      if(selectedUserIdsList!=null && !selectedUserIdsList.isEmpty()){

        startActivityForResult(new Intent(CreateMeetingActivity.this,
                UsersPickerActivity.class).putStringArrayListExtra(
                        "selectedUserIdsList",selectedUserIdsList
        ),3);

      }else{

        startActivityForResult(new Intent(CreateMeetingActivity.this,
                UsersPickerActivity.class),3);

      }

    }else if(view.getId() == R.id.doneButton){

      publishMeeting();

    }else if(view.getId() == R.id.showSelectedUsersTv){

      if(selectedUserIdsList!=null && !selectedUserIdsList.isEmpty()){
        if(selectedUsersAdapter == null){

          setShowSelectedUsersTvDrawable(R.drawable.down_arrow);
          selectedUserRv.setVisibility(View.VISIBLE);
          createSelectedUserAdapter();

        }else if(selectedUserRv.getVisibility() == View.VISIBLE){


          setShowSelectedUsersTvDrawable(R.drawable.down_arrow);
          selectedUserRv.setVisibility(View.GONE);

        }else{

          setShowSelectedUsersTvDrawable(R.drawable.up_arrow_icon);
          selectedUserRv.setVisibility(View.VISIBLE);

        }
      }


    }


  }


  private void setShowSelectedUsersTvDrawable(int drawable){

    showSelectedUsersTv.setCompoundDrawablesWithIntrinsicBounds(
            null,null,
            ResourcesCompat.getDrawable(
                    getResources(),drawable,null
            ),null
    );

  }
  @Override
  public void clickUser(String userId,int position) {



  }

  private class scrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);

      if (!isLoadingUsers && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        getUsersPage(false,selectedUserIdsList.size()-1,
                selectedUserIdsList.size() >= USERS_LIMIT?
                        selectedUserIdsList.size()+USERS_LIMIT:
                  selectedUserIdsList.size());

      }
    }
  }

}