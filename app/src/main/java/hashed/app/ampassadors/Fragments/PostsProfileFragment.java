package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.NotificationsActivity;
import hashed.app.ampassadors.Activities.PostNewActivity;
import hashed.app.ampassadors.Activities.Profile;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.FullScreenImagesUtil;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class PostsProfileFragment extends Fragment implements Toolbar.OnMenuItemClickListener,
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
    TextView biotext;
    private FrameLayout frameLayout;
    private NotificationIndicatorReceiver notificationIndicatorReceiver;
    private TextView roleTv;
    private ListenerRegistration listenerRegistration;
    CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

    private TextView notificationCountTv;

    public PostsProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts_profile, container,
                false);
        frameLayout = view.findViewById(R.id.frameLayout);
        floatingButton = view.findViewById(R.id.floatingbtn);
        username = view.findViewById(R.id.textView6);
        imageView = view.findViewById(R.id.profile_picture);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefresh.setOnRefreshListener(this);
        biotext = view.findViewById(R.id.bio_profile);

        getUserNaImg();

        collectionReference.document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if(snapshot.contains("imageUrl")){

                    String image = snapshot.getString("imageUrl");

                    if(image!=null){

                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

//                            floatingButton.setVisibility(View.GONE);
//                            toolbar.setVisibility(View.GONE);
//                            frameLayout.setVisibility(View.VISIBLE);
                                FullScreenImagesUtil.showImageFullScreen(requireContext(),
                                        image,null,null);
//                            getFragmentManager().beginTransaction().replace(frameLayout.getId(),
//                                    new ImageFullScreenFragment(image), "FullScreen")
//                                    .commit();

                            }
                        });
                    }
                }
            }
        });


//        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
////            roleTv.setText(getResources().getString(R.string.guest));
////        }else if(GlobalVariables.getRole()!=null){
////            roleTv.setText(GlobalVariables.getRole());
//        }

//        NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollView);
//        nestedScrollView.setNestedScrollingEnabled(false);

        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent dsfs = new Intent(requireContext(), PostNewActivity.class);
                dsfs.putExtra("justForUser", true);
                startActivity(dsfs);

            }
        });
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> ((Home_Activity) requireActivity()).showDrawer());

        toolbar.setOnMenuItemClickListener(this);
        final View notificationActionView = toolbar.getMenu()
                .findItem(R.id.action_notifications).getActionView();
        notificationCountTv = notificationActionView.findViewById(R.id.notificationCountTv);

        notificationActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), NotificationsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });





        firebaseFirestore = FirebaseFirestore.getInstance();
        query = firebaseFirestore.collection("Users").
                document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("UserPosts")
                .orderBy("publishTime",
                        Query.Direction.DESCENDING).limit(10);
        postData = new ArrayList<>();

        adapter = new PostAdapter(postData, getActivity(),true);
        post_list = view.findViewById(R.id.userpost_recycler);

        post_list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false));

        post_list.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(GlobalVariables.getNotificationsCount() > 0){
            if(notificationCountTv.getVisibility() == View.GONE){
                notificationCountTv.setVisibility(View.VISIBLE);
            }
            notificationCountTv.setText(GlobalVariables.getNotificationsCount() > 99?"99+":
                    String.valueOf(GlobalVariables.getNotificationsCount()));

        }else if(notificationCountTv.getVisibility() == View.VISIBLE){
            notificationCountTv.setVisibility(View.GONE);
        }

        setupNotificationReceiver();

        ReadPost(true);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_online) {

            DocumentReference reference = fStore.collection("Users").document(userid);

            updateOfflineStatusPreference(status);

            if (status) {

                reference.update("status", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        status = false;
                        toolbar.getMenu().findItem(R.id.action_online).setTitle("online");
                    }
                });
            } else {
                reference.update("status", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        status = true;
                        toolbar.getMenu().findItem(R.id.action_online).setTitle("away");
                    }
                });
            }
        } else if (item.getItemId() == R.id.action_online) {
            ((Home_Activity) requireActivity()).replaceFragment(new ProfileFragment());

        } else if (item.getItemId() == R.id.action_about) {

            Intent mapIntent = new Intent(getActivity(), Profile.class);
            startActivity(mapIntent);
        }
//        else if (item.getItemId() == R.id.action_notifications) {
//            startActivity(new Intent(getContext(), NotificationsActivity.class)
//                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }

        return false;
    }

    private void updateOfflineStatusPreference(boolean alwaysOffline){
        requireContext().getSharedPreferences(getResources().getString(R.string.app_name),
                Context.MODE_PRIVATE).edit().putBoolean("alwaysOffline",alwaysOffline).apply();
    }

    @Override
    public void onRefresh() {

        final int previousSize = postData.size();
        postData.clear();
        adapter.notifyItemRangeRemoved(0,previousSize);
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

                Log.d("ttt", "is at bottom man");

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
                        if(GlobalVariables.getNotificationsCount() > 0){
                            if(notificationCountTv.getVisibility() == View.GONE){
                                notificationCountTv.setVisibility(View.VISIBLE);
                            }
                            notificationCountTv.setText(GlobalVariables.getNotificationsCount() > 99?
                                    "99+":String.valueOf(GlobalVariables.getNotificationsCount()));

                        }else if(notificationCountTv.getVisibility() == View.VISIBLE){
                            notificationCountTv.setVisibility(View.GONE);
                        }
                    }
                };

        requireContext().registerReceiver(notificationIndicatorReceiver,
                new IntentFilter(BuildConfig.APPLICATION_ID + ".notificationIndicator"));

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationIndicatorReceiver != null) {
            requireContext().unregisterReceiver(notificationIndicatorReceiver);
        }
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    private void getUserNaImg() {
        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();
        fStore = FirebaseFirestore.getInstance();
        listenerRegistration = fStore.collection("Users").document(userid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        Log.d("ttt", "value change");
                        String bio = value.getString("Bio");
                        if (bio.isEmpty()) {
                            biotext.setVisibility(View.GONE);
                        }else {
                            biotext.setText(bio);

                        }
                        username.setText(value.getString("username"));
                        Picasso.get().load(value.getString("imageUrl")).fit().centerCrop().into(imageView);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 3) {
            if(data.hasExtra("postData")){
                final PostData newPost =
                        (PostData) data.getSerializableExtra("postData");
                if(newPost!=null){
                    postData.add(0, newPost);
                    adapter.notifyItemInserted(0);
                    post_list.scrollToPosition(0);
                }
            }
        }
    }

}