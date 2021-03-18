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
  private static ArrayList<ChatItem> chatItems;
  private static String currentUid;
  private static int blackColor;

  private static Typeface boldFont;


  public ChatsAdapter(ArrayList<ChatItem> chatItems, String currentUid, Context context) {
    ChatsAdapter.chatItems = chatItems;
    ChatsAdapter.currentUid = currentUid;

    ChatsAdapter.blackColor = ResourcesCompat.getColor(context.getResources(),
            R.color.black, null);
    ChatsAdapter.boldFont = ResourcesCompat.getFont(context, R.font.segoe_ui_bold);

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

  static class ChatsVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final CircleImageView imageIv;
    private final TextView nameTv, messageTv, timeTv;
    private final Picasso picasso = Picasso.get();

    public ChatsVh(@NonNull View itemView) {
      super(itemView);
      imageIv = itemView.findViewById(R.id.imageIv);
      nameTv = itemView.findViewById(R.id.nameTv);
      messageTv = itemView.findViewById(R.id.messageTv);
      timeTv = itemView.findViewById(R.id.timeTv);
    }

    private void bindChat(ChatItem chatItem) {


      if (chatItem.getImageUrl() != null) {
        if (!chatItem.getImageUrl().isEmpty()) {
          picasso.load(chatItem.getImageUrl()).fit().into(imageIv);
        }
        nameTv.setText(chatItem.getUsername());
      } else {
        getUserInfo(chatItem, chatItem.getMessagingUid(), imageIv, nameTv);
      }

      messageTv.setText(getMessageText(chatItem.getMessage()));

      if (!chatItem.isSeen()) {
        messageTv.setTypeface(boldFont);
        messageTv.setTextColor(blackColor);
      }

      timeTv.setText(TimeFormatter.formatTime(chatItem.getMessage().getTime()));

      itemView.setOnClickListener(this);

    }

    private String getMessageText(PrivateMessagePreview message) {

      String text = null;

      if (message.getSender().equals(currentUid)) {

        if (message.getDeleted()) {
          text = "You deleted a message";
        }

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
      } else {

        if (message.getDeleted()) {
          text = "Deleted message";
        }

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


      return text;
    }


    @Override
    public void onClick(View view) {

      itemView.getContext().startActivity(new Intent(itemView.getContext(),
              PrivateMessagingActivity.class).putExtra("messagingUid",
              chatItems.get(getAdapterPosition()).getMessagingUid()));

    }


    private void getUserInfo(ChatItem chatItem, String userId, ImageView imageIv, TextView nameTv) {

      usersCollectionRef.document(userId).get()
              .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                  if (documentSnapshot.exists()) {
                    chatItem.setImageUrl(documentSnapshot.getString("imageUrl"));
                    chatItem.setUsername(documentSnapshot.getString("username"));
                  }
                }
              }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

          if (task.isSuccessful()) {
            if (chatItem.getImageUrl() != null && !chatItem.getImageUrl().isEmpty()) {
              picasso.load(chatItem.getImageUrl()).into(imageIv);
            }
            nameTv.setText(chatItem.getUsername());
          }
        }
      });

    }

  }

}
