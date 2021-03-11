package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import hashed.app.ampassadors.R;

public class HomeNewsHeaderViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

  private final ArrayList<String> titles;

  public HomeNewsHeaderViewPagerAdapter(ArrayList<String> titles) {
    this.titles = titles;
  }

  @Override
  public int getCount() {
    return titles.size();
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, final int position) {

    View view =  ((LayoutInflater) container.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.home_header_item_design, null);

    ((TextView)view.findViewById(R.id.headerTv)).setText(titles.get(position));

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Toast.makeText(view.getContext(), "Clicked: "+
                titles.get(position), Toast.LENGTH_SHORT).show();
      }
    });

    container.addView(view);
    return view;
  }


  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    container.removeView((View) object);
  }

  @Override
  public void onClick(View view) {



  }
}
