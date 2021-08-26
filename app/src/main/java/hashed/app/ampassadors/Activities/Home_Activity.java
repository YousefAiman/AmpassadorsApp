package hashed.app.ampassadors.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Fragments.ChattingFragment;
import hashed.app.ampassadors.Fragments.MeetingsFragment;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Fragments.PostsProfileFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.SigninUtil;

public class  Home_Activity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userid = auth.getCurrentUser().getUid();
    private DocumentReference reference;
    private FirebaseFirestore firebaseFirestore;
    private BottomNavigationView nav_btom;
    private FrameLayout homeFrameLayout;
    private DrawerLayout drawer_layout;
    private NavigationView navigationview;
    private List<ListenerRegistration> listenerRegistrations;

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseFirestore.getInstance().collection("Users")
                .document(userid).update("status", true);
    }

//    public static void main(String[] args){
//
//        String car = "Car";
//        String boy = "boY";
//        boolean carIsFirst = car.compareTo(boy) < 0;
//        boolean boyIsFirst = boy.compareTo(car) < 0;
//
//        Log.d("ttt","car is first: "+carIsFirst);
//        Log.d("ttt","boy is first: "+boyIsFirst);
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        SetUpCompetent();
        GlobalVariables.getInstance().setAppIsRunning(true);


        firebaseFirestore = FirebaseFirestore.getInstance();

        replaceFragment(new PostsFragment());

        if (nav_btom.getSelectedItemId() != R.id.home) {
            nav_btom.setSelectedItemId(R.id.home);
        }


        if (auth.getCurrentUser().isAnonymous()) {
            navigationview.inflateMenu(R.menu.menu_nav);
        } else {
            if (GlobalVariables.getInstance().getRole() != null && GlobalVariables.getInstance().getRole().equals("Admin")) {
                navigationview.inflateMenu(R.menu.menu_admin);
            } else {
                navigationview.inflateMenu(R.menu.menu_nav);
            }
        }

//        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//
//        FirebaseFirestore.getInstance().collection("Users").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//
//                for(DocumentSnapshot snapshot:snapshots){
//
//                    String email = snapshot.getString("email");
//                    String password = snapshot.getString("password");

//
//                    String imageUrl = snapshot.getString("imageUrl");
//                    if(imageUrl!=null && !imageUrl.isEmpty()){
//                        firebaseStorage.getReferenceFromUrl(imageUrl).delete();
//                    }
//

//                    if(email!=null && !email.isEmpty() && password!=null && !password.isEmpty()){
//
//                        auth.signInWithEmailAndPassword(email,password)
//                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                            @Override
//                            public void onSuccess(AuthResult authResult) {
//
//                                authResult.getUser().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        snapshot.getReference().delete();
//                                    }
//                                });
//                            }
//                        });
//                    }else{
//
//                        snapshot.getReference().delete();
//
//                    }
//
//                }
//            }
//        });

//        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
//
//
//        FirebaseDatabase.getInstance().getReference().child("PrivateMessages")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        for (DataSnapshot child : snapshot.getChildren()) {
//
//                            for (DataSnapshot snapshot1 : child.child("messages").getChildren()) {
//                                if (snapshot1.hasChild("attachmentUrl")) {
//                                    firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(snapshot1
//                                            .child("attachmentUrl").getValue(String.class)))
//                                            .delete();
//                                }
//
//                                if (snapshot1.hasChild("videoThumbnail")) {
//                                    firebaseStorage.getReferenceFromUrl(Objects.requireNonNull(snapshot1
//                                            .child("videoThumbnail").getValue(String.class)))
//                                            .delete();
//                                }
//                            }
//
//                            child.getRef().removeValue();
//
//
//                        }
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });




//    CollectionReference postRef = FirebaseFirestore.getInstance().collection("Posts");
//
//     postRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//        @Override
//        public void onSuccess(QuerySnapshot snapshots) {
//
//
//            for(DocumentSnapshot documentSnapshot:snapshots){
//
//                documentSnapshot.getReference().collection("Likes")
//                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot snapshots) {
//
//                        for(DocumentSnapshot snapshot:snapshots){
//                            snapshot.getReference().delete();
//                        }
//                    }
//                });
////
//                postRef.collection(collectionName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot snapshots) {
//                        if(snapshots != null){
//                            for(DocumentSnapshot documentSnapshot:snapshots){
//                                documentSnapshot.getReference().delete();
//                            }
//                        }
//                    }
//                });
//
//                Log.d("ttt",);
//
//                long postType =  documentSnapshot.getLong("type");
//
//                DocumentReference deletedPostRef = documentSnapshot.getReference();
//
//                deleteCollection("Likes",deletedPostRef);
//                deleteCollection("Comments",deletedPostRef);
//
//                if(postType == PostData.TYPE_POLL){
//                    deleteCollection("Options",deletedPostRef);
//                    deleteCollection("UserVotes",deletedPostRef);
//                }

