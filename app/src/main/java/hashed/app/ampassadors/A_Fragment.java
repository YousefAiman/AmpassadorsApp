package hashed.app.ampassadors;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.Activities.CreatePollActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.PostActivity;
import hashed.app.ampassadors.Adapters.HomeNewsHeaderViewPagerAdapter;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Fragments.CommentsFragment;
import hashed.app.ampassadors.Fragments.ImageFullScreenFragment;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class A_Fragment extends Fragment implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener , View.OnClickListener ,
        PostAdapter.CommentsInterface,PostAdapter.ImageInterface{

    private static final int POSTS_LIMIT = 10;
    private Query query;
    private List<PostData> postData;
    private PostAdapter adapter;
    private RecyclerView post_list;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingMessages;
    private SwipeRefreshLayout swipeRefresh;
    private PostsBottomScrollListener scrollListener;
    private ViewPager headerViewPager;
    private LinearLayout dotsLinear;


    //header Pager
    private Handler handler;
    private Runnable pagerRunnable;
    private HomeNewsHeaderViewPagerAdapter pagerAdapter;
    private ArrayList<String> titles;

    public A_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        titles = new ArrayList<>(5);
        pagerAdapter = new HomeNewsHeaderViewPagerAdapter(titles);



        query = FirebaseFirestore.getInstance().collection("Posts")
                .orderBy("publishTime", Query.Direction.DESCENDING).limit(POSTS_LIMIT);
        postData = new ArrayList<>();
        adapter = new PostAdapter(postData, getContext(),this,this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_posts, container, false);
        post_list = view.findViewById(R.id.home_list);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        headerViewPager = view.findViewById(R.id.headerViewPager);
        dotsLinear = view.findViewById(R.id.dotsLinear);
        swipeRefresh.setOnRefreshListener(this);

        Toolbar toolbar = view.findViewById(R.id.home_activity_toolbar);
        toolbar.setNavigationOnClickListener(v -> ((Home_Activity)requireActivity()).showDrawer());
        toolbar.setOnMenuItemClickListener(this);

        FloatingActionButton floatingButton = view.findViewById(R.id.floatingButton);
        floatingButton.setOnClickListener(this);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        headerViewPager.setAdapter(pagerAdapter);


        post_list.setAdapter(adapter);
        ReadPost(true);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onRefresh() {

        //header pager
        titles.clear();
        pagerAdapter.notifyDataSetChanged();
        handler.removeCallbacks(pagerRunnable);
        dotsLinear.removeAllViews();

        //post recycler
        postData.clear();
        adapter.notifyDataSetChanged();
        lastDocSnap = null;
        ReadPost(true);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.floatingButton){

            showPostOptionsBottomSheet();
        }
    }

    @Override
    public void showComments(String postId, int commentsCount) {

        CommentsFragment commentsFragment = new CommentsFragment(postId,commentsCount);
        commentsFragment.show(getChildFragmentManager(),"CommentsFragment");

    }

    @Override
    public void showImage(String imageUrl) {
        new ImageFullScreenFragment(imageUrl).show(getChildFragmentManager(),"FullScreen");
    }

    private class PostsBottomScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingMessages &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                Log.d("ttt","is at bottom");

                ReadPost(false);

            }
        }
    }


    private void ReadPost(boolean isInitial) {

        final AtomicInteger addedCount = new AtomicInteger();

        swipeRefresh.setRefreshing(true);
        isLoadingMessages = false;

        Query updatedQuery = query;

        if(lastDocSnap!=null){
            updatedQuery = query.startAfter(lastDocSnap);
        }
        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size()-1
                );

                for(QueryDocumentSnapshot snapshot:queryDocumentSnapshots){

                    if(snapshot.getLong("type") == PostData.TYPE_POLL){

                        if(snapshot.getBoolean("pollEnded")){

                            if(snapshot.getLong("totalVotes") > 0){
                                addedCount.getAndIncrement();
                                postData.add(snapshot.toObject(PostData.class));
                            }

                        }else{

                            if(System.currentTimeMillis() >
                                    snapshot.getLong("publishTime") +
                                            snapshot.getLong("pollDuration")){

                                snapshot.getReference().update("pollEnded",true);

                                if(snapshot.getLong("totalVotes") > 0){

                                    final PostData post = snapshot.toObject(PostData.class);
                                    post.setPollEnded(true);
                                    postData.add(post);
                                    addedCount.getAndIncrement();
                                }

                            }else{

                                postData.add(snapshot.toObject(PostData.class));
                                addedCount.getAndIncrement();
                            }

                        }

                    }else{
                        addedCount.getAndIncrement();
                        postData.add(snapshot.toObject(PostData.class));
                    }


                }
//          postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));

            }

        }).addOnCompleteListener(task -> {


            if(isInitial){
                adapter.notifyDataSetChanged();

                if(postData.size() == POSTS_LIMIT){
                    post_list.addOnScrollListener(scrollListener = new PostsBottomScrollListener());
                }

            }else{

                adapter.notifyItemRangeInserted((postData.size() - addedCount.get()),
                        addedCount.get());

                if(addedCount.get() < POSTS_LIMIT && scrollListener != null){
                    post_list.removeOnScrollListener(scrollListener);
                }
            }


            swipeRefresh.setRefreshing(false);

        });
    }


    private void showPostOptionsBottomSheet(){

        final BottomSheetDialog bsd = new BottomSheetDialog(getContext(), R.style.SheetDialog);
        final View parentView = getLayoutInflater().inflate(R.layout.post_options_bsd, null);
        parentView.setBackgroundColor(Color.TRANSPARENT);

        parentView.findViewById(R.id.new_post).setOnClickListener(view -> {

            bsd.dismiss();
            Intent intent = new Intent(getContext(), PostActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

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


    @Override
    public void onPause() {
        super.onPause();

        if(handler!=null && pagerRunnable!=null){
            handler.removeCallbacks(pagerRunnable);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if(handler!=null && pagerRunnable!=null){
            handler.postDelayed(pagerRunnable,3000);
        }

    }

    static long remainingTime() {

        Calendar calendar = new GregorianCalendar(Locale.getDefault());
        calendar.setTime(new Date());

        int totalMin = 1440 - 60 * calendar.get(Calendar.HOUR) - calendar.get(Calendar.MINUTE);

        long timeLeftInMillis = totalMin * 60 * 1000;

        return System.currentTimeMillis() + timeLeftInMillis;
    }


}