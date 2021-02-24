package hashed.app.ampassadors.Fragments;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class FullscreenFragment2 extends Fragment {

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

  private final Uri imageUri;

  public FullscreenFragment2(Uri imageUri){
    this.imageUri = imageUri;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_fullscreen2, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ImageView fullScreenIv = view.findViewById(R.id.fullScreenIv);
    EditText messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    ImageView messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);

    messagingPickerSendIv.setOnClickListener(new sendClickListener(messagingPickerEd));



//    fullScreenIv.setOnTouchListener(new View.OnTouchListener() {
//      @SuppressLint("ClickableViewAccessibility")
//      @Override
//      public boolean onTouch(View view, MotionEvent motionEvent) {
//        if(!isStatusBarVisible){
//
//          show(true);
//
//        }
//        return true;
//      }
//    });

    final Toolbar fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);

    fullScreenToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().onBackPressed();
      }
    });

    Picasso.get().load(imageUri).fit().into(fullScreenIv);

  }

  private class sendClickListener implements View.OnClickListener {
    EditText messagingPickerEd;

    sendClickListener(EditText messagingPickerEd){
      this.messagingPickerEd = messagingPickerEd;
    }

    @Override
    public void onClick(View view) {

      final String content = messagingPickerEd.getText().toString();
      if(!content.isEmpty() && imageUri!=null){

        ((PrivateMessagingActivity)getActivity()).sendFileMessage(imageUri, Files.IMAGE, content);
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