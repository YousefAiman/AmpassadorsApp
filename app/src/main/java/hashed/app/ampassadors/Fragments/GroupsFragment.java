package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import hashed.app.ampassadors.Activities.CreateMeetingActivity;
import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.UsersPickerActivity;
import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.R;

public class GroupsFragment extends Fragment implements View.OnClickListener{

  private static final int WORKSHOPS_PAGE = 0,MEETINGS_PAGE = 1;

  //Views
  private TabLayout groupsTabLayout;
  private ViewPager groupsViewPager;
  private FloatingActionButton floatingButton;


  //adapter
  private TabAdapterTitle tabAdapterTitle;


  public GroupsFragment() {
  }



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Fragment[] fragments = {new WorkshopsFragment(),new MeetingsFragment()};
    final String[] titles = {"Workshops","Meetings"};

    tabAdapterTitle = new TabAdapterTitle(getChildFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,fragments,titles);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_groups, container, false);
    groupsTabLayout = view.findViewById(R.id.groupsTabLayout);
    groupsViewPager = view.findViewById(R.id.groupsViewPager);
    floatingButton = view.findViewById(R.id.floatingButton);

    final Toolbar groupToolbar = view.findViewById(R.id.groupToolbar);
    groupToolbar.setNavigationOnClickListener(v-> ((Home_Activity)getActivity()).showDrawer());

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


    groupsViewPager.setAdapter(tabAdapterTitle);
    groupsTabLayout.setupWithViewPager(groupsViewPager);

    floatingButton.setOnClickListener(this);

  }

  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.floatingButton){

      if(groupsViewPager.getCurrentItem() == MEETINGS_PAGE){

        startActivity(new Intent(getContext(),UsersPickerActivity.class));

      }else{


      }

    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);


  }
}