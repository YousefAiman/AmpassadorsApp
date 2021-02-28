package hashed.app.ampassadors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import hashed.app.ampassadors.Activities.Home_Activity;

public class MainActivity extends AppCompatActivity {
    Button button ;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  button = findViewById(R.id.test_btn);

  button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent intent = new Intent(MainActivity.this , Home_Activity.class);
      startActivity(intent);
    }
  });
  }
}