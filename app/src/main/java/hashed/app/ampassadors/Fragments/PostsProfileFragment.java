package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Activities.ComplaintsActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.NotificationsActivity;
import hashed.app.ampassadors.Activities.PostNewActivity;
import hashed.app.ampassadors.Activities.Profile;
import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Adapters.UserPostAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.UserPostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.Files;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class PostsProfileFragment extends Fragment implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener {


    FirebaseFirestore firebaseFirestore;
    Query query;
    List<PostData> postData;
    //private UserPostData posda;
    PostAdapter adapter;
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

    private NotificationIndicatorReceiver notificationIndicatorReceiver;
    private TextView roleTv;
    public PostsProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_profile, container,
                false);

        floatingButton = view.findViewById(R.id.floatingbtn);
        username = view.findViewById(R.id.textView6);
        imageView = view.findViewById(R.id.profile_picture);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout);
        roleTv = view.findViewById(R.id.roleTv);
        swipeRefresh.setOnRefreshListener(this);

        if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
//            roleTv.setText(getResources().getString(R.string.guest));
//        }else if(GlobalVariables.getRole()!=null){
//            roleTv.setText(GlobalVariables.getRole());
        }

        NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollView);
        nestedScrollView.setNestedScrollingEnabled(false);

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent dsfs = new Intent(requireContext(), PostNewActivity.class);
                dsfs.putExtra("justForUser",true);
                startActivity(dsfs);

            }
        });
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> ((Home_Activity) requireActivity()).showDrawer());

        toolbar.setOnMenuItemClickListener(this);

        toolbar.getMenu().findItem(R.id.action_notifications)
                .setIcon(GlobalVariables.getNotificationsCount() > 0 ?
                        R.drawable.notification_indicator_icon :
                        R.drawable.notification_icon);

        setupNotificationReceiver();


        getUserNaImg();
        firebaseFirestore = FirebaseFirestore.getInstance();
        query = firebaseFirestore.collection("Users").
                document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("UserPosts")
                .orderBy("publishTime",
                        Query.Direction.DESCENDING).limit(10);
        postData = new ArrayList<>();

        adapter = new PostAdapter(postData, getActivity());
        post_list = view.findViewById(R.id.userpost_recycler);

        post_list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false));

        post_list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ReadPost(true);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if(item.getItemId() == R.id.action_online){

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
        }else if(item.getItemId() == R.id.action_online){
            ((Home_Activity)requireActivity()).replaceFragment(new ProfileFragment());

        }else if(item.getItemId() == R.id.action_about){

            Intent mapIntent = new Intent(getActivity(), Profile.class);
            startActivity(mapIntent);
        }else  if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(getContext(), NotificationsActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
//        if(lastDocSnap!=null){
//            updatedQuery = query.startAfter(lastDocSnap);
//        }
        updatedQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                            queryDocumentSnapshots.size()-1
                    );
                    postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));
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

        getContext().registerReceiver(notificationIndicatorReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".notificationIndicator"));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationIndicatorReceiver != null) {
            requireContext().unregisterReceiver(notificationIndicatorReceiver);
        }
    }

    private void setUpToolBarAndActions() {

        final Toolbar toolbar = getView().findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "tool", Toast.LENGTH_SHORT).show();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
    }

    private void getUserNaImg(){
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();

        fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        String user_name = task.getResult().getString("username");
                        String imgUrl = task.getResult().getString("imageUrl");

                        task.getResult().getBoolean("status");
                        toolbar.setOnMenuItemClickListener(PostsProfileFragment.this);

                        username.setText(user_name);
                        Picasso.get().load(imgUrl).fit().into(imageView);
                    }
                }else {
                    Toast.makeText(getActivity(), "Error"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void drawer(){
        final DrawerLayout drawerLayout_b = getView().findViewById(R.id.drawer_layout);
        getView().findViewById(R.id.profile_toolbara).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout_b.openDrawer(GravityCompat.START);
            }
        });
    }

}
