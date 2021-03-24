package hashed.app.ampassadors.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.UUID;

import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.SigninUtil;

public class SuggestionsActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {

    EditText titleSubject;
    EditText subject;
    Button send;
    FirebaseFirestore firebaseFirestore;
    CollectionReference reference;
    ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        setupCompontet();
        OnClick();
        setUpToolBarAndActions();



    }

    public void setupCompontet() {
        titleSubject = findViewById(R.id.defendant);
        send = findViewById(R.id.send);
        subject = findViewById(R.id.subject);
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Suggestions");
        progressBar = new ProgressDialog(this);
    }

    public void OnClick() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectText = subject.getText().toString();
                String title = titleSubject.getText().toString();
                String suggid = UUID.randomUUID().toString();
                if (title.trim().isEmpty()) {
                    Toast.makeText(SuggestionsActivity.this, R.string.Error_meassage_subjectTitle, Toast.LENGTH_SHORT).show();
                } else if (subjectText.trim().isEmpty()) {
                    Toast.makeText(SuggestionsActivity.this, R.string.write_your_subject, Toast.LENGTH_SHORT).show();

                } else {
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("title", title);
                    hashMap.put("description", subjectText);
                    hashMap.put("time", System.currentTimeMillis());
                    hashMap.put("userid", FirebaseAuth.getInstance().getUid());
                    hashMap.put("SuggestionId", suggid);
                    hashMap.put("reviewed", false);
                    progressBar.setMessage(getString(R.string.Download));
                    progressBar.show();
                    reference.document(suggid).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.dismiss();
                            Toast.makeText(SuggestionsActivity.this, R.string.SuccessfullMessage, Toast.LENGTH_SHORT).show();
                            subject.setText("");
                            titleSubject.setText("");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.dismiss();
                            Toast.makeText(SuggestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                }
            }
        });
    }

    private void setUpToolBarAndActions() {

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        toolbar.setOnMenuItemClickListener(this);

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}