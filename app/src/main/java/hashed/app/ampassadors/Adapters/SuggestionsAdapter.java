package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
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

import hashed.app.ampassadors.Activities.Suggestions_DtailesActivity;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.Suggestions;
import hashed.app.ampassadors.R;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionHolder> {

    Context context;
    List<Suggestions> suggestions;

    private static final CollectionReference usersCollectionRef =
            FirebaseFirestore.getInstance().collection("Users");

    public SuggestionsAdapter(Context context, List<Suggestions> suggestions) {
        this.context = context;
        this.suggestions = suggestions;

    }

    @NonNull
    @Override
    public SuggestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sug_com_design, parent, false);

        return new SuggestionsAdapter.SuggestionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionHolder holder, int position) {
        Suggestions suggestion = suggestions.get(position);

        holder.username.setText(suggestion.getUserid());
        holder.tile.setText(suggestion.getTitle());
        holder.readmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Suggestions_DtailesActivity.class);
                intent.putExtra("id", suggestion.getSuggestionId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public class SuggestionHolder extends RecyclerView.ViewHolder {

        TextView username;
        TextView tile;
        TextView readmore;

        public SuggestionHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            tile = itemView.findViewById(R.id.title);
            readmore = itemView.findViewById(R.id.title_com);

        }

        private void getUserInfo(PostData postData, String userId) {

            usersCollectionRef.document(userId).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            if (documentSnapshot.exists()) {

                                postData.setPublisherName(documentSnapshot.getString("username"));

                            }

                        }
                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    username.setText(postData.getPublisherName());

                }
            });
        }


        private void bind(PostData postData) {
            if (postData.getPublisherName() == null) {

                getUserInfo(postData, postData.getPublisherId());

            } else {

                if (postData.getPublisherImage() != null) {

                }

                username.setText(postData.getPublisherName());

            }


        }

    }
}