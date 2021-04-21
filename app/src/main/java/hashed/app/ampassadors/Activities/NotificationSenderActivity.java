package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.UUID;

import hashed.app.ampassadors.NotificationUtil.Data;
import hashed.app.ampassadors.R;

public class NotificationSenderActivity extends AppCompatActivity {
  FirebaseMessaging fm = FirebaseMessaging.getInstance();
  private final String userToken =
          "ey52lL0pRfCVe_QqVINxOG:APA91bE_8I231mPigLqV_lxyQedJ-cK5MVDv-eJzIOlNkNmcy-vd9R4c4iVPjWNCBs5ypujQUHpRUQDRU4LUe4cseS8VS7mhCWKZErAcqbgfXd-Xlc2uxyzU3Y-834DUqjOiWVDk8qvv";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notification_sender);

    Button button = findViewById(R.id.sendNotificaitonBtns);

    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        sendNotifcaiton();
      }
    });
  }

  private void sendNotifcaiton(){

    Data data = new Data(
            FirebaseAuth.getInstance().getCurrentUser().getUid(),
            "message",
            "title",
            null,
            "message",
            "mesasge",
            "message"
    );


    fm.send(new RemoteMessage.Builder(userToken + "@gcm.googleapis.com")
            .setMessageId(UUID.randomUUID().toString())
            .setMessageType("message")
            .addData("body", "fdsfs")
            .addData("title", "asdsadasd")
            .build());

  }
}