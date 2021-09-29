package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hashed.app.ampassadors.Activities.Suggestions_DtailesActivity;
import hashed.app.ampassadors.Objects.Suggestions;
import hashed.app.ampassadors.R;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.SuggestionHolder> {


    Context context;
    List<Suggestions> suggestions;

    public SuggestionsAdapter(Context context, List<Suggestions> suggestions) {
        this.context = context;
        this.suggestions = suggestions;

    }

    @NonNull
    @Override
    public SuggestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sug_com_design, parent,
                false);

        return new SuggestionsAdapter.SuggestionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionHolder holder, int position) {
        Suggestions suggestion = suggestions.get(position);

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
        TextView tile;
        TextView readmore;
        public SuggestionHolder(@NonNull View itemView) {
            super(itemView);
            tile = itemView.findViewById(R.id.title);
            readmore = itemView.findViewById(R.id.title_com);
        }
    }

}