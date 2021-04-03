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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
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

public class Home_Activity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

  private String userid;
  private FirebaseAuth auth;
  private DocumentReference reference;
  private FirebaseFirestore firebaseFirestore;
  private BottomNavigationView nav_btom;
  private FrameLayout homeFrameLayout;
  private DrawerLayout drawer_layout;
  private NavigationView navigationview;
  private List<ListenerRegistration> listenerRegistrations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home_activity);

    SetUpCompetent();
    GlobalVariables.setAppIsRunning(true);

    auth = FirebaseAuth.getInstance();
    userid = auth.getCurrentUser().getUid();
    firebaseFirestore = FirebaseFirestore.getInstance();

    replaceFragment(new PostsFragment());

    if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
      navigationview.inflateMenu(R.menu.menu_nav);
    }else{
      if (GlobalVariables.getRole()!=null && GlobalVariables.getRole().equals("Admin")) {
        navigationview.inflateMenu(R.menu.menu_admin);
      } else {
        navigationview.inflateMenu(R.menu.menu_nav);
      }
    }


    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PrivateMessages");

    FirebaseFirestore.getInstance().collection("PrivateMessages")
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
      @Override
      public void onSuccess(QuerySnapshot snapshots) {

        for(DocumentSnapshot snapshot:snapshots.getDocuments()){

          final HashMap<String, String> usersLastSeenMap = new HashMap<>();
          final List<String> users = (List<String>) snapshot.get("users");

          ref.child(snapshot.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

              if(snapshot.exists()){
                  for(String user:users){
//                    if(snapshot.hasChild("LastSeenMessage:"+user)){
//                      usersLastSeenMap.put(user,snapshot.child("LastSeenMessage:"+user)
//                              .getValue(String.class));
//
//                      snapshot.child("LastSeenMessage:"+user).getRef().removeValue();
//
//                    }else{
                      usersLastSeenMap.put(user,"0");
//                    }

                    if(users.indexOf(user) == users.size()-1){

                      //last user loop

                      snapshot.getRef().child("UsersLastSeenMessages").setValue(usersLastSeenMap);
                    }
                  }
              }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
          });
        }
      }
    });


    OnClickButtons();
    createUserLikesListener();
    createNotificationListener();

  }

  private void createUserLikesListener() {

    listenerRegistrations = new ArrayList<>();

    listenerRegistrations.add(
            FirebaseFirestore.getInstance().collection("Users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                      @Override
                      public void onEvent(@Nullable DocumentSnapshot value,
                                          @Nullable FirebaseFirestoreException error) {

                        if (value != null && value.exists()) {

                          if (GlobalVariables.getCurrentUsername() == null) {

                            GlobalVariables.setCurrentUsername(
                                    value.getString("username"));

                            GlobalVariables.setCurrentUserImageUrl(
                                    value.getString("imageUrl"));

                          }

                          GlobalVariables.setRole(value.getString("Role"));
                          if (value.contains("Likes")) {
                            final List<String> likes = (List<String>) value.get("Likes");
                            GlobalVariables.setLikesList(likes);
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
          if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
            return false;
          }else{
              if (nav_btom.getSelectedItemId() != R.id.profile) {
                  replaceFragment(new PostsProfileFragment());
              }
          }

      } else if (item.getItemId() == R.id.chat) {

          if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
            return false;
          }else{
              if (nav_btom.getSelectedItemId() != R.id.chat) {
                  replaceFragment(new ChattingFragment());
              }
          }

      } else if (item.getItemId() == R.id.charity) {

          if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
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

    getSupportFragmentManager().beginTransaction().replace(
            homeFrameLayout.getId(), fragment
    ).commit();

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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == 3) {
                if(nav_btom.getSelectedItemId() == R.id.home){
                    final PostData postData = (PostData) data.getSerializableExtra("postData");
                    ((PostsFragment)getSupportFragmentManager().getFragments().get(0))
                            .addPostData(postData);

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

        if (AccessToken.getCurrentAccessToken() != null) {
          LoginManager.getInstance().logOut();
        }


        FirebaseAuth.getInstance().signOut();

                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(Home_Activity.this, FirebaseMessagingService.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    Toast.makeText(Home_Activity.this, R.string.Succes_Login,
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(Home_Activity.this, sign_in.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

          SigninUtil.getInstance(Home_Activity.this,
                  Home_Activity.this).show();
        }else{
          Intent intent = new Intent(Home_Activity.this, CoursesActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);

        }

        } else if (item.getItemId() == R.id.complaints) {

          if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
          }else{
              Intent intent = new Intent(Home_Activity.this, ComplaintsActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
          }

        } else if (item.getItemId() == R.id.listComplaints && GlobalVariables.getRole().equals("Admin")) {

            Intent intent = new Intent(Home_Activity.this, ComplanitsListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (item.getItemId() == R.id.policy) {
            Intent intent = new Intent(Home_Activity.this, PrivacyPolicy.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);


        } else if (item.getItemId() == R.id.proposals) {
          if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {

              SigninUtil.getInstance(Home_Activity.this,
                      Home_Activity.this).show();
          }else{
              Intent intent = new Intent(Home_Activity.this, SuggestionsActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
          }


        } else if (item.getItemId() == R.id.listSuggestion && GlobalVariables.getRole().equals("Admin")) {
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
                    .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                      @Override
                      public void onEvent(@Nullable QuerySnapshot value,
                                          @Nullable FirebaseFirestoreException error) {
        final AtomicInteger notificationCount = new AtomicInteger();
        listenerRegistrations.add(
                FirebaseFirestore.getInstance().collection("Notifications")
                .whereEqualTo("receiverId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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

                            GlobalVariables.setNotificationsCount(notificationCount.get());

                        }
                    }
                })
        );

    }
    }));


  }
  }