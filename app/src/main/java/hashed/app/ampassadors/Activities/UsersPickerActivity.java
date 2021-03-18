package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import hashed.app.ampassadors.Adapters.UsersPickerAdapter;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class UsersPickerActivity extends AppCompatActivity implements
        Toolbar.OnMenuItemClickListener {

  //  private Query query;
//  private static final int USERS_LIMIT = 15;
  private ArrayList<UserPreview> users;
  private RecyclerView userRv;
  private UsersPickerAdapter pickerAdapter;
  private ArrayList<String> previousSelectedUserIdsList;
  private CollectionReference usersRef;
  private Toolbar pickUserToolbar;
  //  private SearchView searchUserSearchView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_users_picker);

    userRv = findViewById(R.id.userRv);
    FloatingActionButton nextFloatingBtn = findViewById(R.id.nextFloatingBtn);
    pickUserToolbar = findViewById(R.id.pickUserToolbar);
    pickUserToolbar.setNavigationOnClickListener(v -> finish());
    pickUserToolbar.setOnMenuItemClickListener(this);
    usersRef = FirebaseFirestore.getInstance().collection("Users");


    if (getIntent() != null && getIntent().hasExtra("previousSelectedUserIdsList")) {
      previousSelectedUserIdsList =
              getIntent().getStringArrayListExtra("previousSelectedUserIdsList");

    } else {
      previousSelectedUserIdsList = new ArrayList<>();
    }

    updateSubtitle();

    users = new ArrayList<>();
    pickerAdapter = new UsersPickerAdapter(users, previousSelectedUserIdsList);
    userRv.setAdapter(pickerAdapter);


    if (getIntent() != null && getIntent().hasExtra("previousSelectedUserIdsList")) {
      getPreviousUsers();
    }

    nextFloatingBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (pickerAdapter.selectedUserIds.size() < 2) {
          Toast.makeText(UsersPickerActivity.this,
                  "You need to add at least two people to this group!",
                  Toast.LENGTH_SHORT).show();
          return;
        }

        final Intent createMeetingIntent = new Intent(UsersPickerActivity.this,
                CreateMeetingActivity.class)
                .putStringArrayListExtra("selectedUserIdsList",
                        pickerAdapter.selectedUserIds);

        if (getIntent().hasExtra("meetingBundle")) {
          createMeetingIntent.putExtra("meetingBundle",
                  getIntent().getBundleExtra("meetingBundle"));
        }

        startActivity(createMeetingIntent);
        finish();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == 3 && data != null && data.hasExtra("previousSelectedUserIdsList")) {

      users.clear();
      updateSubtitle();
      pickerAdapter.notifyDataSetChanged();

      previousSelectedUserIdsList =
              data.getStringArrayListExtra("previousSelectedUserIdsList");

      pickerAdapter.selectedUserIds = previousSelectedUserIdsList;

      getPreviousUsers();

    }
  }


  private void getPreviousUsers() {

    for (String id : previousSelectedUserIdsList) {

      usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          users.add(documentSnapshot.toObject(UserPreview.class));
        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          pickerAdapter.notifyItemInserted(users.size() - 1);
        }
      });
    }

  }

  private void updateSubtitle() {
    pickUserToolbar.setSubtitle(getResources().getString(R.string.selected) + " " +
            previousSelectedUserIdsList.size() + " " +
            getResources().getString(R.string.out_of) + " 100");

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    Intent intent = new Intent(UsersPickerActivity.this, UserSearchActivity.class);
    intent.putStringArrayListExtra("selectedUserIds", pickerAdapter.selectedUserIds);
    startActivityForResult(intent, 3);
    return false;
  }


}