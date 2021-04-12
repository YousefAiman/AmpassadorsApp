package hashed.app.ampassadors.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity2;
import hashed.app.ampassadors.Adapters.GroupMembersAdapter;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;

public class GroupInfoActivity extends AppCompatActivity implements
        GroupMembersAdapter.GroupMemberClickListener{

  //views
  private ImageView groupIv;
  private TextView contributorsTv,groupNameTv;
  private RecyclerView groupMembersRv;

  //info
  private DocumentReference firebaseMessageDocRef;
  private DatabaseReference userLastSeenDatabaseRef;
  private boolean hasGottenInfo;
  private String groupImageUrl;
  private List<String> adminsList;
  private ListenerRegistration groupInfoListener;
  private Map<DatabaseReference, ChildEventListener> childEventListeners;

  //group update
  private String creatorId;
  private Dialog userInfoDialog;
  private String lastSelectedUserId;

  private CollectionReference usersRef;
  private ArrayList<UserPreview> userPreviews;
  private GroupMembersAdapter groupMembersAdapter;
  private UserIdsScrollListener scrollListener;

  private String currentUid;
  private ArrayList<String> usersIds;

  private boolean isLoadingUsers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_info);

    setUpToolBar();

    getViews();

    initializeValues();

    getGroupInfo();

  }

  private void setUpToolBar() {
    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v->finish());
  }

  private void getViews(){
    groupIv = findViewById(R.id.groupIv);
    groupNameTv = findViewById(R.id.groupNameTv);
    contributorsTv = findViewById(R.id.contributorsTv);
    groupMembersRv = findViewById(R.id.groupMembersRv);
  }


  private void initializeValues(){
    userPreviews = new ArrayList<>();
    usersRef = FirebaseFirestore.getInstance().collection("Users");
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  }

  private void getGroupInfo(){

    final String documentId = getIntent().getStringExtra("firebaseMessageDocRefId");
    final String groupType = getIntent().getStringExtra("groupType");

    switch (groupType){
      case "messagingGroup":
        firebaseMessageDocRef = FirebaseFirestore.getInstance()
                .collection("PrivateMessages").document(documentId);
        break;
    }

    userLastSeenDatabaseRef =
            FirebaseDatabase.getInstance().getReference().child("PrivateMessages")
                    .child(firebaseMessageDocRef.getId()).child("UsersLastSeenMessages");


    groupInfoListener = firebaseMessageDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot value,
                          @Nullable FirebaseFirestoreException error) {
        if(value!=null && value.exists()){

          if(!hasGottenInfo){

            if(value.contains("groupAdmins")){
             adminsList = (List<String>) value.get("groupAdmins");

            groupMembersAdapter = new GroupMembersAdapter(userPreviews,adminsList!=null?
            adminsList: new ArrayList<>(),
                      GroupInfoActivity.this);
              groupMembersRv.setAdapter(groupMembersAdapter);
            }

            if(groupType.equals("messagingGroup")){

              creatorId = value.getString("creatorId");

              groupNameTv.setText(value.getString("groupName"));

              if(value.contains("imageUrl")){
                groupImageUrl= value.getString("imageUrl");
                if(groupImageUrl!=null && !groupImageUrl.isEmpty()){
                  Picasso.get().load(groupImageUrl).fit().into(groupIv);
                }
              }

              usersIds = (ArrayList<String>) value.get("users");

              addUserModificationChildListener();

              if(usersIds!=null && !usersIds.isEmpty()){

                updateParticipantsCount();

                if (usersIds.size() > 10) {
                  getUsers(usersIds.subList(0, 10));
                  groupMembersRv.addOnScrollListener(scrollListener = new UserIdsScrollListener());
                }else{
                  getUsers(usersIds);
                }

              }else{
                contributorsTv.setText("no current participants");
              }
            }

            hasGottenInfo = true;
          }else{

            if(value.contains("groupAdmins")) {

              List<String> newAdmins = (List<String>) value.get("groupAdmins");

              if(newAdmins == null){

                Log.d("ttt","new admins is empty");
                return;
              }

              Log.d("ttt","new admins is not empty");

              List<String> removedAdmins = new ArrayList<>(adminsList);
              removedAdmins.removeAll(newAdmins);


              List<String> addedAdmins = new ArrayList<>(newAdmins);
              addedAdmins.removeAll(adminsList);

              if(!removedAdmins.isEmpty()){


                adminsList.removeAll(removedAdmins);

                for(String removedAdmin:removedAdmins){
                  Log.d("ttt","removedAdmins: "+removedAdmin);
                  updateItem(removedAdmin);
                }

              }else{
                Log.d("ttt","no removedAdmins");
              }

               if(!addedAdmins.isEmpty()){

                 adminsList.addAll(addedAdmins);

                 for(String addAdmin:addedAdmins){
                   Log.d("ttt","addAdmin: "+addAdmin);
                   updateItem(addAdmin);
                 }

              }else{
                 Log.d("ttt","no addedAdmins");
               }

            }
            }
        }
      }
    });


  }

  private void getUsers(List<String> userIdsList) {

    isLoadingUsers = true;
    final int previousSize = userPreviews.size();

    usersRef.whereIn("userId", userIdsList).get()
            .addOnSuccessListener(snapshots -> {
              if(!snapshots.isEmpty()){
                userPreviews.addAll(snapshots.toObjects(UserPreview.class));
              }
            }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(@NonNull Task<QuerySnapshot> task) {

        if (previousSize == 0) {
          groupMembersAdapter.notifyDataSetChanged();
        } else {
          groupMembersAdapter.notifyItemRangeInserted(previousSize, userPreviews.size()
                  - previousSize);
        }

        isLoadingUsers = false;
      }
    });

  }

  @Override
  public void clickUser(String userId) {

    lastSelectedUserId = userId;

    userInfoDialog = new Dialog(this);
    userInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    userInfoDialog.setContentView(R.layout.user_group_options_dialog_layout);

    final TextView messageUserTv = userInfoDialog.findViewById(R.id.messageUserTv);
    messageUserTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        userInfoDialog.cancel();
        startActivity(new Intent(GroupInfoActivity.this,
                PrivateMessagingActivity2.class)
                .putExtra("messagingUid", userId)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
    });

    final TextView showUserTv = userInfoDialog.findViewById(R.id.showUserTv);
    showUserTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        userInfoDialog.cancel();
        startActivity(new Intent(GroupInfoActivity.this,
                ProfileActiv.class).putExtra("userId", userId));
      }
    });


    userInfoDialog.show();

  }

  private class UserIdsScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingUsers && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d("ttt","is at bottom");
          if (usersIds.size() >= userPreviews.size() + 10) {
            getUsers(usersIds.subList(userPreviews.size(), userPreviews.size() + 10));
          } else {

            groupMembersRv.removeOnScrollListener(scrollListener);

            if (usersIds.size() > userPreviews.size()) {
              getUsers(usersIds.subList(userPreviews.size(), usersIds.size()));
            }
          }
