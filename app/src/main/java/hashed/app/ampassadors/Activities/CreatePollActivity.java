package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.Adapters.PollItemsRecyclerAdapter;
import hashed.app.ampassadors.Fragments.NumberPickerDialogFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class CreatePollActivity extends AppCompatActivity implements View.OnClickListener,
        NumberPickerDialogFragment.OnTimePass ,PollItemsRecyclerAdapter.ScrollToBottomListener{


  private ImageView userIv;
  private TextView usernameTv,timeTv;
  private EditText questionEd;
  private RecyclerView pollRv;
  private Button publishBtn;
  private CheckBox importantPollCb;
  private ArrayList<String> pollItems;
  private long pollDuration;
  private Integer[] durations;

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
    publishBtn = findViewById(R.id.edit_btn);
    questionEd = findViewById(R.id.questionEd);
    timeTv = findViewById(R.id.timeTv);
    pollRv = findViewById(R.id.pollRv);
    importantPollCb = findViewById(R.id.importantPollCb);

    NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollView);
    nestedScrollView.setNestedScrollingEnabled(false);

    Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> finish());

    TextView timeTv = findViewById(R.id.timeTv);

    timeTv.setOnClickListener(this);
    publishBtn.setOnClickListener(this);

  }


  private void getUserInfo() {

    usernameTv.setText(GlobalVariables.getInstance().getCurrentUsername());
    Picasso.get().load(GlobalVariables.getInstance().getCurrentUserImageUrl()).fit().centerCrop().into(userIv);
  }


  private void createPollItemsList() {

    pollItems = new ArrayList<>();
    final String option = getResources().getString(R.string.option);
    pollItems.add(option+"1 ");
    pollItems.add(option+"2 ");
    PollItemsRecyclerAdapter adapter = new PollItemsRecyclerAdapter(pollItems,this,this);
    pollRv.setAdapter(adapter);

  }


  @Override
  public void onClick(View view) {

    if (view.getId() == R.id.edit_btn) {
      Log.d("ttt", "pollItems: " + pollItems.size());
      final String question = questionEd.getText().toString();
      if (question.isEmpty()) {
        Toast.makeText(this, R.string.add_question_poll,
                Toast.LENGTH_SHORT).show();
        return;
      } else if (pollDuration == 0) {
        Toast.makeText(this, R.string.set_duration_poll,
                Toast.LENGTH_SHORT).show();
        return;
      }
      final List<Map<String, Object>> optionsMaps = new ArrayList<>();
      for (int i = 0; i < pollItems.size(); i++) {
        final EditText editText = pollRv.getChildAt(i).findViewById(R.id.pollEd);
        if (editText != null && !editText.getText().toString().trim().isEmpty()) {

          final HashMap<String, Object> map = new HashMap<>();
          map.put("option", editText.getText().toString().trim());
          map.put("votes", 0);
          map.put("id", i);

          optionsMaps.add(map);

        }
      }

      if (optionsMaps.size() < 2) {
        Toast.makeText(this, R.string.two_items_poll,
                Toast.LENGTH_SHORT).show();
        return;
      }

      publishBtn.setClickable(false);
      final ProgressDialog progressDialog = new ProgressDialog(this);
      progressDialog.setCancelable(false);
      progressDialog.setTitle(getString(R.string.Publish_Poll));
      progressDialog.show();

      final Map<String, Object> map = new HashMap<>();
      final String postId = UUID.randomUUID().toString();

      map.put("postId", postId);
      map.put("title", question);
      map.put("publishTime", System.currentTimeMillis());
      map.put("pollDuration", pollDuration);
      map.put("type", PostData.TYPE_POLL);
      map.put("publisherId", FirebaseAuth.getInstance().getCurrentUser().getUid());
      map.put("likes", 0);
      map.put("comments", 0);
      map.put("pollEnded", false);
      map.put("totalVotes", 0);
      map.put("important", importantPollCb.isChecked());

      final DocumentReference pollRef = FirebaseFirestore.getInstance()
              .collection("Posts").document(postId);

      for (int i = 0; i < optionsMaps.size(); i++) {

        final Task<Void> task = pollRef.collection("Options")
                .document(String.valueOf(i)).set(optionsMaps.get(i));

        if (i == optionsMaps.size() - 1) {
          task.addOnSuccessListener(Void ->
                  pollRef.set(map).addOnSuccessListener(v -> {
                    progressDialog.dismiss();
                    finish();
                  }).addOnFailureListener(e -> {
                    publishBtn.setClickable(true);
                    progressDialog.dismiss();
                    Toast.makeText(CreatePollActivity.this,
                            R.string.poll_publish_failed,
                            Toast.LENGTH_SHORT).show();
                  }));
        }
      }


    } else if (view.getId() == R.id.timeTv) {


      if (pollDuration > 0) {

        new NumberPickerDialogFragment(durations).show(
                getSupportFragmentManager(), "NumberPicker"
        );
      } else {
        new NumberPickerDialogFragment().show(
                getSupportFragmentManager(), "NumberPicker"
        );
      }

    }
  }

  @Override
  public void passTime(long time, Integer[] durations) {

    pollDuration = time;
    this.durations = durations;

    String durationText = "";
    if (durations[0] > 0) {
      durationText = durationText.concat(durations[0] + " " + getResources().getString(R.string.days) + " ");
    }
    if (durations[1] > 0) {
      durationText = durationText.concat(durations[1] + " " + getResources().getString(R.string.hours) + " ");
    }
    if (durations[2] > 0) {
      durationText = durationText.concat(durations[2] + " " + getResources().getString(R.string.minute));
    }

    timeTv.setText(durationText);

  }

  @Override
  public void scrollToBottom() {
    pollRv.smoothScrollToPosition(pollItems.size()-1);
  }

  @Override
  public void onBackPressed() {

    if (!questionEd.getText().toString().isEmpty() || pollDuration != 0){
      showAlert();
    } else {

      boolean hasFilledOptions = false;
      for (int i = 0; i < pollItems.size(); i++) {
        final EditText editText = pollRv.getChildAt(i).findViewById(R.id.pollEd);
        if(!editText.getText().toString().trim().isEmpty()){
          hasFilledOptions = true;
        }
      }
      if(hasFilledOptions){
        showAlert();
      }else{
        super.onBackPressed();
      }
    }
  }

  private void showAlert(){
    final AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Do you want to leave before publishing your poll?");
    alert.setMessage("leaving will discard this poll!");

    alert.setPositiveButton("Leave", (dialogInterface, i) -> {
      finish();
    });

    alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
    alert.create().show();
  }

}