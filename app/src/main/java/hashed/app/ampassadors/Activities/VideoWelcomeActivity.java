package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.VideoCache;
import hashed.app.ampassadors.Utils.VideoDataSourceFactory;

public class VideoWelcomeActivity extends AppCompatActivity implements View.OnClickListener {


  private static final String videoUrl = "https://firebasestorage.googleapis.com/v0/b/ambassadors-app-93583.appspot.com/o/info_video%2FWhatsApp%20Video%202021-03-23%20at%201.17.37%20AM.mp4?alt=media&token=e2ab5de5-8044-44d7-9bb9-bcd11fe7ff5a";

//  private Drawable playDrawable,pauseDrawable;

  //views
  private PlayerView playerView;
  private ImageView playIv;
  private Button guestBtn;
  private Button createAccountBtn;
  private TextView signInTv;

  //exo player
  private SimpleExoPlayer exoPlayer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_welcome);

    getViews();

    initializeClickers();

    initializePlayer();

  }


  private void getViews(){

    playerView = findViewById(R.id.playerView);
    playIv = findViewById(R.id.playIv);
    guestBtn = findViewById(R.id.guestBtn);
    createAccountBtn = findViewById(R.id.createAccountBtn);
    signInTv = findViewById(R.id.signInTv);

  }

  private void initializeClickers(){

    playerView.getVideoSurfaceView().setOnClickListener(this);
    guestBtn.setOnClickListener(this);
    createAccountBtn.setOnClickListener(this);
    signInTv.setOnClickListener(this);

  }


  private void initializePlayer(){

    final DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);

    trackSelector.setParameters(
            trackSelector.buildUponParameters().setMaxVideoSizeSd());

    exoPlayer = new SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector).build();


//    exoPlayer = new SimpleExoPlayer.Builder(this)
//            .setBandwidthMeter(new DefaultBandwidthMeter.Builder(this).build())
//            .build();

    playerView.setPlayer(exoPlayer);

//    DefaultDataSourceFactory mediaDataSourceFactory = new
//            DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));
//    MediaSource mediaSource = new ProgressiveMediaSource.Factory(mediaDataSourceFactory)
//            .createMediaSource(MediaItem.fromUri(videoUrl));

    final MediaSource mediaSource =
            new ProgressiveMediaSource.Factory(new VideoDataSourceFactory(this))
                    .createMediaSource(MediaItem.fromUri(videoUrl));


    exoPlayer.setMediaSource(mediaSource);
    exoPlayer.prepare();
    exoPlayer.setPlayWhenReady(true);

    exoPlayer.addListener(new Player.EventListener() {
      @Override
      public void onPlaybackStateChanged(int state) {
        if (state == SimpleExoPlayer.STATE_ENDED) {
          playIv.setImageResource(R.drawable.replay_icon_white);
          playIv.setVisibility(View.VISIBLE);
        }
      }
    });

  }


  @Override
  public void onClick(View view) {

    if(view.getId() == playerView.getVideoSurfaceView().getId()){

      if(exoPlayer!=null){

        if (exoPlayer.getPlaybackState() == SimpleExoPlayer.STATE_ENDED) {

          Log.d("videoPager","videos state ended");

          exoPlayer.seekTo(0);

          playVideo();

        }else if(exoPlayer.getPlayWhenReady()){

          pauseVideo();

        }else{

          playVideo();

        }

      }

    }else if(view.getId() == guestBtn.getId()){
      enterAsGuest();
    }else if(view.getId() == createAccountBtn.getId()){
      startActivity(new Intent(VideoWelcomeActivity.this, sign_in.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      finish();

    }else if(view.getId() == signInTv.getId()){

      startActivity(new Intent(VideoWelcomeActivity.this, sign_up.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      finish();

    }

  }


  private void pauseVideo(){
    exoPlayer.setPlayWhenReady(false);
    playIv.setImageResource(R.drawable.play_icon_new);
    playIv.setVisibility(View.VISIBLE);
  }


  private void playVideo(){
    exoPlayer.setPlayWhenReady(true);

    playIv.setImageResource(R.drawable.pause_icon);

    playIv.setVisibility(View.VISIBLE);

    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        playIv.setVisibility(View.INVISIBLE);
      }
    },800);

  }
//
//  private Drawable getPlayDrawable(){
//    if(playDrawable == null){
//      playDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.play_icon_new,
//              null);
//    }
//    return playDrawable;
//  }
//
//  private Drawable getPauseDrawable(){
//    if(pauseDrawable == null){
//      pauseDrawable = ResourcesCompat.getDrawable(getResources(),R.drawable.pause_icon,
//              null);
//    }
//    return pauseDrawable;
//  }

  private void enterAsGuest(){
    guestBtn.setClickable(false);
    FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
              @Override
              public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(VideoWelcomeActivity.this,
                        Home_Activity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        guestBtn.setClickable(true);
        finish();
      }
    });
  }


  @Override
  public void onPause() {
    super.onPause();
    if (exoPlayer != null && exoPlayer.getPlayWhenReady()) {
      exoPlayer.setPlayWhenReady(false);
      pauseVideo();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (exoPlayer != null && !exoPlayer.getPlayWhenReady()) {
      playVideo();
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

      if (!VideoCache.isNull()) {
        VideoDataSourceFactory.clearVideoCache(this);
      }
    }
  }


}