package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hashed.app.ampassadors.R;

public class PollItemsRecyclerAdapter extends RecyclerView.Adapter<PollItemsRecyclerAdapter.PollVh> {

  private final static int POLL_LIMIT = 100;
  private final ArrayList<String> pollOptions;
  private final Context context;

  public PollItemsRecyclerAdapter(ArrayList<String> pollOptions, Context context) {
    this.pollOptions = pollOptions;
    this.context = context;
  }

  @NonNull
  @Override
  public PollVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new PollVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.poll_option_item_layout, parent, false)
    );
  }

  @Override
  public void onBindViewHolder(@NonNull PollVh holder, int position) {

    holder.pollEd.setHint(pollOptions.get(position));

    if (position == getItemCount() - 1 && position != POLL_LIMIT) {
      holder.pollAddIv.setVisibility(View.VISIBLE);
      holder.pollAddIv.setOnClickListener(v -> {
        holder.pollAddIv.setVisibility(View.INVISIBLE);
        addPollItem(position + 1);
      });
    } else {
      holder.pollAddIv.setVisibility(View.INVISIBLE);
      holder.pollAddIv.setOnClickListener(null);
    }

  }

  private void addPollItem(int position) {

    pollOptions.add(context.getResources().getString(R.string.option)+" "+ (position + 1));
    notifyItemInserted(pollOptions.size());
  }
  @Override
  public int getItemCount() {
    return pollOptions.size();
  }

  public static class PollVh extends RecyclerView.ViewHolder {
    private final EditText pollEd;
    private final ImageView pollAddIv;

    public PollVh(@NonNull View itemView) {
      super(itemView);
      pollEd = itemView.findViewById(R.id.pollEd);
      pollAddIv = itemView.findViewById(R.id.pollAddIv);
    }
  }
}
