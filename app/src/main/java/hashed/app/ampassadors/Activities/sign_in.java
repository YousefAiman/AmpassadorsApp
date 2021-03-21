package hashed.app.ampassadors.Activities;

import android.app.ProgressDialog;
import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.GlobalVariables;

public class sign_in extends AppCompatActivity {

  EditText email, password;
  Button btn_login, create_account_btn;
  FirebaseAuth auth;
  TextView verifyEmail, forgetPass;
  FirebaseFirestore fStore;
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

  public void resetPass() {
    forgetPass.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditText resetMail = new EditText(view.getContext());
        AlertDialog.Builder passwordResetDaialog = new AlertDialog.Builder(view.getContext());

        passwordResetDaialog.setTitle(getString(R.string.Rest_Password));
        passwordResetDaialog.setMessage(getString(R.string.Email_Rest_Password));
        passwordResetDaialog.setView(resetMail);

        passwordResetDaialog.setPositiveButton(getString(R.string.YES), new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            String mail = resetMail.getText().toString();
            auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                Toast.makeText(sign_in.this, R.string.Link_rest_password_sent, Toast.LENGTH_SHORT).show();
              }
            }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Toast.makeText(sign_in.this,  e.getMessage(), Toast.LENGTH_SHORT).show();
              }
            });
          }
        });
        passwordResetDaialog.setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {

          }
        });

        passwordResetDaialog.create().show();
      }
    });
  }

  public void verify() {

    FirebaseUser user = auth.getCurrentUser();

    if (user != null && user.isEmailVerified()) {
      verifyEmail.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              Toast.makeText(sign_in.this,
                      R.string.Email_Verfiy,
                      Toast.LENGTH_SHORT).show();
            }
          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Toast.makeText(sign_in.this,
                      R.string.Email_not_Sent, Toast.LENGTH_SHORT).show();
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
    // userid = fAuth.getCurrentUser().getUid();

    fStore = FirebaseFirestore.getInstance();

  }

  private void LogIn() {
    btn_login.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        String txt_email = email.getText().toString();
        String txt_password = password.getText().toString();

        if (TextUtils.isEmpty(txt_email)) {
          Toast.makeText(sign_in.this, R.string.Error_Message_Email,
                  Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(txt_password)){
          Toast.makeText(sign_in.this, R.string.Error_Message_password, Toast.LENGTH_SHORT).show();
        }else {

          final ProgressDialog dialog = new ProgressDialog(sign_in.this);
          dialog.setMessage(getString(R.string.SignUp_Message));
          dialog.setCancelable(false);
          dialog.show();

          auth.signInWithEmailAndPassword(txt_email, txt_password)
                  .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                      FirebaseFirestore.getInstance().collection("Users")
                              .document(authResult.getUser().getUid())
                              .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                          if(!snapshot.exists()){
                            auth.signOut();
                            return;
                          }

                          if(snapshot.contains("rejected")
                           && snapshot.getBoolean("rejected")){

                            auth.signOut();

                            Toast.makeText(sign_in.this,
                                    R.string.Rejcetet_Message,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                          }else{

                            if(snapshot.getBoolean("approvement")){

                              GlobalVariables.setRole(snapshot.getString("Role"));


                              FirebaseMessaging.getInstance()
                                      .getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                  GlobalVariables.setCurrentToken(s);
                                  snapshot.getReference().update("token",s);
                                }
                              });

                              FirebaseMessagingService.startMessagingService(sign_in.this);

                              Intent intent = new Intent(sign_in.this,
                                      Home_Activity.class);
                              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                      Intent.FLAG_ACTIVITY_NEW_TASK);
                              dialog.dismiss();
                              startActivity(intent);
                              finish();

                            }else{

                              auth.signOut();
                              dialog.dismiss();

                              Toast.makeText(sign_in.this,
                                      R.string.Appromvent_Message,
                                      Toast.LENGTH_SHORT).show();
                            }

                          }

                        }
                      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                          if(!task.isSuccessful()){

                            auth.signOut();

                            Toast.makeText(sign_in.this,
                                    R.string.Rejcetet_Message,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();

                          }

                        }
                      }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          auth.signOut();
                          dialog.dismiss();
                        }
                      });


                    }

                  }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              dialog.dismiss();

              Toast.makeText(sign_in.this,
                      R.string.Error_Auth +e.getLocalizedMessage(),
                      Toast.LENGTH_SHORT).show();
            }
          });



        }
      }
    });
  }

  private void CreateAccount() {
    create_account_btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(sign_in.this, sign_up.class);
        startActivity(intent);
      }
    });
  }


}
