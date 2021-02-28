package hashed.app.ampassadors.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import hashed.app.ampassadors.Activities.CreateMeetingActivity;
import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.R;

public class GroupsFragment extends Fragment implements View.OnClickListener{

  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  private static final int MEETINGS_PAGE = 0,WORKSHOPS_PAGE = 1;


  private String mParam1;
  private String mParam2;


  //Views
  private TabLayout groupsTabLayout;
  private ViewPager groupsViewPager;
  private FloatingActionButton floatingButton;


  //adapter
  private TabAdapterTitle tabAdapterTitle;


  public GroupsFragment() {
  }

  public static GroupsFragment newInstance(String param1, String param2) {
    GroupsFragment fragment = new GroupsFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }

    final Fragment[] fragments = {new OnlineUsersFragment()
//            ,new MeetingsFragment()
    };
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

        startActivityForResult(new Intent(getContext(),
                CreateMeetingActivity.class),3);

      }else{


      }

    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);


  }
}