package hashed.app.ampassadors.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.format.DateUtils;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.Objects.ZoomMeeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.AudioPlayer;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;
import hashed.app.ampassadors.Utils.WifiUtil;

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
          MSG_TYPE_RIGHT_DOCUMENT = 25,
          MSG_TYPE_RIGHT_ZOOM = 26;


  //received group message items
  static final int
          MSG_TYPE_LEFT_TEXT_GROUP = 31,
          MSG_TYPE_LEFT_IMAGE_GROUP = 32,
          MSG_TYPE_LEFT_AUDIO_GROUP = 33,
          MSG_TYPE_LEFT_VIDEO_GROUP = 34,
          MSG_TYPE_LEFT_DOCUMENT_GROUP = 35,
          MSG_TYPE_LEFT_ZOOM_GROUP = 36;

  //received group message items
  static final int
          MSG_TYPE_LEFT_DELETED = 41,
          MSG_TYPE_RIGHT_DELETED = 42,
          MSG_TYPE_LEFT_DELETED_GROUP = 43;
  private static String currentUid;


//  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private static final Date date = new Date();
  public static boolean isForGroup;
  public static CollectionReference usersRef =
          FirebaseFirestore.getInstance().collection("Users");
  static ArrayList<PrivateMessage> privateMessages;
  //Group
  static Map<String, String> userNamesMap;
  private static DeleteMessageListener deleteMessageListener;
  private static VideoMessageListener videoMessageListener;
  private static ImageMessageListener imageMessageListener;
  private static DocumentMessageListener documentMessageListener;
  private static TimeClickListener timeClickListener;
  //date formats
