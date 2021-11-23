package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hashed.app.ampassadors.Adapters.SliderPagerAdapter;
import hashed.app.ampassadors.R;

public class SliderActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager vp;
    private SliderPagerAdapter sliderPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        vp = findViewById(R.id.vp);
        Integer[] images = new Integer[]{R.drawable.slider_1,
                R.drawable.slider_2,
                R.drawable.slider_3,
                R.drawable.slider_4,
                R.drawable.slider_5};

        sliderPagerAdapter = new SliderPagerAdapter(images);
        vp.setAdapter(sliderPagerAdapter);

        TextView tvSkip = findViewById(R.id.tvSkip);
        ImageView ivNext = findViewById(R.id.ivNext);

        final LinearLayout sliderLayout = findViewById(R.id.dotsSlider);

        int nonActive = R.drawable.indicator_inactive_icon,
                fullDot = R.drawable.indicator_active_icon;

        ImageView[] dots = new ImageView[images.length];

        float density = getResources().getDisplayMetrics().density;
        int spacing = (int) (8 * density);

        for (int i = 0; i < dots.length; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dots[i] = new ImageView(getApplicationContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), nonActive));
            params.setMargins(spacing, 0, spacing, 0);
            sliderLayout.addView(dots[i], params);
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), fullDot));
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int lastScrolledPosition = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
//                for (int i = 0; i < dots.length; i++) {
//                    if (i == position) continue;
                    dots[lastScrolledPosition].setImageDrawable(
                            ContextCompat.getDrawable(getApplicationContext(), nonActive));
//                }

                dots[position].setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(), fullDot));

                lastScrolledPosition = position;

                if (position == dots.length - 1) {
                    tvSkip.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tvSkip.setOnClickListener(this);
        ivNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        final int id = v.getId();

        if(id == R.id.tvSkip){
//            vp.setCurrentItem(sliderPagerAdapter.getCount()-1,true);
            startActivity(new Intent(this,sign_in.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }else if(id == R.id.ivNext){

            if(vp.getCurrentItem() == sliderPagerAdapter.getCount()-1){
                startActivity(new Intent(this,sign_in.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }else{
                vp.setCurrentItem(vp.getCurrentItem() + 1,true);
            }
        }


    }
}