package hashed.app.ampassadors.Fragments;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class FilePickerPreviewFragment extends Fragment {

  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
  private static final int UI_ANIMATION_DELAY = 300;

  private static final int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
//          | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
          ;


  private final Handler mHideHandler = new Handler();

  private final Runnable hideRunnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {

      if(getActivity()!=null && getActivity().getWindow()!=null){

        isStatusBarVisible = false;

//        getActivity().getWindow().addFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN
////                        |
////                View.SYSTEM_UI_FLAG_LOW_PROFILE
//        );
//
//        View decorView = getActivity().getWindow().getDecorView();
////        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getActivity().getWindow().setFlags(flags,flags);
      }
    }
  };

  private final Runnable showRunnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {

      if(getActivity()!=null && getActivity().getWindow()!=null){

//        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        View decorView = getActivity().getWindow().getDecorView();
//        decorView.setSystemUiVisibility(0);
//        getActivity().getWindow().clearFlags(
////                WindowManager.LayoutParams.FLAG_FULLSCREEN|
////                View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        getActivity().getWindow().clearFlags(flags);
      }
    }
  };

  private boolean isStatusBarVisible;

  private final Uri uri;
  private final int type;

  public FilePickerPreviewFragment(Uri uri, int type){
    this.uri = uri;
    this.type = type;
  }


  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    int layout;
    switch (type){
      case Files.IMAGE:
        layout = R.layout.fragment_image_picker_preview;
        break;

      case Files.DOCUMENT:
        layout = R.layout.fragment_file_picker_preview;
        break;

      case Files.AUDIO:
        layout = R.layout.fragment_image_picker_preview;
        break;

      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    return inflater.inflate(layout, container, false);

  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ImageView fullScreenIv = view.findViewById(R.id.fullScreenIv);
    EditText messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    ImageView messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);


    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);

    fullScreenToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().onBackPressed();
      }
    });

    if(type == Files.IMAGE){
      Picasso.get().load(uri).fit().into(fullScreenIv);

      messagingPickerSendIv.setOnClickListener(new sendClickListener(messagingPickerEd));

    }else if(type == Files.DOCUMENT){

      TextView documentNameTv = view.findViewById(R.id.documentNameTv);

        Map<String,Object> fileInfoMap = Files.getFileInfo(getContext(),uri);

        final String fileName = (String) fileInfoMap.get("fileName");

//        final String fileType = (String) fileInfoMap.get("fileType");
//        Log.d("ttt","fileName: "+fileName);
//        Log.d("ttt","fileType: "+fileType);
        documentNameTv.setText(fileName);

        messagingPickerSendIv.setOnClickListener(new sendClickListener(messagingPickerEd,fileName));

    }


  }

  private class sendClickListener implements View.OnClickListener {
    EditText messagingPickerEd;
    String fileName;

    sendClickListener(EditText messagingPickerEd){
      this.messagingPickerEd = messagingPickerEd;
    }

    sendClickListener(EditText messagingPickerEd,String fileName){
      this.messagingPickerEd = messagingPickerEd;
      this.fileName = fileName;
    }

    @Override
    public void onClick(View view) {

      final String content = messagingPickerEd.getText().toString();
      if(!content.isEmpty() && uri!=null){

        if(getActivity() instanceof PrivateMessagingActivity){

          ((PrivateMessagingActivity)getActivity()).sendFileMessage(
                  uri,
                  type,
                  content,
                  0,
                  fileName);

        }else if(getActivity() instanceof GroupMessagingActivity){

          ((GroupMessagingActivity)getActivity()).sendFileMessage(
                  uri,
                  type,
                  content,
                  0,
                  fileName);

        }


        getActivity().onBackPressed();

      }else{

        //problem with image

      }
    }
  }

//  @Override
//  public void onResume() {
//    super.onResume();
//
//    isStatusBarVisible = false;
//    mHideHandler.removeCallbacks(hideRunnable);
//    mHideHandler.postDelayed(hideRunnable,UI_ANIMATION_DELAY);
//
//  }
//
//  @Override
//  public void onPause() {
//    super.onPause();
//
//    show(false);
//
//  }
//
//  void show(boolean hideAgain){
//    isStatusBarVisible = true;
//    mHideHandler.removeCallbacks(showRunnable);
//    mHideHandler.postDelayed(showRunnable,UI_ANIMATION_DELAY);
//
//    if(hideAgain){
//      mHideHandler.removeCallbacks(hideRunnable);
//      mHideHandler.postDelayed(hideRunnable,AUTO_HIDE_DELAY_MILLIS);
//    }
//  }


}