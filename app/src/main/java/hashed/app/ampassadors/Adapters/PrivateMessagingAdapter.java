package hashed.app.ampassadors.Adapters;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.AudioPlayer;
import hashed.app.ampassadors.Utils.Files;

public class PrivateMessagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  //message types



  //received message items
    static final int
            MSG_TYPE_LEFT_TEXT = 11,
            MSG_TYPE_LEFT_IMAGE = 12,
            MSG_TYPE_LEFT_AUDIO = 13,
            MSG_TYPE_LEFT_VIDEO = 14,
            MSG_TYPE_LEFT_DOCUMENT = 15;

  //sent message items
  static final int
            MSG_TYPE_RIGHT_TEXT = 21,
            MSG_TYPE_RIGHT_IMAGE = 22,
            MSG_TYPE_RIGHT_AUDIO = 23,
            MSG_TYPE_RIGHT_VIDEO = 24,
            MSG_TYPE_RIGHT_DOCUMENT = 25;


  //date formats
  private final DateFormat
          hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
          withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
          formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
          todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
          todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());


//  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

  private static final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private static final Date date = new Date();

   private boolean longCLickEnabled = true;

  static ArrayList<PrivateMessage> privateMessages;
  private final Context context;

  private static DeleteMessageListener deleteMessageListener;
  private static VideoMessageListener videoMessageListener;
  private static DocumentMessageListener documentMessageListener;

  public interface DeleteMessageListener{
    void deleteMessage(PrivateMessage message, DialogInterface dialog);
  }

  public interface VideoMessageListener{
    void playVideo(String url);
  }

  public interface DocumentMessageListener{

    boolean startDownload(int adapterPosition,String url, String fileName);
    boolean cancelDownload(int adapterPosition,long downloadId);

  }


  void disableLongClick(){
    longCLickEnabled = false;
  }

  public PrivateMessagingAdapter(ArrayList<PrivateMessage> privateMessages,
                                 Context context,
                                 DeleteMessageListener deleteMessageListener,
                                 VideoMessageListener videoMessageListener
          , DocumentMessageListener documentMessageListener
  ) {

    PrivateMessagingAdapter.privateMessages = privateMessages;
    this.context = context;
    PrivateMessagingAdapter.deleteMessageListener = deleteMessageListener;
    PrivateMessagingAdapter.videoMessageListener = videoMessageListener;
    PrivateMessagingAdapter.documentMessageListener = documentMessageListener;
  }

  @Override
  public long getItemId(int position) {
    return privateMessages.get(position).hashCode();
  }

  @Override
  public int getItemCount() {
    return privateMessages.size();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    switch (viewType){

      case MSG_TYPE_LEFT_TEXT:
        return new PrivateMessagingTextVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_text_message, parent,
                        false));

      case MSG_TYPE_RIGHT_TEXT:
        return new PrivateMessagingTextVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_text_message, parent,
                        false));

      case MSG_TYPE_LEFT_IMAGE:
        return new PrivateMessagingImageVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_image_message, parent,
                        false));

      case MSG_TYPE_RIGHT_IMAGE:
        return new PrivateMessagingImageVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_image_message, parent,
                        false));

      case MSG_TYPE_LEFT_AUDIO:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_audio_message, parent,
                        false));

      case MSG_TYPE_RIGHT_AUDIO:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_audio_message, parent,
                        false));


      case MSG_TYPE_LEFT_VIDEO:
        return new PrivateMessagingVideoVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_video_message, parent,
                        false));


      case MSG_TYPE_RIGHT_VIDEO:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_video_message, parent,
                        false));

      case MSG_TYPE_LEFT_DOCUMENT:
        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_document_message, parent,
                        false));


      case MSG_TYPE_RIGHT_DOCUMENT:
        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_document_message, parent,
                        false));



    }

    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


    switch (holder.getItemViewType()){


      case MSG_TYPE_LEFT_TEXT:
      case MSG_TYPE_RIGHT_TEXT:

        ((PrivateMessagingTextVh)holder).bindMessage(privateMessages.get(position));


        break;

      case MSG_TYPE_LEFT_IMAGE:
      case MSG_TYPE_RIGHT_IMAGE:

        ((PrivateMessagingImageVh)holder).bindMessage(privateMessages.get(position));

        break;

      case MSG_TYPE_LEFT_AUDIO:
      case MSG_TYPE_RIGHT_AUDIO:

        ((PrivateMessagingAudioVh)holder).bindMessage(privateMessages.get(position));

        break;

      case MSG_TYPE_LEFT_VIDEO:
      case MSG_TYPE_RIGHT_VIDEO:

        ((PrivateMessagingVideoVh)holder).bindMessage(privateMessages.get(position));

        break;

      case MSG_TYPE_LEFT_DOCUMENT:
      case MSG_TYPE_RIGHT_DOCUMENT:

        ((PrivateMessagingDocumentVh)holder).bindMessage(privateMessages.get(position));

        break;

    }

  }

  @Override
  public int getItemViewType(int position) {

    final PrivateMessage message = privateMessages.get(position);

    switch (message.getType()){

      case Files.TEXT:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_TEXT:MSG_TYPE_LEFT_TEXT;

      case Files.IMAGE:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_IMAGE:MSG_TYPE_LEFT_IMAGE;

      case Files.AUDIO:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_AUDIO:MSG_TYPE_LEFT_AUDIO;

      case Files.VIDEO:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_VIDEO:MSG_TYPE_LEFT_VIDEO;

      case Files.DOCUMENT:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_DOCUMENT:MSG_TYPE_LEFT_DOCUMENT;

      default:
        return 0;
    }
  }

   static class PrivateMessagingTextVh extends RecyclerView.ViewHolder
           implements View.OnLongClickListener, View.OnClickListener{

    private final TextView messageTv;

     public PrivateMessagingTextVh(@NonNull View itemView) {
       super(itemView);
       messageTv =  itemView.findViewById(R.id.messageTv);
//       messageTimeTv =  itemView.findViewById(R.id.messageTimeTv);
     }


      private void bindMessage(PrivateMessage message) {

      if(message == null)
        return;

      if(!message.getDeleted()){
        messageTv.setText(message.getContent());
      }else{
        messageTv.setText(R.string.message_deleted);
      }

      itemView.setOnClickListener(this);

      if(!message.getDeleted()){
        itemView.setOnLongClickListener(this);
      }

    }

     @Override
     public boolean onLongClick(View view) {

       final PrivateMessage message = privateMessages.get(getAdapterPosition());

       if (message.getSender().equals(currentUid)) {

         final AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
         alert.setTitle(R.string.message_delete);
         alert.setPositiveButton(R.string.delete, (dialog, which) -> {
           deleteMessageListener.deleteMessage(message,dialog);
         });
         alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
         alert.create().show();

       }

       return false;
     }

     @Override
     public void onClick(View view) {

//
//       if (messageTimeTv.getVisibility() == View.INVISIBLE) {
//
//         long time = messages.get(getAdapterPosition()).getTime();
//
//         if(messageTimeTv.getText().toString().isEmpty()){
//
//           if (time < 1000000000000L) {
//             time *= 1000;
//           }
//
//           if (todayYearMonthDayFormat.format(date)
//                   .equals(todayYearMonthDayFormat.format(time))) {
//             messageTimeTv.setText(hourMinuteFormat.format(time));
//           } else if (todayYearFormat.format(date).equals(todayYearFormat.format(time))) {
//             messageTimeTv.setText(withoutYearFormat.format(time));
//           } else {
//             messageTimeTv.setText(formatter.format(time));
//           }
//         }
//
//         messageTimeTv.setVisibility(View.VISIBLE);
//       } else {
//         messageTimeTv.setVisibility(View.INVISIBLE);
//       }

     }

   }


  static class PrivateMessagingImageVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener{

    private final TextView messageTv;
    private final ImageView imageIv;
    private final Picasso picasso = Picasso.get();

    public PrivateMessagingImageVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      imageIv = itemView.findViewById(R.id.imageIv);
    }

    private void bindMessage(PrivateMessage message) {

      if(message == null)
        return;

      if(message.getAttachmentUrl()!=null){
        picasso.load(message.getAttachmentUrl()).fit().into(imageIv);
      }

      if(!message.getDeleted()){
        messageTv.setText(message.getContent());
      }else{
        messageTv.setText(R.string.message_deleted);
      }

      itemView.setOnClickListener(this);

//      if(!message.getDeleted()){
//        itemView.setOnLongClickListener(this);
//      }

    }

    @Override
    public boolean onLongClick(View view) {

      return false;
    }

    @Override
    public void onClick(View view) {

    }

  }


  static class PrivateMessagingAudioVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener,
          MediaPlayer.OnCompletionListener{

    private final Slider audioProgressSlider;
    private final ImageView imageIv;
    private final ImageView playIv;
    private final Picasso picasso = Picasso.get();
//    Context context;
//
//    private final  Drawable playDrawable=
//            ResourcesCompat.getDrawable(itemView.getResources(),R.drawable.play_icon_new,null);
//    private final  Drawable pauseDrawable =
//            ResourcesCompat.getDrawable(itemView.getResources(),R.drawable.pause_icon,null);

    private AudioPlayer audioPlayer;
    private MediaPlayer lastMediaPlayer;
    private int lastClicked = -1;
//
//    private AnimatedVectorDrawable playToPauseDrawable;
//    private AnimatedVectorDrawable pauseToPlayDrawable;

    public PrivateMessagingAudioVh(@NonNull View itemView) {
      super(itemView);
      audioProgressSlider = itemView.findViewById(R.id.audioProgressSlider);
      imageIv = itemView.findViewById(R.id.imageIv);
      playIv = itemView.findViewById(R.id.playIv);
//
//      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP  ){
//
//        playToPauseDrawable =  (AnimatedVectorDrawable) ResourcesCompat.getDrawable(
//                itemView.getResources(),R.drawable.animated_play_to_pause,null);
//
//        pauseToPlayDrawable =  (AnimatedVectorDrawable) ResourcesCompat.getDrawable(
//                itemView.getResources(),R.drawable.animated_pause_to_play,null);
//
//
//      }else{
//
//        playDrawable =
//                ResourcesCompat.getDrawable(itemView.getResources(),R.drawable.play_icon_new,null);
//
//        pauseDrawable =
//                ResourcesCompat.getDrawable(itemView.getResources(),R.drawable.pause_icon,null);
//
//      }

    }

    private void bindMessage(PrivateMessage message) {

      if(message == null)
        return;

      if(message.getAttachmentUrl() != null){

        playIv.setOnClickListener(this);

      }else{

        playIv.setClickable(false);

      }

      itemView.setOnClickListener(this);


    }

    @Override
    public boolean onLongClick(View view) {

      return false;
    }

    @Override
    public void onClick(View view) {

      if(view.getId() == R.id.playIv){

        Log.d("audioMessage","item clicked");
        PrivateMessage message = privateMessages.get(getAdapterPosition());


        if(lastClicked == -1){
          Log.d("audioMessage","lastClicked == -1");

          audioPlayer = new AudioPlayer(itemView.getContext(),
                  message.getAttachmentUrl(),message.getLength(),
                  audioProgressSlider,playIv);

          lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue()>0?
                  (int) audioProgressSlider.getValue() : 0);

          lastMediaPlayer.setOnCompletionListener(this);

          lastClicked = getAdapterPosition();

        }else if(lastClicked == getAdapterPosition()){
          Log.d("audioMessage","lastClicked == getAdapterPosition()");
          if(audioPlayer == null){

            Log.d("audioMessage","audioPlayer == null");

            audioPlayer = new AudioPlayer(itemView.getContext(),
                    message.getAttachmentUrl(),message.getLength(),
                    audioProgressSlider,playIv);

            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue()>0?
                    (int) audioProgressSlider.getValue() : 0);

            lastMediaPlayer.setOnCompletionListener(this);

            lastClicked = getAdapterPosition();

          }else{

            Log.d("audioMessage","audioPlayer != null");

            if(lastMediaPlayer.isPlaying()){

              Log.d("audioMessage","lastMediaPlayer.isPlaying()");

              audioPlayer.pausePlayer();
            }else{

              Log.d("audioMessage","lastMediaPlayer is paused");

              audioPlayer.resumePlayer();
            }

          }


        }else{

          Log.d("audioMessage","last clicked new");

          if(audioPlayer!=null){
            Log.d("audioMessage","audioPlayer!=null");
            audioPlayer.releasePlayer();
          }

          audioPlayer = new AudioPlayer(itemView.getContext(),
                  message.getAttachmentUrl(),message.getLength(),
                  audioProgressSlider,playIv);

          lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue()>0?
                  (int) audioProgressSlider.getValue() : 0);

          lastMediaPlayer.setOnCompletionListener(this);

          lastClicked = getAdapterPosition();

        }

      }

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

      audioPlayer.releasePlayer();
      audioPlayer = null;
      lastMediaPlayer = null;

    }
  }


  static class PrivateMessagingVideoVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener{

    private final TextView messageTv;
    private final ImageView imageIv;
    private final ImageView playIv;
    private final Picasso picasso = Picasso.get();
    private final ProgressBar videoProgressBar;
    public PrivateMessagingVideoVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      playIv  = itemView.findViewById(R.id.playIv);
      videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
    }

    private void bindMessage(PrivateMessage message) {

      if(message == null)
        return;

      messageTv.setText(message.getContent());

      if(message.getVideoThumbnail()!=null){

        picasso.load(message.getVideoThumbnail()).fit().into(imageIv, new Callback() {
          @Override
          public void onSuccess() {
            videoProgressBar.setVisibility(View.GONE);
            playIv.setVisibility(View.VISIBLE);
          }
          @Override
          public void onError(Exception e) {
            videoProgressBar.setVisibility(View.GONE);
            playIv.setVisibility(View.VISIBLE);
          }
        });

       imageIv.setOnClickListener(this);

      }else{
        itemView.setOnClickListener(null);
      }

    }

    @Override
    public boolean onLongClick(View view) {

      return false;
    }

    @Override
    public void onClick(View view) {

      videoMessageListener.playVideo(privateMessages.get(getAdapterPosition()).getAttachmentUrl());

    }

  }


  class PrivateMessagingDocumentVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener{

    private final TextView messageTv;
    private final TextView documentNameTv;
    private final ImageView downloadIv;
    private final ProgressBar downloadProgressBar;
    private boolean isDownloading;
//    private DownloadReceiver downloadReceiver;
    public PrivateMessagingDocumentVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      documentNameTv = itemView.findViewById(R.id.documentNameTv);
      downloadIv = itemView.findViewById(R.id.downloadIv);
      downloadProgressBar = itemView.findViewById(R.id.downloadProgressBar);
    }

    private void bindMessage(PrivateMessage message) {

      if(message == null)
        return;


      messageTv.setText(message.getContent());
      documentNameTv.setText(message.getFileName());

      if(message.getAttachmentUrl()!=null){

        downloadIv.setOnClickListener(this);

        if(message.getUploadTask() != null){

          if (message.getUploadTask().isCompleted()) {

            downloadIv.setVisibility(View.GONE);
            downloadProgressBar.setVisibility(View.GONE);

          }else if(message.getUploadTask().isDownloading()){

            downloadIv.setImageResource(R.drawable.close_icon);
            downloadProgressBar.setVisibility(View.VISIBLE);

          }
        }else{

          downloadProgressBar.setVisibility(View.GONE);
          downloadIv.setImageResource(R.drawable.download_icon);
          downloadIv.setVisibility(View.VISIBLE);

        }
      }else{

        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadIv.setVisibility(View.GONE);

      }

    }

    @Override
    public boolean onLongClick(View view) {

      return false;
    }

    @Override
    public void onClick(View view) {

//      final PrivateMessage message = privateMessages.get(getAdapterPosition());
//      final FileDownloadTask downloadTask =
//              Files.downloadFile((Activity)itemView.getContext(),
//                      message.getAttachmentUrl(),message.getFileName());
//
//      if(downloadTask == null){
//        Toast.makeText(itemView.getContext(),
//                "These was an error while attempting to download the file!",
//                Toast.LENGTH_SHORT).show();
//        return;
//      }
//
//      downloadIv.setImageResource(R.drawable.close_icon);
//      downloadProgressBar.setVisibility(View.VISIBLE);
//
//      downloadTask.addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
//        @Override
//        public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
//
//          downloadProgressBar.setVisibility(View.GONE);
//
//          if(task.isSuccessful()){
//
//            Toast.makeText(itemView.getContext(), "File downloaded successfully!",
//                    Toast.LENGTH_SHORT).show();
//
//            downloadIv.setVisibility(View.GONE);
//
//          }else{
//
//            downloadIv.setImageResource(R.drawable.download_icon);
//            downloadIv.setVisibility(View.VISIBLE);
//            downloadIv.setOnClickListener(PrivateMessagingDocumentVh.this);
//
//            Toast.makeText(itemView.getContext(), "Download Failed! Please try again",
//                    Toast.LENGTH_SHORT).show();
//
//          }
//        }
//      }).addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//
//          downloadIv.setImageResource(R.drawable.download_icon);
//          downloadIv.setVisibility(View.VISIBLE);
//          downloadIv.setOnClickListener(PrivateMessagingDocumentVh.this);
//
//          Toast.makeText(itemView.getContext(), "Download Failed! Please try again",
//                  Toast.LENGTH_SHORT).show();
//
//          int errorCode = ((StorageException) e).getErrorCode();
//          int httpResultCode = ((StorageException) e).getHttpResultCode();
//
//
//          Log.d("ttt","e.getMessage(): "+e.getMessage());
//          Log.d("ttt","errorCode: "+errorCode);
//          Log.d("ttt","httpResultCode: "+httpResultCode);
//
//        }
//      }).addOnCanceledListener(new OnCanceledListener() {
//        @Override
//        public void onCanceled() {
//
//          downloadTask.cancel();
//          downloadProgressBar.setVisibility(View.GONE);
//          downloadIv.setImageResource(R.drawable.download_icon);
//          downloadIv.setOnClickListener(PrivateMessagingDocumentVh.this);
//
//        }
//      });
//
//      downloadIv.setOnClickListener(v-> downloadTask.cancel());
//      if(Files.checkStorageWritePermission((Activity) itemView.getContext())){

      final PrivateMessage message = privateMessages.get(getAdapterPosition());

      if(message.getUploadTask()!=null &&
              message.getUploadTask().isDownloading()){

//        downloadIv.setImageResource(R.drawable.download_icon);
//        downloadIv.setVisibility(View.VISIBLE);
//        downloadIv.setOnClickListener(PrivateMessagingDocumentVh.this);
//        message.getUploadTask().setDownloading(false);

        if(!documentMessageListener.cancelDownload(
                getAdapterPosition(),message.getUploadTask().getDownloadId())){
          Toast.makeText(context, "Failed to cancel download!", Toast.LENGTH_SHORT).show();
        }

      }else{
//
//        downloadIv.setImageResource(R.drawable.cancel_icon);
//        downloadProgressBar.setVisibility(View.VISIBLE);
//        message.getUploadTask().setDownloading(false);
        documentMessageListener.startDownload(
                getAdapterPosition(),
                message.getAttachmentUrl(),
                message.getFileName());

//        if(!documentMessageListener.startDownload(
//                getAdapterPosition(),
//                message.getAttachmentUrl(),
//                message.getFileName())){
//          Toast.makeText(context, "Failed to start download!", Toast.LENGTH_SHORT).show();
//        }

      }

//      notifyItemChanged(getAdapterPosition());
//      DownloadManager downloadManager = (DownloadManager)itemView.getContext().
//                getSystemService(Context.DOWNLOAD_SERVICE);
//
//        final Uri uri = Uri.parse(message.getAttachmentUrl());
//
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//
//        request.setNotificationVisibility(
//                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//
//        String downloadPath =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                .getAbsolutePath();
//
//
//
//        request.setDestinationInExternalFilesDir(itemView.getContext(),
//                Environment.DIRECTORY_DOWNLOADS,
//                message.getFileName());
//
//
//
//
//        downloadManager.enqueue(request);

//      FileProvider.getUriForFile(context,
//              BuildConfig.APPLICATION_ID + ".provider", file)
//
//      setUpDownloadCompleteReceiver();
//
//      itemView.getContext().
//              registerReceiver(downloadReceiver
//                      , new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));



    }


//    private void setUpDownloadCompleteReceiver(){
//        if(downloadReceiver == null){
//          downloadReceiver = new DownloadReceiver(){
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//              super.onReceive(context, intent);
//
//              getAdapterPosition();
//              downloadProgressBar.setVisibility(View.INVISIBLE);
//
//            }
//          };
//
//        }
//    }

  }
  public interface CommentsBottomSheet {

  }
}
