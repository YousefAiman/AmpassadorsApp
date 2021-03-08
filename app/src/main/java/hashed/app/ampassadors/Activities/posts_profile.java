package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Adapters.UserPostAdapter;
import hashed.app.ampassadors.Adapters.UsersAdapter;
import hashed.app.ampassadors.Fragments.PostsFragment;
import hashed.app.ampassadors.MainActivity;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.UserInfo;
import hashed.app.ampassadors.Objects.UserPostData;
import hashed.app.ampassadors.R;


public class posts_profile extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {
    FirebaseFirestore firebaseFirestore;
    Query query;
    List<UserPostData> postData;
    UserPostAdapter adapter;
    RecyclerView post_list;
    DocumentSnapshot lastDocSnap;
    boolean isLoadingMessages;
    SwipeRefreshLayout swipeRefresh;
    TextView username;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userid ;
    ImageView imageView;
    boolean status;
    FloatingActionButton floatingButton;
    Toolbar toolbar;


    public posts_profile() {
        // Required empty public constructor
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_profile);

        floatingButton = findViewById(R.id.floatingbtn);
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UsersPost.class);
                startActivity(intent);

            }
        });



        toolbar = findViewById(R.id.toolbar);
        getUserNaImg();
        firebaseFirestore = FirebaseFirestore.getInstance();
        query = firebaseFirestore.collection("Users").
                document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("UserPosts")
                .orderBy("publishTime",
                Query.Direction.DESCENDING).limit(10);
        postData = new ArrayList<>();

        adapter = new UserPostAdapter(postData, posts_profile.this);
        post_list = findViewById(R.id.userpost_recycler);

        post_list.setLayoutManager(new LinearLayoutManager(posts_profile.this, RecyclerView.VERTICAL, false));


//        swipeRefresh = findViewById(R.id.swipeRefreesh);
//        swipeRefresh.setOnRefreshListener(this);
//
//        Toolbar toolbar = findViewById(R.id.home_activity_toolbar);

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ((Home_Activity)getApplicationContext())
//                        .showDrawer();
//            }
//        });
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_online:

                DocumentReference reference = fStore.collection("Users").document(userid);

                if (status){
                    reference.update("status", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            status = false;
                            toolbar.getMenu().findItem(R.id.action_online).setTitle("online");
                        }
                    });
                }else{

                    reference.update("status", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            status = true;
                            toolbar.getMenu().findItem(R.id.action_online).setTitle("away");

                        }
                    });
                }
                return true;

            case R.id.action_about:
                startActivity( new Intent(posts_profile.this, profile.class));

        }


        return false;
    }

    @Override
    public void onRefresh() {

        postData.clear();
        adapter.notifyDataSetChanged();
        lastDocSnap = null;
        ReadPost(true);
    }

    private class ChatsScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingMessages &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt","is at bottom man");

                ReadPost(false);

            }
        }
    }


    private void ReadPost(boolean isInitial) {
        swipeRefresh.setRefreshing(true);
        isLoadingMessages = true;
        Query updatedQuery = query;
        if(lastDocSnap!=null){
            updatedQuery = query.startAfter(lastDocSnap);
        }
        updatedQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                            queryDocumentSnapshots.size()-1
                    );
                    postData.addAll(queryDocumentSnapshots.toObjects(UserPostData.class));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(isInitial){
                    adapter.notifyDataSetChanged();

                }else{
                    adapter.notifyItemRangeInserted((postData.size()-task.getResult().size())-1,
                            task.getResult().size());
                }
                swipeRefresh.setRefreshing(false);

            }
        });
    }


    private void setUpToolBarAndActions() {

        final Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(posts_profile.this, "tool", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
    }

    private void getUserNaImg(){
        username = findViewById(R.id.textView6);
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        imageView = findViewById(R.id.profile_picture);

        fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String user_name = task.getResult().getString("username");
                        String imgUrl = task.getResult().getString("imageUrl");



                        task.getResult().getBoolean("status");
                        toolbar.setOnMenuItemClickListener(posts_profile.this);

                        username.setText(user_name);
                        Picasso.get().load(imgUrl).fit().into(imageView);
                    }
                }else {
                    Toast.makeText(posts_profile.this, "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}