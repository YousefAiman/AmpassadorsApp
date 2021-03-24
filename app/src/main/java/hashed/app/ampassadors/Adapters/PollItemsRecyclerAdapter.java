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
  private final String option;
  private final ScrollToBottomListener scrollToBottomListener;
  public interface ScrollToBottomListener{
    void scrollToBottom();
  }

  public PollItemsRecyclerAdapter(ArrayList<String> pollOptions, Context context,
                                  ScrollToBottomListener scrollToBottomListener) {
    this.pollOptions = pollOptions;
    this.scrollToBottomListener = scrollToBottomListener;
    option = context.getResources().getString(R.string.option);
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
    holder.bind(position);
  }

  private void addPollItem(int position) {
    pollOptions.add(option+" "+ (position + 1));
    notifyItemInserted(pollOptions.size());
    scrollToBottomListener.scrollToBottom();
  }

  @Override
  public int getItemCount() {
    return pollOptions.size();
  }

  public class PollVh extends RecyclerView.ViewHolder {
    private final EditText pollEd;
    private final ImageView pollAddIv;

    public PollVh(@NonNull View itemView) {
      super(itemView);
      pollEd = itemView.findViewById(R.id.pollEd);
      pollAddIv = itemView.findViewById(R.id.pollAddIv);
    }

    private void bind(int position){
      pollEd.setHint(pollOptions.get(position));

      if (position == getItemCount() - 1 && position != POLL_LIMIT) {
        pollAddIv.setVisibility(View.VISIBLE);
        pollAddIv.setOnClickListener(v -> {
          pollAddIv.setVisibility(View.INVISIBLE);
          addPollItem(position + 1);
        });
      } else {
        pollAddIv.setVisibility(View.INVISIBLE);
        pollAddIv.setOnClickListener(null);
      }

    }

  }
}
