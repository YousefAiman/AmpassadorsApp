package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Objects.UserApprovment;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.R;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminHolder> {

  Context context;
  List<UserApprovment> data;

  public AdminAdapter(Context context, List<UserApprovment> data) {
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

    holder.bind(data.get(position));

  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public class AdminHolder extends RecyclerView.ViewHolder {

    TextView email;
    TextView password;
    Button delete_account;
    Button approve_account;
    Spinner options;

    public AdminHolder(@NonNull View itemView) {
      super(itemView);
      email = itemView.findViewById(R.id.ema);
      password = itemView.findViewById(R.id.pass);
      delete_account = itemView.findViewById(R.id.delete_account);
      approve_account = itemView.findViewById(R.id.approve_account);
      options = itemView.findViewById(R.id.options);
    }

    private void bind(UserApprovment userApprovment){
      email.setText(userApprovment.getUsername());
      password.setText(userApprovment.getEmail());
      delete_account.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          FirebaseFirestore.getInstance()
                  .collection("Users").document(userApprovment.getUserId())
                  .update("rejected",false).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              data.remove(userApprovment);
              notifyItemRemoved(getAdapterPosition());
            }
          });
        }
      });
     approve_account.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {


          FirebaseFirestore.getInstance()
                  .collection("Users").document(userApprovment.getUserId())
                  .update("Role",options.getSelectedItem().toString())
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                      data.remove(userApprovment);
//                      notifyItemRemoved(getAdapterPosition());
                      Toast.makeText(context, "Validity Confirmation Is deleted", Toast.LENGTH_SHORT).show();
                    }
                  });
        }
      });

    }
  }
}
