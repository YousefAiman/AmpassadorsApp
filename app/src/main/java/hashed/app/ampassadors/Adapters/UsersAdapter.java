package hashed.app.ampassadors.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Activities.ProfileActiv;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {

  private final int itemLayout;
  private ArrayList<UserPreview> users;
  private ArrayList<UserPreview> filteredUsers;
  private UserAdapterClicker userAdapterClicker;
  private String currentUid;

  public interface UserAdapterClicker {
    void clickUser(String userId);
  }

  public UsersAdapter(ArrayList<UserPreview> users, int itemLayout) {
    this.users = users;
    this.itemLayout = itemLayout;
  }


  public UsersAdapter(ArrayList<UserPreview> users, int itemLayout,
                      UserAdapterClicker userAdapterClicker) {
    this.users = users;
    this.filteredUsers = users;
    this.userAdapterClicker = userAdapterClicker;
    this.itemLayout = itemLayout;
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  }


  @Override
  public int getItemCount() {
    if (filteredUsers != null) {
      return filteredUsers.size();
    } else {
      return users.size();
    }
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    if (itemLayout == R.layout.user_item_layout) {

      return new UsersVh(LayoutInflater.from(parent.getContext())
              .inflate(itemLayout, parent, false));
    } else {
      return new UsersPickedVh(LayoutInflater.from(parent.getContext())
              .inflate(itemLayout, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    if (itemLayout == R.layout.user_item_layout) {
      if(filteredUsers!=null){
        ((UsersVh) holder).bindChat(filteredUsers.get(position));
      }else{
        ((UsersVh) holder).bindChat(users.get(position));
      }

    } else if (itemLayout == R.layout.user_picked_preview_item_layout) {

      if(filteredUsers!=null){
        ((UsersPickedVh) holder).bindChat(filteredUsers.get(position));
      }else{
        ((UsersPickedVh) holder).bindChat(users.get(position));
      }

    }

  }

  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        String filterString = constraint.toString().toLowerCase();
        FilterResults results = new FilterResults();

        final List<UserPreview> list = users;

        int count = list.size();
        final ArrayList<UserPreview> nlist = new ArrayList<>(count);

        UserPreview filteredUser;

        for (int i = 0; i < count; i++) {
          filteredUser = list.get(i);
          if (list.get(i).getUsername().toLowerCase().contains(filterString)) {
            nlist.add(filteredUser);
          }
        }
        results.values = nlist;
        results.count = nlist.size();

        return results;
      }

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        filteredUsers = (ArrayList<UserPreview>) results.values;
        notifyDataSetChanged();
      }
    };
  }




  public static class UsersPickedVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final CircleImageView userIv;
    private final TextView usernameTv;
    private final Picasso picasso = Picasso.get();

    public UsersPickedVh(@NonNull View itemView) {
      super(itemView);
      userIv = itemView.findViewById(R.id.userIv);
      usernameTv = itemView.findViewById(R.id.usernameTv);
    }

    private void bindChat(UserPreview user) {

      if (user.getUserId() == null)
        return;

      if (user.getImageUrl() != null) {
        picasso.load(user.getImageUrl()).fit().into(userIv);
      }else{
        userIv.setImageResource(R.color.white);
      }

      usernameTv.setText(user.getUsername());
    }

    @Override
    public void onClick(View view) {
//      itemView.getContext().startActivity(new Intent(itemView.getContext(),
//              PrivateMessagingActivity.class).putExtra("messagingUid",
//              users.get(getAdapterPosition()).getUserId()));

    }

  }

  public class UsersVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ImageView imageIv;
    private final ImageView statusIv;
    private final TextView nameTv, statusTv;
    private final Picasso picasso = Picasso.get();

    public UsersVh(@NonNull View itemView) {
      super(itemView);
      imageIv = itemView.findViewById(R.id.imageIv);
      nameTv = itemView.findViewById(R.id.nameTv);
      statusTv = itemView.findViewById(R.id.statusTv);
      statusIv = itemView.findViewById(R.id.statusIv);
    }

    private void bindChat(UserPreview user) {

      if (user.getUserId() == null)
        return;
      if (user.getImageUrl() != null) {
        picasso.load(user.getImageUrl()).fit().centerCrop().into(imageIv);
      }else{
        imageIv.setImageResource(R.color.white);
      }

      nameTv.setText(user.getUsername());

      if (user.isStatus()) {

        DrawableCompat.setTint(
                DrawableCompat.wrap(statusIv.getDrawable()),
                itemView.getContext().getResources().getColor(R.color.neon_green)
        );

        statusTv.setText(R.string.online);

      } else {

        DrawableCompat.setTint(
                DrawableCompat.wrap(statusIv.getDrawable()),
                itemView.getContext().getResources().getColor(R.color.red)
        );
        statusTv.setText(R.string.offline);

      }

      itemView.setOnClickListener(this);
//         itemView.setOnClickListener(v->
//                 userClickListener.clickUser(user.getUserId(),getAdapterPosition()));
    }
    @Override
    public void onClick(View view) {

      if (userAdapterClicker != null) {
        userAdapterClicker.clickUser(filteredUsers.get(getAdapterPosition()).getUserId());
      } else {
        itemView.getContext().startActivity(new Intent(itemView.getContext(),
                PrivateMessagingActivity.class).putExtra("messagingUid",
                users.get(getAdapterPosition()).getUserId())
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        imageIv.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            imageIv.getContext().startActivity(new Intent(itemView.getContext(),
                    ProfileActiv.class).putExtra("userId",users.get(getAdapterPosition())
                    .getUserId()).putExtra("ImageUrl",
                    users.get(getAdapterPosition()).getImageUrl())
                    .putExtra("username",users.get(getAdapterPosition()).getUsername()));
          }
        });
      }
      }
  }
}