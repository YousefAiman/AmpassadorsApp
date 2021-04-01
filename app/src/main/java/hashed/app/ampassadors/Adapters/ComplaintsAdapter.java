package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import hashed.app.ampassadors.Activities.Complanits_DetailsActivity;
import hashed.app.ampassadors.Objects.Complaints;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ComplainsHolder> {
    private static final CollectionReference usersCollectionRef =
            FirebaseFirestore.getInstance().collection("Users");
    List<Complaints> complaints;
    Context context;

    public ComplaintsAdapter(List<Complaints> complaints, Context context) {
        this.complaints = complaints;
        this.context = context;

    }

    @NonNull
    @Override
    public ComplainsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sug_com_design,
                parent, false);

        return new ComplaintsAdapter.ComplainsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplainsHolder holder, int position) {
        Complaints complaint = complaints.get(position);
        holder.tile.setText(complaint.getTitle());
        holder.readmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Complanits_DetailsActivity.class);
                intent.putExtra("id", complaint.getComplaintsId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    public class ComplainsHolder extends RecyclerView.ViewHolder {

        TextView tile;
        TextView readmore;

        public ComplainsHolder(@NonNull View itemView) {
            super(itemView);
            tile = itemView.findViewById(R.id.title);
            readmore = itemView.findViewById(R.id.title_com);

        }

    }
}
