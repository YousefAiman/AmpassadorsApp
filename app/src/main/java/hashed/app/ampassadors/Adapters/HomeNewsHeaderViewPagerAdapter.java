package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class HomeNewsHeaderViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

  private final ArrayList<PostData> postData;

  public HomeNewsHeaderViewPagerAdapter(ArrayList<PostData> postData) {
    this.postData = postData;
  }

  @Override
  public int getCount() {
    return postData.size();
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }

  @NonNull
  @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

    final PostData data= postData.get(position);
    View view = ((LayoutInflater) container.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.home_header_item_design, null);


    ((TextView) view.findViewById(R.id.headerTv)).setText(data.getTitle());

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        container.getContext().startActivity(
                new Intent(container.getContext(), PostNewsActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("postData",data));

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
  public void onClick(View view) {}
}
