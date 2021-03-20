package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.NotificationsActivity;
import hashed.app.ampassadors.Activities.UserMessageSearchActivity;
import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class ChattingFragment extends Fragment implements MenuItem.OnMenuItemClickListener,
        View.OnClickListener {

  private TabLayout chattingTabLayout;
  private ViewPager chattingViewPager;
  private TabAdapterTitle tabAdapterTitle;
  private Toolbar toolbar;
  private NotificationIndicatorReceiver notificationIndicatorReceiver;
//  private SearchView chattingSearchView;
  private TextView searchTv;
  public ChattingFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Fragment[] fragments = {new OnlineUsersFragment(), new MessagesFragment()};
    final String[] titles = {"Online", "Messages"};

    tabAdapterTitle = new TabAdapterTitle(getChildFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragments, titles);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_chatting, container, false);
    chattingTabLayout = view.findViewById(R.id.chattingTabLayout);
    chattingViewPager = view.findViewById(R.id.chattingViewPager);
    toolbar = view.findViewById(R.id.chattingToolbar);
    searchTv = view.findViewById(R.id.searchTv);
//    chattingSearchView = view.findViewById(R.id.chattingSearchView);
    toolbar.setNavigationOnClickListener(v -> ((Home_Activity) requireActivity()).showDrawer());
    toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    toolbar.getMenu().findItem(R.id.action_notifications)
            .setIcon(GlobalVariables.getNotificationsCount() > 0 ?
                    R.drawable.notification_indicator_icon :
                    R.drawable.notification_icon);


    setupNotificationReceiver();


    chattingViewPager.setAdapter(tabAdapterTitle);
    chattingTabLayout.setupWithViewPager(chattingViewPager);

    searchTv.setOnClickListener(this);
//    chattingSearchView.setOnClickListener(this);
//    chattingSearchView.setOnSearchClickListener(this);

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


  @Override
  public boolean onMenuItemClick(MenuItem item) {

    if (item.getItemId() == R.id.action_notifications) {
      startActivity(new Intent(getContext(), NotificationsActivity.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    return false;
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.searchTv) {
      startActivity(new Intent(requireContext(), UserMessageSearchActivity.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
  }


}