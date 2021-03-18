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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
  private static final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


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
  //date formats
  private final DateFormat
          hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
          withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
          formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
          todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
          todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());
  private final Context context;
  private boolean longCLickEnabled = true;

  public PrivateMessagingAdapter(ArrayList<PrivateMessage> privateMessages,
                                 Context context,
                                 DeleteMessageListener deleteMessageListener,
                                 VideoMessageListener videoMessageListener,
                                 DocumentMessageListener documentMessageListener,
                                 ImageMessageListener imageMessageListener
          , boolean isForGroup
  ) {

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
    void playVideo(String url);
  }

  public interface ImageMessageListener {
    void showImage(String url);
  }


  public interface DocumentMessageListener {

    void startDownload(int adapterPosition, String url, String fileName);

    boolean cancelDownload(int adapterPosition, long downloadId);

  }

  static class PrivateMessagingTextVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv;
    private TextView senderTv;

    public PrivateMessagingTextVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
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
    }

    @Override
    public boolean onLongClick(View view) {

      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());

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
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv;
    private final ImageView imageIv;
    private final Picasso picasso = Picasso.get();
    private TextView senderTv;

    public PrivateMessagingImageVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
      imageIv = itemView.findViewById(R.id.imageIv);
      if (isForGroup) {
        senderTv = itemView.findViewById(R.id.senderTv);
      }
    }

    private void bindMessage(PrivateMessage message, boolean bindUsername) {

      if (message == null)
        return;

      if (message.getAttachmentUrl() != null) {
        picasso.load(message.getAttachmentUrl()).fit().into(imageIv);
      }

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }


      messageTv.setText(message.getContent());

      imageIv.setOnClickListener(this);

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if (view.getId() == R.id.imageIv) {
        if (privateMessages.get(getAdapterPosition()).getAttachmentUrl() != null) {
          imageMessageListener.showImage(privateMessages.get(
                  getAdapterPosition()).getAttachmentUrl());
        }
      }

    }

  }

  static class PrivateMessagingAudioVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener,
          MediaPlayer.OnCompletionListener {

    private final Slider audioProgressSlider;
    private final ImageView playIv;

    private AudioPlayer audioPlayer;
    private MediaPlayer lastMediaPlayer;
    private int lastClicked = -1;
    private TextView senderTv;
//
//    private AnimatedVectorDrawable playToPauseDrawable;
//    private AnimatedVectorDrawable pauseToPlayDrawable;

    public PrivateMessagingAudioVh(@NonNull View itemView) {
      super(itemView);
      audioProgressSlider = itemView.findViewById(R.id.audioProgressSlider);
//      imageIv = itemView.findViewById(R.id.imageIv);
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

        playIv.setClickable(false);

      }

      if (bindUsername && !message.getSender().equals(currentUid)) {
        getUserName(message.getSender(), senderTv);
      }

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }


    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      if (view.getId() == R.id.playIv) {

        Log.d("audioMessage", "item clicked");
        PrivateMessage message = privateMessages.get(getAdapterPosition());


        if (lastClicked == -1) {
          Log.d("audioMessage", "lastClicked == -1");

          audioPlayer = new AudioPlayer(itemView.getContext(),
                  message.getAttachmentUrl(), message.getLength(),
                  audioProgressSlider, playIv);

          lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
                  (int) audioProgressSlider.getValue() : 0);

          lastMediaPlayer.setOnCompletionListener(this);

          lastClicked = getAdapterPosition();

        } else if (lastClicked == getAdapterPosition()) {
          Log.d("audioMessage", "lastClicked == getAdapterPosition()");
          if (audioPlayer == null) {

            Log.d("audioMessage", "audioPlayer == null");

            audioPlayer = new AudioPlayer(itemView.getContext(),
                    message.getAttachmentUrl(), message.getLength(),
                    audioProgressSlider, playIv);

            lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
                    (int) audioProgressSlider.getValue() : 0);

            lastMediaPlayer.setOnCompletionListener(this);

            lastClicked = getAdapterPosition();

          } else {

            Log.d("audioMessage", "audioPlayer != null");

            if (lastMediaPlayer.isPlaying()) {

              Log.d("audioMessage", "lastMediaPlayer.isPlaying()");

              audioPlayer.pausePlayer();
            } else {

              Log.d("audioMessage", "lastMediaPlayer is paused");

              audioPlayer.resumePlayer();
            }

          }


        } else {

          Log.d("audioMessage", "last clicked new");

          if (audioPlayer != null) {
            Log.d("audioMessage", "audioPlayer!=null");
            audioPlayer.releasePlayer();
          }

          audioPlayer = new AudioPlayer(itemView.getContext(),
                  message.getAttachmentUrl(), message.getLength(),
                  audioProgressSlider, playIv);

          lastMediaPlayer = audioPlayer.startPlaying(audioProgressSlider.getValue() > 0 ?
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
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView messageTv;
    private final ImageView imageIv;
    private final ImageView playIv;
    private final Picasso picasso = Picasso.get();
    private final ProgressBar videoProgressBar;
    private TextView senderTv;

    public PrivateMessagingVideoVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
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


      } else {
        itemView.setOnClickListener(null);
      }

      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }


    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      videoMessageListener.playVideo(privateMessages.get(getAdapterPosition()).getAttachmentUrl());

    }

  }

  static class PrivateMessagingZoomVh extends RecyclerView.ViewHolder
          implements View.OnLongClickListener, View.OnClickListener {

    private final TextView titleTv, senderTv, timeTv, startTimeTv, durationTv;

    public PrivateMessagingZoomVh(@NonNull View itemView) {
      super(itemView);
      titleTv = itemView.findViewById(R.id.titleTv);
      senderTv = itemView.findViewById(R.id.senderTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      startTimeTv = itemView.findViewById(R.id.startTimeTv);
      durationTv = itemView.findViewById(R.id.durationTv);
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
      durationTv.setText(String.valueOf(meeting.getDuration()));

      if (meeting.getType() == 1) {
        startTimeTv.setText(TimeFormatter.formatTime(message.getTime()));
      } else {
        startTimeTv.setText(TimeFormatter.formatTime(meeting.getStartTime()));
      }

      itemView.setOnClickListener(this);
      if (message.getSender().equals(currentUid)) {
        itemView.setOnLongClickListener(this);
      }

    }

    @Override
    public boolean onLongClick(View view) {
      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {

      final ZoomMeeting meeting = privateMessages.get(getAdapterPosition()).getZoomMeeting();
      if (meeting.getType() == 2 && meeting.getStartTime() < System.currentTimeMillis()) {
        Toast.makeText(itemView.getContext(),
                "Meeting will start on: " + TimeFormatter.formatTime(meeting.getStartTime())
                , Toast.LENGTH_SHORT).show();
        return;
      } else if (meeting.getType() == 1) {

        if (privateMessages.get(getAdapterPosition()).getTime() +
                (privateMessages.get(getAdapterPosition()).getZoomMeeting().getDuration() *
                        DateUtils.MINUTE_IN_MILLIS) >= System.currentTimeMillis()) {

          //meeting has ended
          Toast.makeText(itemView.getContext(),
                  "Meeting has ended! you can't join it"
                  , Toast.LENGTH_SHORT).show();
          return;
        }

      }

      final PackageManager pm = itemView.getContext().getPackageManager();
//      Intent intent = pm.getLaunchIntentForPackage("us.zoom.videomeetings");
//
//      if (intent != null) {
//
//        itemView.getContext().startActivity(intent);
//
//      }else{

      final Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
              privateMessages.get(getAdapterPosition()).getZoomMeeting().getJoinUrl()));
      try {

        if (urlIntent.resolveActivity(pm) != null) {
          itemView.getContext().startActivity(urlIntent);
        }

      } catch (NullPointerException ignored) {

      }
//      }


//      if(view.getId() == R.id.imageIv){
//        imageMessageListener.showImage(privateMessages.get(getAdapterPosition())
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

      if (!privateMessages.get(getAdapterPosition()).getSender().equals(currentUid)) {
        getUserName(privateMessages.get(getAdapterPosition()).getSender(), senderTv);
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

    //    private DownloadReceiver downloadReceiver;
    public PrivateMessagingDocumentVh(@NonNull View itemView) {
      super(itemView);
      messageTv = itemView.findViewById(R.id.messageTv);
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
      showMessageDeletionDialog(privateMessages.get(getAdapterPosition()), itemView.getContext());
      return false;
    }

    @Override
    public void onClick(View view) {


      final PrivateMessage message = privateMessages.get(getAdapterPosition());

      if (message.getUploadTask() != null &&
              message.getUploadTask().isDownloading()) {

        if (!documentMessageListener.cancelDownload(
                getAdapterPosition(), message.getUploadTask().getDownloadId())) {
          Toast.makeText(context, "Failed to cancel download!", Toast.LENGTH_SHORT).show();
        }

      } else {
        documentMessageListener.startDownload(
                getAdapterPosition(),
                message.getAttachmentUrl(),
                message.getFileName());

      }


    }

  }

}
