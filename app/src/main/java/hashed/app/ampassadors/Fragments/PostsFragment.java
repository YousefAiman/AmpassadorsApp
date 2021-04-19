  package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import hashed.app.ampassadors.Activities.CreatePollActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.NotificationsActivity;
import hashed.app.ampassadors.Activities.PostNewActivity;
import hashed.app.ampassadors.Activities.PostsSearchActivity;
import hashed.app.ampassadors.Adapters.NewsAdapter;
import hashed.app.ampassadors.Objects.HeaderItem;
import hashed.app.ampassadors.Adapters.HomeHeaderViewPagerAdapter;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.Objects.PostNewsPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class PostsFragment extends Fragment implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final int POSTS_LIMIT = 10;
    private Query query;
    private List<PostNewsPreview> posts;
    private NewsAdapter adapter;
    private RecyclerView post_list;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingMessages;
    private SwipeRefreshLayout swipeRefresh;
    private PostsBottomScrollListener scrollListener;
    private ViewPager headerViewPager;
    private LinearLayout dotsLinear;
    private Toolbar toolbar;
    private FloatingActionButton floatingButton;
    //header Pager
    private Handler handler;
    private Runnable pagerRunnable;
    private HomeHeaderViewPagerAdapter pagerAdapter;
    private ArrayList<HeaderItem> headerItems;
    private NotificationIndicatorReceiver notificationIndicatorReceiver;
    private boolean[] headerQueriesFinished;
    private FirebaseFirestore firestore;
    private ViewPager.OnPageChangeListener pageChangeListener;
    private List<ListenerRegistration> listenerRegistrations;

    public PostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listenerRegistrations = new ArrayList<>();

        headerItems = new ArrayList<>();
        pagerAdapter = new HomeHeaderViewPagerAdapter(headerItems);
        headerQueriesFinished = new boolean[4];

        posts = new ArrayList<>();
        adapter = new NewsAdapter(posts, getContext());

        firestore = FirebaseFirestore.getInstance();
        query = firestore.collection("Posts")
                .orderBy("publishTime", Query.Direction.DESCENDING)
                .whereEqualTo("type", PostData.TYPE_NEWS)
                .limit(POSTS_LIMIT);


        fetchHeaderItems();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_posts, container, false);
        post_list = view.findViewById(R.id.home_list);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        headerViewPager = view.findViewById(R.id.headerViewPager);
        dotsLinear = view.findViewById(R.id.dotsLinear);
        swipeRefresh.setOnRefreshListener(this);

        toolbar = view.findViewById(R.id.home_activity_toolbar);
        toolbar.setNavigationOnClickListener(v -> ((Home_Activity) requireActivity()).showDrawer());
        toolbar.setOnMenuItemClickListener(this);

        floatingButton = view.findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar.getMenu().findItem(R.id.action_notifications)
                .setIcon(GlobalVariables.getNotificationsCount() > 0 ?
                        R.drawable.notification_indicator_icon :
                        R.drawable.notification_icon);
