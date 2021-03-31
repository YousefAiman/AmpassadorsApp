package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class CreateMeetingActivity extends AppCompatActivity implements View.OnClickListener {


  private final Integer[] meetingStartTime = new Integer[5];
  //time
  private DateFormat todayYearMonthDayFormat;
  private long scheduleTime;
  private boolean timeWasSelected, dateWasSelected;


  //database
  private CollectionReference meetingsRef;
  private CollectionReference usersRef;
  private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap;

  //seleceted users
  private String currentUid;
  private ArrayList<String> selectedUserIdsList;
  private ArrayList<UserPreview> selectedUsers;

  //views
  private Toolbar toolbar;
  private CircleImageView groupIv;
  private EditText groupNameEd;
  private FloatingActionButton doneFloatingBtn;
  private FloatingActionButton editUsersFloatingBtn;
  private TextView contributorsTv, dateSetterTv, timeSetterTv;
  private RecyclerView usersPickedRv;

  private Uri imageUri;
  private UsersAdapter selectedUsersAdapter;
  private String meetingImageUrl;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_meeting);

    initViews();

    initValues();

    setViewClickers();

    selectedUserIdsList = getIntent().getStringArrayListExtra("selectedUserIdsList");

//    if (getIntent().hasExtra("meetingBundle")) {
//
//      final Bundle meetingBundle = getIntent().getBundleExtra("meetingBundle");
//      if (meetingBundle.containsKey("groupName")) {
//        groupNameEd.setText(meetingBundle.getString("meetingBundle"));
//      }
//      if (meetingBundle.containsKey("imageUri")) {
//        imageUri = Uri.parse(meetingBundle.getString("imageUri"));
//        Picasso.get().load(imageUri).fit().centerCrop().into(groupIv);
//      }
//    }

    updateContributorsCount();

    createSelectedUserAdapter();

  }

  private void initViews() {

//    meetingTimeTv = findViewById(R.id.meetingTimeTv);
    toolbar = findViewById(R.id.toolbar);
    groupIv = findViewById(R.id.groupIv);
    groupNameEd = findViewById(R.id.groupNameEd);
    doneFloatingBtn = findViewById(R.id.doneFloatingBtn);
    editUsersFloatingBtn = findViewById(R.id.editUsersFloatingBtn);
    contributorsTv = findViewById(R.id.contributorsTv);
    usersPickedRv = findViewById(R.id.usersPickedRv);
    dateSetterTv = findViewById(R.id.dateSetterTv);
    timeSetterTv = findViewById(R.id.timeSetterTv);

  }

  private void initValues() {

    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());

    meetingsRef = FirebaseFirestore.getInstance().collection("Meetings");

  }

  private void setViewClickers() {

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        onBackPressed();

      }
    });

    doneFloatingBtn.setOnClickListener(this);
    editUsersFloatingBtn.setOnClickListener(this);
    groupIv.setOnClickListener(this);
    dateSetterTv.setOnClickListener(this);
    timeSetterTv.setOnClickListener(this);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 3 && data != null && data.hasExtra("selectedUserIdsList")) {

      selectedUserIdsList = data.getStringArrayListExtra("selectedUserIdsList");
      updateContributorsCount();
      selectedUsers.clear();
      selectedUsersAdapter.notifyDataSetChanged();

      getUsers();
      Log.d("ttt", "selectedUserIdsList: " + selectedUserIdsList.size());

    } else if (resultCode == RESULT_OK && requestCode == Files.PICK_IMAGE && data != null) {

      imageUri = data.getData();
      Picasso.get().load(imageUri).fit().centerCrop().into(groupIv);

    }
  }

