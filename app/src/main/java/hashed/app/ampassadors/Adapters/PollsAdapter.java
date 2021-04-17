package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Activities.PostPollActivity;
import hashed.app.ampassadors.Objects.PollOption;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.PostPollPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;

public class PollsAdapter extends RecyclerView.Adapter<PollsAdapter.PollPreviewVh> {

  private final CollectionReference postsCollectionRef;
  private final List<PostPollPreview> posts;
  private final String currentUid;
  private final Context context;
  public List<Integer> loadingItems;

  public PollsAdapter(List<PostPollPreview> posts, Context context) {
    this.posts = posts;
    this.context = context;

    postsCollectionRef = FirebaseFirestore.getInstance().collection("Posts");
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    loadingItems = new ArrayList<>();

  }

  @NonNull
  @Override
  public PollPreviewVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PollPreviewVh(LayoutInflater.from(context).inflate(
                R.layout.poll_preview_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull PollPreviewVh holder, int position) {
    ((PollPreviewVh) holder).bind(posts.get(position));
  }

  @Override
  public int getItemCount() {
    return posts.size();
  }

  public class PollPreviewVh extends RecyclerView.ViewHolder
          implements View.OnClickListener {

    private final TextView questionTv;
    private final TextView showMoreTv;
    private final RecyclerView pollRv;

    public PollPreviewVh(@NonNull View itemView) {
      super(itemView);
      questionTv = itemView.findViewById(R.id.questionTv);
      pollRv = itemView.findViewById(R.id.pollRv);
      showMoreTv = itemView.findViewById(R.id.showMoreTv);
    }

    private void bind(PostPollPreview postData) {

      questionTv.setText(postData.getTitle());

      if(postData.getPollOptions() == null){
        postData.setChosenPollOption(-1);
      }

      if(postData.getChosenPollOption() == -1 &&
              postData.getPollOptions() == null){

        postsCollectionRef.document(postData.getPostId())
                .collection("UserVotes").document(currentUid)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
          @Override
          public void onSuccess(DocumentSnapshot snapshot) {

            if (snapshot.exists()) {
              final int option = snapshot.get("voteOption", Integer.class);
              postData.setChosenPollOption(option);
            }

            checkShowProgress(postData);
          }
        });

      }else{

        checkShowProgress(postData);
      }

      itemView.setOnClickListener(this);

    }

    private void checkShowProgress(PostPollPreview postData){

      if (postData.isPollEnded()) {
        getPollRecycler(postData,true,true);
      } else {

        if (System.currentTimeMillis() >
                postData.getPublishTime() + postData.getPollDuration()) {

          postsCollectionRef.document(postData.getPostId())
                  .update("pollEnded", true);

          getPollRecycler(postData,true,true);

        } else {

          getPollRecycler(postData,false,
                  postData.getChosenPollOption() != -1);

        }
      }

    }
    private void getPollRecycler(PostPollPreview postData,boolean hasEnded,boolean showProgress) {

      if(postData.getPollOptions() == null){
//        postData.setChosenPollOption(-1);
        postData.setPollOptions(new ArrayList<>());
      }

      final PollPostAdapter adapter = new PollPostAdapter(postData.getPollOptions()
              , postData.getPostId(), hasEnded, postData.getTotalVotes());

      adapter.setChosenOption(postData.getChosenPollOption());

      adapter.showProgress = showProgress;

      pollRv.setNestedScrollingEnabled(false);
      pollRv.setHasFixedSize(true);
      adapter.setHasStableIds(true);
      pollRv.setAdapter(adapter);

      if(!loadingItems.contains(getAdapterPosition())){

        loadingItems.add(getAdapterPosition());

        postsCollectionRef.document(postData.getPostId())
                .collection("Options")
                .orderBy("votes", Query.Direction.DESCENDING)
                .orderBy("option", Query.Direction.ASCENDING)
                .limit(6)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                  @Override
                  public void onSuccess(QuerySnapshot snapshots) {

                    if (!snapshots.isEmpty()) {
                      if(snapshots.size() == 6){
                        showMoreTv.setVisibility(View.VISIBLE);
                        for(int i=0;i<snapshots.size()-1;i++){
                          postData.getPollOptions().add(
                                  snapshots.getDocuments().get(i).toObject(PollOption.class));
                        }
                      }else{
                        showMoreTv.setVisibility(View.GONE);
                        postData.getPollOptions().addAll(snapshots.toObjects(PollOption.class));
                      }
                    }
                  }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
          @Override
          public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful() && !postData.getPollOptions().isEmpty()) {
              adapter.notifyDataSetChanged();
            }
          }
        });

      }
    }

    @Override
    public void onClick(View view) {
      view.getContext().startActivity(new Intent(view.getContext(), PostPollActivity.class)
              .putExtra("postId", posts.get(getAdapterPosition()).getPostId())
              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
  }
}
