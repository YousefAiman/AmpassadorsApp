package hashed.app.ampassadors.Activities;

import android.app.ProgressDialog;
import android.os.Bundle;
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

public class ComplaintsActivity extends AppCompatActivity {

  EditText titleSubject;
  EditText defendant;
  EditText subject;
  Button send;
  FirebaseFirestore firebaseFirestore;
  CollectionReference reference;
  ProgressDialog progressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_complaints);
    setupCompontet();
    onClickButtons();

    final Toolbar toolbar = findViewById(R.id.toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());

  }

  public void setupCompontet() {
    titleSubject = findViewById(R.id.title_com);
    send = findViewById(R.id.sendd);
    defendant = findViewById(R.id.defendant);
    subject = findViewById(R.id.subject);
    firebaseFirestore = FirebaseFirestore.getInstance();
    reference = firebaseFirestore.collection("Complaints");
    progressDialog = new ProgressDialog(this);

  }

  public void onClickButtons() {
    send.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String defendantname = defendant.getText().toString();
        String title = titleSubject.getText().toString();
        String subjectText = subject.getText().toString();
        String compID = UUID.randomUUID().toString();
        if (defendantname.trim().isEmpty()) {
          Toast.makeText(ComplaintsActivity.this, R.string.Error_message_defname, Toast.LENGTH_SHORT).show();

        } else if (title.trim().isEmpty()) {
          Toast.makeText(ComplaintsActivity.this, R.string.Error_meassage_subjectTitle, Toast.LENGTH_SHORT).show();

        } else if (subjectText.trim().isEmpty()) {
          Toast.makeText(ComplaintsActivity.this, R.string.write_your_subject, Toast.LENGTH_SHORT).show();

        } else {
          HashMap<String, Object> hashMap = new HashMap<>();
          hashMap.put("title", title);
          hashMap.put("complaintsId", compID);
          hashMap.put("description", subjectText);
          hashMap.put("defendant", defendantname);
          hashMap.put("time", System.currentTimeMillis());
          hashMap.put("reviewed", false);
          hashMap.put("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());
          progressDialog.setMessage(getString(R.string.Download));
          progressDialog.show();
          reference.document(compID).set(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              Toast.makeText(ComplaintsActivity.this, R.string.SuccessfullMessage, Toast.LENGTH_SHORT).show();
              progressDialog.dismiss();
              defendant.setText("");
              titleSubject.setText("");
              subject.setText("");
            }
          }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Toast.makeText(ComplaintsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
              progressDialog.dismiss();

            }
          });

        }
      }
    });
  }
}