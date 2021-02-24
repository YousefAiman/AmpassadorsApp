package hashed.app.ampassadors.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import hashed.app.ampassadors.R;

public class ImagePickerBottomSheetFragment extends BottomSheetDialogFragment {

  public static ImagePickerBottomSheetFragment newInstance() {
    ImagePickerBottomSheetFragment fragment = new ImagePickerBottomSheetFragment();
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    return inflater.inflate(R.layout.message_options_bsd, container, false);

  }

//
//  @Override
//  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//    super.onViewCreated(view, savedInstanceState);
//
//    parentView.findViewById(R.id.imageIv).setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View view) {
//
//        filePicker = new Files(PrivateMessagingActivity.this);
//
//        filePicker.startImageFetchIntent();
//
//      }
//    });
//
//  }
}
