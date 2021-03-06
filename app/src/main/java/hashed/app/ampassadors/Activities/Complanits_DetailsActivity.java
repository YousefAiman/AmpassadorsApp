package hashed.app.ampassadors.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Objects.Complaints;
import hashed.app.ampassadors.R;

public class Complanits_DetailsActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
  FirebaseFirestore firebaseFirestore;
  CollectionReference reference;
  TextView title;
  TextView subject;
  TextView defednat;
  ImageButton delete;
  List<Complaints> complaints;
  Complaints complaint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_complanits__details);
    SetUpCompntet();
    ReadComplaints();
    ReviewedComplaint();
    setUpToolBarAndActions();
  }


  public void SetUpCompntet() {
    firebaseFirestore = FirebaseFirestore.getInstance();
    reference = firebaseFirestore.collection("Complaints");
    title = findViewById(R.id.title);
    subject = findViewById(R.id.subject);
    delete = findViewById(R.id.deletebtn);
    defednat = findViewById(R.id.defendant);
    complaints = new ArrayList<>();
  }

  private void ReadComplaints() {
    Intent intent = getIntent();
    if (intent.hasExtra("id")) {
//            Query query = reference.whereEqualTo("reviewed",false);
      String id = intent.getStringExtra("id");

      reference.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot documentSnapshot) {
          complaint = documentSnapshot.toObject(Complaints.class);
          title.setText(complaint.getTitle());
          subject.setText(complaint.getDescription());
          defednat.setText(complaint.getDefendant());
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          Toast.makeText(Complanits_DetailsActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  private void ReviewedComplaint() {
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        reference.document(id).update("reviewed", true).addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            Toast.makeText(Complanits_DetailsActivity.this, R.string.Delete_success, Toast.LENGTH_SHORT).show();
            finish();
          }
        });
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