package hashed.app.ampassadors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import hashed.app.ampassadors.Activities.Home_Activity;
import hashed.app.ampassadors.Activities.UsersPost;
import hashed.app.ampassadors.Activities.posts_profile;
import hashed.app.ampassadors.Activities.profile;
import hashed.app.ampassadors.Activities.profile_edit;
import hashed.app.ampassadors.Activities.sign_in;
import hashed.app.ampassadors.Activities.sign_up;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if(FirebaseAuth.getInstance().getCurrentUser()!=null){

      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity( new Intent(MainActivity.this, posts_profile.class));
          finish();
        }
      },1000);

    }else{

      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          startActivity( new Intent(MainActivity.this, UsersPost.class));
          finish();
        }
      },2000);

    }

}

}