//        }


      }
    }
  }

  private void updateParticipantsCount(){
    contributorsTv.setText(getString(R.string.participants)+": "+usersIds.size());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if(groupMembersRv!=null && scrollListener!=null){
      groupMembersRv.removeOnScrollListener(scrollListener);
    }

    if(groupInfoListener!=null){
      groupInfoListener.remove();
    }

    if (childEventListeners != null && !childEventListeners.isEmpty()) {
      for (DatabaseReference reference : childEventListeners.keySet()) {
        reference.removeEventListener(Objects.requireNonNull(childEventListeners.get(reference)));
      }
    }

  }

  private void removeUserFromList(String userId){

    usersIds.remove(userId);

    for(int i= 0;i<userPreviews.size();i++){
      if(userPreviews.get(i).getUserId().equals(userId)){
        userPreviews.remove(i);
        groupMembersAdapter.notifyItemRemoved(i);
        break;
      }
    }
    updateParticipantsCount();
  }

  private void addUserModificationChildListener(){
    ChildEventListener childEventListener;

    userLastSeenDatabaseRef.addChildEventListener(childEventListener = new ChildEventListener() {
      @Override
      public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d("ttt","last seen added: "+snapshot.getKey());
        if(snapshot.exists() && usersIds!=null){
          final String userId = snapshot.getKey();
          if(userId!=null && !usersIds.contains(userId) &&
                  !userId.equals(currentUid) && !userId.equals(creatorId)){

            usersIds.add(snapshot.getKey());

            if(userPreviews.size() < 10){
              getUsers(usersIds.subList(usersIds.size()-1,usersIds.size()));
            }
          }
        }
      }

      @Override
      public void onChildChanged(@NonNull DataSnapshot snapshot,@Nullable String previousChildName){
        Log.d("ttt","last seen changed: "+snapshot.getKey());
      }

      @Override
      public void onChildRemoved(@NonNull DataSnapshot snapshot) {

        if(snapshot.exists() && usersIds!=null){

          if(lastSelectedUserId!=null && userInfoDialog !=null && userInfoDialog.isShowing()){
            userInfoDialog.cancel();
            Toast.makeText(GroupInfoActivity.this,
                    "This user was removed by another admin!", Toast.LENGTH_SHORT).show();
          }

          if(usersIds.contains(snapshot.getKey())){
            removeUserFromList(snapshot.getKey());
          }
        }

        Log.d("ttt","last seen removed: "+snapshot.getKey());
      }

      @Override
      public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {

      }
    });

    childEventListeners = new HashMap<>();
    childEventListeners.put(userLastSeenDatabaseRef, childEventListener);
  }

  private void updateItem(String userId){
    for (UserPreview userPreview : userPreviews) {
      if (userPreview.getUserId().equals(userId)) {
        groupMembersAdapter.notifyItemChanged(userPreviews.indexOf(userPreview));
        break;
      }
    }
  }
}