package hashed.app.ampassadors.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Activities.PrivateMessagingActivity;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class MeetingsAdapter extends RecyclerView.Adapter<MeetingsAdapter.MeetingsVh> {

  private static ArrayList<Meeting> meetings;

  public MeetingsAdapter(ArrayList<Meeting> meetings){
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

   static class MeetingsVh extends RecyclerView.ViewHolder implements View.OnClickListener{

    private final Button joinBtn;
    private final TextView titleTv,dateTv,timeTv;

     public MeetingsVh(@NonNull View itemView) {
       super(itemView);
       joinBtn = itemView.findViewById(R.id.joinBtn);
       titleTv = itemView.findViewById(R.id.titleTv);
       dateTv = itemView.findViewById(R.id.dateTv);
       timeTv = itemView.findViewById(R.id.timeTv);
     }


     private void bindItem(Meeting meeting){

       titleTv.setText(meeting.getTitle());

       dateTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
               TimeFormatter.MONTH_DAY_YEAR));

       timeTv.setText(TimeFormatter.formatWithPattern(meeting.getStartTime(),
               TimeFormatter.HOUR_MINUTE));

       joinBtn.setOnClickListener(this);

     }

     @Override
     public void onClick(View view) {

       itemView.getContext().startActivity(
               new Intent(itemView.getContext(), GroupMessagingActivity.class)
                       .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("groupId",
                       meetings.get(getAdapterPosition()).getMeetingId()));

     }

   }
}
