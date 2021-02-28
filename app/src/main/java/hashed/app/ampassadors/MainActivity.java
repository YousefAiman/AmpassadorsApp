package hashed.app.ampassadors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.Activities.PrivateMessagingActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);



    if( FirebaseAuth.getInstance().getCurrentUser() == null){

      EditText emailEd = findViewById(R.id.emailEd);
      EditText passwordEd = findViewById(R.id.passwordEd);
      findViewById(R.id.signinBtn).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          String email = emailEd.getText().toString();
          String password = passwordEd.getText().toString();
          if(!email.isEmpty() && !password.isEmpty()){

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email,password
            ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
              @Override
              public void onSuccess(AuthResult authResult) {
                new Handler().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    startActivity(new Intent(MainActivity.this,
                            PrivateMessagingActivity.class));
                  }
                },0);
              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {

                Log.d("privateMessaging","sign in failed: "+e!=null?e.getMessage():"failed");

              }
            });
          }
        }
      });



    }else{
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity(new Intent(MainActivity.this,PrivateMessagingActivity.class));
        }
      },0);
    }

  }
}