package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.ZoomUtil;

public class ZoomActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_zoom);

    final EditText topicEd = findViewById(R.id.topicEd);
    final EditText descriptionEd = findViewById(R.id.descriptionEd);
    final EditText durationEd = findViewById(R.id.durationEd);
    final TextView startUrlTv = findViewById(R.id.startUrlTv);
    final TextView joinUrlTv = findViewById(R.id.joinUrlTv);
    final Button createMeetingBtn = findViewById(R.id.createMeetingBtn);

    createMeetingBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        final String topic = topicEd.getText().toString();
        final String description = descriptionEd.getText().toString();
        final String duration = durationEd.getText().toString();

        if(!topic.isEmpty() && !description.isEmpty() && !duration.isEmpty()){

          final int durationInt = Integer.parseInt(duration);

          if(durationInt == 0 || durationInt > 40){

            Toast.makeText(ZoomActivity.this, "Meeting duration needs " +
                    "to be more than 0 and less than 40 minutes!", Toast.LENGTH_SHORT).show();
            return;
          }

//          ZoomUtil.createZoomMeeting(
//                  ZoomActivity.this,
//                  topic,
//                  durationInt,
//                  description,
//                  joinUrlTv,
//                  startUrlTv);

        }

      }
    });


  }
}