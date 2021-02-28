package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hashed.app.ampassadors.R;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollHolder> {
    List<EditText>editTexts ;
    Context context ;
    public PollAdapter(List<EditText>editTexts , Context context  ){
       this.editTexts =  editTexts;
        this.context = context;
    }
    @NonNull
    @Override
    public PollHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.poll_item_list , parent , false);

        return new PollAdapter.PollHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PollHolder holder, int position) {
    EditText editText = editTexts.get(position);
    holder.question1.setText(editText.getText().toString());
    holder.questiuon2.setText(editText.getText().toString());
    holder.add_qus.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (editTexts.size() > 4){
                Toast.makeText(context, "Max Question is  5 ", Toast.LENGTH_SHORT).show();
            }else {
                EditText editText1 = new EditText(context);
                holder.mainlaLayout.addView(editText1);
                editTexts.add(editText1);
            }
        }
    });

    }

    @Override
    public int getItemCount() {
        return editTexts.size();
    }

    public class PollHolder extends RecyclerView.ViewHolder {
        EditText question1 ;
        EditText questiuon2 ;
        ImageButton add_qus;
        LinearLayout mainlaLayout;
        public PollHolder(@NonNull View itemView) {
            super(itemView);
            question1 = itemView.findViewById(R.id.qus1_text);
            questiuon2 = itemView.findViewById(R.id.qus2_text);
            add_qus = itemView.findViewById(R.id.add_qus_btn);
            mainlaLayout =itemView.findViewById(R.id.main_lay);
        }
    }
//    private void addEditView() {
//        // TODO Auto-generated method stub
//        LinearLayout li=new LinearLayout(context);
//        EditText et=new EditText(context);
//        Button b=new Button(context);
//
//        b.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//
//                int pos=(Integer) v.getTag();
//                li.removeViewAt(pos);
//
//            }
//        });
//
//        b.setTag((li.getChildCount()+1));
//    }
}