//    Log.d("tttt", GlobalVariables.getRole());
        if (!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
            if (GlobalVariables.getRole().equals("Admin") ||
                    GlobalVariables.getRole().equals("Publisher")) {
                floatingButton.setVisibility(View.VISIBLE);
            }
        }

        setupNotificationReceiver();

        headerViewPager.setAdapter(pagerAdapter);
        createHeaderPager();
        post_list.setAdapter(adapter);
        ReadPost(true);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_notifications) {
            startActivity(new Intent(getContext(), NotificationsActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (item.getItemId() == R.id.action_search) {
            startActivity(new Intent(getContext(), PostsSearchActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        return false;
    }

    @Override
    public void onRefresh() {

        //header pager
        headerItems.clear();
//        headerViewPager.removeAllViews();
        pagerAdapter.notifyDataSetChanged();
        fetchHeaderItems();

        if(pageChangeListener!=null){

            headerViewPager.removeOnPageChangeListener(pageChangeListener);
        }

        if (handler != null && pagerRunnable != null) {
            handler.removeCallbacks(pagerRunnable);
        }
        dotsLinear.removeAllViews();

        //post recycler
        posts.clear();
        adapter.notifyDataSetChanged();
        adapter.loadingItems.clear();
        lastDocSnap = null;
        ReadPost(true);

    }

//  @Override
//  public void showComments(String postId, int commentsCount) {
//
//    CommentsFragment commentsFragment = new CommentsFragment(postId,commentsCount);
//    commentsFragment.show(getChildFragmentManager(),"CommentsFragment");
//
//  }
//
//  @Override
//  public void showImage(String imageUrl) {
//    new ImageFullScreenFragment(imageUrl).show(getChildFragmentManager(),"FullScreen");
//  }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.floatingButton) {

            showPostOptionsBottomSheet();
        }
    }

    private void ReadPost(boolean isInitial) {

        isLoadingMessages = true;

//    final AtomicInteger addedCount = new AtomicInteger();

        swipeRefresh.setRefreshing(true);

        Query updatedQuery = query;

        if (lastDocSnap != null) {

            updatedQuery = query.startAfter(lastDocSnap);

        }
        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size() - 1
                );

                if (isInitial) {
                    posts.addAll(queryDocumentSnapshots.toObjects(PostNewsPreview.class));
                } else {
                    posts.addAll(posts.size(), queryDocumentSnapshots.toObjects(PostNewsPreview.class));
                }
            }
        }).addOnCompleteListener(task -> {
            if (isInitial) {

                addNewsDeleteListener();

                adapter.notifyDataSetChanged();

                if (task.getResult().size() == POSTS_LIMIT && scrollListener == null) {
                    post_list.addOnScrollListener(scrollListener = new PostsBottomScrollListener());
                }

            } else {

                final int resultSize = task.getResult().size();

                adapter.notifyItemRangeInserted(posts.size() - resultSize, resultSize);
                if (resultSize < POSTS_LIMIT && scrollListener != null) {
                    post_list.removeOnScrollListener(scrollListener);
                }
            }

            swipeRefresh.setRefreshing(false);

            isLoadingMessages = false;
        });
    }

    private void showPostOptionsBottomSheet() {

        final BottomSheetDialog bsd = new BottomSheetDialog(getContext(), R.style.SheetDialog);
        final View parentView = getLayoutInflater().inflate(R.layout.post_options_bsd, null);
        parentView.setBackgroundColor(Color.TRANSPARENT);

        parentView.findViewById(R.id.new_post).setOnClickListener(view -> {

            bsd.dismiss();
            Intent intent = new Intent(getContext(), PostNewActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(intent,3);

        });
        parentView.findViewById(R.id.new_poll).setOnClickListener(view -> {
            bsd.dismiss();
            Intent intent = new Intent(getContext(), CreatePollActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        bsd.setContentView(parentView);
        bsd.show();
    }

//    private void createHeaderPager() {
//
//        FirebaseFirestore.getInstance().collection("Posts")
//                .whereEqualTo("important", true)
//                .orderBy("publishTime", Query.Direction.ASCENDING)
//                .limit(10).get().addOnSuccessListener(snapshots -> {
//            if (!snapshots.isEmpty()) {
//                importantPost.addAll(snapshots.toObjects(PostData.class));
//            }
//        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful() && importantPost.size() > 0) {
//
//                    if (headerViewPager.getVisibility() == View.GONE) {
//                        headerViewPager.setVisibility(View.VISIBLE);
//                        dotsLinear.setVisibility(View.VISIBLE);
//                    }
//                    pagerAdapter.notifyDataSetChanged();
//
//                    if (importantPost.size() > 1) {
//                        final ImageView[] dots = new ImageView[importantPost.size()];
//
//                        final LinearLayout.LayoutParams params =
//                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                                        LinearLayout.LayoutParams.WRAP_CONTENT);
//
//                        float density;
//                        if (getContext() != null) {
//                            density = requireContext().getResources().getDisplayMetrics().density;
//                        } else {
//                            density = 1;
//                        }
//
//                        for (int i = 0; i < importantPost.size(); i++) {
//                            dots[i] = new ImageView(requireContext());
//                            dots[i].setImageResource(R.drawable.indicator_inactive_icon);
//                            params.setMargins((int) (5 * density), 0, (int) (5 * density), 0);
//                            dotsLinear.addView(dots[i], params);
//                        }
//
//                        dots[0].setImageResource(R.drawable.indicator_active_icon);
//
//                        headerViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                            int previousPage = 0;
//
//                            @Override
//                            public void onPageScrolled(int position, float positionOffset,
//                                                       int positionOffsetPixels) {
//                            }
//
//                            @Override
//                            public void onPageSelected(int position) {
//
//                                dots[previousPage].setImageResource(R.drawable.indicator_inactive_icon);
//                                dots[position].setImageResource(R.drawable.indicator_active_icon);
//
//                                previousPage = position;
//                            }
//
//                            @Override
//                            public void onPageScrollStateChanged(int state) {
//                            }
//                        });
//
//                        handler = new Handler();
//
//                        pagerRunnable = new Runnable() {
//                            int scrollPosition;
//
//                            @Override
//                            public void run() {
//
//                                if (scrollPosition + 1 == importantPost.size()) {
//                                    scrollPosition = 0;
//                                } else {
//                                    scrollPosition++;
//                                }
//
//                                headerViewPager.setCurrentItem(scrollPosition, true);
//
//                                handler.postDelayed(this, 5000);
//
//                            }
//                        };
//
//                        handler.postDelayed(pagerRunnable, 5000);
//
//                    }
//                } else {
//                    headerViewPager.setVisibility(View.GONE);
//                    dotsLinear.setVisibility(View.GONE);
//                }
//            }
//        });
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 3) {
                if(data.hasExtra("postNewsPreview")){
                    final PostNewsPreview postNewsPreview =
                            (PostNewsPreview) data.getSerializableExtra("postNewsPreview");
                    if(postNewsPreview!=null){
                        posts.add(0, postNewsPreview);
                        adapter.notifyItemInserted(0);
                        post_list.scrollToPosition(0);
                    }
                }
        }
    }

    private void fetchHeaderItems(){


        firestore.collection("Posts")
                .whereEqualTo("important",true)
                .whereEqualTo("type",PostData.TYPE_NEWS)
                .limit(5)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snapshots) {
                for(DocumentSnapshot document:snapshots.getDocuments()){
                    headerItems.add(new HeaderItem(document.getString("title"),
                            "news",document.getId(),document.toObject(PostData.class)));
                }

                Query pollsQuery = firestore.collection("Posts")
                        .whereEqualTo("important",true)
                        .whereEqualTo("type",PostData.TYPE_POLL)
                        .whereEqualTo("pollEnded",false)
                        .limit(5);

                pollsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        for(DocumentSnapshot snapshot:snapshots.getDocuments()){

                            if (snapshot.getBoolean("pollEnded")) {

                                if (snapshot.getLong("totalVotes") > 0) {
                                    headerItems.add(new HeaderItem(snapshot.getString("title"),
                                            "poll",snapshot.getId(),snapshot.toObject(PostData.class)));
                                }

                            } else {

                                if (System.currentTimeMillis() >
                                        snapshot.getLong("publishTime") +
                                                snapshot.getLong("pollDuration")) {

                                    snapshot.getReference().update("pollEnded", true);

                                    if (snapshot.getLong("totalVotes") > 0) {

                                        final PostData post = snapshot.toObject(PostData.class);
                                        post.setPollEnded(true);

                                        headerItems.add(new HeaderItem(snapshot.getString("title"),
                                                "poll",snapshot.getId(),snapshot.toObject(PostData.class)));

                                    }

                                } else {
                                    headerItems.add(new HeaderItem(snapshot.getString("title"),
                                            "poll",snapshot.getId(),snapshot.toObject(PostData.class)));
                                }

                            }

                        }

                        firestore.collection("Meetings")
                                .whereArrayContains("members",
                                        FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .whereEqualTo("important",true)
                                .whereGreaterThan("startTime",
                                        System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS)
                                .whereEqualTo("hasEnded",false)
                                .limit(5)
                                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {

                                for(DocumentSnapshot document:snapshots.getDocuments()){
                                    headerItems.add(new HeaderItem(document.getString("title"),
                                            "meeting",document.getId(),document.toObject(Meeting.class)));
                                }


                                firestore.collection("Courses")
                                        .whereEqualTo("important",true)
                                        .whereGreaterThan("startTime",
                                                System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS)
                                        .whereEqualTo("hasEnded",false)
                                        .limit(5)
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot snapshots) {
                                        for(DocumentSnapshot document:snapshots.getDocuments()){
                                            headerItems.add(new HeaderItem(document.getString("title"),
                                                    "course",document.getId(),document.toObject(Course.class)));
                                        }

                                        createHeaderPager();
                                    }
                                });
                            }
                        });
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        listenerRegistrations.add(
                        pollsQuery.whereEqualTo("deleting",true)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot value,
                                                        @Nullable FirebaseFirestoreException error) {
                                        if(value!=null){
                                            for(DocumentChange dc:value.getDocumentChanges()){
                                                Log.d("ttt","change at: "+dc.getDocument().getId());
                                                if(dc.getType().equals(DocumentChange.Type.REMOVED)){
                                                    removeItemFromHeader(dc.getDocument().getId());
                                                }
                                            }
                                        }
                                    }
                                }));

                    }
                });
            }
        });

    }

    private void createHeaderPager(){

//        for(boolean hasFinished:headerQueriesFinished){
//            if(!hasFinished)
//                return;
//        }

        if (headerItems.size() > 0) {

            if (headerViewPager.getVisibility() == View.GONE) {
                headerViewPager.setVisibility(View.VISIBLE);
                dotsLinear.setVisibility(View.VISIBLE);
            }
            pagerAdapter.notifyDataSetChanged();

            if (headerItems.size() > 1) {

                headerViewPager.setCurrentItem(0);

                final ImageView[] dots = new ImageView[headerItems.size()];

                final LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);

                float density;
                if (getContext() != null) {
                    density = requireContext().getResources().getDisplayMetrics().density;
                } else {
                    density = 1;
                }

                if(getActivity()!=null){

                    for (int i = 0; i < headerItems.size(); i++) {
                        dots[i] = new ImageView(getActivity());
                        dots[i].setImageResource(R.drawable.indicator_inactive_icon);
                        params.setMargins((int) (5 * density), 0, (int) (5 * density), 0);
                        dotsLinear.addView(dots[i], params);
                    }

                    dots[0].setImageResource(R.drawable.indicator_active_icon);


                headerViewPager.addOnPageChangeListener(pageChangeListener =
                        new ViewPager.OnPageChangeListener() {
                    int previousPage = 0;

                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {

                        dots[previousPage].setImageResource(R.drawable.indicator_inactive_icon);
                        dots[position].setImageResource(R.drawable.indicator_active_icon);

                        previousPage = position;
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });

                handler = new Handler();

                pagerRunnable = new Runnable() {
                    int scrollPosition;

                    @Override
                    public void run() {

                        if (scrollPosition + 1 == headerItems.size()) {
                            scrollPosition = 0;
                        } else {
                            scrollPosition++;
                        }

                        headerViewPager.setCurrentItem(scrollPosition, true);

                        handler.postDelayed(this, 4000);

                    }
                };

                handler.postDelayed(pagerRunnable, 4000);
                }

            }
        } else {
            headerViewPager.setVisibility(View.GONE);
            dotsLinear.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (handler != null && pagerRunnable != null) {
            handler.removeCallbacks(pagerRunnable);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (handler != null && pagerRunnable != null) {
            handler.postDelayed(pagerRunnable, 3000);
        }

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

        if (post_list != null && scrollListener != null) {
            post_list.removeOnScrollListener(scrollListener);
        }
        if(listenerRegistrations!=null){
            for(ListenerRegistration listenerRegistration:listenerRegistrations){
                listenerRegistration.remove();
            }
        }
    }



    private class PostsBottomScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingMessages &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt", "is at bottom");

                ReadPost(false);

            }
        }
    }

    private void addNewsDeleteListener(){
        listenerRegistrations.add(
        firestore.collection("Posts")
                .whereEqualTo("type",PostData.TYPE_NEWS)
                .whereEqualTo("deleting",true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {

                        if (value!=null && posts!=null){
                            for(DocumentChange dc:value.getDocumentChanges()){

                                if(dc.getType().equals(DocumentChange.Type.ADDED)) {
                                    final String documentId = dc.getDocument().getId();

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (documentId != null) {
                                                for (PostNewsPreview post : posts) {
                                                    if (post.getPostId().equals(documentId)) {
                                                        int index = posts.indexOf(post);
                                                        posts.remove(index);
                                                        post_list.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                adapter.notifyItemRemoved(index);
                                                            }
                                                        });

                                                        break;
                                                    }
                                                }

                                            }
                                        }
                                    }).start();

                                    removeItemFromHeader(documentId);

                                }
                            }
                        }

                    }
                }));
    }


    private void removeItemFromHeader(String documentId){

        new Thread(new Runnable() {
            @Override
            public void run() {

        for (HeaderItem headerItem :headerItems) {

            Log.d("ttt",
                    "headerItem.getId(): "+ headerItem.getId()+ "documentId "+documentId);

            if (headerItem.getId().equals(documentId)) {
                int index = headerItems.indexOf(headerItem);

                headerItems.remove(index);

                Log.d("ttt","removing item at "+index);

                headerViewPager.post(new Runnable() {
                    @Override
                    public void run() {

//                        pagerAdapter.destroyItem(
//                                headerViewPager,
//                                index,
//                                headerItem);

                        dotsLinear.removeViewAt(index);
                        if(dotsLinear.getChildCount() == 1){
                            dotsLinear.setVisibility(View.GONE);
                        }
                        pagerAdapter.notifyDataSetChanged();

                        if(headerItems.size() == 0){
                            dotsLinear.setVisibility(View.GONE);
                            headerViewPager.setVisibility(View.GONE);
                        }

                    }
                });
                break;
            }
        }
            }
        }).start();

    }


}