//  private void pickTime() {
//
//    Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());
//    DatePickerDialog StartTime = new DatePickerDialog(this,
//            new DatePickerDialog.OnDateSetListener() {
//      String date;
//
//      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//        Calendar newDate = Calendar.getInstance(Locale.getDefault());
//        newDate.set(year, monthOfYear, dayOfMonth);
//        date = todayYearMonthDayFormat.format(newDate.getTime());
//        Calendar mcurrentTime = Calendar.getInstance(Locale.getDefault());
//        TimePickerDialog mTimePicker = new TimePickerDialog(
//                CreateMeetingActivity.this, (timePicker, selectedHour, selectedMinute) -> {
//          final Calendar calendar = Calendar.getInstance(Locale.getDefault());
//          final long currentTime = calendar.getTimeInMillis();
//          calendar.set(Calendar.YEAR, year);
//          calendar.set(Calendar.MONTH, monthOfYear);
//          calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//          calendar.set(Calendar.HOUR, selectedHour);
////          calendar.set(Calendar.AM, selectedHour);
//          calendar.set(Calendar.MINUTE, selectedMinute);
//          calendar.set(Calendar.SECOND, 0);
//          calendar.set(Calendar.MILLISECOND, 0);
//
//            if (calendar.getTimeInMillis() >= currentTime) {
//              startMillis = calendar.getTimeInMillis();
//              date = date.concat(" " + selectedHour + ":" + selectedMinute);
//              meetingTimeTv.setText(date);
//            } else {
//              Toast.makeText(CreateMeetingActivity.this,
//                      "Meeting time can't be at selected time!", Toast.LENGTH_SHORT).show();
//            }
//
//        }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
//                true);
//        mTimePicker.setTitle("Select Meeting Time");
//        mTimePicker.show();
//      }
//
//    }, mcurrentDate.get(Calendar.YEAR), mcurrentDate.get(Calendar.MONTH),
//            mcurrentDate.get(Calendar.DAY_OF_MONTH));
//    StartTime.show();
//
//  }
//

  private void createSelectedUserAdapter() {

    usersRef = FirebaseFirestore.getInstance().collection("Users");

    selectedUsers = new ArrayList<>();

    selectedUsersAdapter = new UsersAdapter(selectedUsers,
            R.layout.user_picked_preview_item_layout);

    usersPickedRv.setAdapter(selectedUsersAdapter);

    getUsers();

//    getUsersPage(true,0,
//            selectedUserIdsList.size() > USERS_LIMIT? USERS_LIMIT - 1:
//                    selectedUserIdsList.size());

  }

  private void getUsers() {

//    double loopCount = Math.ceil(selectedUserIdsList.size()/10.0);
//
//    for(int i=0;i<loopCount)

    for (String id : selectedUserIdsList) {
//      usersRef.whereArrayContainsAny(id,selectedUserIdsList)

      usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          selectedUsers.add(documentSnapshot.toObject(UserPreview.class));
        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if (task.isSuccessful()) {
            selectedUsersAdapter.notifyItemInserted(selectedUsers.size() - 1);
          }
        }
      });
    }
  }

  private void updateContributorsCount() {
    contributorsTv.setText(getResources().getString(R.string.contributors) + ": " + selectedUserIdsList.size());
  }


  private void publishMeeting() {

    final String name = groupNameEd.getText().toString().trim();

    if (!name.isEmpty() && scheduleTime != 0 && selectedUserIdsList != null &&
            !selectedUserIdsList.isEmpty() && selectedUserIdsList.size() > 1) {

      ProgressDialog progressDialog = new ProgressDialog(CreateMeetingActivity.this);
      progressDialog.setTitle(getString(R.string.Publish_Meeting));
      progressDialog.setCancelable(false);
      progressDialog.show();

      String meetingId = UUID.randomUUID().toString();
      selectedUserIdsList.add(currentUid);

      final Map<String, Object> meetingMap = new HashMap<>();
      meetingMap.put("creatorId", currentUid);
      meetingMap.put("title", name);
      meetingMap.put("startTime", scheduleTime);
      meetingMap.put("createdTime", System.currentTimeMillis());
      meetingMap.put("members", selectedUserIdsList);
      meetingMap.put("meetingId", meetingId);
      meetingMap.put("hasEnded", false);


      if (imageUri != null) {

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("Meetings-Images/").child(UUID.randomUUID().toString() + "-" +
                        System.currentTimeMillis());

        final UploadTask uploadTask = reference.putFile(imageUri);

        uploadTaskMap = new HashMap<>();

        StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                  uploadTaskMap.remove(uploadTask);
                  reference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    meetingImageUrl = uri1.toString();
                    meetingMap.put("imageUrl", meetingImageUrl);
//                    meeting.setImageUrl(meetingImageUrl);

                    createMeeting(meetingMap, meetingId, name, progressDialog);
                  });
                }).addOnCompleteListener(task -> new File(imageUri.getPath()).delete());

        uploadTaskMap.put(uploadTask, onSuccessListener);

      } else {
        createMeeting(meetingMap, meetingId, name, progressDialog);
      }


    }

  }


  private void createMeeting(Map<String, Object> meeting, String meetingId, String name, ProgressDialog progressDialog) {
    meetingsRef.document(meetingId).set(meeting)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                  final Map<String, Object> meetingMap = new HashMap<>();
                  meetingMap.put("Moderator", currentUid);
                  meetingMap.put("groupId", meetingId);

//                   meetingMap.put("Messages",new ArrayList<>());


                  FirebaseDatabase.getInstance().getReference()
                          .child("GroupMessages").child(meetingId)
                          .setValue(meetingMap)
                          .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                              final Data data = new Data(
                                      currentUid,
                                      "Meeting starts at: " +
                                              TimeFormatter.formatTime(scheduleTime),
                                      "Meeting about: " + name,
                                      meetingImageUrl,
                                      "meeting",
                                      "meetingCreated",
                                      meetingId);


                              selectedUserIdsList.remove(currentUid);

                              for (String userId : selectedUserIdsList) {
                                CloudMessagingNotificationsSender.sendNotification(userId, data);
                                FirestoreNotificationSender.sendFirestoreNotification(
                                        userId, "meetingCreated",
                                        getResources().getString(R.string.invited_meeting),
                                        name,
                                        meetingId
                                );
                              }

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
                R.string.Error_meassage_MeetingFiald, Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();

      }
    });

  }

  @Override
  public void onClick(View view) {

    if (view.getId() == R.id.doneFloatingBtn) {
      publishMeeting();
    }else  if (view.getId() == R.id.editUsersFloatingBtn) {
      editContributors();
    } else if (view.getId() == R.id.groupIv) {

      Files.startImageFetchIntent(this);

    } else if (view.getId() == R.id.dateSetterTv || view.getId() == R.id.settingsIv1) {

      getMeetingDate();

    } else if (view.getId() == R.id.timeSetterTv || view.getId() == R.id.settingsIv2) {

      getMeetingTime();

    }
  }


  private void getMeetingDate() {

    Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());


    DatePickerDialog StartTime = new DatePickerDialog(this,
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

                  text = "Meeting time cannot be scheduled to this time!" +
                          "Please Selected a different time day or change the day";

                } else {
                  text = "Meeting time cannot be scheduled to this time!";
                }
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
              } else {

                dateWasSelected = true;
                meetingStartTime[0] = year;
                meetingStartTime[1] = monthOfYear;
                meetingStartTime[2] = dayOfMonth;
                dateSetterTv.setText(year + "/" + monthOfYear + "/" + dayOfMonth);


                if (timeWasSelected) {
                  calculateTime();
                }
              }

            }, mcurrentDate.get(Calendar.YEAR), mcurrentDate.get(Calendar.MONTH),
            mcurrentDate.get(Calendar.DAY_OF_MONTH));
    StartTime.show();

  }

  private void getMeetingTime() {

    Calendar mcurrentTime = Calendar.getInstance(Locale.getDefault());
    TimePickerDialog mTimePicker = new TimePickerDialog(
            this, (timePicker, selectedHour, selectedMinute) -> {

      final Calendar calendar = Calendar.getInstance(Locale.getDefault());
      calendar.setTime(new Date(System.currentTimeMillis()));

      calendar.set(Calendar.HOUR, selectedHour);
      calendar.set(Calendar.MINUTE, selectedMinute);


      if (calendar.getTimeInMillis() >= calendar.getTimeInMillis()) {

        timeWasSelected = true;
        meetingStartTime[3] = selectedHour;
        meetingStartTime[4] = selectedMinute;
        timeSetterTv.setText(selectedHour + ":" + selectedMinute);

        if (dateWasSelected) {
          calculateTime();
        }
      } else {
        Toast.makeText(this,
                R.string.Meetings_Message_cant_Slected_item, Toast.LENGTH_SHORT).show();
      }

    }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
            true);
    mTimePicker.setTitle(R.string.meeting_title);
    mTimePicker.show();

  }

  private void calculateTime() {

    final Calendar calendar = Calendar.getInstance(Locale.getDefault());
    calendar.set(meetingStartTime[0], meetingStartTime[1], meetingStartTime[2],
            meetingStartTime[3], meetingStartTime[4]);

    scheduleTime = calendar.getTimeInMillis();
    Log.d("ttt", "scheduleTime: " + scheduleTime);

  }


  private void cancelUploadTasks() {

    final UploadTask uploadTask = uploadTaskMap.keySet().iterator().next();

    uploadTask.removeOnSuccessListener(
            (OnSuccessListener<? super UploadTask.TaskSnapshot>) uploadTaskMap.get(uploadTask));

    uploadTask.addOnSuccessListener(taskSnapshot ->
            uploadTask.getSnapshot().getStorage().delete()
                    .addOnSuccessListener(v -> Log.d("ttt", "ref delete sucess")).
                    addOnFailureListener(e -> Log.d("ttt", "ref delete failed: " +
                            e.getMessage())));

  }


  @Override
  public void onBackPressed() {
    super.onBackPressed();

    if(!groupNameEd.getText().toString().trim().isEmpty()
    || !selectedUserIdsList.isEmpty() || timeWasSelected || dateWasSelected
    || imageUri!=null){

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Do you want to leave without creating this meeting?");
      alert.setMessage("Leaving will discard this meeting");

      alert.setPositiveButton("Leave", (dialogInterface, i) -> {
        if (uploadTaskMap != null && !uploadTaskMap.isEmpty()) {
          cancelUploadTasks();
          dialogInterface.dismiss();
          finish();
        }});

        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alert.create().show();

    } else {
      super.onBackPressed();
    }

  }

  private void editContributors(){

   Intent intent = new Intent(CreateMeetingActivity.this,
                UsersPickerActivity.class);
        intent.putExtra("previousSelectedUserIdsList", selectedUserIdsList);
//        final String name = groupNameEd.getText().toString().trim();
//        if (imageUri != null || name.isEmpty()) {
//
//          final Bundle bundle = new Bundle();
//          if (imageUri != null) {
//            bundle.putString("imageUri", imageUri.toString());
//          }
//          if (!name.isEmpty()) {
//            bundle.putString("groupName", name);
//          }
//
//          intent.putExtra("meetingBundle", bundle);
//        }
        startActivityForResult(intent, 3);
  }
}