package hashed.app.ampassadors.Fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Response;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Activities.CoursesActivity;
import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.UserMessageSearchActivity;
import hashed.app.ampassadors.Activities.UserSearchActivity;
import hashed.app.ampassadors.Activities.UsersPickerActivity;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;
import hashed.app.ampassadors.Utils.ZoomUtil;

public class AddCourseFragment extends DialogFragment implements View.OnClickListener,
        HourMinutePickerDialogFragment.OnTimePass{

  public static final int TUTOR_REQUEST = 1;
  //views
  private EditText courseNameEd;
  private TextView courseDateSetterTv,courseTimeSetterTv,courseDurationTv,courseTutorNameTv;
  private ImageView settingsIv1,settingsIv2;
  private Button coursePublishBtn;
  private RecyclerView tutorPickerRv;

  //time
  private final Integer[] meetingStartTime = new Integer[5];
  private int minutes,hours;
  private long scheduleTime;
  private boolean timeWasSelected, dateWasSelected;


  //pick attendee
  private UsersAdapter usersAdapter;
  private ArrayList<UserPreview> users;
  private TextWatcher textWatcher;
  private CollectionReference usersRef;
  private String pickedTutorId;
  public AddCourseFragment() {
  }

    @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

//    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    View view =  inflater.inflate(R.layout.fragment_add_course, container, false);

//    setStyle(DialogFragment.STYLE_NORMAL, R.style.ThemeOverlay_Demo_BottomSheetDialog);


    if (getDialog() != null && getDialog().getWindow() != null) {
      getDialog().getWindow().setBackgroundDrawableResource(R.drawable.course_dialog_back);
//      getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//      getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    courseNameEd = view.findViewById(R.id.courseNameEd);
    courseTutorNameTv = view.findViewById(R.id.courseTutorNameTv);
    tutorPickerRv = view.findViewById(R.id.tutorPickerRv);
    courseDateSetterTv = view.findViewById(R.id.courseDateSetterTv);
    courseTimeSetterTv = view.findViewById(R.id.courseTimeSetterTv);
    courseDurationTv = view.findViewById(R.id.courseDurationTv);
    settingsIv1 = view.findViewById(R.id.settingsIv1);
    settingsIv2 = view.findViewById(R.id.settingsIv2);
    coursePublishBtn = view.findViewById(R.id.coursePublishBtn);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    addClickListeners();

//    setUpTutorRecycler();

  }

  private void addClickListeners(){

//    courseTutorNameEd.setOnTouchListener(new View.OnTouchListener() {
//      @Override
//      public boolean onTouch(View v, MotionEvent event) {
//        if(MotionEvent.ACTION_UP == event.getAction()) {
//          mQuaternion_1.setText("" + mQ1);
//        }
//
//        return true; // return is important...
//      }
//    });

    courseTutorNameTv.setOnClickListener(this);
    courseDateSetterTv.setOnClickListener(this);
    courseTimeSetterTv.setOnClickListener(this);
    courseDurationTv.setOnClickListener(this);
    coursePublishBtn.setOnClickListener(this);
    settingsIv1.setOnClickListener(this);
    settingsIv2.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {

    if(view.getId() == coursePublishBtn.getId()){

      if(pickedTutorId == null){
        Toast.makeText(requireContext(),
                "Please pick a tutor for this course!", Toast.LENGTH_SHORT).show();
        return;
      }

      final String name = courseNameEd.getText().toString().trim();
      final String tutor = courseTutorNameTv.getText().toString().trim();
      final int duration = minutes + (hours*60);

      if (!name.isEmpty() && !tutor.isEmpty() && duration > 0) {

        if((!timeWasSelected || !dateWasSelected)){
          Toast.makeText(requireContext(), "Please select the course's start time!",
                  Toast.LENGTH_SHORT).show();
          return;
        }

        requestCourseCreation(name,tutor,duration);

      } else {

        Toast.makeText(requireContext(), "Please fill in the fields!",
                Toast.LENGTH_SHORT).show();
      }

    }else if (view.getId() == courseDurationTv.getId()) {

      final HourMinutePickerDialogFragment hourMinutePicker
              = new HourMinutePickerDialogFragment(minutes,hours,this);

      hourMinutePicker.show(getChildFragmentManager(), "hourMinutePicker");

    } else if (view.getId() == courseDateSetterTv.getId() || view.getId() == settingsIv1.getId()) {

      getMeetingDate();

    } else if (view.getId() == courseTimeSetterTv.getId() || view.getId() == settingsIv2.getId()) {

      getMeetingTime();

    }else if(view.getId() == courseTutorNameTv.getId()){
//      if(usersAdapter == null){
//
//      }
//
//      tutorPickerRv.setVisibility(View.VISIBLE);

//      Intent intent = new Intent(requireContext(), UserMessageSearchActivity.class);
//      intent.putStringArrayListExtra("selectedUserIds", pickerAdapter.selectedUserIds);
//      startActivityForResult(intent, 3);
//
    getActivity().startActivityForResult(new Intent(requireContext(), UserMessageSearchActivity.class)
            .putExtra("isForCourse",true),TUTOR_REQUEST);

    }

  }


//  private void setUpTutorRecycler(){
//
////    FirebaseFirestore.getInstance().collection("Users")
////            .limit(100).get().
//    usersRef = FirebaseFirestore.getInstance().collection("Users");
//
//    users = new ArrayList<>();
//    usersAdapter = new UsersAdapter(users,R.layout.user_item_layout,this);
//    tutorPickerRv.setAdapter(usersAdapter);
//
//    final long[] lastTextChange = new long[1];
//
////    Handler handler = new Handler();
////    Runnable target;
////    Thread currentThread = new Thread(new );
//
//    courseTutorNameEd.addTextChangedListener(textWatcher = new TextWatcher() {
//      @Override
//      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//      }
//
//      @Override
//      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//        if(!users.isEmpty()){
//          users.clear();
//          usersAdapter.notifyDataSetChanged();
//          tutorPickerRv.setVisibility(View.GONE);
//        }
//
//        searchForUsers(charSequence.toString());
//      }
//
//      @Override
//      public void afterTextChanged(Editable editable) {
//        Log.d("ttt","afterTextChanged");
////        if(lastTextChange[0]!=0){
////          if(System.currentTimeMillis() - lastTextChange[0] > 1500){
////            Log.d("ttt","will search now");
//////            searchForUsers(courseTutorNameEd.getText().toString());
////          }
////        }
////        lastTextChange[0] = System.currentTimeMillis();
//      }
//    });
//
//  }

  private void searchForUsers(String name){

    usersRef.whereEqualTo("username",name)
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {
          if(!snapshots.isEmpty()){

            users.addAll(snapshots.toObjects(UserPreview.class));
          }
      }
    }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if(task.isSuccessful() && !users.isEmpty()){
          tutorPickerRv.setVisibility(View.VISIBLE);
          usersAdapter.notifyDataSetChanged();
        }
      }
    });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
