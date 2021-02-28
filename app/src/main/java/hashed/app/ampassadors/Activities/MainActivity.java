package hashed.app.ampassadors.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if(FirebaseAuth.getInstance().getCurrentUser()!=null){

      startActivity(new Intent(this,HomeExampleActivity.class));

    }else{

      EditText emailEd = findViewById(R.id.emailEd);
      EditText passwordEd = findViewById(R.id.passwordEd);
      Button signinBtn = findViewById(R.id.signinBtn);


      signinBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          signinBtn.setClickable(false);
          String email = emailEd.getText().toString();
          String password = passwordEd.getText().toString();

          if(!email.isEmpty() && !password.isEmpty()){

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,
                    password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
              @Override
              public void onSuccess(AuthResult authResult) {
                startActivity(new Intent(MainActivity.this,HomeExampleActivity.class));
              }
            });

          }


        }
      });



    }



  }
}