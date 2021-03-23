package hashed.app.ampassadors.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.UsersPickerActivity;
import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.BroadcastReceivers.NotificationIndicatorReceiver;
import hashed.app.ampassadors.BuildConfig;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class GroupsFragment extends Fragment implements View.OnClickListener {

  private static final int WORKSHOPS_PAGE = 0, MEETINGS_PAGE = 1;

  //Views
  private TabLayout groupsTabLayout;
  private ViewPager groupsViewPager;
  private FloatingActionButton floatingButton;
  private Toolbar toolbar;


  //adapter
  private TabAdapterTitle tabAdapterTitle;

  private NotificationIndicatorReceiver notificationIndicatorReceiver;

  public GroupsFragment() {
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    final Fragment[] fragments = {new WorkshopsFragment(), new MeetingsFragment()};
    final String[] titles = {"Workshops", "Meetings"};

    tabAdapterTitle = new TabAdapterTitle(getChildFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragments, titles);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_groups, container, false);
    groupsTabLayout = view.findViewById(R.id.groupsTabLayout);
    groupsViewPager = view.findViewById(R.id.groupsViewPager);
    floatingButton = view.findViewById(R.id.floatingButton);
    toolbar = view.findViewById(R.id.groupToolbar);
    toolbar.setNavigationOnClickListener(v -> ((Home_Activity) getActivity()).showDrawer());

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


    groupsViewPager.setAdapter(tabAdapterTitle);
    groupsTabLayout.setupWithViewPager(groupsViewPager);

    floatingButton.setOnClickListener(this);

    if(!FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
      if (GlobalVariables.getRole().equals("Admin") ||
              GlobalVariables.getRole().equals("Coordinator")){

        floatingButton.setVisibility(View.VISIBLE);
      }
    }

  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.floatingButton) {

      if (groupsViewPager.getCurrentItem() == MEETINGS_PAGE) {

        startActivity(new Intent(getContext(), UsersPickerActivity.class));

      } else {


      }

    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);


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

}