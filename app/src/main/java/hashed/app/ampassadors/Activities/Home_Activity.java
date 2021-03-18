package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

import hashed.app.ampassadors.A_Fragment;
import hashed.app.ampassadors.B_Fragment;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Fragments.ChattingFragment;
import hashed.app.ampassadors.Fragments.GroupsFragment;
import hashed.app.ampassadors.Fragments.MeetingsFragment;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Fragments.PostsProfileFragment;
import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class Home_Activity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

  String userid;
  FirebaseAuth auth;
  DocumentReference reference;
  FirebaseFirestore firebaseFirestore;
  private BottomNavigationView nav_btom;
  private FrameLayout homeFrameLayout;
  private DrawerLayout drawer_layout;
  private NavigationView navigationview;
  private List<ListenerRegistration> listenerRegistrations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home_activity);


    auth = FirebaseAuth.getInstance();
    userid = auth.getCurrentUser().getUid();
    firebaseFirestore = FirebaseFirestore.getInstance();

    if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){

      replaceFragment(new PostsFragment());
    }else{
      firebaseFirestore.collection("Users").document(userid).get().
              addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {

          GlobalVariables.setRole(documentSnapshot.getString("Role"));


        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if(task.isSuccessful()){
            replaceFragment(new PostsFragment());
          }
        }
      });
    }

    GlobalVariables.setAppIsRunning(true);
    SetUpCompetent();
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
    String admin = "AdmSSSin";
    if (admin.equals("Admin")) {
      navigationview.inflateMenu(R.menu.menu_admin);

    } else {
      navigationview.inflateMenu(R.menu.menu_nav);

    }


    navigationview.setNavigationItemSelectedListener(this);

    nav_btom.setOnNavigationItemSelectedListener(item -> {

      drawer_layout.closeDrawer(GravityCompat.START);

      if (item.getItemId() == R.id.home) {

        if (nav_btom.getSelectedItemId() != R.id.home) {
          replaceFragment(new PostsFragment());
        }

      } else if (item.getItemId() == R.id.profile) {

                if(nav_btom.getSelectedItemId()!=R.id.profile){
                    replaceFragment(new PostsProfileFragment());
                }

      } else if (item.getItemId() == R.id.chat) {

        if (nav_btom.getSelectedItemId() != R.id.chat) {
          replaceFragment(new ChattingFragment());
        }

      } else if (item.getItemId() == R.id.charity) {


        if (nav_btom.getSelectedItemId() != R.id.charity) {
          replaceFragment(new MeetingsFragment());
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

                if(nav_btom.getSelectedItemId()==R.id.home &&
                        getSupportFragmentManager().getFragments().get(0) instanceof PostsFragment){

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

            if(item.getItemId() == R.id.log_out){
                Log.d("ttt","log_out clicked");
//                if(WifiUtil.checkWifiConnection(this)){

                    Log.d("ttt","internet exists");

                    NotificationManagerCompat.from(this).cancelAll();

                    FirebaseAuth.getInstance().signOut();

                    getPackageManager().setComponentEnabledSetting(
                            new ComponentName(Home_Activity.this, FirebaseMessagingService.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    Toast.makeText(Home_Activity.this, "Logged out successfully",
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(Home_Activity.this, sign_in.class));
                    finish();
//                }
            }else if (item.getItemId() == R.id.news){

              B_Fragment fragment = new B_Fragment();
              Bundle bundle = new Bundle();
              bundle.putInt("postType",PostData.TYPE_NEWS);
              fragment.setArguments(bundle);
             replaceFragment(fragment);

            }else if (item.getItemId() == R.id.polls) {

              B_Fragment fragment = new B_Fragment();
              Bundle bundle = new Bundle();
              bundle.putInt("postType",PostData.TYPE_POLL);
              fragment.setArguments(bundle);
              replaceFragment(fragment);

    } else if (item.getItemId() == R.id.policy) {

    } else if (item.getItemId() == R.id.complaints) {
              Intent mapIntent = new Intent(Home_Activity.this, ComplaintsActivity.class);
              startActivity(mapIntent);
            } else if (item.getItemId() == R.id.policy){
                Intent inte = new Intent(Home_Activity.this, PrivacyPolicy.class);
                startActivity(inte);
            }else if (item.getItemId() == R.id.proposals){
                Intent pIntent = new Intent(Home_Activity.this, SuggestionsActivity.class);
                startActivity(pIntent);

    } else if (item.getItemId() == R.id.about) {
              Intent intent = new Intent(Home_Activity.this, About_us.class);
              startActivity(intent);
    }

    return true;
    }

    private void createNotificationListener() {

    final String indicatorAction = BuildConfig.APPLICATION_ID + ".notificationIndicator";
    final IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(indicatorAction);
    registerReceiver(new NotificationIndicatorReceiver(), intentFilter);

    final AtomicInteger notificationCount = new AtomicInteger();

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