  package hashed.app.ampassadors.Activities;

  import android.app.AlertDialog;
  import android.app.ProgressDialog;
  import android.content.Intent;
  import android.content.pm.PackageManager;
  import android.net.Uri;
  import android.os.Bundle;
  import android.util.Log;
  import android.view.View;
  import android.widget.EditText;
  import android.widget.TextView;
  import android.widget.Toast;

  import androidx.annotation.NonNull;
  import androidx.annotation.Nullable;
  import androidx.appcompat.app.AppCompatActivity;
  import androidx.appcompat.widget.Toolbar;
  import androidx.recyclerview.widget.RecyclerView;

  import com.google.android.gms.tasks.OnCompleteListener;
  import com.google.android.gms.tasks.OnFailureListener;
  import com.google.android.gms.tasks.OnSuccessListener;
  import com.google.android.gms.tasks.Task;
  import com.google.android.material.floatingactionbutton.FloatingActionButton;
  import com.google.firebase.auth.FirebaseAuth;
  import com.google.firebase.database.DatabaseReference;
  import com.google.firebase.database.FirebaseDatabase;
  import com.google.firebase.firestore.CollectionReference;
  import com.google.firebase.firestore.DocumentSnapshot;
  import com.google.firebase.firestore.FieldValue;
  import com.google.firebase.firestore.FirebaseFirestore;
  import com.google.firebase.firestore.QuerySnapshot;
  import com.google.firebase.storage.FirebaseStorage;
  import com.google.firebase.storage.StorageReference;
  import com.google.firebase.storage.StorageTask;
  import com.google.firebase.storage.UploadTask;
  import com.squareup.picasso.Picasso;

  import java.io.File;
  import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.Map;
  import java.util.UUID;

  import de.hdodenhof.circleimageview.CircleImageView;
  import hashed.app.ampassadors.Adapters.UsersAdapter;
  import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
  import hashed.app.ampassadors.NotificationUtil.Data;
  import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
  import hashed.app.ampassadors.Objects.UserPreview;
  import hashed.app.ampassadors.R;
  import hashed.app.ampassadors.Utils.Files;
  import hashed.app.ampassadors.Utils.UploadTaskUtil;

  public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener {

  //database
  private CollectionReference groupsRef;
  private CollectionReference usersRef;
  private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap;

  //seleceted users
  private String currentUid;
  private ArrayList<String> selectedUserIdsList;
  private ArrayList<UserPreview> selectedUsers;

  //views
  private Toolbar toolbar;
  private CircleImageView groupIv;
  private EditText groupNameEd;
  private FloatingActionButton doneFloatingBtn;
  private FloatingActionButton editUsersFloatingBtn;
  private TextView contributorsTv;
  private RecyclerView usersPickedRv;

  private Uri imageUri;
  private UsersAdapter selectedUsersAdapter;
  private String meetingImageUrl;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_meeting);

    initViews();

    initValues();

    setViewClickers();

    selectedUserIdsList = getIntent().getStringArrayListExtra("selectedUserIdsList");

    updateContributorsCount();

    createSelectedUserAdapter();

  }

  private void initViews() {

    toolbar = findViewById(R.id.toolbar);
    groupIv = findViewById(R.id.groupIv);
    groupNameEd = findViewById(R.id.groupNameEd);
    doneFloatingBtn = findViewById(R.id.doneFloatingBtn);
    editUsersFloatingBtn = findViewById(R.id.editUsersFloatingBtn);
    contributorsTv = findViewById(R.id.contributorsTv);
    usersPickedRv = findViewById(R.id.usersPickedRv);
    TextView dateSetterTv = findViewById(R.id.dateSetterTv);
    dateSetterTv.setVisibility(View.GONE);
    TextView timeSetterTv = findViewById(R.id.timeSetterTv);
    timeSetterTv.setVisibility(View.GONE);
    TextView timeTv = findViewById(R.id.timeTv);
    timeTv.setVisibility(View.GONE);
    TextView dateTv = findViewById(R.id.dateTv);
    dateTv.setVisibility(View.GONE);

  }

  private void initValues() {
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    groupsRef = FirebaseFirestore.getInstance().collection("PrivateMessages");
  }

  private void setViewClickers() {

    toolbar.setNavigationOnClickListener(view -> onBackPressed());

    doneFloatingBtn.setOnClickListener(this);
    editUsersFloatingBtn.setOnClickListener(this);
    groupIv.setOnClickListener(this);

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 3 && data != null && data.hasExtra("selectedUserIdsList")) {

      selectedUserIdsList = data.getStringArrayListExtra("selectedUserIdsList");
      updateContributorsCount();
      selectedUsers.clear();
      selectedUsersAdapter.notifyDataSetChanged();

      getUsers();
      Log.d("ttt", "selectedUserIdsList: " + selectedUserIdsList.size());

    } else if (resultCode == RESULT_OK && requestCode == Files.PICK_IMAGE && data != null) {

      imageUri = data.getData();
      Picasso.get().load(imageUri).fit().centerCrop().into(groupIv);

    }
  }

  private void createSelectedUserAdapter() {

    usersRef = FirebaseFirestore.getInstance().collection("Users");

    selectedUsers = new ArrayList<>();

    selectedUsersAdapter = new UsersAdapter(selectedUsers,
            R.layout.user_picked_preview_item_layout);

    usersPickedRv.setAdapter(selectedUsersAdapter);

    getUsers();


  }

  private void getUsers() {

    for (String id : selectedUserIdsList) {
      usersRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          selectedUsers.add(documentSnapshot.toObject(UserPreview.class));
        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if (task.isSuccessful()) {
            selectedUsersAdapter.notifyItemInserted(selectedUsers.size() - 1);
          }
        }
      });
    }
  }

  private void updateContributorsCount() {
    contributorsTv.setText(getResources().getString(R.string.contributors) + ": " + selectedUserIdsList.size());
  }

  private void publishGroup() {

    final String name = groupNameEd.getText().toString().trim();

    if (!name.isEmpty() && selectedUserIdsList != null &&
            !selectedUserIdsList.isEmpty() && selectedUserIdsList.size() > 1) {

      ProgressDialog progressDialog = new ProgressDialog(CreateGroupActivity.this);
      progressDialog.setTitle(getString(R.string.creating_group));
      progressDialog.setCancelable(false);
      progressDialog.show();

      final String groupId = UUID.randomUUID().toString();
      selectedUserIdsList.add(currentUid);

      final Map<String, Object> groupMap = new HashMap<>();
      groupMap.put("creatorId", currentUid);
      groupMap.put("groupAdmins", FieldValue.arrayUnion(currentUid));
      groupMap.put("groupName", name);
      groupMap.put("createdTime", System.currentTimeMillis());
      groupMap.put("users", selectedUserIdsList);
      groupMap.put("latestMessageTime",  System.currentTimeMillis());
      groupMap.put("groupId", groupId);
      groupMap.put("databaseRefId", groupId);

      if (imageUri != null) {
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("Group-Images/").child(UUID.randomUUID().toString() + "-" +
                        System.currentTimeMillis());

        final UploadTask uploadTask = reference.putFile(imageUri);

        uploadTaskMap = new HashMap<>();

        StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                  uploadTaskMap.remove(uploadTask);
                  reference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    meetingImageUrl = uri1.toString();
                    groupMap.put("imageUrl", meetingImageUrl);
//                    meeting.setImageUrl(meetingImageUrl);

                    createGroup(groupMap, groupId, name, progressDialog);
                  });
                }).addOnCompleteListener(task -> new File(imageUri.getPath()).delete());

        uploadTaskMap.put(uploadTask, onSuccessListener);

      } else {
        createGroup(groupMap, groupId, name, progressDialog);
      }
    }

  }

  private void createGroup(Map<String, Object> group,
                             String groupId, String name, ProgressDialog progressDialog) {

    groupsRef.whereEqualTo("groupName",name).whereEqualTo("users",selectedUserIdsList)
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if(task!=null && task.isSuccessful() && task.getResult()!=null &&
                !task.getResult().isEmpty()){
          Toast.makeText(CreateGroupActivity.this,
                  R.string.group_already_exists,
                  Toast.LENGTH_LONG).show();
          return;
        }

        groupsRef.document(groupId).set(group)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                  @Override
                  public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                      final Map<String, Object> groupMap = new HashMap<>();
                      groupMap.put("groupId", groupId);

                      final DatabaseReference groupRef =  FirebaseDatabase.getInstance().getReference()
                              .child("PrivateMessages").child(groupId);

                      groupRef.setValue(groupMap)
                              .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                  HashMap<String, String> lastSeenMap = new HashMap<>();

                                  for(String user:selectedUserIdsList){
                                    lastSeenMap.put(user,"0");
                                  }

                                  groupRef.child("UsersLastSeenMessages").setValue(lastSeenMap);

                                  usersRef.document(currentUid).get()
                                          .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot snapshot) {
                                              if(snapshot.exists()){

                                                final String username  =
                                                        snapshot.getString("username");
                                                final Data data = new Data(
                                                        currentUid,
                                                        username+" "+
                                                                getResources().getString(R.string.added_to_group),
                                                        groupNameEd.getText().toString().trim(),
                                                        meetingImageUrl,
                                                        "Group added",
                                                        FirestoreNotificationSender.TYPE_GROUP_ADDED,
                                                        groupId);


                                                selectedUserIdsList.remove(currentUid);

                                                for (String userId : selectedUserIdsList) {
                                                  CloudMessagingNotificationsSender.sendNotification(userId, data);
                                                  FirestoreNotificationSender.sendFirestoreNotification(
                                                          userId, FirestoreNotificationSender.TYPE_GROUP_ADDED,
                                                          username +" "+
                                                                  getResources()
                                                                          .getString(R.string.added_to_group),
                                                          name, groupId);
                                                }

                                                progressDialog.dismiss();
                                                finish();

                                              }
                                            }
                                          });
                                }
                              });
                    }
                  }
                }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {

            Toast.makeText(CreateGroupActivity.this,
                    R.string.Error_meassage_MeetingFiald, Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

          }
        });

      }
    });



  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.doneFloatingBtn) {
      publishGroup();
    }else  if (view.getId() == R.id.editUsersFloatingBtn) {
      editContributors();
    } else if (view.getId() == R.id.groupIv) {
      Files.startImageFetchIntent(this);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (requestCode == Files.PICK_IMAGE) {
          Files.startImageFetchIntent(this);
        }
      }
  }



  @Override
  public void onBackPressed() {


    if(!groupNameEd.getText().toString().trim().isEmpty()
    || !selectedUserIdsList.isEmpty() || imageUri!=null){

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Do you want to leave without creating this group?");
      alert.setMessage("Leaving will discard this group");

      alert.setPositiveButton("Leave", (dialogInterface, i) -> {
          UploadTaskUtil.cancelUploadTasks(uploadTaskMap);
          dialogInterface.dismiss();
          finish();
      });

        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        alert.create().show();

    } else {
      super.onBackPressed();
    }

  }

  private void editContributors(){
   final Intent intent = new Intent(CreateGroupActivity.this,
                UsersPickerActivity.class)
           .putExtra("previousSelectedUserIdsList", selectedUserIdsList)
           .putExtra("isForGroup", true);
        startActivityForResult(intent, 3);
  }
}