//  private final DateFormat
//          hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
//          withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
//          formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
//          todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
//          todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());
  private final Context context;
  private boolean longCLickEnabled = true;

  //audio messages
  private AudioPlayer audioPlayer;
  private MediaPlayer lastMediaPlayer;
  private int lastClicked = -1;

  public PrivateMessagingAdapter(ArrayList<PrivateMessage> privateMessages,
                                 Context context,
                                 DeleteMessageListener deleteMessageListener,
                                 VideoMessageListener videoMessageListener,
                                 DocumentMessageListener documentMessageListener,
                                 ImageMessageListener imageMessageListener,
                                 TimeClickListener timeClickListener,
                                 boolean isForGroup
  ) {

    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    PrivateMessagingAdapter.privateMessages = privateMessages;
    this.context = context;
    PrivateMessagingAdapter.isForGroup = isForGroup;
    if (isForGroup) {
      userNamesMap = new HashMap<>();
    }
    PrivateMessagingAdapter.deleteMessageListener = deleteMessageListener;
    PrivateMessagingAdapter.videoMessageListener = videoMessageListener;
    PrivateMessagingAdapter.documentMessageListener = documentMessageListener;
    PrivateMessagingAdapter.imageMessageListener = imageMessageListener;
    PrivateMessagingAdapter.timeClickListener = timeClickListener;
  }

  private static void getUserName(String userId, TextView tv) {

    if (userNamesMap.containsKey(userId)) {
      tv.setText(userNamesMap.get(userId));
    } else {

      usersRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          if (documentSnapshot.exists()) {
            userNamesMap.put(userId, documentSnapshot.getString("username"));
          }
        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if (task.isSuccessful() && userNamesMap.containsKey(userId)) {
            tv.setText(userNamesMap.get(userId));
          }
        }
      });

    }
  }

  private static void getUserImage(String userId, ImageView iv) {
    usersRef.document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists() && documentSnapshot.contains("imageUrl")) {

          final String imageUrl = documentSnapshot.getString("imageUrl");
          if(imageUrl!=null && !imageUrl.isEmpty()){
            Picasso.get().load(imageUrl).fit().centerCrop().into(iv);
          }
        }
      }
    });
  }


  private static void showMessageDeletionDialog(PrivateMessage message, Context context) {
    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(R.string.message_delete);
    alert.setPositiveButton(R.string.delete, (dialog, which) -> {
      deleteMessageListener.deleteMessage(message, dialog);
    });
    alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
    alert.create().show();
  }

  void disableLongClick() {
    longCLickEnabled = false;
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

    switch (viewType) {

      case MSG_TYPE_LEFT_DELETED:
        return new PrivateMessagingDeletedVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_revceived_deleted_message, parent,
                        false));

      case MSG_TYPE_RIGHT_DELETED:
        return new PrivateMessagingDeletedVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_deleted_message, parent,
                        false));

      case MSG_TYPE_LEFT_DELETED_GROUP:
        return new PrivateMessagingDeletedGroupVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_deleted_message, parent,
                        false));


      case MSG_TYPE_LEFT_TEXT:
        return new PrivateMessagingTextVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_text_message, parent,
                        false));

      case MSG_TYPE_RIGHT_TEXT:
        return new PrivateMessagingTextVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_text_message, parent,
                        false));

      case MSG_TYPE_LEFT_TEXT_GROUP:
        return new PrivateMessagingTextVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_text_message, parent,
                        false));

      case MSG_TYPE_LEFT_IMAGE:
        return new PrivateMessagingImageVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_image_message, parent,
                        false));

      case MSG_TYPE_RIGHT_IMAGE:
        return new PrivateMessagingImageVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_image_message, parent,
                        false));

      case MSG_TYPE_LEFT_IMAGE_GROUP:
        return new PrivateMessagingImageVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_image_message, parent,
                        false));


      case MSG_TYPE_LEFT_AUDIO:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_audio_message, parent,
                        false));

      case MSG_TYPE_RIGHT_AUDIO:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_audio_message, parent,
                        false));

      case MSG_TYPE_LEFT_AUDIO_GROUP:
        return new PrivateMessagingAudioVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_audio_message, parent,
                        false));


      case MSG_TYPE_LEFT_VIDEO:
        return new PrivateMessagingVideoVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_video_message, parent,
                        false));


      case MSG_TYPE_RIGHT_VIDEO:
        return new PrivateMessagingVideoVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_video_message, parent,
                        false));


      case MSG_TYPE_LEFT_VIDEO_GROUP:
        return new PrivateMessagingVideoVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_video_message, parent,
                        false));


      case MSG_TYPE_LEFT_DOCUMENT:
        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_received_document_message, parent,
                        false));


      case MSG_TYPE_RIGHT_DOCUMENT:
        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.private_chat_item_sent_document_message, parent,
                        false));

      case MSG_TYPE_LEFT_DOCUMENT_GROUP:
        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_document_message, parent,
                        false));

