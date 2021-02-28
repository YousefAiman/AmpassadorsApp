package hashed.app.ampassadors.Fragments;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;


public class VideoPickerPreviewFragment extends Fragment {


  //views
  private Toolbar fullScreenToolbar;
  private ImageView messagingPickerSendIv;
  private ImageView videoThumbnailTv ;
  private ImageView videoPlayIv ;
  private EditText messagingPickerEd;
  private Uri videoUri;
  private SimpleExoPlayer simpleExoPlayer;
  private PlayerView playerView;
  private Bitmap videoThumbnailBitmap = null;
  public VideoPickerPreviewFragment(Uri videoUri) {
    this.videoUri = videoUri;
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.fragment_video_picker_preview, container, false);

    fullScreenToolbar = view.findViewById(R.id.fullScreenToolbar);
    messagingPickerSendIv = view.findViewById(R.id.messagingPickerSendIv);
    messagingPickerEd = view.findViewById(R.id.messagingPickerEd);
    videoThumbnailTv = view.findViewById(R.id.videoThumbnailTv);
    videoPlayIv = view.findViewById(R.id.videoPlayIv);
    playerView = view.findViewById(R.id.exoPlayer);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    fullScreenToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        getActivity().onBackPressed();
      }
    });

    messagingPickerSendIv.setOnClickListener(new sendClickListener());

    new Thread(new Runnable() {

      @Override
      public void run() {

        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getContext(), videoUri);
        final long time = Long.parseLong(
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        if(time > 0){

          if(time>=1000){
            videoThumbnailBitmap = retriever.getFrameAtTime(1000);
          }else{
            videoThumbnailBitmap = retriever.getFrameAtTime(time);
          }

        }else{
          //video stupid
        }

        if(videoThumbnailBitmap!=null){
          videoThumbnailTv.post(new Runnable() {
            @Override
            public void run() {
              videoThumbnailTv.setImageBitmap(videoThumbnailBitmap);
            }
          });
        }
      }
    }).start();


    Objects.requireNonNull(playerView.getVideoSurfaceView()).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        playPauseOrrInitializeVideo();
        playerView.getVideoSurfaceView().setOnClickListener(null);
      }
    });


  }

  private void playPauseOrrInitializeVideo(){

    if(simpleExoPlayer==null){
      videoThumbnailTv.setImageBitmap(null);
      videoThumbnailTv.setVisibility(View.GONE);
      videoPlayIv.setVisibility(View.GONE);
      playerView.setPlayer(SetupPlayer());
    }

  }
  private void checkVideoDuration(){

    final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    retriever.setDataSource(getContext(), videoUri);
    final String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

    final long timeInMillisec = Long.parseLong(time);

    if ((timeInMillisec / 1000) > Files.MAX_VIDEO_LENGTH) {
      retriever.release();
      Toast.makeText(getContext(), "طول الفيديو يجب ان لا يزيد عن 30 ثانية!",
              Toast.LENGTH_SHORT).show();

    }else{

    }

  }

  private SimpleExoPlayer SetupPlayer(){

    simpleExoPlayer = new SimpleExoPlayer.Builder(getContext(),
            new DefaultRenderersFactory(getContext())).build();

    DataSpec dataSpec = new DataSpec(videoUri);
    final FileDataSource fileDataSource = new FileDataSource();
    try {
      fileDataSource.open(dataSpec);
    } catch (FileDataSource.FileDataSourceException e) {
      e.printStackTrace();
    }


    DataSource.Factory dataSourceFactory =
            new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(),
                    "simpleExoPlayer"));

    MediaSource firstSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(fileDataSource.getUri());

    simpleExoPlayer.prepare(firstSource, true, true);

    simpleExoPlayer.setPlayWhenReady(true);

    return simpleExoPlayer;
  }


  class sendClickListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {

      final String content = messagingPickerEd.getText().toString();
      if(!content.isEmpty() && videoUri!=null){

        ((PrivateMessagingActivity)getActivity()).uploadVideoMessage(
                videoUri,
                content,
                videoThumbnailBitmap
        );

        getActivity().onBackPressed();

      }else{

        //problem with image

      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (simpleExoPlayer != null) {
      simpleExoPlayer.setPlayWhenReady(false);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (simpleExoPlayer != null) {
      simpleExoPlayer.setPlayWhenReady(true);
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (simpleExoPlayer != null) {
      playerView.setPlayer(null);
      simpleExoPlayer.release();
      simpleExoPlayer = null;
    }
  }
}