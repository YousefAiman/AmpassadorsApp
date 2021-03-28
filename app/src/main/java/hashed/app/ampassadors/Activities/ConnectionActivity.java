package hashed.app.ampassadors.Activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.WifiUtil;

public class ConnectionActivity extends AppCompatActivity {

  public static final int CONNECTION_RESULT = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_connection);

    findViewById(R.id.retryBtn).setOnClickListener(v -> {
      if (WifiUtil.isConnectedToInternet(this)) {

        setResult(CONNECTION_RESULT);
        finish();

      } else {
        Toast.makeText(ConnectionActivity.this,
                R.string.AuthMessage, Toast.LENGTH_SHORT).show();
      }
    });
  }
}