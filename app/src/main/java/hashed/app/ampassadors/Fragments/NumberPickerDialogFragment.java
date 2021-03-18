package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import hashed.app.ampassadors.R;

public class NumberPickerDialogFragment extends DialogFragment implements View.OnClickListener {

  private NumberPicker dayPicker;
  private NumberPicker hourPicker;
  private NumberPicker minutePicker;
  private OnTimePass onTimePass;
  private Integer[] durations;

  public NumberPickerDialogFragment() {
  }

  public NumberPickerDialogFragment(Integer[] durations) {
    this.durations = durations;
  }

  @Override
  public void onAttach(@NotNull Context context) {
    super.onAttach(context);
    onTimePass = (OnTimePass) context;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_number_picker_dialog, container,
            false);

    dayPicker = view.findViewById(R.id.dayPicker);
    hourPicker = view.findViewById(R.id.hourPicker);
    minutePicker = view.findViewById(R.id.minutePicker);
    TextView cancelTv = view.findViewById(R.id.cancelTv);
    TextView setTv = view.findViewById(R.id.setTv);

    dayPicker.setMaxValue(7);
    dayPicker.setMinValue(0);


    hourPicker.setMaxValue(24);
    hourPicker.setMinValue(0);


    minutePicker.setMaxValue(59);
    minutePicker.setMinValue(0);


    if (durations != null && durations.length > 0) {

      if (durations[0] > 0) {
        dayPicker.setValue(durations[0]);
      }

      if (durations[1] > 0) {
        dayPicker.setValue(durations[1]);
      }

      if (durations[2] > 0) {
        dayPicker.setValue(durations[2]);
      }

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

      final int daysValue = dayPicker.getValue();
      final int hoursValue = hourPicker.getValue();
      final int minutesValue = minutePicker.getValue();

      if (daysValue > 0 || hoursValue > 0 || minutesValue > 0) {

        long duration = (daysValue * DateUtils.DAY_IN_MILLIS) +
                (hoursValue * DateUtils.HOUR_IN_MILLIS) +
                (minutesValue * DateUtils.MINUTE_IN_MILLIS);

        Log.d("ttt", "duration: " + duration);

        final Integer[] durations = {daysValue, hoursValue, minutesValue};

        onTimePass.passTime(duration, durations);

        dismiss();

      } else {
        Toast.makeText(getContext(),
                "Please set a duration longer than 0", Toast.LENGTH_SHORT).show();
      }

    }
  }


  public interface OnTimePass {
    void passTime(long time, Integer[] durations);
  }
}