//      case MSG_TYPE_LEFT_ZOOM:
//        return new PrivateMessagingDocumentVh(LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.private_chat_item_sent_document_message, parent,
//                        false));

      case MSG_TYPE_RIGHT_ZOOM:

        return new PrivateMessagingZoomVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_sent_zoom_message, parent,
                        false));

      case MSG_TYPE_LEFT_ZOOM_GROUP:
        return new PrivateMessagingZoomVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_chat_item_received_zoom_message, parent,
                        false));

    }

    return null;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


    switch (holder.getItemViewType()) {

      case MSG_TYPE_LEFT_TEXT:
      case MSG_TYPE_RIGHT_TEXT:
        ((PrivateMessagingTextVh) holder).bindMessage(privateMessages.get(position),
                false);
        break;

      case MSG_TYPE_LEFT_TEXT_GROUP:
        ((PrivateMessagingTextVh) holder).bindMessage(privateMessages.get(position),
                true);
        break;


      case MSG_TYPE_LEFT_IMAGE:
      case MSG_TYPE_RIGHT_IMAGE:

        ((PrivateMessagingImageVh) holder).bindMessage(privateMessages.get(position),
                false);

        break;

      case MSG_TYPE_LEFT_IMAGE_GROUP:
        ((PrivateMessagingImageVh) holder).bindMessage(privateMessages.get(position),
                true);
        break;


      case MSG_TYPE_LEFT_AUDIO:
      case MSG_TYPE_RIGHT_AUDIO:

        ((PrivateMessagingAudioVh) holder).bindMessage(privateMessages.get(position),
                false);

        break;

      case MSG_TYPE_LEFT_AUDIO_GROUP:
        ((PrivateMessagingAudioVh) holder).bindMessage(privateMessages.get(position),
                true);
        break;

      case MSG_TYPE_LEFT_VIDEO:
      case MSG_TYPE_RIGHT_VIDEO:

        ((PrivateMessagingVideoVh) holder).bindMessage(privateMessages.get(position),
                false);

        break;

      case MSG_TYPE_LEFT_VIDEO_GROUP:
        ((PrivateMessagingVideoVh) holder).bindMessage(privateMessages.get(position),
                true);
        break;

      case MSG_TYPE_LEFT_DOCUMENT:
      case MSG_TYPE_RIGHT_DOCUMENT:

        ((PrivateMessagingDocumentVh) holder).bindMessage(privateMessages.get(position),
                false);
        break;

      case MSG_TYPE_LEFT_DOCUMENT_GROUP:
        ((PrivateMessagingDocumentVh) holder).bindMessage(privateMessages.get(position),
                true);
        break;


      case MSG_TYPE_RIGHT_ZOOM:
      case MSG_TYPE_LEFT_ZOOM_GROUP:

        ((PrivateMessagingZoomVh) holder).bindMessage(privateMessages.get(position));

        break;

      case MSG_TYPE_LEFT_DELETED_GROUP:

        ((PrivateMessagingDeletedGroupVh) holder).bindUserName();

        break;
    }

  }

  @Override
  public int getItemViewType(int position) {

    final PrivateMessage message = privateMessages.get(position);


    if (message.getDeleted()) {
      return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_DELETED :
              isForGroup ? MSG_TYPE_LEFT_DELETED_GROUP : MSG_TYPE_LEFT_DELETED;
    }

    switch (message.getType()) {

      case Files.TEXT:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_TEXT :
                isForGroup ? MSG_TYPE_LEFT_TEXT_GROUP : MSG_TYPE_LEFT_TEXT;

      case Files.IMAGE:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_IMAGE :
                isForGroup ? MSG_TYPE_LEFT_IMAGE_GROUP : MSG_TYPE_LEFT_IMAGE;

      case Files.AUDIO:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_AUDIO :
                isForGroup ? MSG_TYPE_LEFT_AUDIO_GROUP : MSG_TYPE_LEFT_AUDIO;

      case Files.VIDEO:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_VIDEO :
                isForGroup ? MSG_TYPE_LEFT_VIDEO_GROUP : MSG_TYPE_LEFT_VIDEO;

      case Files.DOCUMENT:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_DOCUMENT :
                isForGroup ? MSG_TYPE_LEFT_DOCUMENT_GROUP : MSG_TYPE_LEFT_DOCUMENT;

      case Files.ZOOM:
        return message.getSender().equals(currentUid) ? MSG_TYPE_RIGHT_ZOOM :
                MSG_TYPE_LEFT_ZOOM_GROUP;

      default:
        return 0;
    }
  }

  public interface DeleteMessageListener {
    void deleteMessage(PrivateMessage message, DialogInterface dialog);
  }

  public interface VideoMessageListener {
    void playVideo(String url,String fileName);
  }

  public interface ImageMessageListener {
    void showImage(String url,String fileName);
  }

  public interface TimeClickListener {
    void hideTime(int itemPosition);
  }


  public interface DocumentMessageListener {

    void startDownload(int adapterPosition, String url, String fileName);

    boolean cancelDownload(int adapterPosition, long downloadId);

  }

  static class PrivateMessagingTextVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv,timeTv;
    private TextView senderTv;


    public PrivateMessagingTextVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }

