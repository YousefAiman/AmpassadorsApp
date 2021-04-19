package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Fragments.PostsProfileFragment;
import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class ProfileActiv extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener {

    FirebaseFirestore firebaseFirestore;
    Query query;
    List<PostData> postData;


    PostAdapter adapter;
    RecyclerView post_list;
    DocumentSnapshot lastDocSnap;
    boolean isLoadingMessages;
    SwipeRefreshLayout swipeRefresh;
    TextView username;
    FirebaseAuth fAuth;
FirebaseFirestore fStore;
    String userid;
    ImageView imageView;
    boolean status;
    FloatingActionButton floatingButton;
    Toolbar toolbar;
        TextView bio;
    String bio_txt;
    CollectionReference collectionReference;
    private FrameLayout frameLayout;
    private NotificationIndicatorReceiver notificationIndicatorReceiver;
    Intent userimage;
    String id;
    public ProfileActiv() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        username = findViewById(R.id.textView6);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
        });
        bio = findViewById(R.id.textView15);
        imageView = findViewById(R.id.profile_picture);
        swipeRefresh = findViewById(R.id.swipeRefreshLayout);
        frameLayout = findViewById(R.id.frameLayout);
        collectionReference = FirebaseFirestore.getInstance().collection("Users");
        swipeRefresh.setOnRefreshListener(this);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                collectionReference.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String image = documentSnapshot.get("imageUrl").toString();
//                        Toolbar toolbar1 = view.findViewById(R.id.fullScreenToolbar);

                        toolbar.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.VISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(),
                                new ImageFullScreenFragment(image), "FullScreen")
                                .commit();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActiv.this, "Empty image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Intent id = getIntent();
        if (id.hasExtra("userId")) {

            String mn = id.getStringExtra("userId");
            firebaseFirestore = FirebaseFirestore.getInstance();
            query = firebaseFirestore.collection("Users").
                    document(mn)
                    .collection("UserPosts")
                    .orderBy("publishTime", Query.Direction.DESCENDING).limit(10);
            postData = new ArrayList<>();
            adapter = new PostAdapter(postData, ProfileActiv.this,true);
            post_list = findViewById(R.id.userpost_recycler);
            post_list.setLayoutManager(new LinearLayoutManager(ProfileActiv.this, RecyclerView.VERTICAL,
                    false));
            post_list.setAdapter(adapter);
            getUserNaImg();
            ReadPost(true);
        }

//        firebaseFirestore = FirebaseFirestore.getInstance();
//        query = firebaseFirestore.collection("Users").
//                document(userid)
//                .collection("UserPosts")
//                .orderBy("publishTime",
//                        Query.Direction.DESCENDING).limit(10);
//        postData = new ArrayList<>();
//        adapter = new PostAdapter(postData, ProfileActiv.this);
//        post_list = findViewById(R.id.userpost_recycler);
//        post_list.setLayoutManager(new LinearLayoutManager(ProfileActiv.this, RecyclerView.VERTICAL,
//                false));
//        post_list.setAdapter(adapter);
//        ReadPost(true);
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
                ReadPost(false);
            }
        }
    }

    private void ReadPost(boolean isInitial) {
        swipeRefresh.setRefreshing(true);
        isLoadingMessages = true;
        Query updatedQuery = query;
//        if(lastDocSnap!=null){
//            updatedQuery = query.startAfter(lastDocSnap);
//        }
        updatedQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                            queryDocumentSnapshots.size() - 1
                    );
                    postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (isInitial) {
                    adapter.notifyDataSetChanged();
                } else {
                    adapter.notifyItemRangeInserted((postData.size() - task.getResult().size()) - 1,
                            task.getResult().size());
                }
                swipeRefresh.setRefreshing(false);

            }
        });
    }

    private void setupNotificationReceiver() {

        notificationIndicatorReceiver =
                new NotificationIndicatorReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.hasExtra("showIndicator")) {
                            final MenuItem item = toolbar.getMenu().findItem(R.id.action_notifications);
                            if (intent.getBooleanExtra("showIndicator", false)) {
                                item.setIcon(R.drawable.notification_indicator_icon);
                            } else {
                                item.setIcon(R.drawable.notification_icon);
                            }
                        }
                    }
                };

        ProfileActiv.this.registerReceiver(notificationIndicatorReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".notificationIndicator"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationIndicatorReceiver != null) {
            ProfileActiv.this.unregisterReceiver(notificationIndicatorReceiver);
        }
    }

//
    private void getUserNaImg() {
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();

        Intent userimage = getIntent();
        if (userimage.hasExtra("username") && userimage.hasExtra("ImageUrl")) {
            String userimg = userimage.getStringExtra("ImageUrl");
            String usernam = userimage.getStringExtra("username");
             id  = userimage.getStringExtra("userId");

            fStore.collection("Users").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    bio_txt = documentSnapshot.getString("Bio");

                    if (bio_txt == null || bio_txt.isEmpty()){
                        bio.setVisibility(View.GONE);
                    }else {
                        bio_txt = documentSnapshot.getString("Bio");
                        bio.setText(bio_txt);
                    }
                    Picasso.get().load(userimg).fit().into(imageView);
                    username.setText(usernam);
                }
            });
                   }




        }
//        fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()){
//                    if (task.getResult().exists()){
//                        String user_name = task.getResult().getString("username");
//                        String imgUrl = task.getResult().getString("imageUrl");
//
//                        task.getResult().getBoolean("status");
//
//                        username.setText(user_name);
//                        Picasso.get().load(imgUrl).fit().into(imageView);
//                    }
//                }else {
//                    Toast.makeText(ProfileActiv.this, "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    //private void setUpToolBarAndActions() {
//
//        final Toolbar toolbar = findViewById(R.id.toolbar);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(ProfileActiv.this, "tool", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
