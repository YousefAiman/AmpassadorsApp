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

import hashed.app.ampassadors.Objects.PollItem;
import hashed.app.ampassadors.R;

public class PollItemsRecyclerAdapter extends RecyclerView.Adapter<PollItemsRecyclerAdapter.PollVh>{

    ArrayList<PollItem> pollItems;

    public PollItemsRecyclerAdapter(ArrayList<PollItem> pollItems){
        this.pollItems = pollItems;
    }

    @NonNull
    @Override
    public PollVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PollVh(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poll_item_layout , parent , false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PollVh holder, int position) {

//        if(getItemCount() == 5){
//            holder.pollAddIv.setVisibility(View.VISIBLE);
//        }

        holder.pollEd.setHint(pollItems.get(position).getChoice());

        if(position == getItemCount() - 1 && position != 4){
            holder.pollAddIv.setVisibility(View.VISIBLE);
            holder.pollAddIv.setOnClickListener(v-> {
                holder.pollAddIv.setVisibility(View.INVISIBLE);
                addPollItem(position+1);


            });
        }else{
            holder.pollAddIv.setVisibility(View.INVISIBLE);
            holder.pollAddIv.setOnClickListener(null);
        }

    }

    private void addPollItem(int position){

        String pollText = "الخيار "+ (position + 1);
        pollItems.add(new PollItem(pollText,false));
        notifyItemInserted(pollItems.size());

    }

    @Override
    public int getItemCount() {
        return pollItems.size();
    }

    public static class PollVh extends RecyclerView.ViewHolder {
        EditText pollEd;
        ImageView pollAddIv;
        public PollVh(@NonNull View itemView) {
            super(itemView);
            pollEd = itemView.findViewById(R.id.pollEd);
            pollAddIv  = itemView.findViewById(R.id.pollAddIv);

        }
    }
}
