package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.Adapters.WelcomePagerAdapter;
import hashed.app.ampassadors.R;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

  private ViewPager viewPager;
  private int dotsCount;
  private ImageView[] dots;
  private TextView signinTv,skipTv;
  private Button nextSlideBtn,signintoAccountBtn,guestBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_welcome);

    String[] strings = new String[]{
            "Welcome to Pal Vision",
            "Welcome to Pal Vision",
            "Welcome to Pal Vision",
            "Welcome to Pal Vision"
    };

    guestBtn = findViewById(R.id.guestBtn);
    viewPager = findViewById(R.id.viewPager);

    final WelcomePagerAdapter viewPagerAdapter =
            new WelcomePagerAdapter(this, strings);

    viewPager.setAdapter(viewPagerAdapter);
    skipTv = findViewById(R.id.nextTv);

    signinTv = findViewById(R.id.signinTv);
    signinTv.setVisibility(View.INVISIBLE);

    signintoAccountBtn = findViewById(R.id.signintoAccountBtn);
    signintoAccountBtn.setVisibility(View.INVISIBLE);
    nextSlideBtn = findViewById(R.id.nextSlideBtn);
    guestBtn.setOnClickListener(this);
    signinTv.setOnClickListener(this);
    signintoAccountBtn.setOnClickListener(this);
    nextSlideBtn.setOnClickListener(this);

    final LinearLayout sliderLayout = findViewById(R.id.dotsSlider);
    dotsCount = viewPagerAdapter.getCount();
    dots = new ImageView[dotsCount];


    int nonActive = R.drawable.indicator_inactive_icon;
    int FullDot = R.drawable.indicator_active_icon;

    for (int i = 0; i < dotsCount; i++) {
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
          dots[i].setImageDrawable(
                  ContextCompat.getDrawable(getApplicationContext(), nonActive));
        }

        dots[position].setImageDrawable(
                ContextCompat.getDrawable(getApplicationContext(), FullDot));

        if (position == dotsCount - 1) {
          signinTv.setVisibility(View.VISIBLE);
          signinTv.setClickable(true);
          nextSlideBtn.setVisibility(View.INVISIBLE);
          nextSlideBtn.setClickable(false);
          signintoAccountBtn.setVisibility(View.VISIBLE);
          signintoAccountBtn.setClickable(true);
          skipTv.setVisibility(View.INVISIBLE);
        } else {
          signintoAccountBtn.setVisibility(View.INVISIBLE);
          signintoAccountBtn.setClickable(false);
          signinTv.setVisibility(View.INVISIBLE);
          signinTv.setClickable(false);
          nextSlideBtn.setVisibility(View.VISIBLE);
          nextSlideBtn.setClickable(true);
          skipTv.setVisibility(View.VISIBLE);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

    skipTv.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {

    if(view.getId() == guestBtn.getId()){
      guestBtn.setClickable(false);
      FirebaseAuth.getInstance().signInAnonymously()
              .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                  startActivity(new Intent(WelcomeActivity.this, Home_Activity.class)
                          .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                  finish();
                }
              }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          guestBtn.setClickable(true);
          finish();
        }
      });
    }else if(view.getId() == signinTv.getId()){
      startActivity(new Intent(getApplicationContext(), sign_in.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      finish();
    }else if(view.getId() == signintoAccountBtn.getId()){
      startActivity(new Intent(getApplicationContext(), sign_up.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      finish();
    }else if(view.getId() == nextSlideBtn.getId()){
      viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }else if(view.getId() == skipTv.getId()){
      viewPager.setCurrentItem(viewPager.getChildCount() - 1);
    }
    }
}