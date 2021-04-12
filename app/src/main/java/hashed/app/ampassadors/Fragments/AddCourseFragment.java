package hashed.app.ampassadors.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Activities.CoursesActivity;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.R;

public class AddCourseFragment extends DialogFragment implements View.OnClickListener
//        ,  HourMinutePickerDialogFragment.OnTimePass
{

  public static final int TUTOR_REQUEST = 1;
  //views
  private EditText courseNameEd,courseDurationEd;
  private TextView courseDateSetterTv,courseTimeSetterTv,addTutorTv;
  private LinearLayout courseTutorNamesLinear;
  private ImageView settingsIv1,settingsIv2;
  private Button coursePublishBtn;
//  private RecyclerView tutorPickerRv;
private boolean important = false;

  //time
  private final Integer[] meetingStartTime = new Integer[5];
//  private int minutes,hours;
  private long scheduleTime;
  private boolean timeWasSelected, dateWasSelected;

  CheckBox checkBox ;

  //pick attendee
//  private UsersAdapter usersAdapter;
//  private ArrayList<UserPreview> users;
//  private TextWatcher textWatcher;
//  private CollectionReference usersRef;
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
//    tutorPickerRv = view.findViewById(R.id.tutorPickerRv);
    courseDateSetterTv = view.findViewById(R.id.courseDateSetterTv);
    courseTimeSetterTv = view.findViewById(R.id.courseTimeSetterTv);
    courseDurationEd = view.findViewById(R.id.courseDurationEd);
    settingsIv1 = view.findViewById(R.id.settingsIv1);
    settingsIv2 = view.findViewById(R.id.settingsIv2);
    coursePublishBtn = view.findViewById(R.id.coursePublishBtn);
    courseTutorNamesLinear = view.findViewById(R.id.courseTutorNamesLinear);
    addTutorTv = view.findViewById(R.id.addTutorTv);
    checkBox = view.findViewById(R.id.checkBox);

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

//    courseTutorNameEd.setOnClickListener(this);
    courseDateSetterTv.setOnClickListener(this);
    courseTimeSetterTv.setOnClickListener(this);
//    courseDurationTv.setOnClickListener(this);
    coursePublishBtn.setOnClickListener(this);
    settingsIv1.setOnClickListener(this);
    settingsIv2.setOnClickListener(this);
    addTutorTv.setOnClickListener(this);
    courseDurationEd.setFilters(new InputFilter[]{new NumberMaxAndMinFilter("1", "200")});

  }

  @Override
  public void onClick(View view) {

    if(view.getId() == coursePublishBtn.getId()){


      List<String> tutorNames = new ArrayList<>();
      for(int i=0;i<courseTutorNamesLinear.getChildCount() - 1;i++){
        Log.d("ttt","id for ed: "+courseTutorNamesLinear.getChildAt(i).getId());
        String tutorName = ((EditText)courseTutorNamesLinear.getChildAt(i)).getText().toString().trim();
        if(!tutorName.isEmpty()){
          tutorNames.add(tutorName);
        }
      }


      if(tutorNames.size() == 0){
        Toast.makeText(requireContext(),
                "Please Enter at least one tutor for this course!", Toast.LENGTH_SHORT).show();
        return;
      }


      final String courseName = courseNameEd.getText().toString().trim();
      final String duration = courseDurationEd.getText().toString().trim();

      if (!courseName.isEmpty() &&  !duration.isEmpty()) {

        if((!timeWasSelected || !dateWasSelected)){
          Toast.makeText(requireContext(), "Please select the course's start time!",
                  Toast.LENGTH_SHORT).show();
          return;
        }

        requestCourseCreation(courseName,tutorNames,Integer.parseInt(duration));

      } else {

        Toast.makeText(requireContext(), "Please fill in the fields!",
                Toast.LENGTH_SHORT).show();
      }

    }
//    else if (view.getId() == courseDurationTv.getId()) {
//
//      final HourMinutePickerDialogFragment hourMinutePicker
//              = new HourMinutePickerDialogFragment(minutes,hours,this);
//
//      hourMinutePicker.show(getChildFragmentManager(), "hourMinutePicker");
//
//    }
    else if (view.getId() == courseDateSetterTv.getId() || view.getId() == settingsIv1.getId()) {

      getMeetingDate();

    } else if (view.getId() == courseTimeSetterTv.getId() || view.getId() == settingsIv2.getId()) {

      getMeetingTime();

    } else if (view.getId() == addTutorTv.getId()) {
      addNewTutorEd();
    }
//    else if(view.getId() == courseTutorNameTv.getId()){
////      if(usersAdapter == null){
////
////      }
////
////      tutorPickerRv.setVisibility(View.VISIBLE);
//
////      Intent intent = new Intent(requireContext(), UserMessageSearchActivity.class);
////      intent.putStringArrayListExtra("selectedUserIds", pickerAdapter.selectedUserIds);
////      startActivityForResult(intent, 3);
////
//    getActivity().startActivityForResult(new Intent(requireContext(), UserMessageSearchActivity.class)
//            .putExtra("isForCourse",true),TUTOR_REQUEST);
//
//    }

  }

  private void addNewTutorEd(){

    if(courseTutorNamesLinear.getChildCount() + 1 == 4){
      addTutorTv.setVisibility(View.GONE);
      addTutorTv.setOnClickListener(null);
    }

    EditText tutorEd = (EditText)
            LayoutInflater.from(requireContext()).inflate(R.layout.course_tutor_ed_item_layout,
                    null);

    tutorEd.setHint("tutor "+(courseTutorNamesLinear.getChildCount()));

    final int marginTop = (int) (10*getResources().getDisplayMetrics().density);

    courseTutorNamesLinear.addView(tutorEd,courseTutorNamesLinear.getChildCount()-1);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tutorEd.getLayoutParams();
    params.setMargins(0, marginTop, 0, 0);
    tutorEd.setLayoutParams(params);
    tutorEd.requestLayout();
  }

  private static class NumberMaxAndMinFilter implements InputFilter {

    private final int min,max;

    public NumberMaxAndMinFilter(int min, int max) {
      this.min = min;
      this.max = max;
    }

    public NumberMaxAndMinFilter(String min, String max) {
      this.min = Integer.parseInt(min);
      this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned,
                               int spanStart, int spanEnd) {
      try {
        // Remove the string out of destination that is to be replaced
        String newVal = spanned.toString().substring(0, spanStart) +
                spanned.toString().substring(spanEnd);
        // Add the new string in
        newVal = newVal.substring(0, spanStart) +
                charSequence.toString() + newVal.substring(spanStart);
        int input = Integer.parseInt(newVal);
        if (isInRange(min, max, input))
          return null;
      } catch (NumberFormatException ignored) { }
      return "";
    }

    private boolean isInRange(int a, int b, int c) {
      return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

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

  private void requestCourseCreation(String title, List<String> tutorNames,int duration) {

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
    courseMap.put("tutorNames", tutorNames);
    courseMap.put("tutorId", pickedTutorId);
    courseMap.put("startTime", scheduleTime);
    courseMap.put("createdTime", System.currentTimeMillis());
    courseMap.put("duration",duration);
    courseMap.put("hasEnded", false);
    courseMap.put("hasStarted", false);
    if (checkBox.isChecked()) {
      important = true;
      courseMap.put("important", important);
    }else {
      courseMap.put("important", false);
    }
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

//  @Override
//  public void passTime(int minutes,int hours) {
//    this.minutes = minutes;
//    this.hours = hours;
////    courseDurationTv.setText(String.format(Locale.getDefault(),"%d:%d", hours, minutes));
//  }
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

//  @Override
//  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//    super.onActivityResult(requestCode, resultCode, data);
//
//    Log.d("ttt",requestCode+ " :requestCode");
////    Log.d("ttt",resultCode+ " :resultCode");
////    if(data!=null){
////      Log.d("ttt",data.toString());
////    }else{
////      Log.d("ttt","data is null");
////    }
//
//    if(requestCode == TUTOR_REQUEST && data!=null && data.hasExtra("userId")){
//
//      pickedTutorId = data.getStringExtra("userId");
//      FirebaseFirestore.getInstance().collection("Users")
//              .document(pickedTutorId).get()
//              .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//      @Override
//      public void onSuccess(DocumentSnapshot snapshot) {
//       if(snapshot.exists()){
//         courseTutorNameEd.setText(snapshot.getString("username"));
//       }
//      }
//    });
//
//    }
//
//  }

  public void backPressing(){

    if(!courseNameEd.getText().toString().trim().isEmpty()
            || !courseDurationEd.getText().toString().trim().isEmpty()
            || timeWasSelected || dateWasSelected){

      final AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
      alert.setTitle("Do you want to leave without creating this course?");
      alert.setMessage("Leaving will discard this course");

      alert.setPositiveButton("Leave", (dialogInterface, i) -> {
          dialogInterface.dismiss();
          dismiss();
      });

      alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
      alert.create().show();

    } else {
     dismiss();
     requireActivity().onBackPressed();
    }

  }

}