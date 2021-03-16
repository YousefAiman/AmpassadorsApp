package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import hashed.app.ampassadors.Fragments.ProfileFragment;
import hashed.app.ampassadors.R;

public class sign_in extends AppCompatActivity {
    EditText email, password ;
    Button btn_login , create_account_btn;
    FirebaseAuth auth;
    TextView verifyEmail, forgetPass;
    ProgressDialog progressDialog;

    FirebaseFirestore fStore;
    String userid;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        init();
        LogIn();
        CreateAccount();
        verify();
        resetPass();
    }

    public void resetPass(){
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText resetMail = new EditText(view.getContext());
                AlertDialog.Builder passwordResetDaialog = new AlertDialog.Builder(view.getContext());

                passwordResetDaialog.setTitle("Reset Password");
                passwordResetDaialog.setMessage("Enter your Email Ro Recived Reset Link");
                passwordResetDaialog.setView(resetMail);

                passwordResetDaialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetMail.getText().toString();
                        auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(sign_in.this, "Reset Link To Your Email. ", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(sign_in.this, "Error! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDaialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                passwordResetDaialog.create().show();
            }
        });
    }

    public void verify(){

        FirebaseUser user = auth.getCurrentUser();

        if (user!=null && user.isEmailVerified()){
            verifyEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(sign_in.this, "Verification Email Has been Sent. ", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(sign_in.this, "Email not Sent", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    public void init() {
        create_account_btn = findViewById(R.id.create_account_btn);
        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.input_email);
        password = findViewById(R.id.imput_password);
        btn_login = findViewById(R.id.sign_in_btn);
        verifyEmail = findViewById(R.id.verify_email);
        forgetPass = findViewById(R.id.forget_pass);


        fAuth = FirebaseAuth.getInstance();
        userid = fAuth.getCurrentUser().getUid();

        fStore = FirebaseFirestore.getInstance();



    }

    private void LogIn(){
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(sign_in.this);
                progressDialog.setMessage("Wait for approval from the administrator");
                progressDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setCanceledOnTouchOutside(true);
                        progressDialog.dismiss();
                    }
                },5000);


               fStore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                       if (task.isSuccessful()) {
                           if (task.getResult().exists()) {
                               String approvment = task.getResult().getString("approvment");
                               if (approvment.equals("true")){
                                   String txt_email = email.getText().toString();
                                   String txt_password = password.getText().toString();
                                   if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                                       Toast.makeText(sign_in.this, "All field are required", Toast.LENGTH_SHORT).show();
                                   }else {
                                       auth.signInWithEmailAndPassword(txt_email, txt_password)
                                               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<AuthResult> task) {
                                                       if (task.isSuccessful()){
                                                           Intent intent = new Intent(sign_in.this, Home_Activity.class);
                                                           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                           startActivity(intent);
                                                           finish();
                                                       }else{
                                                           Toast.makeText(sign_in.this, "Error Authentication!", Toast.LENGTH_SHORT).show();
                                                       }
                                                   }
                                               });
                                   }



                               }else{
                                   Toast.makeText(sign_in.this, "Admin Not Approved" , Toast.LENGTH_SHORT).show();
                               }

                           }
                       }
                   }
               });
            }
        });
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