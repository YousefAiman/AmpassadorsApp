package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessaging;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class sign_in extends AppCompatActivity {
    EditText email, password ;
    Button btn_login , create_account_btn;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        init();
        LogIn();
        CreateAccount();


    }

    public void init() {
        create_account_btn = findViewById(R.id.create_account_btn);
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.imput_password);
        btn_login = findViewById(R.id.sign_in_btn);
    }

    private void LogIn(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    Toast.makeText(sign_in.this, "All field are required", Toast.LENGTH_SHORT).show();
                }else {
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    FirebaseMessaging.startMessagingService(sign_in.this);

                                    com.google.firebase.messaging.FirebaseMessaging.getInstance()
                                            .getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {

                                            if(task.isSuccessful()){

                                                FirebaseFirestore.getInstance().collection("Users")
                                                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                        .update("token", task.getResult())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> t) {
                                                        GlobalVariables.setCurrentToken(task.getResult());
                                                        startHomeActivity();
                                                    }
                                                });

                                            }else{
                                                startHomeActivity();
                                            }
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(sign_in.this,
                                    "Error Authentication!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void startHomeActivity(){

        Intent intent = new Intent(sign_in.this,
                Home_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }
    private void CreateAccount(){
        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(sign_in.this, sign_up.class);
                startActivity(intent);
            }
        });
    }
}