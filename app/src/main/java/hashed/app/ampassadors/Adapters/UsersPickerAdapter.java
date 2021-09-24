package hashed.app.ampassadors.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UsersPickerAdapter extends RecyclerView.Adapter<UsersPickerAdapter.UsersVh>
        implements Filterable {

  public ArrayList<String> selectedUserIds;
  private final ArrayList<UserPreview> users;
  private ArrayList<UserPreview> filteredUsers;

  public UsersPickerAdapter(ArrayList<UserPreview> users, ArrayList<String> selectedUserIds) {
    this.users = users;
    this.selectedUserIds = selectedUserIds;
  }

  public UsersPickerAdapter(ArrayList<UserPreview> users, ArrayList<String> selectedUserIds,
                            boolean isFiltered) {
    filteredUsers = users;
    this.users = users;
    this.selectedUserIds = selectedUserIds;
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
  public UsersVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new UsersVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.select_user_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull UsersVh holder, int position) {

    if (filteredUsers != null) {
      holder.bindChat(filteredUsers.get(position));
    } else {
      holder.bindChat(users.get(position));
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

  public class UsersVh extends RecyclerView.ViewHolder implements View.OnClickListener,
          CompoundButton.OnCheckedChangeListener {

    private final CircleImageView userIv;
    private final TextView usernameTv;
    private final CheckBox selectCheckBox;

    public UsersVh(@NonNull View itemView) {
      super(itemView);
      userIv = itemView.findViewById(R.id.userIv);
      usernameTv = itemView.findViewById(R.id.usernameTv);
      selectCheckBox = itemView.findViewById(R.id.selectCheckBox);
    }

    private void bindChat(UserPreview user) {

      if (user.getUserId() == null)
        return;

      selectCheckBox.setChecked(selectedUserIds.contains(user.getUserId()));

      if (user.getImageUrl() != null) {
        Picasso.get().load(user.getImageUrl()).fit().centerCrop().into(userIv);
      }else{
        userIv.setImageResource(R.color.white);
      }

      usernameTv.setText(user.getUsername());

      itemView.setOnClickListener(this);
//      selectCheckBox.setOnCheckedChangeListener(this);

    }

    @Override
    public void onClick(View view) {

      Log.d("ttt","clicked");
      if (filteredUsers != null) {

        if (selectedUserIds.contains(filteredUsers.get(getAdapterPosition()).getUserId())) {
          selectCheckBox.setChecked(false);
          selectedUserIds.remove(filteredUsers.get(getAdapterPosition()).getUserId());
        } else {
          selectCheckBox.setChecked(true);
          selectedUserIds.add(filteredUsers.get(getAdapterPosition()).getUserId());
        }

      } else {

        if (selectedUserIds.contains(users.get(getAdapterPosition()).getUserId())) {
          selectCheckBox.setChecked(false);
          selectedUserIds.remove(users.get(getAdapterPosition()).getUserId());
        } else {
          selectCheckBox.setChecked(true);
          selectedUserIds.add(users.get(getAdapterPosition()).getUserId());
        }

      }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

      if (filteredUsers != null) {

        if(b){
          selectedUserIds.add(filteredUsers.get(getAdapterPosition()).getUserId());
        }else{
          selectedUserIds.remove(filteredUsers.get(getAdapterPosition()).getUserId());
        }

      } else {

        if(b){
          selectedUserIds.add(users.get(getAdapterPosition()).getUserId());
        }else{
          selectedUserIds.remove(users.get(getAdapterPosition()).getUserId());
        }
      }

    }
  }
}