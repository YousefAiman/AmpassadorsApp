package hashed.app.ampassadors.Adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class TabAdapterTitle extends FragmentStatePagerAdapter {

  private final Fragment[] fragments;
  private final String[] titles;

  public TabAdapterTitle(FragmentManager fm, int behavior, Fragment[] fragments,
                         String[] titles) {
    super(fm, behavior);
    this.fragments = fragments;
    this.titles = titles;
  }

  @NonNull
  @Override
  public Fragment getItem(int position) {
    return fragments[position];
  }

  @Override
  public int getCount() {
    return fragments.length;
  }

  @Nullable
  @Override
  public CharSequence getPageTitle(int position) {
    return titles[position];

  }

  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
  }
}
