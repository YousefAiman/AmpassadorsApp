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
import hashed.app.ampassadors.Objects.MeetingPreview;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class HomeNewsHeaderViewPagerAdapter extends PagerAdapter implements View.OnClickListener {

  private final ArrayList<MeetingPreview> meetingPreviews;

  public HomeNewsHeaderViewPagerAdapter(ArrayList<MeetingPreview> meetingPreviews) {
    this.meetingPreviews = meetingPreviews;
  }

  @Override
  public int getCount() {
    return meetingPreviews.size();
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
    return view == object;
  }

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, final int position) {

    final MeetingPreview meetingPreview = meetingPreviews.get(position);
    View view = ((LayoutInflater) container.getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.home_header_item_design, null);


    ((TextView) view.findViewById(R.id.headerTv)).setText(meetingPreview.getTitle() + " at: " +
            TimeFormatter.formatWithPattern(meetingPreview.getStartTime(),
                    TimeFormatter.HOUR_MINUTE));

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {


        if (meetingPreview.isHasEnded()) {

          Toast.makeText(container.getContext(),
                  "This Meeting has ended!", Toast.LENGTH_SHORT).show();
          return;
        }

        if (meetingPreview.getStartTime() > System.currentTimeMillis()) {

          Toast.makeText(container.getContext(),
                  "This Meeting hasn't started yet!", Toast.LENGTH_SHORT).show();

          return;
        }

        container.getContext().startActivity(
                new Intent(container.getContext(), GroupMessagingActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
                        meetingPreviews.get(position).getMeetingId()));

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