//       messageTimeTv =  itemView.findViewById(R.id.messageTimeTv);
    }


    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;

      messageTv.setText(message.getContent());

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }


      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }
      itemView.setOnClickListener(this);
    }

    @Override
    public boolean onLongClick(View view) {

      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());

      return false;
    }

    @Override
    public void onClick(View view) {

      Log.d("ttt","clicked");

//      timeClickListener.hideTime(getBindingAdapterPosition());

      showOrHideTime(timeTv,getBindingAdapterPosition());

    }

  }


  static class PrivateMessagingImageVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv,timeTv;
    private final ImageView imageIv;
    private final Picasso picasso = Picasso.get();
    private TextView senderTv;

    public PrivateMessagingImageVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }
    }

    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;

      if (message.getAttachmentUrl() != null) {
        picasso.load(message.getAttachmentUrl()).fit().centerCrop().into(imageIv);
      }

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }


      messageTv.setText(message.getContent());

      imageIv.setOnClickListener(this);

      itemView.setOnClickListener(this);
      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if (view.getId() == R.id.imageIv) {
        if (privateMessages.get(getBindingAdapterPosition()).getAttachmentUrl() != null) {
          imageMessageListener.showImage(privateMessages.get(getBindingAdapterPosition()).getAttachmentUrl(),
                  privateMessages.get(getBindingAdapterPosition()).getFileName());
        }
      }else{
        showOrHideTime(timeTv,getBindingAdapterPosition());
      }

    }

  }

   class PrivateMessagingAudioVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener,
          MediaPlayer.OnCompletionListener {

    private final Slider audioProgressSlider;
    private final ImageView playIv,imageIv;
    private TextView senderTv;
    private final TextView timeTv;
//
//    private AnimatedVectorDrawable playToPauseDrawable;
//    private AnimatedVectorDrawable pauseToPlayDrawable;

    public PrivateMessagingAudioVh(@NonNull View itemView) {
      super(itemView);
      audioProgressSlider = itemView.findViewById(R.id.audioProgressSlider);
      timeTv = itemView.findViewById(R.id.timeTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      playIv = itemView.findViewById(R.id.playIv);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }
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

    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;

      if (message.getAttachmentUrl() != null) {
        playIv.setOnClickListener(this);
      } else {
        itemView.setClickable(false);
        playIv.setClickable(false);
      }

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }

      getUserImage(message.getSender(), imageIv);

      itemView.setOnClickListener(this);
      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

//
//      if(message.isStopPlayingAudio()){
//
//
//
////        if (audioPlayer != null) {
////          Log.d("audioMessage", "audioPlayer != null");
////          if (lastMediaPlayer.isPlaying()) {
////            Log.d("audioMessage", "lastMediaPlayer.isPlaying()");
////            audioPlayer.pausePlayer();
////          }
////        }
//      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if (view.getId() == R.id.playIv) {

        if(WifiUtil.checkWifiConnection(itemView.getContext())) {
          Log.d("audioMessage", "lastClicked: " + lastClicked);

          PrivateMessage message = privateMessages.get(getBindingAdapterPosition());

//        if(lastClicked != -1 && lastClicked != getBindingAdapterPosition()){
//
//          if (audioPlayer != null) {
//            Log.d("audioMessage","previous audio player is not null so releasing it");
//            audioPlayer.releasePlayer();
////            privateMessages.get(lastClicked).setStopPlayingAudio(true);
////            getBindingAdapter().notifyItemChanged(lastClicked);
////            notifyItemChanged(lastClicked);
//          }else {
//
//            audioPlayer = new AudioPlayer(itemView.getContext(),
//                    message.getAttachmentUrl(), message.getLength(),
//                    audioProgressSlider, playIv);
//
//            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
//                    (int) audioProgressSlider.getValue() : 0);
//
//            lastMediaPlayer.setOnCompletionListener(this);
//
//          }
////            else {
////            if (lastMediaPlayer.isPlaying()) {
////              audioPlayer.pausePlayer();
////            } else {
////              audioPlayer.resumePlayer();
////            }
////          }
//        }else{
//
//          if (audioPlayer == null) {
//
//            Log.d("audioMessage", "audioPlayer == null");
//
//            audioPlayer = new AudioPlayer(itemView.getContext(),
//                    message.getAttachmentUrl(), message.getLength(),
//                    audioProgressSlider, playIv);
//
//            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
//                    (int) audioProgressSlider.getValue() : 0);
//
//            lastMediaPlayer.setOnCompletionListener(this);
//
//
//          } else {
//
//            if (lastMediaPlayer.isPlaying()) {
//              audioPlayer.pausePlayer();
//            } else {
//              audioPlayer.resumePlayer();
//            }
//
//          }
//
//        }
//
//        Log.d("ttt","lastClicked: "+lastClicked);
//
//        lastClicked = getBindingAdapterPosition();
          if (lastClicked == -1) {

            audioPlayer = new AudioPlayer(itemView.getContext(),
                    message.getAttachmentUrl(), message.getLength(),
                    audioProgressSlider, playIv);

            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
                    (int) audioProgressSlider.getValue() : 0);

            lastMediaPlayer.setOnCompletionListener(this);

          } else if (lastClicked == getBindingAdapterPosition()) {

            if (lastMediaPlayer != null && audioPlayer != null) {
              if (lastMediaPlayer.isPlaying()) {
                audioPlayer.pausePlayer();
              } else {
                audioPlayer.resumePlayer();
              }
            } else {

              audioPlayer = new AudioPlayer(itemView.getContext(),
                      message.getAttachmentUrl(), message.getLength(),
                      audioProgressSlider, playIv);

              lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
                      (int) audioProgressSlider.getValue() : 0);

              lastMediaPlayer.setOnCompletionListener(this);

            }

//          if (audioPlayer == null) {
//
//            Log.d("audioMessage", "audioPlayer == null");
//
//            audioPlayer = new AudioPlayer(itemView.getContext(),
//                    message.getAttachmentUrl(), message.getLength(),
//                    audioProgressSlider, playIv);
//
//            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
//                    (int) audioProgressSlider.getValue() : 0);
//
//            lastMediaPlayer.setOnCompletionListener(this);
//
//
//          } else {
//
//
//            Log.d("audioMessage", "audioPlayer != null");
//
//            if(lastMediaPlayer!=null){
//              if (lastMediaPlayer.isPlaying()) {
//
//                Log.d("audioMessage", "lastMediaPlayer.isPlaying()");
//
//                audioPlayer.pausePlayer();
//              } else {
//
//                Log.d("audioMessage", "lastMediaPlayer is paused");
//
//                audioPlayer.resumePlayer();
//              }
//            }
//          }


          } else {

            Log.d("audioMessage", "last clicked new");

            if (lastMediaPlayer != null && audioPlayer != null) {
              audioPlayer.releasePlayer();
              audioPlayer = null;
              lastMediaPlayer = null;
            }

            audioPlayer = new AudioPlayer(itemView.getContext(),
                    message.getAttachmentUrl(), message.getLength(),
                    audioProgressSlider, playIv);

            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
                    (int) audioProgressSlider.getValue() : 0);

            lastMediaPlayer.setOnCompletionListener(this);

          }

          lastClicked = getBindingAdapterPosition();

        }
      }else{
        showOrHideTime(timeTv,getBindingAdapterPosition());
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
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv,timeTv;
    private final ImageView imageIv,playIv;
    private final Picasso picasso = Picasso.get();
    private final ProgressBar videoProgressBar;
    private TextView senderTv;

    public PrivateMessagingVideoVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      playIv = itemView.findViewById(R.id.playIv);
      videoProgressBar = itemView.findViewById(R.id.videoProgressBar);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }
    }


    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;

      messageTv.setText(message.getContent());

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }

      if (message.getVideoThumbnail() != null) {

        picasso.load(message.getVideoThumbnail()).fit().centerCrop().into(imageIv, new Callback() {
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


      } else {
        imageIv.setOnClickListener(null);
      }

      itemView.setOnClickListener(this);

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }


    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if(view.getId() == imageIv.getId()){

        videoMessageListener.playVideo(privateMessages.get(getBindingAdapterPosition()).getAttachmentUrl(),
                privateMessages.get(getBindingAdapterPosition()).getFileName());
      }else{
        showOrHideTime(timeTv,getBindingAdapterPosition());
      }

    }

  }

  static class PrivateMessagingZoomVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView titleTv, senderTv, timeTv, startTimeTv, estimatedTimeRangeTv;

    public PrivateMessagingZoomVh(@NonNull View itemView) {
      super(itemView);
      titleTv = itemView.findViewById(R.id.titleTv);
      senderTv = itemView.findViewById(R.id.senderTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      startTimeTv = itemView.findViewById(R.id.startTimeTv);
      estimatedTimeRangeTv = itemView.findViewById(R.id.estimatedTimeRangeTv);
    }

    private void bindMessage(PrivateMessage message) {

      if (message == null)
        return;

      if (!message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }

      timeTv.setText(TimeFormatter.formatTime(message.getTime()));

      final ZoomMeeting meeting = message.getZoomMeeting();
      titleTv.setText(meeting.getTopic());

//      int hours = meeting.getDuration() / 60;
//      int minutes = meeting.getDuration() % 60;

      estimatedTimeRangeTv.setText(
              TimeFormatter.formatWithPattern(meeting.getEstimatedStartTime(),TimeFormatter.HOUR_MINUTE)
      +" - " + TimeFormatter.formatWithPattern(meeting.getEstimatedEndTime(),TimeFormatter.HOUR_MINUTE));

//      durationTv.setText(String.format(Locale.getDefault(),"%d:%d", hours, minutes));

      if (meeting.getType() == 1) {
        startTimeTv.setText(TimeFormatter.formatTime(message.getTime()));
      } else {
        startTimeTv.setText(TimeFormatter.formatTime(meeting.getStartTime()));
      }

//      if(meeting.getStatus().equals("ended")){
//        itemView.setOnClickListener(null);
//      }else{
        itemView.setOnClickListener(this);
//      }

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      final ZoomMeeting meeting = privateMessages.get(getBindingAdapterPosition()).getZoomMeeting();

      if(meeting.getStatus().equals("ended")){
        Toast.makeText(itemView.getContext(),"This meeting has already ended!", Toast.LENGTH_SHORT).show();
        return;
      }

      if (meeting.getType() == 2 && meeting.getStartTime() < System.currentTimeMillis()) {
        Toast.makeText(itemView.getContext(),
                "Meeting will start on: " + TimeFormatter.formatTime(meeting.getStartTime())
                , Toast.LENGTH_SHORT).show();
        return;
      }


//      else if (meeting.getType() == 1) {
//
//        if (privateMessages.get(getBindingAdapterPosition()).getTime() +
//                (privateMessages.get(getBindingAdapterPosition()).getZoomMeeting().getDuration() *
//                        DateUtils.MINUTE_IN_MILLIS) >= System.currentTimeMillis()) {
//
//          //meeting has ended
//          Toast.makeText(itemView.getContext(),
//                  "Meeting has ended! you can't join it"
//                  , Toast.LENGTH_SHORT).show();
//          return;
//        }
//
//      }

      final PackageManager pm = itemView.getContext().getPackageManager();
//      Intent intent = pm.getLaunchIntentForPackage("us.zoom.videomeetings");
//
//      if (intent != null) {
//
//        itemView.getContext().startActivity(intent);
//
//      }else{

      final Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
              privateMessages.get(getBindingAdapterPosition()).getZoomMeeting().getJoinUrl()));
      try {

        if (urlIntent.resolveActivity(pm) != null) {
          itemView.getContext().startActivity(urlIntent);
        }

      } catch (NullPointerException ignored) {

      }
