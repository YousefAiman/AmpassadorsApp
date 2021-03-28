package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.annotations.NotNull;

import hashed.app.ampassadors.R;

public class HourMinutePickerDialogFragment extends DialogFragment implements View.OnClickListener {

  private final OnTimePass onTimePass;
  private NumberPicker minutePicker,hourPicker;
  private final int minutes, hours;


  public HourMinutePickerDialogFragment(int minutes,int hours, OnTimePass onTimePass) {
    this.minutes = minutes;
    this.hours = hours;
    this.onTimePass = onTimePass;
  }

  @Override
  public void onAttach(@NonNull @NotNull Context context) {
    super.onAttach(context);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_hour_minute_picker_dialog, container,
            false);

    minutePicker = view.findViewById(R.id.minutePicker);
    hourPicker = view.findViewById(R.id.hourPicker);

    final TextView cancelTv = view.findViewById(R.id.cancelTv);
    final TextView setTv = view.findViewById(R.id.setTv);

    hourPicker.setMaxValue(23);
    hourPicker.setMinValue(0);

    minutePicker.setMaxValue(59);
    minutePicker.setMinValue(0);

    if (minutes > 0) {
      minutePicker.setValue(minutes);
    }
    if (hours > 0) {
      hourPicker.setValue(hours);
    }

    cancelTv.setOnClickListener(this);
    setTv.setOnClickListener(this);

    return view;
  }

  @Override
  public void onClick(View view) {

    if (view.getId() == R.id.cancelTv) {

      dismiss();

    } else if (view.getId() == R.id.setTv) {

      onTimePass.passTime(minutePicker.getValue(), hourPicker.getValue());
      dismiss();
    }
  }


  public interface OnTimePass {
    void passTime(int minutes,int hours);
  }

}