//
//                String attachmentUrl =  documentSnapshot.getString("attachmentUrl");
//                String videoThumbnailUrl =  documentSnapshot.getString("videoThumbnailUrl");
//
//
//                DocumentReference deletedPostRef = documentSnapshot.getReference();
//
//                deletedPostRef.update("deleting",true).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        deletedPostRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if(task.isSuccessful()){
//                                    deleteCollection("Likes",deletedPostRef);
//                                    deleteCollection("Comments",deletedPostRef);
//
//
//                                    if(postType == PostData.TYPE_POLL){
//                                        deleteCollection("Options",deletedPostRef);
//                                        deleteCollection("UserVotes",deletedPostRef);
//                                    }
//
//                                    final FirebaseStorage storage = FirebaseStorage.getInstance();
//
//                                    if(attachmentUrl!=null){
//                                        storage.getReferenceFromUrl(attachmentUrl).delete();
//                                    }
//
//                                    if(videoThumbnailUrl!=null){
//                                        storage.getReferenceFromUrl(videoThumbnailUrl).delete();
//                                    }
//
//                                    FirebaseFirestore.getInstance().collection("Notifications")
//                                            .whereEqualTo("destinationId",documentSnapshot.getId())
//                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                        @Override
//                                        public void onSuccess(QuerySnapshot snapshots) {
//                                            for(DocumentSnapshot documentSnapshot:snapshots){
//                                                documentSnapshot.getReference().delete();
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        });

//                    }
//                });
//
//            }
//        }
//    });
//
//        CollectionReference privateMessagesRef = FirebaseFirestore.getInstance().collection("PrivateMessages");
//
//        final FirebaseStorage storage = FirebaseStorage.getInstance();
//
//        privateMessagesRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//                for(DocumentSnapshot snapshot:snapshots){
//
//                    if(snapshot.contains("imageUrl")) {
//                        String imageUrl = snapshot.getString("imageUrl");
//
//                        if (imageUrl != null && !imageUrl.isEmpty()) {
//                            storage.getReferenceFromUrl(imageUrl).delete();
//                        }
//
//                    }
//                    snapshot.getReference().delete();
//                }
//            }
//        });






//    FirebaseFirestore.getInstance().collection("Users")
//          .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//      @Override
//      public void onSuccess(QuerySnapshot snapshots) {
//
//        for(DocumentSnapshot snapshot:snapshots.getDocuments()){
//          snapshot.getReference().update("status",false);
////          if(snapshot.contains("email") && snapshot.contains("password")){
////
////            String email = snapshot.getString("email");
////            String password = snapshot.getString("password");
////
////            if(email!=null && !email.isEmpty() && password!=null && !password.isEmpty()){
////              auth.signInWithEmailAndPassword(email,password)
////                      .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
////                        @Override
////                        public void onSuccess(AuthResult authResult) {
////                          if(authResult!=null){
////                            Log.d("ttt","logged in as :"+authResult.getUser().getEmail());
////                            if(authResult.getUser().isEmailVerified()){
////                              snapshot.getReference().update("isEmailVerified",true);
////                            }
//////                            auth.signOut();
////                          }
////                        }
////                      });
////            }
////
////          }
//        }
//      }
//    });

