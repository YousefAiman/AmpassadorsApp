package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import hashed.app.ampassadors.A_Fragment;
import hashed.app.ampassadors.Adapters.PostAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

import static hashed.app.ampassadors.Objects.PostData.TYPE_POLL;

public class ShowPollsActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener  {



    private static final int POSTS_LIMIT = 10;
    private Query query;
    private List<PostData> postData;
    private PostAdapter adapter;
    private RecyclerView post_list;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingMessages;
    private ShowPollsActivity.PostsBottomScrollListener scrollListener;


    //header Pager
    private Handler handler;
    private Runnable pagerRunnable;
    private ArrayList<String> titles;

    public ShowPollsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_polls);




        titles = new ArrayList<>(5);
        query = FirebaseFirestore.getInstance().collection("Posts")
                .orderBy("publishTime", Query.Direction.DESCENDING).whereEqualTo("type",
                        TYPE_POLL).limit(POSTS_LIMIT);
        postData = new ArrayList<>();
        adapter = new PostAdapter(postData, ShowPollsActivity.this);

        post_list = findViewById(R.id.home_listt);
        post_list.setLayoutManager(new LinearLayoutManager(ShowPollsActivity.this
                , RecyclerView.VERTICAL, false));

        post_list.setAdapter(adapter);
        ReadPost(true);

        Toolbar toolbar = findViewById(R.id.toolbaraa);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(this);
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    @Override
    public void onRefresh() {

        //header pager
        titles.clear();
        handler.removeCallbacks(pagerRunnable);

        //post recycler
        postData.clear();
        adapter.notifyDataSetChanged();
        lastDocSnap = null;
        ReadPost(true);

    }

    @Override
    public void onClick(View view) {
    }

//    @Override
//    public void showComments(String postId, int commentsCount) {
//
//        CommentsFragment commentsFragment = new CommentsFragment(postId,commentsCount);
//        commentsFragment.show(getChildFragmentManager(),"CommentsFragment");
//
//    }
//
//    @Override
//    public void showImage(String imageUrl) {
//        new ImageFullScreenFragment(imageUrl).show(getChildFragmentManager(),"FullScreen");
//    }

    private void ReadPost(boolean isInitial) {

        final AtomicInteger addedCount = new AtomicInteger();

        isLoadingMessages = false;

        Query updatedQuery = query;

        if (lastDocSnap != null) {
            updatedQuery = query.startAfter(lastDocSnap);
        }
        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size() - 1
                );

                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {

                    if (snapshot.getLong("type") == TYPE_POLL) {
                        //  Log.d("ggggg", snapshot.getLong("type") + "هههههههههههههههه");


                        if (snapshot.getBoolean("pollEnded")) {

                            if (snapshot.getLong("totalVotes") > 0) {
                                addedCount.getAndIncrement();
                                postData.add(snapshot.toObject(PostData.class));
                            }

                        } else {

                            if (System.currentTimeMillis() >
                                    snapshot.getLong("publishTime") +
                                            snapshot.getLong("pollDuration")) {

                                snapshot.getReference().update("pollEnded", true);

                                if (snapshot.getLong("totalVotes") > 0) {

                                    final PostData post = snapshot.toObject(PostData.class);
                                    post.setPollEnded(true);
                                    postData.add(post);
                                    addedCount.getAndIncrement();
                                }

                            } else {

                                postData.add(snapshot.toObject(PostData.class));
                                addedCount.getAndIncrement();
                            }

                        }

                    } else {
                        addedCount.getAndIncrement();
                        postData.add(snapshot.toObject(PostData.class));
                    }
                }
                //   postData.addAll(queryDocumentSnapshots.toObjects(PostData.class));

            }

        }).addOnCompleteListener(task -> {


            if (isInitial) {
                adapter.notifyDataSetChanged();

                if (postData.size() == POSTS_LIMIT) {
                    post_list.addOnScrollListener(scrollListener = new PostsBottomScrollListener());
                }

            } else {

                adapter.notifyItemRangeInserted((postData.size() - addedCount.get()),
                        addedCount.get());

                if (addedCount.get() < POSTS_LIMIT && scrollListener != null) {
                    post_list.removeOnScrollListener(scrollListener);
                }
            }


        });
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

}