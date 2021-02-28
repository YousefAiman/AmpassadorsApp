package hashed.app.ampassadors.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hashed.app.ampassadors.Adapters.PollAdapter;
import hashed.app.ampassadors.Objects.PostData;
import hashed.app.ampassadors.R;

public class PollActivity extends AppCompatActivity {
    PollAdapter adapter;
    RecyclerView recyclerView;
    Button new_poll_btn;
    EditText poll_qus;
    List<PollAdapter> poll_lsit;
    TextView poll_length;
    DatePicker datePicker;
    FirebaseFirestore firebaseFirestore ;
    CollectionReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);
        SetUpCompetent();
        OnClickButtons();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


    public void SetUpCompetent() {
        recyclerView = findViewById(R.id.poll_list);
        poll_qus = findViewById(R.id.qust_text);
        new_poll_btn = findViewById(R.id.create_poll_btn);
        poll_length = findViewById(R.id.date_for_poll);
        poll_lsit = new ArrayList<>();
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Poll");
    }
    public  void OnClickButtons(){
        //upload text post
        new_poll_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String polltext = poll_qus.getText().toString();
                if (polltext.isEmpty()) {
                    Toast.makeText(PollActivity.this, "pales write your post", Toast.LENGTH_SHORT).show();
                } else {
                    PostData data = new PostData();
                    data.setDescription(polltext);

                    Task<DocumentReference>task = reference.add(data);
                    task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(PollActivity.this, "Posting now", Toast.LENGTH_SHORT).show();
                        }
                    });

                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PollActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        poll_length.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PollActivity.this, "Calnnder", Toast.LENGTH_SHORT).show();
            }
        });
    }

//
//    private void SetDatePoll() {
//        datePicker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showDialog(0);
//            }
//        });
//
//
//        protected Dialog onCreateDialog ( int id){
//            return new DatePickerDialog(getApplicationContext(), "", );
//        }
//
//        DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
//            public void onDateSet(DatePicker view, int selectedYear,
//                                  int selectedMonth, int selectedDay) {
//                day = selectedDay;
//                month = selectedMonth;
//                year = selectedYear;
//                datePickerButton.setText(selectedDay + " / " + (selectedMonth + 1) + " / "
//                        + selectedYear);
//            }
//        };
//    }
}