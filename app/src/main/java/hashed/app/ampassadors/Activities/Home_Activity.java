package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class Home_Activity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    CircleImageView post;
    RecyclerView post_list;
    FirebaseFirestore firebaseFirestore;
    Task<QuerySnapshot> task;
    List<PostData> postData;
    PostAdapter adapter;
    TextView newpost;
    TextView newPoll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        SetUpCompetent();
        post_list.setHasFixedSize(true);
        post_list.setLayoutManager(new LinearLayoutManager(this));
        OnClickButtons();
        ReadPost();
        setUpToolBarAndActions();

    }

    // Choose from the side menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }


    public void SetUpCompetent() {
        post = findViewById(R.id.posting_image_btn);
        post_list = findViewById(R.id.home_list);
        firebaseFirestore = FirebaseFirestore.getInstance();
        task = firebaseFirestore.collection("Post").get();
        postData = new ArrayList<>();
        newPoll = findViewById(R.id.new_poll);
        newpost = findViewById(R.id.new_post);

    }

    // Tool bar
    private void setUpToolBarAndActions() {

        final Toolbar toolbar = findViewById(R.id.home_activity_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Home_Activity.this, "tool", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.setOnMenuItemClickListener(this);

    }


    // Buttons Click
    public void OnClickButtons() {

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPostOptionsBottomSheet();

            }
        });

    }

    public void ReadPost() {
        task.addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot sp : task.getResult().getDocuments()) {
                        String posttrxt = sp.getString("description");
                        PostData data = new PostData();
                        data.setDescription(posttrxt);
                        postData.add(data);

                    }
                }

                adapter = new PostAdapter(postData, getApplicationContext());
                adapter.notifyDataSetChanged();
                post_list.setAdapter(adapter);


            }
        });
    }

    private void showPostOptionsBottomSheet() {
        final BottomSheetDialog bsd = new BottomSheetDialog(this, R.style.SheetDialog);
        final View parentView = getLayoutInflater().inflate(R.layout.post_options_bsd, null);
        parentView.setBackgroundColor(Color.TRANSPARENT);

        parentView.findViewById(R.id.new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bsd.dismiss();
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                startActivity(intent);
            }
        });
        parentView.findViewById(R.id.new_poll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bsd.dismiss();
                Intent intent = new Intent(getApplicationContext(), PollActivity.class);
                startActivity(intent);
            }
        });
        bsd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        bsd.setContentView(parentView);
        bsd.show();

    }
}