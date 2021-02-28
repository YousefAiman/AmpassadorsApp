package hashed.app.ampassadors.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Objects.ChatItem;
import hashed.app.ampassadors.Objects.PrivateMessage;
import hashed.app.ampassadors.Objects.PrivateMessagePreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.AudioPlayer;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsVh> {

  private static ArrayList<ChatItem> chatItems;
  private static String currentUid;

  private static final CollectionReference usersCollectionRef =
                  FirebaseFirestore.getInstance().collection("Users");

  private static int blackColor;

  private static Typeface boldFont;


  public ChatsAdapter(ArrayList<ChatItem> chatItems,String currentUid,Context context){
    ChatsAdapter.chatItems = chatItems;
    ChatsAdapter.currentUid = currentUid;

    ChatsAdapter.blackColor = ResourcesCompat.getColor(context.getResources(),
            R.color.black,null);
    ChatsAdapter.boldFont = ResourcesCompat.getFont(context, R.font.segoe_ui_bold);

  }

  @Override
  public int getItemCount() {
    return chatItems.size();
  }

  @NonNull
  @Override
  public ChatsAdapter.ChatsVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new ChatsVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatting_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ChatsVh holder, int position) {

    holder.bindChat(chatItems.get(position));

  }

   static class ChatsVh extends RecyclerView.ViewHolder implements View.OnClickListener{

    private final CircleImageView imageIv;
    private final TextView nameTv,messageTv,timeTv;
    private final Picasso picasso = Picasso.get();

     public ChatsVh(@NonNull View itemView) {
       super(itemView);
       imageIv = itemView.findViewById(R.id.imageIv);
       nameTv = itemView.findViewById(R.id.nameTv);
       messageTv = itemView.findViewById(R.id.messageTv);
       timeTv = itemView.findViewById(R.id.timeTv);
     }

      private void bindChat(ChatItem chatItem){

       if(chatItem.getImageUrl()!=null){
         picasso.load(chatItem.getImageUrl()).fit().into(imageIv);
         nameTv.setText(chatItem.getUsername());
       }else{
         getUserInfo(chatItem,chatItem.getMessagingUid());
       }

        messageTv.setText(getMessageText(chatItem.getMessage()));

        if(!chatItem.isSeen()){
          messageTv.setTypeface(boldFont);
          messageTv.setTextColor(blackColor);
        }

        timeTv.setText(TimeFormatter.formatTime(chatItem.getMessage().getTime()));

        itemView.setOnClickListener(this);

     }

     private String getMessageText(PrivateMessagePreview message){

       String text = null;
       if(message.getDeleted()){

         if(message.getSender().equals(currentUid)){
           text = "You deleted a message";
         }else{
           text = "Deleted message";
         }

       }else{

         switch (message.getType()){

           case Files.TEXT:
             text = message.getContent();
             break;

           case Files.IMAGE:
             text = "Sent an image";
             break;

           case Files.VIDEO:
             text = "Sent a video";
             break;

           case Files.DOCUMENT:
             text = "Sent an attachment";
             break;

           case Files.AUDIO:
             text = "Sent an audio message";
             break;

         }
       }

       return text;
     }

     private void getUserInfo(ChatItem chatItem,String userId){

       usersCollectionRef.whereEqualTo("userId",userId).get()
               .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                 @Override
                 public void onSuccess(QuerySnapshot snapshots) {

                   if(snapshots.isEmpty()){
                     return;
                   }

                   final DocumentSnapshot userSnap = snapshots.getDocuments().get(0);

                   chatItem.setImageUrl(userSnap.getString("imageUrl"));
                   chatItem.setUsername(userSnap.getString("username"));


                 }
               }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
         @Override
         public void onComplete(@NonNull Task<QuerySnapshot> task) {
           if(task.isSuccessful()){
             picasso.load(chatItem.getImageUrl()).into(imageIv);
             nameTv.setText(chatItem.getUsername());
           }
         }
       });

     }

     @Override
     public void onClick(View view) {

       itemView.getContext().startActivity(new Intent(itemView.getContext(),
               PrivateMessagingActivity.class).putExtra("messagingUid",
               chatItems.get(getAdapterPosition()).getMessagingUid()));

     }

   }

}
