package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;

public class CreateMeetingActivity extends AppCompatActivity {

  private final DateFormat todayYearMonthDayFormat
          = new SimpleDateFormat("yyyy MMMM dd", Locale.getDefault());

  private final CollectionReference meetingsRef =
          FirebaseFirestore.getInstance().collection("Meetings");

  private String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

  ArrayList<String> selectedUserIdsList;

  TextView meetingTimeTv;
  long startMillis;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_meeting);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v->finish());

    meetingTimeTv = findViewById(R.id.meetingTimeTv);
    EditText titleEd = findViewById(R.id.titleEd);
    EditText descreptionEd = findViewById(R.id.descreptionEd);

    TextView usersEd = findViewById(R.id.usersEd);

    usersEd.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivityForResult(new Intent(CreateMeetingActivity.this,
                UsersPickerActivity.class),3);

      }
    });

    meetingTimeTv.setOnClickListener(v-> pickTime());

    Button doneButton = findViewById(R.id.doneButton);

    doneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        String title = titleEd.getText().toString();
        String description = descreptionEd.getText().toString();

        if(!title.isEmpty() && !description.isEmpty() && startMillis!=0 &&
                !selectedUserIdsList.isEmpty()){

          ProgressDialog progressDialog = new ProgressDialog(CreateMeetingActivity.this);
          progressDialog.setTitle("Creating Meeting");
          progressDialog.setCancelable(false);
          progressDialog.show();

          String randomId = UUID.randomUUID().toString();

          Meeting meeting = new Meeting(
                  currentUid,
                  title,
                  description,
                  startMillis,
                  System.currentTimeMillis(),
                  selectedUserIdsList,
                  randomId
          );

          meetingsRef.document(randomId).set(meeting)
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                      progressDialog.dismiss();
                      finish();

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
    });

  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if(requestCode == 3 && data!=null){

      if(data.hasExtra("selectedUserIds")){
        selectedUserIdsList = data.getStringArrayListExtra("selectedUserIds");
        Log.d("ttt","selectedUserIdsList: "+selectedUserIdsList.size());
      }

    }
  }


  void pickTime() {

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

}