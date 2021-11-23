package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import hashed.app.ampassadors.R;

public class SliderActivity extends AppCompatActivity {

    private ViewPager SliderPagerAdapter;
    private Integer[] imageIds;
    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);




    }
}