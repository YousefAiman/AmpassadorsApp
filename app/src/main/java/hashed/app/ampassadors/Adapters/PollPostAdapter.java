package hashed.app.ampassadors.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.R;

public class PollPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int POLL_ACTIVE = 1,POLL_ENDED = 2;
    public final ArrayList<PollOption> pollOptions;
    private final String pollPostId;
    private final boolean hasEnded;
    public boolean showProgress;
    public long totalVotes;

    public PollPostAdapter(ArrayList<PollOption> pollOptions,String pollPostId,boolean hasEnded,
                           long totalVotes){
        this.pollOptions = pollOptions;
        this.pollPostId = pollPostId;
        this.hasEnded = hasEnded;
        this.totalVotes = totalVotes;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType){

            case POLL_ENDED:
                return new PollEndedVh(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poll_post_option_item_layout, parent , false));

            case POLL_ACTIVE:
                return new PollChooseVh(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.poll_post_option_item_layout, parent,false));

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){

            case POLL_ENDED:
                ((PollEndedVh)holder).bindItem(pollOptions.get(position));
                break;
            case POLL_ACTIVE:
                ((PollChooseVh)holder).bindItem(pollOptions.get(position));
                break;

        }
    }

    @Override
    public int getItemCount() {
        return pollOptions.size();
    }

    @Override
    public int getItemViewType(int position) {
     return hasEnded?POLL_ENDED:POLL_ACTIVE;
    }

    public class PollChooseVh extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView optionTv;
        private final ProgressBar optionProgress;
        private final ImageView checkIv;

        public PollChooseVh(@NonNull View itemView) {
            super(itemView);
            optionTv = itemView.findViewById(R.id.optionTv);
            optionProgress  = itemView.findViewById(R.id.optionProgress);
            checkIv  = itemView.findViewById(R.id.checkIv);
        }


        private void bindItem(PollOption pollOption){

            if(showProgress && totalVotes > 0){

                Log.d("ttt","pollOption: "+pollOption.getVotes());
                Log.d("ttt","totalVotes: "+totalVotes);

                Log.d("ttt","pollOption.getVotes() / totalVotes) * 100"+
                        (( pollOption.getVotes() / totalVotes) * 100f));

                final int progress = (int)(((float)pollOption.getVotes() / totalVotes) * 100f);

                optionProgress.post(() ->
                        optionProgress.setProgress(progress));
            }

            if(pollOption.isChosen()){

                checkIv.setVisibility(View.VISIBLE);
//                optionTv.setCompoundDrawablesRelativeWithIntrinsicBounds(
//                        ResourcesCompat.getDrawable(
//                                itemView.getResources(),
//                                R.drawable.check_icon_round,null
//                        ),
//                        null,
//                        null,
//                        null
//                );

            }

            optionTv.setText(pollOption.getOption());

            if(!showProgress){
                optionTv.setOnClickListener(this);
            }


        }


        @Override
        public void onClick(View view) {

            if(view.getId() == R.id.optionTv){

                optionTv.setClickable(false);

                voteOnOption(optionTv,getAdapterPosition());

            }
        }
    }

    private class PollEndedVh extends RecyclerView.ViewHolder {

        private final TextView optionTv;
        private final ProgressBar optionProgress;
        private final ImageView checkIv;

        public PollEndedVh(@NonNull View itemView) {
            super(itemView);
            optionTv = itemView.findViewById(R.id.optionTv);
            optionProgress  = itemView.findViewById(R.id.optionProgress);
            checkIv  = itemView.findViewById(R.id.checkIv);
        }


        private void bindItem(PollOption pollOption){

            optionTv.setText(pollOption.getOption());

            if(totalVotes > 0){
                optionProgress.post(new Runnable() {
                    @Override
                    public void run() {
                        optionProgress.setProgress((int)(pollOption.getVotes() / totalVotes) * 100);
                    }
                });
            }

            if(pollOption.isChosen()){
                checkIv.setVisibility(View.VISIBLE);
//                optionTv.setCompoundDrawablesWithIntrinsicBounds(
//                        ResourcesCompat.getDrawable(
//                                itemView.getResources(),
//                                R.drawable.check_icon_round,null)
//                        ,null,null,null);
            }


        }
    }


    public void voteOnOption(TextView optionTv,int position){

        final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String,Object> voteMap = new HashMap<>();
        voteMap.put("userId",currentUid);
        voteMap.put("voteTime",System.currentTimeMillis());
        voteMap.put("voteOption",position);

        final DocumentReference postRef = FirebaseFirestore.getInstance()
                .collection("Posts").document(pollPostId);
        final DocumentReference voteOptionRef =  postRef
                .collection("Options").document(String.valueOf(position));

        postRef.collection("UserVotes").document(currentUid)
                .set(voteMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        voteOptionRef.update("votes", FieldValue.increment(1))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        totalVotes++;

                                        optionTv.setOnClickListener(null);


                                        pollOptions.get(position).setChosen(true);
                                        pollOptions.get(position).setVotes(
                                                pollOptions.get(position).getVotes() + 1
                                        );

                                        showProgress = true;
//                                        for(PollOption pollOption:pollOptions){
//                                            pollOption.setShowProgress(true);
//                                        }

                                        notifyItemRangeChanged(0,pollOptions.size());

                                        postRef.update("totalVotes",FieldValue.increment(1));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        optionTv.setClickable(true);

                                        voteOptionRef.collection("UserVotes")
                                                .document(currentUid).delete();

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(optionTv.getContext(),
                                "Failed to register vote! Please Try again",
                                Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public long getItemId(int position) {
        return pollOptions.get(position).hashCode();
    }
}
