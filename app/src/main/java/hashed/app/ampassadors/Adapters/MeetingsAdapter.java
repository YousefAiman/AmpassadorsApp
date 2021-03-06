package hashed.app.ampassadors.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hashed.app.ampassadors.Activities.MeetingActivity;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.MeetingsVh> {

  private static ArrayList<Meeting> meetings;

  public MeetingsAdapter(ArrayList<Meeting> meetings) {
    MeetingsAdapter.meetings = meetings;
  }

  @Override
  public int getItemCount() {
    return meetings.size();
  }

  @NonNull
  @Override
  public MeetingsAdapter.MeetingsVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new MeetingsVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.meeting_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull MeetingsVh holder, int position) {
    holder.bindItem(meetings.get(position));
  }

  class MeetingsVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final Button joinBtn;
    private final TextView titleTv, dateTv, timeTv;

    public MeetingsVh(@NonNull View itemView) {
      super(itemView);
      joinBtn = itemView.findViewById(R.id.joinBtn);
      titleTv = itemView.findViewById(R.id.titleTv);
      dateTv = itemView.findViewById(R.id.dateTv);
      timeTv = itemView.findViewById(R.id.timeTv);
    }


    private void bindItem(Meeting meeting) {

      titleTv.setText(meeting.getTitle());

      dateTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
              TimeFormatter.MONTH_DAY_YEAR));

      timeTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
              TimeFormatter.HOUR_MINUTE));

      joinBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

//      if (meetings.get(getAdapterPosition()).isHasEnded()) {
//
//        Toast.makeText(itemView.getContext(),
//                "This Meeting has ended!", Toast.LENGTH_SHORT).show();
//
//        meetings.remove(getAdapterPosition());
//        notifyItemRemoved(getAdapterPosition());
//        return;
//      }
//
//      if (meetings.get(getAdapterPosition()).getStartTime() > System.currentTimeMillis()) {
//
//        Log.d("ttt","meeting start time: "+
//                meetings.get(getAdapterPosition()).getStartTime());
//
//        Log.d("ttt","System.currentTimeMillis(): "+
//                System.currentTimeMillis());
//
//        Toast.makeText(itemView.getContext(),
//                "This Meeting hasn't started yet!", Toast.LENGTH_SHORT).show();
//
//        return;
//      }

      itemView.getContext().startActivity(
              new Intent(itemView.getContext(), MeetingActivity.class)
                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("meeting",
                      meetings.get(getAdapterPosition())));


//      itemView.getContext().startActivity(
//              new Intent(itemView.getContext(), GroupMessagingActivity.class)
//                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
//                      meetings.get(getAdapterPosition()).getMeetingId()));

    }

  }
}
