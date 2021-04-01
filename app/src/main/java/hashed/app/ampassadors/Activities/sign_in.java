package hashed.app.ampassadors.Activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Services.FirebaseMessagingService;
import hashed.app.ampassadors.Utils.GlobalVariables;
import hashed.app.ampassadors.Utils.WifiUtil;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;

import java.util.HashMap;


public class sign_in extends AppCompatActivity implements View.OnClickListener {

    private static final int GOOGLE_REQUEST = 10;
    EditText email, password;
    Button btn_login, create_account_btn,gmailbtn,facebookbtn;
    FirebaseAuth auth;
    TextView verifyEmail, forgetPass;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;


    private ProgressDialog googleProgressDialog;


    LoginButton facebookLoginBtn;
    CallbackManager callbackManager;
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
                        String mail = resetMail.getText().toString().trim();


                        if (TextUtils.isEmpty(mail)) {
                            Toast.makeText(sign_in.this, "Fields are empty", Toast.LENGTH_LONG).show();
                        } else {
                            auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(sign_in.this, R.string.Link_rest_password_sent, Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(sign_in.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
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
        gmailbtn = findViewById(R.id.gmailbtn);
        facebookbtn = findViewById(R.id.facebookbtn);
        facebookLoginBtn = findViewById(R.id.facebookLoginBtn);
        gmailbtn.setOnClickListener(this);
        facebookbtn.setOnClickListener(this);


        facebookbtn = findViewById(R.id.facebookbtn);

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
                } else if (TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(sign_in.this, R.string.Error_Message_password, Toast.LENGTH_SHORT).show();
                } else {

                    final ProgressDialog dialog = new ProgressDialog(sign_in.this);
                    dialog.setMessage(getString(R.string.SignUp_Message));
                    dialog.setCancelable(false);
                    dialog.show();

                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    if (auth.getCurrentUser().isEmailVerified()) {

                                        FirebaseFirestore.getInstance().collection("Users")
                                                .document(authResult.getUser().getUid())
                                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot snapshot) {
                                                if (!snapshot.exists()) {
                                                    auth.signOut();
                                                    return;
                                                }

                                                if (snapshot.contains("rejected")
                                                        && snapshot.getBoolean("rejected")) {

                                                    auth.signOut();

                                                    Toast.makeText(sign_in.this,
                                                            R.string.Rejcetet_Message,
                                                            Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();

                                                } else {
//                                                    if (snapshot.getBoolean("approvement")) {
//
//
//                                                        FirebaseMessaging.getInstance()
//                                                                .getToken().addOnSuccessListener(new OnSuccessListener<String>() {
//                                                            @Override
//                                                            public void onSuccess(String s) {
//                                                                GlobalVariables.setCurrentToken(s);
//                                                                snapshot.getReference().update("token", s);
//                                                            }
//                                                        });
                                                    GlobalVariables.setRole(snapshot.getString("Role"));

                                                        FirebaseMessagingService.startMessagingService(sign_in.this);

                                                        Intent intent = new Intent(sign_in.this,
                                                                Home_Activity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        dialog.dismiss();
                                                        startActivity(intent);
                                                        finish();

                                                    }
//                                                    else {
//
//                                                        auth.signOut();
//                                                        dialog.dismiss();
//
//                                                        Toast.makeText(sign_in.this,
//                                                                R.string.Appromvent_Message,
//                                                                Toast.LENGTH_SHORT).show();
//                                                    }
                                              //  }
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if (!task.isSuccessful()) {

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

                                    } else {
                                        auth.signOut();
                                        dialog.dismiss();

                                        Toast.makeText(sign_in.this,
                                                "You need to verify your email in order to Sign in!",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                }

                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();

                            Toast.makeText(sign_in.this,
                                    R.string.Error_Auth + e.getLocalizedMessage(),
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


    @Override
    public void onClick(View view) {
        if(view.getId() == gmailbtn.getId()){
            if (WifiUtil.checkWifiConnection(this)) {
                googleSignIn();
            }
        }else if(view.getId() == facebookbtn.getId()){
            if (WifiUtil.checkWifiConnection(this)) {
                facebookLoginBtn.performClick();
            }
        }else if(view.getId() == facebookLoginBtn.getId()){
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Logging in with facebook");
            progressDialog.show();

            FacebookSdk.fullyInitialize();

            callbackManager = CallbackManager.Factory.create();
            facebookLoginBtn.setReadPermissions("email", "public_profile");
            facebookLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    handleFacebookAccessToken(loginResult.getAccessToken(),progressDialog);
                }

                @Override
                public void onCancel() {
                    Log.d("ttt", "facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d("ttt", "facebook:onError", error);
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GOOGLE_REQUEST){
//            if(resultCode ==RESULT_OK){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                task.addOnSuccessListener(googleSignInAccount -> {
                    try {
                        final GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        googleProgressDialog.dismiss();

                        Log.d("ttt","ApiException google: "+e.getMessage());

                        Log.w("ttt", "Google sign in failed" + e.getMessage());
                    }
                }).addOnFailureListener(e -> {

                    Log.d("ttt","task google: "+e.toString());
                    googleProgressDialog.dismiss();
                });
        }else if(callbackManager != null){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void googleSignIn() {

        googleProgressDialog = new ProgressDialog(this);
        googleProgressDialog.setTitle("Signing in with Gmail account!");
        googleProgressDialog.setCancelable(false);
        googleProgressDialog.show();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
////                .requestIdToken(R.string)
//                .requestEmail().build();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        Intent googleIntent = GoogleSignIn.getClient(this, gso).getSignInIntent();
        startActivityForResult(googleIntent, GOOGLE_REQUEST);

    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {

       final  AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Log.d("ttt", "signInWithCredential:success");

                    if (authResult.getAdditionalUserInfo().isNewUser()) {
                        addUserToFirestore(account.getDisplayName(),account.getEmail()
                                , auth.getCurrentUser().getUid(),
                                account.getPhotoUrl().toString());
                    } else {

                        FirebaseFirestore.getInstance().collection("Users")
                                .document(authResult.getUser().getUid())
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot snapshot) {

                                if(snapshot.exists()){

                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                                        snapshot.getReference().update("token", s);
                                    });

                                    GlobalVariables.setRole(snapshot.getString("Role"));

                                    FirebaseMessagingService.
                                            startMessagingService(sign_in.this);

                                    googleProgressDialog.dismiss();

                                    startActivity(new Intent(getApplicationContext(),
                                            Home_Activity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();

                                }else{

                                    googleProgressDialog.dismiss();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });


                    }

                }).addOnFailureListener(e -> {
            googleProgressDialog.dismiss();
            Toast.makeText(sign_in.this, "لقد فشلت عملية التسجيل عن طريق حساب gmail!"
                    , Toast.LENGTH_SHORT).show();
        });
    }



    private void addUserToFirestore(String username,String email,String userId,
                                    String imageUrl){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("username", username);
        hashMap.put("email", email);
        hashMap.put("rejected", false);
        hashMap.put("userId",userId);
        if (imageUrl != null) {
            hashMap.put("imageurl", imageUrl);
        }
        hashMap.put("status", true);
        hashMap.put("Role", "Ambassador");

        GlobalVariables.setRole("Ambassador");

        final DocumentReference userRef =
                FirebaseFirestore.getInstance().collection("Users").document(userId);

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                hashMap.put("token", s);

                userRef.set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        FirebaseMessagingService.
                            startMessagingService(sign_in.this);
                        startActivity(new Intent(sign_in.this,Home_Activity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(sign_in.this,
                                e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }


    private void handleFacebookAccessToken(final AccessToken token,ProgressDialog progressDialog) {

        FacebookSdk.setAutoInitEnabled(true);

        GraphRequest graphRequest = GraphRequest.newMeRequest(token, (object, response) -> {
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            auth.signInWithCredential(credential).addOnSuccessListener(authResult -> {

                final FirebaseUser facebookUser = auth.getCurrentUser();

                FirebaseFirestore.getInstance().collection("Users")
                        .document(facebookUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if (!task.getResult().exists()) {
                                    String email = "";
                                    if (object.has("email")) {
                                        try {
                                            email = object.getString("email");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    addUserToFirestore(email, facebookUser.getDisplayName(),
                                            facebookUser.getPhotoUrl().toString(),
                                            facebookUser.getUid());
                                } else {

                                    final DocumentReference userRef = task.getResult().getReference();

                                    FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
                                            userRef.update("token", s));

                                    GlobalVariables.setRole(task.getResult().getString("Role"));

                                    FirebaseMessagingService.
                                            startMessagingService(sign_in.this);

                                    progressDialog.dismiss();
                                    startActivity(new Intent(getApplicationContext(),
                                            Home_Activity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();

                                }

                            }
                        });



            }).addOnFailureListener(e -> Toast.makeText(sign_in.this,
                    "لقد فشلت عملية تسجيل الدخول:"
                            + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

    }

}
