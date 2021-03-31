package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Objects.ChatItem;
import hashed.app.ampassadors.Objects.PrivateMessagePreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsVh> {

  private static final CollectionReference usersCollectionRef =
          FirebaseFirestore.getInstance().collection("Users");
  private final ArrayList<ChatItem> chatItems;
  private final String currentUid;
  private final Typeface boldFont,normalFont;


  public ChatsAdapter(ArrayList<ChatItem> chatItems, String currentUid, Context context) {
    this.chatItems = chatItems;
    this.currentUid = currentUid;

    this.boldFont = ResourcesCompat.getFont(context, R.font.segoe_ui_bold);
    this.normalFont = ResourcesCompat.getFont(context, R.font.segoe_ui);

  }

  @Override
  public int getItemCount() {
    return chatItems.size();
  }

  @NonNull
  @Override
  public ChatsVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new ChatsVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.chatting_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull ChatsVh holder, int position) {

    holder.bindChat(chatItems.get(position));

  }

  public class ChatsVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final CircleImageView imageIv;
    private final TextView nameTv, messageTv, timeTv,unSeenTv;
    private final Picasso picasso = Picasso.get();

    public ChatsVh(@NonNull View itemView) {
      super(itemView);
      imageIv = itemView.findViewById(R.id.imageIv);
      nameTv = itemView.findViewById(R.id.nameTv);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
      unSeenTv = itemView.findViewById(R.id.unSeenTv);
    }

    private void bindChat(ChatItem chatItem) {

      if (chatItem.getImageUrl() != null) {
        if (!chatItem.getImageUrl().isEmpty()) {
          picasso.load(chatItem.getImageUrl()).fit().centerCrop().into(imageIv);
        }
        nameTv.setText(chatItem.getUsername());
      } else {
        getUserInfo(chatItem, chatItem.getMessagingUid(), imageIv, nameTv);
      }

      messageTv.setText(getMessageText(chatItem.getMessage()));

      if (!chatItem.isSeen()) {
        messageTv.setTypeface(boldFont);
      }else{
        messageTv.setTypeface(normalFont);
      }

      if(chatItem.getUnSeenCount() > 0){
        unSeenTv.setVisibility(View.VISIBLE);
        unSeenTv.setText(String.valueOf(chatItem.getUnSeenCount()));
      }else{
        unSeenTv.setVisibility(View.GONE);
      }

      timeTv.setText(TimeFormatter.formatTime(chatItem.getMessage().getTime()));

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

      itemView.getContext().startActivity(new Intent(itemView.getContext(),
              PrivateMessagingActivity.class).putExtra("messagingUid",
              chatItems.get(getAdapterPosition()).getMessagingUid()));

    }

    private String getMessageText(PrivateMessagePreview message) {

      String text = null;

      if (message.getSender().equals(currentUid)) {

        if (message.getDeleted()) {
          text = "You deleted a message";
        }else{
          switch (message.getType()) {

            case Files.TEXT:
              text = message.getContent();
              break;

            case Files.IMAGE:
              text = "You sent an image";
              break;

            case Files.VIDEO:
              text = "You sent a video";
              break;

            case Files.DOCUMENT:
              text = "You sent an attachment";
              break;

            case Files.AUDIO:
              text = "You sent an audio message";
              break;

          }
        }
      } else {

        if (message.getDeleted()) {
          text = "Deleted message";
        }else{
          switch (message.getType()) {

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
      }


      return text;
    }

    private void getUserInfo(ChatItem chatItem, String userId, ImageView imageIv, TextView nameTv) {

      usersCollectionRef.document(userId).get()
              .addOnSuccessListener(documentSnapshot -> {

                if (documentSnapshot.exists()) {
                  chatItem.setImageUrl(documentSnapshot.getString("imageUrl"));
                  chatItem.setUsername(documentSnapshot.getString("username"));
                }
              }).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                  if (chatItem.getImageUrl() != null && !chatItem.getImageUrl().isEmpty()) {
                    picasso.load(chatItem.getImageUrl()).fit().centerCrop().into(imageIv);
                  }
                  nameTv.setText(chatItem.getUsername());
                }
              });

    }

  }

}
