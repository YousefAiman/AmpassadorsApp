package hashed.app.ampassadors.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class GroupMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final ArrayList<UserPreview> users;
  private final GroupMemberClickListener groupMemberClickListener;
  private List<String> adminIds;
  public interface GroupMemberClickListener {
    void clickUser(String userId);
  }

  public GroupMembersAdapter(ArrayList<UserPreview> users,List<String> adminIds,
                             GroupMemberClickListener groupMemberClickListener) {
    this.users = users;
    this.adminIds = adminIds;
    this.groupMemberClickListener = groupMemberClickListener;
  }

  public void setAdminIds(List<String> newAdminsIds){
    this.adminIds = newAdminsIds;
  }

  public List<String> getAdminIds(){
    return adminIds;
  }

  @Override
  public int getItemCount() {
      return users.size();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new GroupMembersVH(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.group_member_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    ((GroupMembersVH) holder).bind(users.get(position));
  }


  public class GroupMembersVH extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final CircleImageView memberImageIv;
    private final TextView memberNameTv,groupAdminTv;

    public GroupMembersVH(@NonNull View itemView) {
      super(itemView);
      memberImageIv = itemView.findViewById(R.id.memberImageIv);
      memberNameTv = itemView.findViewById(R.id.memberNameTv);
      groupAdminTv = itemView.findViewById(R.id.groupAdminTv);
    }

    private void bind(UserPreview user) {

      if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
        Picasso.get().load(user.getImageUrl()).fit().into(memberImageIv);
      }else{
        memberImageIv.setImageResource(R.color.white);
      }

      memberNameTv.setText(user.getUsername());

      if(adminIds.contains(user.getUserId())){
        groupAdminTv.setVisibility(View.VISIBLE);
      }else{
        groupAdminTv.setVisibility(View.GONE);
      }

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      groupMemberClickListener.clickUser(users.get(getAdapterPosition()).getUserId());
    }

  }



}