//    if(courseTutorNameEd!=null && textWatcher!=null){
//      courseTutorNameEd.removeTextChangedListener(textWatcher);
//    }
  }

  private void getMeetingDate() {

    final Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());

    DatePickerDialog StartTime = new DatePickerDialog(getContext(),
            (view, year, monthOfYear, dayOfMonth) -> {

              if (mcurrentDate.get(Calendar.YEAR) > year ||

                      (mcurrentDate.get(Calendar.YEAR) == year &&
                              mcurrentDate.get(Calendar.MONTH) > monthOfYear) ||

                      (mcurrentDate.get(Calendar.YEAR) == year &&
                              mcurrentDate.get(Calendar.MONTH) == monthOfYear &&
                              mcurrentDate.get(Calendar.DAY_OF_MONTH) > dayOfMonth)
              ) {

                String text;
                if ((timeWasSelected &&
                        mcurrentDate.get(Calendar.YEAR) == year &&
                        mcurrentDate.get(Calendar.MONTH) == monthOfYear &&
                        mcurrentDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth &&
                        (meetingStartTime[3] < mcurrentDate.get(Calendar.HOUR))
                        && meetingStartTime[4] < mcurrentDate.get(Calendar.MINUTE))) {

                  text = "Course time cannot be scheduled to this time!" +
                          "Please Selected a different time day or change the day";

                } else {
                  text = "Course time cannot be scheduled to this time!";
                }
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
              } else {

                dateWasSelected = true;
                meetingStartTime[0] = year;
                meetingStartTime[1] = monthOfYear;
                meetingStartTime[2] = dayOfMonth;
                courseDateSetterTv.setText(year + "/" + (monthOfYear + 1) + "/" + dayOfMonth);

                if (timeWasSelected) {
                  calculateTime();
                }
              }

            }, mcurrentDate.get(Calendar.YEAR), mcurrentDate.get(Calendar.MONTH),
            mcurrentDate.get(Calendar.DAY_OF_MONTH));
    StartTime.show();

  }

  private void getMeetingTime() {

    final Calendar mcurrentTime = Calendar.getInstance(Locale.getDefault());

    final TimePickerDialog mTimePicker = new TimePickerDialog(
            getContext(), (timePicker, selectedHour, selectedMinute) -> {

      final Calendar calendar = Calendar.getInstance(Locale.getDefault());
      calendar.setTime(new Date(System.currentTimeMillis()));

      calendar.set(Calendar.HOUR, selectedHour);
      calendar.set(Calendar.MINUTE, selectedMinute);


      if (calendar.getTimeInMillis() >= calendar.getTimeInMillis()) {

        timeWasSelected = true;
        meetingStartTime[3] = selectedHour;
        meetingStartTime[4] = selectedMinute;
        courseTimeSetterTv.setText(selectedHour + ":" + selectedMinute);

        if (dateWasSelected) {
          calculateTime();
        }
      } else {
        Toast.makeText(getContext(),
                "Course time can't be at selected time!", Toast.LENGTH_SHORT).show();
      }

    }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
            true);
    mTimePicker.setTitle("Select Course Time");
    mTimePicker.show();

  }

  private void calculateTime() {

    final Calendar calendar = Calendar.getInstance(Locale.getDefault());
    calendar.set(meetingStartTime[0], meetingStartTime[1], meetingStartTime[2],
            meetingStartTime[3], meetingStartTime[4]);

    scheduleTime = calendar.getTimeInMillis();
    Log.d("ttt", "scheduleTime: " + scheduleTime);

  }

  private void requestCourseCreation(String title, String tutor,int duration) {

    final ProgressDialog courseDialog = new ProgressDialog(getContext());
    courseDialog.setTitle("Creating course!");
    courseDialog.setCancelable(false);
    courseDialog.show();

    final String courseId = UUID.randomUUID().toString();

    final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final Map<String, Object> courseMap = new HashMap<>();
    courseMap.put("courseId", courseId);
    courseMap.put("creatorId", currentUid);
    courseMap.put("title", title);
    courseMap.put("tutorName", tutor);
    courseMap.put("tutorId", pickedTutorId);
    courseMap.put("startTime", scheduleTime);
    courseMap.put("createdTime", System.currentTimeMillis());
    courseMap.put("duration",duration);
    courseMap.put("hasEnded", false);

    final DocumentReference courseRef = FirebaseFirestore.getInstance()
            .collection("Courses").document(courseId);
    courseRef.set(courseMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

                final Map<String, Object> meetingMap = new HashMap<>();
                meetingMap.put("Moderator", currentUid);
                meetingMap.put("groupId", courseId);

                FirebaseDatabase.getInstance().getReference()
                        .child("GroupMessages").child(courseId)
                        .setValue(meetingMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {

                            courseDialog.dismiss();
                            dismiss();

                            Toast.makeText(requireContext(),
                                    "Course created successfully!",
                                    Toast.LENGTH_SHORT).show();
                            ((CoursesActivity)requireActivity())
                                    .addCourseToList(new Course(courseMap));




                          }
                        }).addOnFailureListener(new OnFailureListener() {
                  @Override
                  public void onFailure(@NonNull Exception e) {

                    courseRef.delete();

                    courseDialog.dismiss();

                    Toast.makeText(requireContext(),
                            "Failed to create course! Please try again",
                            Toast.LENGTH_SHORT).show();

                  }
                });

              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        courseDialog.dismiss();

        Toast.makeText(requireContext(),
                "Failed to create course! Please try again", Toast.LENGTH_SHORT).show();

      }
    });

  }

  @Override
  public void passTime(int minutes,int hours) {
    this.minutes = minutes;
    this.hours = hours;
    courseDurationTv.setText(String.format(Locale.getDefault(),"%d:%d", hours, minutes));
  }
//
//  @Override
//  public void clickUser(String userId) {
//    users.clear();
//    usersAdapter.notifyDataSetChanged();
//    tutorPickerRv.setVisibility(View.GONE);
//      pickedTutorId = userId;
//      usersRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//      @Override
//      public void onSuccess(DocumentSnapshot snapshot) {
//       if(snapshot.exists()){
////         final UserPreview userPreview = snapshot.toObject(UserPreview.class);
//         courseTutorNameEd.setText(snapshot.getString("username"));
//       }
//      }
//    });
//
//  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("ttt",requestCode+ " :requestCode");
//    Log.d("ttt",resultCode+ " :resultCode");
//    if(data!=null){
//      Log.d("ttt",data.toString());
//    }else{
//      Log.d("ttt","data is null");
//    }

    if(requestCode == TUTOR_REQUEST && data!=null && data.hasExtra("userId")){

      pickedTutorId = data.getStringExtra("userId");
      FirebaseFirestore.getInstance().collection("Users")
              .document(pickedTutorId).get()
              .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot snapshot) {
       if(snapshot.exists()){
         courseTutorNameTv.setText(snapshot.getString("username"));
       }
      }
    });

    }

  }
}