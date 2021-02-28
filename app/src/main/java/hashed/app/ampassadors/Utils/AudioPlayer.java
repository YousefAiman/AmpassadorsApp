package hashed.app.ampassadors.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.slider.Slider;

import java.io.IOException;

import hashed.app.ampassadors.R;

public class AudioPlayer implements Slider.OnSliderTouchListener
//        , MediaPlayer.OnCompletionListener
{

  private MediaPlayer mediaPlayer;
  private Context context;
  private String url;
  private long length;
  private Slider slider;
  private ImageView playerIv;
  private Drawable playDrawable,pauseDrawable;
  private Handler progressHandle;
  private Runnable playBackProgressRunnable;
  private Boolean paused;

  public AudioPlayer(Context context, String url, long length, Slider slider, ImageView playerIv){

    this.context = context;
    this.url = url;
    this.length = length;
    this.slider = slider;
    this.playerIv = playerIv;

//    mediaPlayer.setOnCompletionListener(this);
    slider.addOnSliderTouchListener(this);

    playDrawable=
            ResourcesCompat.getDrawable(context.getResources(), R.drawable.play_icon_new,null);
    pauseDrawable =
            ResourcesCompat.getDrawable(context.getResources(),R.drawable.pause_icon,null);

  }

  public MediaPlayer startPlaying(int position){

    mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    try {
      mediaPlayer.setDataSource(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mediaPlayer.prepareAsync();

    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {

        playerIv.setImageDrawable(pauseDrawable);

        if(position > 0){
          mediaPlayer.seekTo(position);
        }

        mediaPlayer.start();

        slider.setValueTo((float)length);

        long delay = length / 100;

        progressHandle = new Handler();
        playBackProgressRunnable = new Runnable() {
          @Override
          public void run() {
            try {

//              float progressPercentage =
//                      (float)mediaPlayer.getCurrentPosition() /length;
//
//              slider.post(new Runnable() {
//                @Override
//                public void run() {
//
//
//                }
//              });

                slider.setValue((float)mediaPlayer.getCurrentPosition());

              progressHandle.postDelayed(this, 10);

            } catch (IllegalStateException ed) {
              ed.printStackTrace();
            }
          }
        };

        progressHandle.postDelayed(playBackProgressRunnable, 0);

      }
    });

    return mediaPlayer;
  }


  public void pausePlayer(){

    mediaPlayer.pause();
    playerIv.setImageDrawable(playDrawable);
    progressHandle.removeCallbacks(playBackProgressRunnable);

  }


  public void resumePlayer(){

    float sliderPos = slider.getValue();

    if(sliderPos > 0){
      Log.d("audioMessage","resuming from: "+sliderPos);
      mediaPlayer.seekTo((int) sliderPos);
    }else{
      Log.d("audioMessage","resuming from start");
    }
    mediaPlayer.start();

    playerIv.setImageDrawable(pauseDrawable);
    progressHandle.postDelayed(playBackProgressRunnable, 0);

  }



  public void releasePlayer(){

    progressHandle.removeCallbacks(playBackProgressRunnable);
    slider.removeOnSliderTouchListener(this);

    playerIv.setImageDrawable(playDrawable);

    slider.setValue(0);

    mediaPlayer.stop();
    mediaPlayer.release();
    mediaPlayer = null;

  }

  @Override
  public void onStartTrackingTouch(@NonNull Slider slider) {

    if(mediaPlayer.isPlaying()){
      paused = false;
      mediaPlayer.pause();
      progressHandle.removeCallbacks(playBackProgressRunnable);
    }else{
      paused = true;
    }

  }

  @Override
  public void onStopTrackingTouch(@NonNull Slider slider) {
    mediaPlayer.seekTo((int) slider.getValue());
    if(!paused){
      mediaPlayer.start();
      progressHandle.postDelayed(playBackProgressRunnable, 0);
    }
  }

//  @Override
//  public void onCompletion(MediaPlayer player) {
//
//    releasePlayer();
//
//  }
}
