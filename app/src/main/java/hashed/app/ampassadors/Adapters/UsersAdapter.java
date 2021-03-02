package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Objects.ChatItem;
import hashed.app.ampassadors.Objects.PrivateMessagePreview;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersVh> implements Filterable {

  private static ArrayList<UserPreview> users;
  private static ArrayList<UserPreview> filteredUsers;
  Context context;

  public ArrayList<String> selectedUserIds;
//  public ArrayList<String> previousSelectedUserIds;

  private final UserClickListener userClickListener;

  public interface UserClickListener{
    void clickUser(String userId,int position);
  }

  public UsersAdapter(ArrayList<UserPreview> users, Context context,UserClickListener
                      userClickListener){
    UsersAdapter.users = users;
    filteredUsers = users;
    this.userClickListener = userClickListener;
    this.context = context;
    selectedUserIds = new ArrayList<>();
  }

  public UsersAdapter(ArrayList<UserPreview> users, Context context,UserClickListener
          userClickListener,ArrayList<String> selectedUserIds){
    UsersAdapter.users = users;
    filteredUsers = users;
    this.userClickListener = userClickListener;
    this.context = context;
    this.selectedUserIds = selectedUserIds;
  }

  @Override
  public int getItemCount() {
    return filteredUsers.size();
  }

  @NonNull
  @Override
  public UsersVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new UsersVh(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull UsersVh holder, int position) {

    holder.bindChat(filteredUsers.get(position));

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

  public class UsersVh extends RecyclerView.ViewHolder implements View.OnClickListener{

    private final CircleImageView imageIv;
    private final ImageView statusIv,selectedIv;
    private final TextView nameTv,statusTv;
    private final Picasso picasso = Picasso.get();

     public UsersVh(@NonNull View itemView) {
       super(itemView);
       imageIv = itemView.findViewById(R.id.imageIv);
       nameTv = itemView.findViewById(R.id.nameTv);
       statusTv = itemView.findViewById(R.id.statusTv);
       statusIv = itemView.findViewById(R.id.statusIv);
       selectedIv = itemView.findViewById(R.id.selectedIv);
     }

      private void bindChat(UserPreview user){

       if(user.getUserId() == null)
         return;

       if(selectedUserIds.contains(user.getUserId())){
         selectedIv.setVisibility(View.VISIBLE);

       }else{
         selectedIv.setVisibility(View.GONE);
       }

       if(user.getImageUrl()!=null){
         picasso.load(user.getImageUrl()).fit().into(imageIv);
       }

        nameTv.setText(user.getUsername());

       if(user.isOnline()){

         DrawableCompat.setTint(
                 DrawableCompat.wrap(statusIv.getDrawable()),
                 itemView.getContext().getResources().getColor(R.color.neon_green)
         );

         statusTv.setText(R.string.online);
       }else{

         DrawableCompat.setTint(
                 DrawableCompat.wrap(statusIv.getDrawable()),
                 itemView.getContext().getResources().getColor(R.color.red)
         );
         statusTv.setText(R.string.offline);
       }

         itemView.setOnClickListener(v-> userClickListener.clickUser(user.getUserId(),getAdapterPosition()));

     }

     @Override
     public void onClick(View view) {


//       itemView.getContext().startActivity(new Intent(itemView.getContext(),
//               PrivateMessagingActivity.class).putExtra("messagingUid",
//               chatItems.get(getAdapterPosition()).getMessagingUid()));

     }

   }

}
