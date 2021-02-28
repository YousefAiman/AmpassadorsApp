package hashed.app.ampassadors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import hashed.app.ampassadors.Activities.profile;
import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.Activities.sign_in;
import hashed.app.ampassadors.Activities.sign_up;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    Button button = findViewById(R.id.bbb);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, sign_in.class);
        startActivity(intent);
        finish();

      }
    });
}
}