//    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PrivateMessages");
//    FirebaseFirestore.getInstance().collection("PrivateMessages")
//            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//      @Override
//      public void onSuccess(QuerySnapshot snapshots) {
//
//        for(DocumentSnapshot snapshot:snapshots.getDocuments()){
//
//          final HashMap<String, String> usersLastSeenMap = new HashMap<>();
//          final List<String> users = (List<String>) snapshot.get("users");
//
//          ref.child(snapshot.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//              if(snapshot.exists()){
//                  for(String user:users){
////                    if(snapshot.hasChild("LastSeenMessage:"+user)){
////                      usersLastSeenMap.put(user,snapshot.child("LastSeenMessage:"+user)
////                              .getValue(String.class));
////
////                      snapshot.child("LastSeenMessage:"+user).getRef().removeValue();
////
////                    }else{
//                      usersLastSeenMap.put(user,"0");
////                    }
//
//                    if(users.indexOf(user) == users.size()-1){
//
//                      //last user loop
//
//                      snapshot.getRef().child("UsersLastSeenMessages").setValue(usersLastSeenMap);
//                    }
//                  }
//              }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//            }
//          });
//        }
//      }
//    });

    OnClickButtons();
    createUserLikesListener();
    createNotificationListener();

  }

    private static void deleteCollection(String collectionName,DocumentReference postRef){
        postRef.collection(collectionName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                if(snapshots != null){
                    for(DocumentSnapshot documentSnapshot:snapshots){
                        documentSnapshot.getReference().delete();
                    }
                }
            }
        });
    }


    private void createUserLikesListener() {

    listenerRegistrations = new ArrayList<>();

    listenerRegistrations.add(
            FirebaseFirestore.getInstance().collection("Users")
                    .document(userid)
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                      @Override
                      public void onEvent(@Nullable DocumentSnapshot value,
                                          @Nullable FirebaseFirestoreException error) {

                        if (value != null && value.exists()) {

                          if (GlobalVariables.getInstance().getCurrentUsername() == null) {

                            GlobalVariables.getInstance().setCurrentUsername(
                                    value.getString("username"));

                            GlobalVariables.getInstance().setCurrentUserImageUrl(
                                    value.getString("imageUrl"));

                          }

                          GlobalVariables.getInstance().setRole(value.getString("Role"));
                          if (value.contains("Likes")) {
                            final List<String> likes = (List<String>) value.get("Likes");
                            GlobalVariables.getInstance().setLikesList(likes);
                          }
                        }
                      }
                    })
    );

  }

  public void SetUpCompetent() {

    nav_btom = findViewById(R.id.nav_btom);
    homeFrameLayout = findViewById(R.id.homeFrameLayout);
    drawer_layout = findViewById(R.id.drawer_layout);
    navigationview = findViewById(R.id.navigationview);

  }


  // Buttons Click
  public void OnClickButtons() {

    navigationview.setNavigationItemSelectedListener(this);

    nav_btom.setOnNavigationItemSelectedListener(item -> {
      if (item.getItemId() == R.id.home) {
        if (nav_btom.getSelectedItemId() != R.id.home) {
          replaceFragment(new PostsFragment());
        }
      } else if (item.getItemId() == R.id.profile) {
          if (auth.getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
            return false;
          }else{
              if (nav_btom.getSelectedItemId() != R.id.profile) {
                  replaceFragment(new PostsProfileFragment());
              }
          }

      } else if (item.getItemId() == R.id.chat) {

          if (auth.getCurrentUser().isAnonymous()){
              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
            return false;
          }else{
              if (nav_btom.getSelectedItemId() != R.id.chat) {
                  replaceFragment(new ChattingFragment());
              }
          }

      } else if (item.getItemId() == R.id.charity) {

          if (auth.getCurrentUser().isAnonymous()){
              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
            return false;
          }else{
              if (nav_btom.getSelectedItemId() != R.id.charity) {
                  replaceFragment(new MeetingsFragment());
              }
          }

      }

      return true;
    });

  }


  public void replaceFragment(Fragment fragment) {

    getSupportFragmentManager().beginTransaction().replace(R.id.homeFrameLayout, fragment).commit();

  }
  @Override
  public void onBackPressed() {

    if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
      drawer_layout.closeDrawer(GravityCompat.START);
    } else {
      Log.d("ttt","first frag: "+getSupportFragmentManager().getFragments()
              .get(0));

      Log.d("ttt","last frag: "+getSupportFragmentManager().getFragments()
      .get(getSupportFragmentManager().getFragments().size()-1));
      if (nav_btom.getSelectedItemId() != R.id.home) {
        nav_btom.setSelectedItemId(R.id.home);
        replaceFragment(new PostsFragment());
      } else {
        super.onBackPressed();
      }
    }

  }

    public void showDrawer() {
        drawer_layout.openDrawer(GravityCompat.START);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(listenerRegistrations!=null && !listenerRegistrations.isEmpty()){
            for(ListenerRegistration listenerRegistration:listenerRegistrations){
                listenerRegistration.remove();
            }
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Log.d("ttt","navigation clicked");

      drawer_layout.closeDrawer(GravityCompat.START);

      if(item.getItemId() == R.id.log_out){
                Log.d("ttt","log_out clicked");
//                if(WifiUtil.checkWifiConnection(this)){
                    Log.d("ttt","internet exists");

                    NotificationManagerCompat.from(this).cancelAll();


          FirebaseFirestore.getInstance().collection("Users")
                  .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                  .update("status",false);

        if (AccessToken.getCurrentAccessToken() != null) {
          LoginManager.getInstance().logOut();
        }

        auth.signOut();

        GoogleSignIn.getClient(
                this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();

//        if(auth.getCurrentUser().getProviderId()){
//
//          GoogleSignInOptions gso = new GoogleSignInOptions
//                  .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                  .requestIdToken(getString(R.string.web_client_id))
//                  .requestEmail()
//                  .build();
//          final GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
//
//          client.revokeAccess();
//        if (client.asGoogleApiClient() != null && client.asGoogleApiClient().isConnected()) {
//            client.asGoogleApiClient().clearDefaultAccountAndReconnect();
//        }
//
//
//        }

                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(Home_Activity.this, FirebaseMessagingService.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    Toast.makeText(Home_Activity.this, R.string.Succes_Login,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Home_Activity.this, sign_in.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    GlobalVariables.clear();
                    startActivity(intent);
                    finish();
//                }
            }else if (item.getItemId() == R.id.news){

//              B_Fragment fragment = new B_Fragment();
//              Bundle bundle = new Bundle();
//              bundle.putInt("postType",PostData.TYPE_NEWS);
//              fragment.setArguments(bundle);
//             replaceFragment(fragment);

          Intent intent = new Intent(Home_Activity.this, ShowNewsActivity.class);
          Bundle bundle = new Bundle();
          bundle.putInt("postType", PostData.TYPE_NEWS);
          intent.putExtras(bundle);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
      }else if (item.getItemId() == R.id.polls) {

            Intent intent = new Intent(Home_Activity.this, ShowPollsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("postType", PostData.TYPE_POLL);
            intent.putExtras(bundle);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);


//              B_Fragment fragment = new B_Fragment();
//              Bundle bundle = new Bundle();
//              bundle.putInt("postType",PostData.TYPE_POLL);
//              fragment.setArguments(bundle);
//              replaceFragment(fragment);


        } else if (item.getItemId() == R.id.courses) {

        if (auth.getCurrentUser().isAnonymous()) {

          SigninUtil.getInstance(Home_Activity.this,
                  Home_Activity.this).show();
        }else{
          Intent intent = new Intent(Home_Activity.this, CoursesActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);

        }

        } else if (item.getItemId() == R.id.complaints) {

          if (auth.getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
          }else{
              Intent intent = new Intent(Home_Activity.this, ComplaintsActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
          }

        } else if (item.getItemId() == R.id.listComplaints &&
              GlobalVariables.getInstance().getRole().equals("Admin")) {

            Intent intent = new Intent(Home_Activity.this, ComplanitsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.policy) {
            Intent intent = new Intent(Home_Activity.this, PrivacyPolicy.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);


        } else if (item.getItemId() == R.id.proposals) {
          if (auth.getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
          }else{
              Intent intent = new Intent(Home_Activity.this, SuggestionsActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
          }


        } else if (item.getItemId() == R.id.listSuggestion &&
              GlobalVariables.getInstance().getRole().equals("Admin")) {
            Intent intent = new Intent(Home_Activity.this, List_Sug_Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(Home_Activity.this, About_us.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.user_requests) {
            Intent intent = new Intent(Home_Activity.this, Admin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.contact_us) {
            Intent intent = new Intent(Home_Activity.this, ContactUsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else  if (item.getItemId() == R.id.repotred){

          Intent intent = new Intent(Home_Activity.this, RepostedPosts.class);
          Bundle bundle = new Bundle();
          bundle.putInt("postType", PostData.TYPE_NEWS);
          intent.putExtras(bundle);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
      }

    return true;
    }

    private void createNotificationListener() {

    final String indicatorAction = BuildConfig.APPLICATION_ID + ".notificationIndicator";
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(indicatorAction);
    registerReceiver(new NotificationIndicatorReceiver(), intentFilter);

//    final AtomicInteger notificationCount = new AtomicInteger();

    listenerRegistrations.add(
            FirebaseFirestore.getInstance().collection("Notifications")
                    .whereEqualTo("receiverId", userid)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                      @Override
                      public void onEvent(@Nullable QuerySnapshot value,
                                          @Nullable FirebaseFirestoreException error) {
        final AtomicInteger notificationCount = new AtomicInteger();
        listenerRegistrations.add(
                FirebaseFirestore.getInstance().collection("Notifications")
                .whereEqualTo("receiverId", userid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {

                        if(value==null)
                            return;
                        for(DocumentChange dc:value.getDocumentChanges()){

                            switch (dc.getType()){
                                case ADDED:

                                    Log.d("ttt","added notificationn");

                                    Log.d("ttt","notificationCount: "+
                                            notificationCount.get());
                                    if(notificationCount.getAndIncrement() == 0){
                                        Intent intent = new Intent(indicatorAction);
                                        intent.putExtra("showIndicator",true);
                                        sendBroadcast(intent);
                                    }


                              Log.d("ttt", "notificationCount: " +
                                      notificationCount.get());

                              break;
                              case REMOVED:

                                if(notificationCount.decrementAndGet() == 0){

                                  Intent intent = new Intent(indicatorAction);
                                  intent.putExtra("showIndicator",false);
                                  sendBroadcast(intent);

                                }
                                break;
                            }

                            GlobalVariables.getInstance().setNotificationsCount(notificationCount.get());

                        }
                    }
                })
        );

    }
    }));


  }


  }