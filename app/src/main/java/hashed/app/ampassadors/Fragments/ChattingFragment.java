package hashed.app.ampassadors.Fragments;

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

import com.google.android.material.tabs.TabLayout;

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.R;

public class ChattingFragment extends Fragment {

  private TabLayout chattingTabLayout;
  private ViewPager chattingViewPager;
  private TabAdapterTitle tabAdapterTitle;

  public ChattingFragment() {
    // Required empty public constructor
  }


  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Fragment[] fragments = {new OnlineUsersFragment(),new MessagesFragment()};
    final String[] titles = {"Online","Messages"};

    tabAdapterTitle = new TabAdapterTitle(getChildFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,fragments,titles);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view =  inflater.inflate(R.layout.fragment_chatting, container, false);
    chattingTabLayout = view.findViewById(R.id.chattingTabLayout);
    chattingViewPager = view.findViewById(R.id.chattingViewPager);

    final Toolbar chattingToolbar = view.findViewById(R.id.chattingToolbar);
    chattingToolbar.setNavigationOnClickListener(v -> ((Home_Activity)getActivity()).showDrawer());

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);


    chattingViewPager.setAdapter(tabAdapterTitle);
    chattingTabLayout.setupWithViewPager(chattingViewPager);

  }
}