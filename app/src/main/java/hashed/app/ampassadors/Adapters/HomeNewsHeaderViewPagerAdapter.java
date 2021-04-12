package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PostNewsActivity;
import hashed.app.ampassadors.Objects.HeaderItem;
import hashed.app.ampassadors.Objects.MeetingPreview;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

  public class HomeNewsHeaderViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

  private final ArrayList<HeaderItem> headerItems;

  public HomeNewsHeaderViewPagerAdapter(ArrayList<HeaderItem> headerItems) {
    this.headerItems = headerItems;
  }

  @Override
  public int getCount() {
    return headerItems.size();
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }

  @NonNull
  @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

    final HeaderItem data= headerItems.get(position);
    View view = ((LayoutInflater) container.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.home_header_item_design, null);

    ((TextView) view.findViewById(R.id.headerTv)).setText(data.getTitle());

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent;
      switch (data.getType()){
        case "news":
          new Intent(container.getContext(), PostNewsActivity.class).putExtra("postData",(PostData)data.getObject());
          break;
        case "poll":
          new Intent(container.getContext(), PostNewsActivity.class).putExtra("postData",(PostData)data.getObject());

          break;
          case "course":
            new Intent(container.getContext(), PostNewsActivity.class).putExtra("postData",(PostData)data.getObject());

            break;
        case "meet":
          new Intent(container.getContext(), PostNewsActivity.class).putExtra("postData",(PostData)data.getObject());

          break;
      }
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
