package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminHolder> {

  Context context;
  List<UserInfo> data;

  public AdminAdapter(Context context, List<UserInfo> data) {
    this.context = context;
    this.data = data;
  }

  @NonNull
  @Override
  public AdminHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.admin_layout, parent, false);
    return new AdminAdapter.AdminHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull AdminHolder holder, int position) {
    UserInfo user_info = data.get(position);
    holder.email.setText(user_info.getEmail());
    holder.password.setText(user_info.getPassword());
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public class AdminHolder extends RecyclerView.ViewHolder {
    TextView email;
    TextView password;
    LinearLayout linearLayout;

    public AdminHolder(@NonNull View itemView) {
      super(itemView);
      email = itemView.findViewById(R.id.ema);
      password = itemView.findViewById(R.id.pass);
      linearLayout = itemView.findViewById(R.id.item_recycle);
    }
  }
}
