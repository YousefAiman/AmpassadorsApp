package hashed.app.ampassadors.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PrivateMessagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  //message types



  //received message items
    static final int
            MSG_TYPE_LEFT_TEXT = 11,
            MSG_TYPE_LEFT_IMAGE = 12,
            MSG_TYPE_LEFT_AUDIO = 13,
            MSG_TYPE_LEFT_VIDEO = 14,
            MSG_TYPE_LEFT_FILE = 15;

  //sent message items
  static final int
            MSG_TYPE_RIGHT_TEXT = 21,
            MSG_TYPE_RIGHT_IMAGE = 22,
            MSG_TYPE_RIGHT_AUDIO = 23,
            MSG_TYPE_RIGHT_VIDEO = 24,
            MSG_TYPE_RIGHT_FILE = 25;


  //date formats
  private final DateFormat
          hourMinuteFormat = new SimpleDateFormat("h:mm a", Locale.getDefault()),
          withoutYearFormat = new SimpleDateFormat("h:mm a MMM dd", Locale.getDefault()),
          formatter = new SimpleDateFormat("h:mm a yyyy MMM dd", Locale.getDefault()),
          todayYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault()),
          todayYearMonthDayFormat = new SimpleDateFormat("yyyy MMM dd", Locale.getDefault());


//  private final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

  private static String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  private static final Date date = new Date();

   private boolean longCLickEnabled = true;

  static ArrayList<PrivateMessage> privateMessages;
  private final Context context;

  private static DeleteMessageListener deleteMessageListener;

  public interface DeleteMessageListener{
    void deleteMessage(PrivateMessage message, DialogInterface dialog);
  }

  void disableLongClick(){
    longCLickEnabled = false;
  }

  public PrivateMessagingAdapter(ArrayList<PrivateMessage> privateMessages,
                                 Context context,
                                 DeleteMessageListener deleteMessageListener) {

    PrivateMessagingAdapter.privateMessages = privateMessages;
    this.context = context;
    PrivateMessagingAdapter.deleteMessageListener = deleteMessageListener;

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
        return  message.getSender().equals(currentUid)?MSG_TYPE_LEFT_VIDEO:MSG_TYPE_LEFT_VIDEO;

      case Files.FILE:
        return  message.getSender().equals(currentUid)?MSG_TYPE_RIGHT_FILE:MSG_TYPE_LEFT_FILE;

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


}