//      }


//      if(view.getId() == R.id.imageIv){
//        imageMessageListener.showImage(privateMessages.get(getBindingAdapterPosition())
//                .getAttachmentUrl());
//      }

    }

  }

  static class PrivateMessagingDeletedVh extends RecyclerView.ViewHolder {
    public PrivateMessagingDeletedVh(@NonNull View itemView) {
      super(itemView);
    }
  }

  static class PrivateMessagingDeletedGroupVh extends RecyclerView.ViewHolder {

    private final TextView senderTv;

    public PrivateMessagingDeletedGroupVh(@NonNull View itemView) {
      super(itemView);
      senderTv = itemView.findViewById(R.id.senderTv);
    }

    private void bindUserName() {

      if (!privateMessages.get(getBindingAdapterPosition()).getSender().equals(currentUid)) {
        getUserName(privateMessages.get(getBindingAdapterPosition()).getSender(), senderTv);
      }

    }

  }

  class PrivateMessagingDocumentVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv;
    private final TextView documentNameTv;
    private final ImageView downloadIv;
    private final ProgressBar downloadProgressBar;
    private boolean isDownloading;
    private TextView senderTv;
    private final TextView timeTv;

    //    private DownloadReceiver downloadReceiver;
    public PrivateMessagingDocumentVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      documentNameTv = itemView.findViewById(R.id.documentNameTv);
      downloadIv = itemView.findViewById(R.id.downloadIv);
      downloadProgressBar = itemView.findViewById(R.id.downloadProgressBar);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }
    }


    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;


      messageTv.setText(message.getContent());
      documentNameTv.setText(message.getFileName());

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }
      itemView.setOnClickListener(this);
      if (message.getAttachmentUrl() != null) {

        downloadIv.setOnClickListener(this);

        if (message.getUploadTask() != null) {

          if (message.getUploadTask().isCompleted()) {

            downloadIv.setVisibility(View.GONE);
            downloadProgressBar.setVisibility(View.GONE);

          } else if (message.getUploadTask().isDownloading()) {

            downloadIv.setImageResource(R.drawable.close_icon);
            downloadProgressBar.setVisibility(View.VISIBLE);

          }
        } else {

          downloadProgressBar.setVisibility(View.GONE);
          downloadIv.setImageResource(R.drawable.download_icon);
          downloadIv.setVisibility(View.VISIBLE);

        }
      } else {

        downloadProgressBar.setVisibility(View.VISIBLE);
        downloadIv.setVisibility(View.GONE);

      }

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getBindingAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if(view.getId() == downloadIv.getId()) {
        final PrivateMessage message = privateMessages.get(getBindingAdapterPosition());

        if (message.getUploadTask() != null &&
                message.getUploadTask().isDownloading()) {

          if (!documentMessageListener.cancelDownload(
                  getBindingAdapterPosition(), message.getUploadTask().getDownloadId())) {
            Toast.makeText(context, "Failed to cancel download!", Toast.LENGTH_SHORT).show();
          }

        } else {
          documentMessageListener.startDownload(
                  getBindingAdapterPosition(),
                  message.getAttachmentUrl(),
                  message.getFileName());

        }
      }else{
        showOrHideTime(timeTv,getBindingAdapterPosition());
      }

    }

  }
  private static void showOrHideTime(TextView timeTv,int position){
    if (timeTv.getVisibility() == View.GONE) {
      timeTv.setText(TimeFormatter.formatTime(privateMessages
              .get(position).getTime()));

      timeTv.setVisibility(View.VISIBLE);
    } else {
      timeTv.setVisibility(View.GONE);
    }

  }

}
