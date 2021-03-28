package hashed.app.ampassadors.Fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.ZoomUtil;


public class ZoomMeetingCreationFragment extends Fragment implements View.OnClickListener,
        MinutePickerDialogFragment.OnTimePass, CompoundButton.OnCheckedChangeListener {

  private final Integer[] meetingStartTime = new Integer[5];
  private EditText topicEd, descriptionEd, messagingPickerEd;
  private CheckBox createNowCheckbox, scheduleCheckBox;
  private TextView minutesTv, dateTv, timeTv, dateSetterTv, timeSetterTv;
  private ImageView messagingPickerSendIv, settingsIv1, settingsIv2;
  private ZoomMeeting zoomMeeting;
  private int duration;
  private int meetingType = 1;
  private long scheduleTime;
  private boolean timeWasSelected, dateWasSelected;

  public ZoomMeetingCreationFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_zoom_meeting_creation, container,
            false);

    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);

    fullScreenToolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

    topicEd = view.findViewById(R.id.topicEd);
    descriptionEd = view.findViewById(R.id.descriptionEd);
    messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);
    minutesTv = view.findViewById(R.id.minutesTv);
    createNowCheckbox = view.findViewById(R.id.createNowCheckbox);
    scheduleCheckBox = view.findViewById(R.id.scheduleCheckBox);
    dateTv = view.findViewById(R.id.dateTv);
    timeTv = view.findViewById(R.id.timeTv);
    dateSetterTv = view.findViewById(R.id.dateSetterTv);
    timeSetterTv = view.findViewById(R.id.timeSetterTv);
    settingsIv1 = view.findViewById(R.id.settingsIv1);
    settingsIv2 = view.findViewById(R.id.settingsIv2);

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initializeClickers();

  }


  private void initializeClickers() {


    messagingPickerSendIv.setOnClickListener(this);
    minutesTv.setOnClickListener(this);
    dateSetterTv.setOnClickListener(this);
    timeSetterTv.setOnClickListener(this);
    settingsIv1.setOnClickListener(this);
    settingsIv2.setOnClickListener(this);


    createNowCheckbox.setOnCheckedChangeListener(this);
    scheduleCheckBox.setOnCheckedChangeListener(this);


  }


  @Override
  public void onClick(View view) {

//    requestMeetingCreation();

    if (view.getId() == R.id.messagingPickerSendIv) {
      final String topic = topicEd.getText().toString();
      final String description = descriptionEd.getText().toString();
      Log.d("ttt","meeting type: "+meetingType );
      if (!messagingPickerEd.getText().toString().isEmpty() &&
              !topic.isEmpty() && !description.isEmpty() && duration > 0
              && meetingType != 0) {


        if (meetingType == 2 && (!timeWasSelected || !dateWasSelected)) {
          Toast.makeText(requireContext(), "Please fill in the fields!",
                  Toast.LENGTH_SHORT).show();
          return;
        }

        requestMeetingCreation(topic,description);

      } else {

        Toast.makeText(requireContext(), "Please fill in the fields!",
                Toast.LENGTH_SHORT).show();
      }

    } else if (view.getId() == R.id.minutesTv) {

      MinutePickerDialogFragment minutePickerDialogFragment;

      if (duration > 0) {
        minutePickerDialogFragment = new MinutePickerDialogFragment(duration, this);
      } else {
        minutePickerDialogFragment = new MinutePickerDialogFragment(this);
      }

      minutePickerDialogFragment.show(getChildFragmentManager(), "minutePicker");

    } else if (view.getId() == R.id.dateSetterTv || view.getId() == R.id.settingsIv1) {

      getMeetingDate();

    } else if (view.getId() == R.id.timeSetterTv || view.getId() == R.id.settingsIv2) {

      getMeetingTime();

    }


  }

  private void requestMeetingCreation(String topic, String description) {


//      createMeetBtn.setClickable(true);
    final ProgressDialog zoomDialog = new ProgressDialog(getContext());
    zoomDialog.setTitle("Creating meeting!");
    zoomDialog.setCancelable(false);
    zoomDialog.show();

    final Response.Listener<JSONObject> responseListener = response -> {
      try {

        zoomMeeting = new ZoomMeeting(
                response.getString("id"),
                response.getString("host_id"),
                response.getString("host_email"),
                response.getString("topic"),
                meetingType,
                duration,
                response.getString("status"),
                response.getString("start_url"),
                response.getString("join_url"));

        if (meetingType == 2) {

          zoomMeeting.setStartTime(scheduleTime);

        }else{

          zoomMeeting.setStartTime(System.currentTimeMillis());

        }


        Log.d("ttt", "zoomMeeting: " + zoomMeeting.toString());

        Log.d("ttt", "zoomMeeting: " + zoomMeeting.toString());


        zoomDialog.dismiss();

        ((GroupMessagingActivity) requireActivity()).sendZoomMessage(
                messagingPickerEd.getText().toString(), zoomMeeting);

        requireActivity().onBackPressed();
      } catch (JSONException e) {

        zoomDialog.dismiss();
//          createMeetBtn.setClickable(true);
        Toast.makeText(getContext(), "Meeting creation failed! Please try again",
                Toast.LENGTH_SHORT).show();
        Log.d("ttt", "JSONException: " + e.getMessage());
        e.printStackTrace();
      }
    };

    ZoomUtil.createMeeting(topic,duration,description,responseListener,
            zoomDialog,requireContext());

  }


  @Override
  public void passTime(int duration) {
    this.duration = duration;

    minutesTv.setText(String.valueOf(duration));

  }

  @Override
  public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    if (compoundButton.getId() == R.id.createNowCheckbox) {

      meetingType = 1;

      if (b && scheduleCheckBox.isChecked()) {
        scheduleCheckBox.setChecked(false);
        setScheduleViewsVisibility(false);
      }

    } else if (compoundButton.getId() == R.id.scheduleCheckBox) {


      meetingType = 2;

      setScheduleViewsVisibility(b);

      if (b && createNowCheckbox.isChecked()) {
        createNowCheckbox.setChecked(false);
      }

    }
  }

  private void setScheduleViewsVisibility(boolean visibility) {

    final int color = ResourcesCompat.getColor(getResources(),
            visibility ? R.color.black : R.color.black_fully_transparent
            , null);

    dateTv.setTextColor(color);
    timeTv.setTextColor(color);
    dateSetterTv.setTextColor(color);
    timeSetterTv.setTextColor(color);

    DrawableCompat.setTint(
            DrawableCompat.wrap(settingsIv1.getDrawable()),
            color
    );

    DrawableCompat.setTint(
            DrawableCompat.wrap(settingsIv2.getDrawable()),
            color
    );

    dateSetterTv.setClickable(visibility);
    timeSetterTv.setClickable(visibility);
    settingsIv1.setClickable(visibility);
    settingsIv2.setClickable(visibility);
  }

  private void getMeetingDate() {

    Calendar mcurrentDate = Calendar.getInstance(Locale.getDefault());


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

                  text = "Meeting time cannot be scheduled to this time!" +
                          "Please Selected a different time day or change the day";

                } else {
                  text = "Meeting time cannot be scheduled to this time!";
                }
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
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
            getContext(), (timePicker, selectedHour, selectedMinute) -> {

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
        Toast.makeText(getContext(),
                "Meeting time can't be at selected time!", Toast.LENGTH_SHORT).show();
      }

    }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE),
            true);
    mTimePicker.setTitle("Select Meeting Time");
    mTimePicker.show();

  }

  private void calculateTime() {

    final Calendar calendar = Calendar.getInstance(Locale.getDefault());
    calendar.set(meetingStartTime[0], meetingStartTime[1], meetingStartTime[2],
            meetingStartTime[3], meetingStartTime[4]);

    scheduleTime = calendar.getTimeInMillis();
    Log.d("ttt", "scheduleTime: " + scheduleTime);

  }


}