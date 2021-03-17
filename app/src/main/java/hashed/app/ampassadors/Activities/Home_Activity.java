package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.A_Fragment;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.B_Fragment;
import hashed.app.ampassadors.Fragments.ChattingFragment;
import hashed.app.ampassadors.Fragments.GroupsFragment;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessaging;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.WifiUtil;

public class Home_Activity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

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

        replaceFragment(new PostsFragment());
        OnClickButtons();

        createUserLikesListener();

    }

    private void createUserLikesListener(){

        listenerRegistrations = new ArrayList<>();
        listenerRegistrations.add(
        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException error) {

                        if(value!=null && value.exists()){

                            if(GlobalVariables.getCurrentUsername() == null){

                                GlobalVariables.setCurrentUsername(
                                        value.getString("username"));

                                GlobalVariables.setCurrentUserImageUrl(
                                        value.getString("imageUrl"));

                            }

                            if(value.contains("Likes")){
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

        drawer_layout.closeDrawer(GravityCompat.START);
        nav_btom.setOnNavigationItemSelectedListener(item -> {

            if(item.getItemId() == R.id.home){

                if(nav_btom.getSelectedItemId()!=R.id.home){
                    replaceFragment(new PostsFragment());
                }

            }else if(item.getItemId() == R.id.profile){

                if(nav_btom.getSelectedItemId()!=R.id.profile){
                    replaceFragment(new ProfileFragment());
                }

            }else if(item.getItemId() == R.id.chat){

                if(nav_btom.getSelectedItemId()!=R.id.chat){
                    replaceFragment(new ChattingFragment());
                }

            }else if(item.getItemId() == R.id.charity){


                if(nav_btom.getSelectedItemId()!=R.id.charity){
                    replaceFragment(new GroupsFragment());
                }

            }


            return true;
        });

    }


    public void replaceFragment(Fragment fragment){

        getSupportFragmentManager().beginTransaction().replace(
                homeFrameLayout.getId(),fragment
        ).commit();

    }

    @Override
    public void onBackPressed() {

        if(drawer_layout.isDrawerOpen(GravityCompat.START)){
            drawer_layout.closeDrawer(GravityCompat.START);
        }else{
            if(nav_btom.getSelectedItemId()!=R.id.home){
                nav_btom.setSelectedItemId(R.id.home);
                replaceFragment(new PostsFragment());
            }else{
                super.onBackPressed();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == 3){

            if(!getSupportFragmentManager().getFragments().isEmpty()){
                if(getSupportFragmentManager().getFragments().get(0) instanceof PostsFragment){
                    ((PostsFragment)getSupportFragmentManager().getFragments().get(0))
                            .onRefresh();
                }
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
                            new ComponentName(Home_Activity.this, FirebaseMessaging.class),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                    Toast.makeText(Home_Activity.this, "Logged out successfully",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Home_Activity.this, sign_in.class));
                    finish();
//                }
            }else if (item.getItemId() == R.id.news){
                replaceFragment(new B_Fragment());

            }
            else if (item.getItemId() == R.id.awreaness_post){

            }
            else if (item.getItemId() == R.id.courses){

            }
            else if (item.getItemId() == R.id.polls){
                replaceFragment(new A_Fragment());

                //getSupportFragmentManager().beginTransaction().replace( new A_Fragment()).commit();
            }
            else if (item.getItemId() == R.id.policy){

            }
            else if (item.getItemId() == R.id.complaints) {
                Intent mapIntent = new Intent(Home_Activity.this, ComplaintsActivity.class);
                startActivity(mapIntent);

            }else if (item.getItemId() == R.id.proposals){
                Intent pIntent = new Intent(Home_Activity.this, SuggestionsActivity.class);
                startActivity(pIntent);

            }
            else if (item.getItemId() == R.id.about){
            }

        return true;
    }
}