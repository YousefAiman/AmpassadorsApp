package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Fragments.ChattingFragment;
import hashed.app.ampassadors.Fragments.GroupsFragment;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class Home_Activity extends AppCompatActivity  {

    TextView newpost;
    TextView newPoll;
    BottomNavigationView nav_btom;

    FrameLayout homeFrameLayout;
    DrawerLayout drawer_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        SetUpCompetent();
        OnClickButtons();
        createUserLikesListener();

    }


    private void createUserLikesListener(){


        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(value!=null && value.exists()){

                            if(GlobalVariables.getCurrentUsername() == null){
                                GlobalVariables.setCurrentUsername(value.getString("username"));
                                GlobalVariables.setCurrentUserImageUrl(value.getString("imageUrl"));
                            }


                            if(value.contains("Likes")){
                                List<String> likes = (List<String>) value.get("Likes");
                                GlobalVariables.setLikesList(likes);
                            }
                        }


                    }
                });


    }
    public void SetUpCompetent() {


        newPoll = findViewById(R.id.new_poll);
        newpost = findViewById(R.id.new_post);
        nav_btom = findViewById(R.id.nav_btom);
        homeFrameLayout = findViewById(R.id.homeFrameLayout);
        drawer_layout = findViewById(R.id.drawer_layout);

    }

    // Tool bar


    // Buttons Click
    public void OnClickButtons() {


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

        nav_btom.setSelectedItemId(R.id.home);
    }


    private void replaceFragment(Fragment fragment){

        getSupportFragmentManager().beginTransaction().replace(
                homeFrameLayout.getId(),fragment
        ).commit();

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

    @Override
    public void onBackPressed() {

        if(nav_btom.getSelectedItemId()!=R.id.home){
            nav_btom.setSelectedItemId(R.id.home);
            replaceFragment(new PostsFragment());
        }else{
            super.onBackPressed();
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
}