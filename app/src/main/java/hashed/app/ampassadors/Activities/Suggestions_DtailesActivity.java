package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Objects.Suggestions;
import hashed.app.ampassadors.R;

public class Suggestions_DtailesActivity extends AppCompatActivity {

    FirebaseFirestore firebaseFirestore;
    CollectionReference reference;
    TextView title;
    TextView subject;
    ImageButton delete;
    List<Suggestions> suggestions;
    Suggestions suggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions__dtailes);
        SetUpCompntet();
        ReadSuggest();
        ReviewedSuggest();

    }

    public void SetUpCompntet() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Suggestions");
        title = findViewById(R.id.title_com);
        subject = findViewById(R.id.subject);
        delete = findViewById(R.id.deletebtn);
        suggestions = new ArrayList<>();
    }

    public void ReadSuggest() {
        Intent intent = getIntent();
        if (intent.hasExtra("id")) {

            String id = intent.getStringExtra("id");
            reference.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    suggestion = documentSnapshot.toObject(Suggestions.class);
                    title.setText(suggestion.getTitle());
                    subject.setText(suggestion.getDescription());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Suggestions_DtailesActivity.this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }

    }

    private void ReviewedSuggest() {

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = suggestion.getSuggestionId();

                reference.document(id).update("reviewed", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                        Intent intent = new Intent(getApplicationContext() , List_Sug_Activity.class);
                        startActivity(intent);
                        Toast.makeText(Suggestions_DtailesActivity.this, "Delete Successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Suggestions_DtailesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}