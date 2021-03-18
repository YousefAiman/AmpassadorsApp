package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hashed.app.ampassadors.Adapters.WelcomePagerAdapter;
import hashed.app.ampassadors.R;

public class WelcomeActivity extends AppCompatActivity {

  ViewPager viewPager;
  LinearLayout sliderLayout;
  int dotsCount;
  ImageView[] dots;
  TextView signinTv;
  Button nextSlideBtn;
  Button signintoAccountBtn;
  TextView nexttv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.welcome_activity);
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

    String[] strings = new String[]{
            "Welcome to Pal Vision",
            "Welcome to Pal Vision",
            "Welcome to Pal Vision",
            "Welcome to Pal Vision"
    };

    viewPager = findViewById(R.id.viewPager);
    final WelcomePagerAdapter viewPagerAdapter =
            new WelcomePagerAdapter(this, strings);

    viewPager.setAdapter(viewPagerAdapter);
    nexttv = findViewById(R.id.nextTv);

    signinTv = findViewById(R.id.signinTv);
    signinTv.setVisibility(View.INVISIBLE);
    signinTv.setOnClickListener(v -> {
      Intent intent = new Intent(getApplicationContext(), sign_in.class);
      startActivity(intent);
    });

    signintoAccountBtn = findViewById(R.id.signintoAccountBtn);
    signintoAccountBtn.setVisibility(View.INVISIBLE);

    signintoAccountBtn.setOnClickListener(v -> {
//            if (networkInfo != null && networkInfo.isConnected()) {

      startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
//            } else {
//                Intent disconnectIntent = new Intent(getApplicationContext(), ConnectionActivity.class);
//                disconnectIntent.putExtra("activity", "slider");
//                startActivity(disconnectIntent);
//
//            }
    });


    sliderLayout = findViewById(R.id.dotsSlider);
    dotsCount = viewPagerAdapter.getCount();
    dots = new ImageView[dotsCount];
    nextSlideBtn = findViewById(R.id.nextSlideBtn);
    nextSlideBtn.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
    int nonActive = R.drawable.indicator_inactive_icon;
    int FullDot = R.drawable.indicator_active_icon;

    for (int i = 0; i < dotsCount; i++) {
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      dots[i] = new ImageView(getApplicationContext());
      dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), nonActive));
      params.setMargins(10, 0, 10, 0);
      sliderLayout.addView(dots[i], params);
    }
    dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), FullDot));
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(final int position) {
        for (int i = 0; i < dotsCount; i++) {
          if (i == position) continue;
          dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), nonActive));
        }
        dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), FullDot));
        if (position == dotsCount - 1) {
          signinTv.setVisibility(View.VISIBLE);
          signinTv.setClickable(true);
          nextSlideBtn.setVisibility(View.INVISIBLE);
          nextSlideBtn.setClickable(false);
          signintoAccountBtn.setVisibility(View.VISIBLE);
          signintoAccountBtn.setClickable(true);
        } else {
          signintoAccountBtn.setVisibility(View.INVISIBLE);
          signintoAccountBtn.setClickable(false);
          signinTv.setVisibility(View.INVISIBLE);
          signinTv.setClickable(false);
          nextSlideBtn.setVisibility(View.VISIBLE);
          nextSlideBtn.setClickable(true);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

    nexttv.setOnClickListener(v -> {
      if (viewPager.getCurrentItem() == dotsCount - 1) {
        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
//                intent.putExtra("guest", true);
        startActivity(intent);
      } else {
        viewPager.setCurrentItem(dotsCount);
      }

    });


  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }

}