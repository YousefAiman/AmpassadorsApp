package hashed.app.ampassadors.Fragments;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;

import java.util.Objects;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.VideoDataSourceFactory;

public class VideoFullScreenFragment extends Fragment {

  private static final boolean AUTO_HIDE = true;

  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {

      int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
              | View.SYSTEM_UI_FLAG_FULLSCREEN
              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

      Activity activity = getActivity();
      if (activity != null
              && activity.getWindow() != null) {
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
      }

    }
  };

  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };

  //video
  private PlayerView playerView;
  private SimpleExoPlayer exoPlayer;
  private final String videoUrl;

  private boolean mVisible;
  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };

  public VideoFullScreenFragment(String videoUrl){
    this.videoUrl = videoUrl;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_video_full_screen, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mVisible = true;

    playerView = view.findViewById(R.id.playerView);

    playerView.setOnClickListener(v-> toggle());

    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(v -> {


      if(playerView.isControllerVisible()){
        playerView.hideController();
      }else{
        playerView.showController();
      }

      toggle();
      Log.d("ttt","mVisible: "+mVisible);

    });

    playVideo();

  }


  private void playVideo(){

    final DefaultTrackSelector trackSelector = new DefaultTrackSelector(getContext());

    trackSelector.setParameters(
            trackSelector.buildUponParameters().setMaxVideoSizeSd());

    exoPlayer = new SimpleExoPlayer.Builder(getContext())
            .setTrackSelector(trackSelector).build();

    final MediaSource mediaSource =
            new ProgressiveMediaSource.Factory(new VideoDataSourceFactory(getContext()))
                    .createMediaSource(Uri.parse(videoUrl));

    exoPlayer.setPlayWhenReady(true);
    exoPlayer.prepare(mediaSource);


    playerView.setPlayer(exoPlayer);

  }

  private void toggle() {
    if(mVisible){
      hide();
    }else{

      mVisible = true;
      if(getActivity()!=null && getActivity().getWindow()!=null){

        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        delayedHide(3000);
      }


    }
  }

  private void hide() {
    mVisible = false;
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  @SuppressLint("InlinedApi")
  private void show() {
    // Show the system bar
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    mVisible = true;

    // Schedule a runnable to display UI elements after a delay
    mHideHandler.removeCallbacks(mHidePart2Runnable);

  }


  private void delayedHide(int delayMillis) {

    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }

  @Override
  public void onPause() {
    super.onPause();

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      getActivity().getWindow().getDecorView().setSystemUiVisibility(0);

      getActivity().setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    show();

    if (exoPlayer != null) {
      exoPlayer.setPlayWhenReady(false);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (getActivity() != null && getActivity().getWindow() != null) {
      getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      getActivity().setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    delayedHide(100);

    if (exoPlayer != null) {
      exoPlayer.setPlayWhenReady(true);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (exoPlayer != null) {
      playerView.setPlayer(null);
      playerView = null;
      exoPlayer.release();
      exoPlayer = null;
    }
  }

}