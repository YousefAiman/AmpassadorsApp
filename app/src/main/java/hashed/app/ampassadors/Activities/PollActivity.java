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
import android.widget.ImageView;
import android.widget.LinearLayout;
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


    ImageView circleImageView;
    TextView usernameTv;
    Button pulibshBtn;
    EditText editText;
    LinearLayout linearLayout;
    List<EditText> editTexts;
    EditText editText1;
    EditText editText2;
    LinearLayout addLinear;
    TextView timeTv;
    ImageView addIv;
    FirebaseFirestore firebaseFirestore ;
    CollectionReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);


        SetUpCompetent();
        OnClickButtons();

    }


    public void SetUpCompetent() {

        circleImageView = findViewById(R.id.circleImageView);
        usernameTv = findViewById(R.id.usernameTv);
        pulibshBtn = findViewById(R.id.pulibshBtn);
        editText = findViewById(R.id.editText);
        linearLayout = findViewById(R.id.linearLayout);
        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        timeTv = findViewById(R.id.timeTv);
        addIv = findViewById(R.id.addIv);

        editTexts = new ArrayList<>();
        editTexts.add(editText1);
        editTexts.add(editText2);
        firebaseFirestore = FirebaseFirestore.getInstance();
        reference = firebaseFirestore.collection("Poll");

    }

    private void addEditText(){

        addIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                linearLayout.addView(editText2);


            }
        });


    }

    public  void OnClickButtons(){


        editText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                addIv.setVisibility(View.INVISIBLE);

                LinearLayout linearLayout = new LinearLayout(PollActivity.this);
                EditText editText3 = new EditText(PollActivity.this);
                ImageView imageView = new ImageView(PollActivity.this);


                linearLayout.addView(addLinear,editTexts.size()-1);


                if(editTexts.size() < 5){
                    addIv.setVisibility(View.VISIBLE);
                }

            }
        });

        //upload text post
//        new_poll_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String polltext = poll_qus.getText().toString();
//                if (polltext.isEmpty()) {
//                    Toast.makeText(PollActivity.this, "pales write your post", Toast.LENGTH_SHORT).show();
//                } else {
//                    PostData data = new PostData();
//                    data.setDescription(polltext);
//
//                    Task<DocumentReference>task = reference.add(data);
//                    task.addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            Toast.makeText(PollActivity.this, "Posting now", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    task.addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(PollActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        });
//
//        poll_length.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(PollActivity.this, "Calnnder", Toast.LENGTH_SHORT).show();
//            }
//        });
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