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

public class MinutePickerDialogFragment extends DialogFragment implements View.OnClickListener {

  private NumberPicker minutePicker;
  private final OnTimePass onTimePass;
  private int minutes;

  public interface OnTimePass {
    void passTime(int duration);
  }

  public MinutePickerDialogFragment(OnTimePass onTimePass) {
    this.onTimePass = onTimePass;
  }

  public MinutePickerDialogFragment(int minutes,OnTimePass onTimePass) {
    this.minutes = minutes;
    this.onTimePass = onTimePass;
  }

  @Override
  public void onAttach(@NotNull Context context) {
    super.onAttach(context);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.fragment_minute_picker_dialog, container,
            false);

    minutePicker = view.findViewById(R.id.minutePicker);
    TextView cancelTv = view.findViewById(R.id.cancelTv);
    TextView setTv = view.findViewById(R.id.setTv);


    minutePicker.setMaxValue(40);
    minutePicker.setMinValue(1);


    if(minutes > 0){
      minutePicker.setValue(minutes);
    }

    cancelTv.setOnClickListener(this);
    setTv.setOnClickListener(this);

    return view;
  }


  @Override
  public void onClick(View view) {

    if(view.getId() == R.id.cancelTv){

      dismiss();

    }else if(view.getId() == R.id.setTv){

      onTimePass.passTime(minutePicker.getValue());
      dismiss();
    }
  }
}