package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.PollItemsAdapter;
import hashed.app.ampassadors.Adapters.PollItemsRecyclerAdapter;
import hashed.app.ampassadors.Objects.PollItem;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class CreatePollActivity extends AppCompatActivity implements View.OnClickListener {


    private ImageView userIv;
    private TextView usernameTv;
    private EditText questionEd;
    private TextView timeTv;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference reference;
    private RecyclerView pollRv;
    private Button publishBtn;
    private ArrayList<PollItem> pollItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poll);

        SetUpCompetent();

        getUserInfo();


        createPollItemsList();


    }


    public void SetUpCompetent() {

        userIv = findViewById(R.id.userIv);
        usernameTv = findViewById(R.id.usernameTv);
        publishBtn = findViewById(R.id.publishBtn);
        questionEd = findViewById(R.id.questionEd);
        timeTv = findViewById(R.id.timeTv);
        pollRv = findViewById(R.id.pollRv);
        NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
        nestedScrollView.setNestedScrollingEnabled(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v-> finish());

        publishBtn.setOnClickListener(this);

        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Poll");

    }


    private void getUserInfo(){

        usernameTv.setText(GlobalVariables.getCurrentUsername());
        Picasso.get().load(GlobalVariables.getCurrentUserImageUrl()).fit().into(userIv);

    }


    private void createPollItemsList(){

        pollItems = new ArrayList<>();
        pollItems.add(new PollItem("الخيار 1",false));
        pollItems.add(new PollItem("الخيار 2",false));

        PollItemsRecyclerAdapter adapter = new PollItemsRecyclerAdapter(pollItems);
        pollRv.setAdapter(adapter);

    }


    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.publishBtn){

            Log.d("ttt","pollItems: "+pollItems.size());

            final String question = questionEd.getText().toString();

            if(question.isEmpty()){
                Toast.makeText(this, "Please Add a question to poll!",
                        Toast.LENGTH_SHORT).show();
                return;
            }




        }
    }
}