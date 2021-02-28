package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.android.material.tabs.TabLayout;

import hashed.app.ampassadors.Adapters.TabAdapterTitle;
import hashed.app.ampassadors.Fragments.ChattingFragment;
import hashed.app.ampassadors.Fragments.GroupsFragment;
import hashed.app.ampassadors.Fragments.MessagesFragment;
import hashed.app.ampassadors.Fragments.OnlineUsersFragment;
import hashed.app.ampassadors.R;

public class HomeExampleActivity extends AppCompatActivity {


  //Views
  private TabLayout homeTabLayout;
  private ViewPager homeViewPager;
  private TabAdapterTitle tabAdapterTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_example);

    homeTabLayout = findViewById(R.id.homeTabLayout);
    homeViewPager = findViewById(R.id.homeViewPager);

    final Fragment[] fragments = {new GroupsFragment(),new ChattingFragment()};
    final String[] titles = {"Groups","Chats"};

    tabAdapterTitle = new TabAdapterTitle(getSupportFragmentManager(),
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,fragments,titles);


    homeViewPager.setAdapter(tabAdapterTitle);
    homeTabLayout.setupWithViewPager(homeViewPager);

  }
}