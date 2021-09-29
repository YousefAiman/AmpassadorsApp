package hashed.app.ampassadors.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import hashed.app.ampassadors.Activities.MessagingActivities.PrivateMessagingActivity;
import hashed.app.ampassadors.Adapters.GroupMembersAdapter;
import hashed.app.ampassadors.NotificationUtil.CloudMessagingNotificationsSender;
import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.NotificationUtil.FirestoreNotificationSender;
import hashed.app.ampassadors.Objects.UserPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.MessagingUtil;
import hashed.app.ampassadors.Utils.UploadTaskUtil;

public class GroupEditingActivity extends AppCompatActivity implements View.OnClickListener,
        Toolbar.OnMenuItemClickListener , GroupMembersAdapter.GroupMemberClickListener{

  //views
  private ImageView groupIv,changeImageIv,addMemberIv;
  private EditText groupNameEd;
  private TextView contributorsTv,addParticipantsTv;
  private RecyclerView groupMembersRv;


  //info
  private DocumentReference firebaseMessageDocRef;
  private DatabaseReference userLastSeenDatabaseRef;
  private boolean hasGottenInfo;
  private String groupName,groupImageUrl;
  private List<String> adminsList;
  private ListenerRegistration groupInfoListener;
  private Map<DatabaseReference, ChildEventListener> childEventListeners;

  //group update
  private Uri imageUri;
  private ArrayList<String> newSelectedUserIdsList;
  private String creatorId;
  private Dialog userUpdateDialog;
  private String lastSelectedUserId;

  private CollectionReference usersRef;
  private ArrayList<UserPreview> userPreviews;
  private GroupMembersAdapter groupMembersAdapter;
  private UserIdsScrollListener scrollListener;

  private String currentUid;
  //messaging group fields
  private ArrayList<String> usersIds;
//  private ArrayList<String> originalUserIds;

  private boolean isLoadingUsers;

  private Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_group_editing);

    setUpToolBar();

    getViews();

    initializeValues();

    getGroupInfo();

  }

  private void setUpToolBar() {
    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.inflateMenu(R.menu.edit_group_menu);
    toolbar.setTitle(getString(R.string.edit_group));
    toolbar.setSubtitle(getString(R.string.edit));
    toolbar.setNavigationOnClickListener(v->finish());
    toolbar.setOnMenuItemClickListener(this);
  }

  private void getViews(){
    groupIv = findViewById(R.id.groupIv);
    changeImageIv = findViewById(R.id.changeImageIv);
    addMemberIv = findViewById(R.id.addMemberIv);
    groupNameEd = findViewById(R.id.groupNameEd);
    contributorsTv = findViewById(R.id.contributorsTv);
    addParticipantsTv = findViewById(R.id.addParticipantsTv);
    groupMembersRv = findViewById(R.id.groupMembersRv);

    addMemberIv.setVisibility(View.VISIBLE);
    addParticipantsTv.setVisibility(View.VISIBLE);


    changeImageIv.setOnClickListener(this);
    addMemberIv.setOnClickListener(this);
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
                      GroupEditingActivity.this);
              groupMembersRv.setAdapter(groupMembersAdapter);
            }

            if(groupType.equals("messagingGroup")){

              creatorId = value.getString("creatorId");

              groupNameEd.setText(groupName = value.getString("groupName"));

              if(value.contains("imageUrl")){
                groupImageUrl= value.getString("imageUrl");
                if(groupImageUrl!=null && !groupImageUrl.isEmpty()){
                  Picasso.get().load(groupImageUrl).fit().into(groupIv);
                }
              }

              usersIds = (ArrayList<String>) value.get("users");

              addUserModificationChildListener();

              if(usersIds!=null && !usersIds.isEmpty()){

                if(!creatorId.equals(currentUid)){
                  usersIds.remove(creatorId);
                }

                usersIds.remove(currentUid);

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

                if(removedAdmins.contains(currentUid)){
                  Toast.makeText(GroupEditingActivity.this,
                          "You are not an admin of this group anymore!",
                          Toast.LENGTH_SHORT).show();
                  finish();
                }
              }else{
                Log.d("ttt","no removedAdmins");
              }

               if(!addedAdmins.isEmpty()){

                 adminsList.addAll(addedAdmins);

                 for(String addAdmin:addedAdmins){
                   Log.d("ttt","addAdmin: "+addAdmin);
                   updateItem(addAdmin);

                   if(usersIds!=null){
                     if(!usersIds.contains(addAdmin) &&
                             !addAdmin.equals(currentUid) && !addAdmin.equals(creatorId)){

                       usersIds.add(addAdmin);

                       updateParticipantsCount();

                       if(userPreviews.size() < 10){
                         getUsers(usersIds.subList(usersIds.size()-1,usersIds.size()));
                       }
                     }
                   }
                 }


              }else{
                 Log.d("ttt","no addedAdmins");
               }


//              if(!adminsList.containsAll(newAdmins)){
//
//                adminsList = newAdmins;
//
//                groupMembersAdapter.notifyDataSetChanged();
//
//
//
//                //admin list changed
////                groupMembersAdapter.setAdminIds(newAdmins);
////                for(String newAdmin:newAdmins) {
////
////                  for (UserPreview userPreview : userPreviews) {
////                    if (userPreview.getUserId().equals(adminsList)) {
////
////
////                    }
////                  }
////                }
////
//////                  int changedIndex = usersIds.indexOf(newAdmin);
//////                  if(changedIndex!=-1){
//////
//////                  }
////                }
////
//
//              }
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
    Log.d("ttt","clicekd user");

    userUpdateDialog = new Dialog(this);
    userUpdateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    userUpdateDialog.setContentView(R.layout.admin_group_options_dialog_layout);

    final TextView messageUserTv = userUpdateDialog.findViewById(R.id.messageUserTv);
    messageUserTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        userUpdateDialog.cancel();
        startActivity(new Intent(GroupEditingActivity.this,
                PrivateMessagingActivity.class)
                .putExtra("messagingUid", userId)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
    });

    final TextView showUserTv = userUpdateDialog.findViewById(R.id.showUserTv);
    showUserTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        userUpdateDialog.cancel();
        startActivity(new Intent(GroupEditingActivity.this,
                ProfileActiv.class).putExtra("userId", userId));
      }
    });


    final TextView changeAdminStatusTv = userUpdateDialog.findViewById(R.id.changeAdminStatusTv);

    if(!creatorId.equals(currentUid)){

      userUpdateDialog.findViewById(R.id.adminBottomSeparator).setVisibility(View.GONE);

      changeAdminStatusTv.setVisibility(View.GONE);

    }else{
      if(adminsList!=null && !adminsList.isEmpty() && adminsList.contains(userId)){
        changeAdminStatusTv.setText(getString(R.string.remove_from_admins));
      }else{
        changeAdminStatusTv.setText(getString(R.string.set_as_an_admin));
      }

      changeAdminStatusTv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

          userUpdateDialog.cancel();

          if(userId.equals(creatorId)){
            return;
          }

          if (adminsList != null && !adminsList.isEmpty() && adminsList.contains(userId)) {

            adminsList.remove(userId);

            firebaseMessageDocRef.update("groupAdmins", FieldValue.arrayRemove(userId))
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
//                groupMembersAdapter.getAdminIds().remove(userId);
                updateItem(userId);
              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                adminsList.add(userId);
              }
            });

          } else {

            if(adminsList == null){
              adminsList = new ArrayList<>();
            }


            adminsList.add(userId);

            firebaseMessageDocRef.update("groupAdmins", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

//                groupMembersAdapter.getAdminIds().add(userId);
                updateItem(userId);
              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                adminsList.remove(userId);
              }
            });
          }
        }
      });
    }



    final TextView removeUserFromGroupTv = userUpdateDialog.findViewById(R.id.removeUserFromGroupTv);

    removeUserFromGroupTv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        userUpdateDialog.cancel();

        if(userId.equals(creatorId)){
          return;
        }

        if(newSelectedUserIdsList!=null && newSelectedUserIdsList.contains(userId)){

          removeUserFromList(userId);

        }else{

          if(usersIds.size() == 2){
            Toast.makeText(GroupEditingActivity.this,
                    "You can't have less than 2 members in a group!",
                    Toast.LENGTH_SHORT).show();
            return;
          }

          if(adminsList.contains(userId)){
            firebaseMessageDocRef.update("groupAdmins", FieldValue.arrayRemove(userId));
            adminsList.remove(userId);
          }

          MessagingUtil.leaveGroup(GroupEditingActivity.this,userId,
                  firebaseMessageDocRef.getId(), firebaseMessageDocRef, userLastSeenDatabaseRef);

//          new ProgressDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialogInterface) {
//              removeUserFromList(userId);
//            }
//          });
//

        }
      }
    });

    userUpdateDialog.show();

  }

  private class UserIdsScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
      super.onScrollStateChanged(recyclerView, newState);
      if (!isLoadingUsers && !recyclerView.canScrollVertically(1) &&
              newState == RecyclerView.SCROLL_STATE_IDLE) {

        Log.d("ttt","is at bottom");

//        if(newSelectedUserIdsList!=null){
//          if (newSelectedUserIdsList.size() >= userPreviews.size() + 10) {
//            getUsers(newSelectedUserIdsList.subList(userPreviews.size(), userPreviews.size() + 10));
//          } else {
//            groupMembersRv.removeOnScrollListener(scrollListener);
//            if (newSelectedUserIdsList.size() > userPreviews.size()) {
//              getUsers(newSelectedUserIdsList.subList(userPreviews.size(),
//                      newSelectedUserIdsList.size()));
//            }
//          }
//        }else{
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

  @Override
  public boolean onMenuItemClick(MenuItem item) {

    if(item.getItemId() == R.id.action_submit_edit){
      checkEditedInfo();
    }

    return false;
  }

  private void checkEditedInfo(){
    Map<String, Object> updateMap = new HashMap<>();

    final String newGroupName = groupNameEd.getText().toString().trim();

    if(newGroupName.isEmpty()){
      Toast.makeText(this, "You need to fill in the new group name!",
              Toast.LENGTH_SHORT).show();
      return;
    }

    final ProgressDialog groupUpdateDialog = new ProgressDialog(this);
    groupUpdateDialog.setMessage("Updating group!");
    groupUpdateDialog.setCancelable(false);
    groupUpdateDialog.show();

    if(!newGroupName.equals(groupName)){
      updateMap.put("groupName",groupName = newGroupName);
    }


    if(newSelectedUserIdsList!=null && !newSelectedUserIdsList.isEmpty()){
      for(String userId:newSelectedUserIdsList){
        Log.d("ttt","newSelectedUserIdsList: "+userId);
      }
      usersIds.add(currentUid);
      if(creatorId != null && !usersIds.contains(creatorId)){
       usersIds.add(creatorId);
      }
      updateMap.put("users",usersIds);
//      usersIds.remove(currentUid);
//      usersIds.remove(creatorId);
    }else{
      Log.d("ttt","newSelectedUserIdsList is empty");
    }

    if (imageUri != null) {
      final StorageReference reference = FirebaseStorage.getInstance().getReference()
              .child("Group-Images/").child(UUID.randomUUID().toString() + "-" +
                      System.currentTimeMillis());

      final UploadTask uploadTask = reference.putFile(imageUri);

      uploadTaskMap = new HashMap<>();

      StorageTask<UploadTask.TaskSnapshot> onSuccessListener =
              uploadTask.addOnSuccessListener(taskSnapshot -> {
                uploadTaskMap.remove(uploadTask);
                reference.getDownloadUrl().addOnSuccessListener(uri -> {

                  if(groupImageUrl!=null && !groupImageUrl.isEmpty()){
                    FirebaseStorage.getInstance().getReferenceFromUrl(groupImageUrl)
                            .delete();
                  }

                  updateMap.put("imageUrl",groupImageUrl = uri.toString());

                  updateGroup(updateMap,groupUpdateDialog);

                });
              }).addOnCompleteListener(task -> new File(imageUri.getPath()).delete());

      uploadTaskMap.put(uploadTask, onSuccessListener);

    }else if(!updateMap.isEmpty()){
      updateGroup(updateMap,groupUpdateDialog);
//        firebaseMessageDocRef.update(updateMap);

    }else{
      Toast.makeText(this, "No changes were made to the group!",
              Toast.LENGTH_SHORT).show();
      groupUpdateDialog.dismiss();
    }
  }

  private void updateUsersLastSeenMessages(ProgressDialog groupUpdateDialog){


    final Map<String, Object> lastSeenMap = new HashMap<>();

//    usersIds.removeAll(newSelectedUserIdsList);
//    final ArrayList<String> removedUsers = usersIds;
//    usersIds.addAll(newSelectedUserIdsList);

//    for(String removedUser:newSelectedUserIdsList){
//
//      if(adminsList.contains(removedUser)){
//        firebaseMessageDocRef.update("groupAdmins", FieldValue.arrayRemove(removedUser));
//      }
//
//      userLastSeenDatabaseRef.child(removedUser).removeValue();
//    }

      for(String userId:newSelectedUserIdsList){
        lastSeenMap.put(userId,"0");
      }

//    newSelectedUserIdsList.addAll(usersIds);

    userLastSeenDatabaseRef.updateChildren(lastSeenMap).addOnSuccessListener(new OnSuccessListener<Void>() {
       @Override
       public void onSuccess(Void aVoid) {

         usersIds.remove(currentUid);
         usersIds.remove(creatorId);

         Toast.makeText(GroupEditingActivity.this, "Group updated successfully!",
                 Toast.LENGTH_SHORT).show();

         groupUpdateDialog.dismiss();
       }
     }).addOnFailureListener(new OnFailureListener() {
       @Override
       public void onFailure(@NonNull Exception e) {
         groupUpdateDialog.dismiss();
       }
     });
  }

  private void updateGroup(Map<String,Object> updateMap,ProgressDialog groupUpdateDialog){
    firebaseMessageDocRef.update(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
      @Override
      public void onSuccess(Void aVoid) {

//        newSelectedUserIdsList.removeAll(usersIds);

        if(newSelectedUserIdsList!=null && !newSelectedUserIdsList.isEmpty()){

          updateUsersLastSeenMessages(groupUpdateDialog);

          sendGroupInvitationNotification();

          newSelectedUserIdsList.clear();

        }else{
          groupUpdateDialog.dismiss();
          Toast.makeText(GroupEditingActivity.this, "Group updated successfully!",
                  Toast.LENGTH_SHORT).show();
        }

      }
    }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {

        groupUpdateDialog.dismiss();
        Toast.makeText(GroupEditingActivity.this, "Failed to update group!" +
                        "Please try again",
                Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void sendGroupInvitationNotification(){

    usersRef.document().get()
            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot snapshot) {
                if(snapshot.exists()){

                  final String username = snapshot.getString("username");

                  final Data data = new Data(
                          currentUid,
                          username+" "+ getResources().getString(R.string.added_to_group),
                          groupNameEd.getText().toString().trim(),
                          groupImageUrl,
                          "Group added",
                          FirestoreNotificationSender.TYPE_GROUP_ADDED,
                          firebaseMessageDocRef.getId());

                  for (String userId : newSelectedUserIdsList) {
                    CloudMessagingNotificationsSender.sendNotification(userId, data);
                    FirestoreNotificationSender.sendFirestoreNotification(
                            userId, FirestoreNotificationSender.TYPE_GROUP_ADDED,
                            username +" "+ getResources().getString(R.string.added_to_group),
                            groupName,
                            firebaseMessageDocRef.getId());
                  }

                }
              }
            });
  }

  @Override
  public void onClick(View view) {

    if(view.getId() == changeImageIv.getId()){

      Files.startImageFetchIntent(this);

    }else if(view.getId() == addMemberIv.getId() && usersIds!=null){

    if(usersIds.size() == 100){
      Toast.makeText(this, "You can't add more than 100 member to this group!",
              Toast.LENGTH_SHORT).show();
      return;
    }

      Intent intent = new Intent(GroupEditingActivity.this,UserSearchActivity.class)
              .putStringArrayListExtra("selectedUserIds",usersIds)
              .putExtra("excludePreviousUsers",true);

      startActivityForResult(intent, 3);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    UploadTaskUtil.cancelUploadTasks(uploadTaskMap);

    if(!groupNameEd.getText().toString().trim().equals(groupName) || imageUri!=null){

      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setTitle("Do you want to leave without updating this group?");
      alert.setMessage("Leaving will discard these changes");

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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d("ttt","gotten result");
    if (requestCode == 3 && data != null &&
            data.hasExtra("previousSearchSelectedUserIdsList")) {
      Log.d("ttt","result previousSearchSelectedUserIdsList");
      final ArrayList<String> newAddedUsers =
              data.getStringArrayListExtra("previousSearchSelectedUserIdsList");

//      if(newSelectedUserIdsList.size() < 2){
//
//        Toast.makeText(this, "You can't have less than 2 members in your group!",
//                Toast.LENGTH_SHORT).show();
//
//      }else{

      if(newSelectedUserIdsList == null){
        newSelectedUserIdsList = new ArrayList<>();
      }

      for(String userId:newAddedUsers){
        Log.d("ttt","newAddedUsers: "+userId);
      }

      newAddedUsers.remove(creatorId);
      newAddedUsers.remove(currentUid);

      newSelectedUserIdsList.addAll(newAddedUsers);


      userPreviews.clear();
      groupMembersAdapter.notifyDataSetChanged();

//        if(originalUserIds == null){
//          originalUserIds = usersIds;
//        }
//        usersIds = newPickedUserIds;
//      newAddedUsers.removeAll(usersIds);

      usersIds.addAll(newAddedUsers);

      updateParticipantsCount();

      if (usersIds.size() > 10) {
        getUsers(usersIds.subList(0, 10));
        if(scrollListener == null){
          groupMembersRv.addOnScrollListener(scrollListener = new UserIdsScrollListener());
        }
      }else{
        getUsers(usersIds);
      }
//      }

    }else if (resultCode == RESULT_OK && requestCode == Files.PICK_IMAGE && data != null) {

      imageUri = data.getData();
      Picasso.get().load(imageUri).fit().centerCrop().into(groupIv);

    }else{
      Log.d("ttt","result is empty");
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

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && requestCode == Files.PICK_IMAGE) {
      Files.startDocumentFetchIntent(this);
    }
  }

  private void removeUserFromList(String userId){

    if(newSelectedUserIdsList!=null){
      newSelectedUserIdsList.remove(userId);
    }

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

            updateParticipantsCount();

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

          Log.d("groupEditing","child removed: "+snapshot.getKey());
          if(lastSelectedUserId!=null && userUpdateDialog!=null && userUpdateDialog.isShowing()){
            userUpdateDialog.cancel();
            Toast.makeText(GroupEditingActivity.this,
                    "This user was removed by another admin!", Toast.LENGTH_SHORT).show();
            return;
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