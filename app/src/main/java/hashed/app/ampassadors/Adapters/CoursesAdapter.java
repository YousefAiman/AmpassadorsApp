package hashed.app.ampassadors.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import hashed.app.ampassadors.Activities.GroupMessagingActivity;
import hashed.app.ampassadors.Objects.Course;
import hashed.app.ampassadors.Objects.Meeting;
import hashed.app.ampassadors.R;
import hashed.app.ampassadors.Utils.TimeFormatter;

public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CoursesVh> {

  private static ArrayList<Course> courses;
  private static CollectionReference coursesRef;
  private static String currentUid;

  public CoursesAdapter(ArrayList<Course> courses) {
    CoursesAdapter.courses = courses;
    coursesRef = FirebaseFirestore.getInstance().collection("Courses");
    currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
  }

  @Override
  public int getItemCount() {
    return courses.size();
  }

  @NonNull
  @Override
  public CoursesAdapter.CoursesVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    return new CoursesVh(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.course_item_layout, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull CoursesVh holder, int position) {
    holder.bindItem(courses.get(position));
  }


   class CoursesVh extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView courseNameTv, courseTutorNameTv, courseStartTimeTv,courseDurationTv;

    public CoursesVh(@NonNull View itemView) {
      super(itemView);
      courseNameTv = itemView.findViewById(R.id.courseNameTv);
      courseTutorNameTv = itemView.findViewById(R.id.courseTutorNameTv);
      courseStartTimeTv = itemView.findViewById(R.id.courseStartTimeTv);
      courseDurationTv = itemView.findViewById(R.id.courseDurationTv);
    }

    @SuppressLint("SetTextI18n")
    private void bindItem(Course course) {

      courseNameTv.setText(course.getTitle());
      courseTutorNameTv.setText(itemView.getResources().getString(R.string.tutor_name) + " " +
              course.getTutorName());


      courseStartTimeTv.setText(itemView.getResources().getString(R.string.start_time) + " " +
              TimeFormatter.formatWithPattern(course.getStartTime(),
              TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

      courseDurationTv.setText(itemView.getResources().getString(R.string.course_duration) + " " +
              String.format(Locale.getDefault(),"%d:%d", course.getDuration() / 60
                      , course.getDuration() % 60));

      itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

      final Course course = courses.get(getAdapterPosition());

      if(course.isHasEnded()){
        Toast.makeText(itemView.getContext(),
                "This Course has ended!", Toast.LENGTH_SHORT).show();
        final int index = courses.indexOf(course);
        courses.remove(index);
        notifyItemRemoved(index);
        return;
      }

      final String courseId = course.getCourseId();

      if(currentUid.equals(course.getTutorId()) || currentUid.equals(course.getCreatorId())){
        if (!course.isHasStarted()) {
          //coordinator or tutor can start the course

          final AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
          alert.setTitle("Do you want to start this course?");
          alert.setPositiveButton("Start", (dialog, which) -> {
            dialog.dismiss();
            coursesRef.document(courseId).update("hasStarted",true)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {
                itemView.getContext().startActivity(
                        new Intent(itemView.getContext(), GroupMessagingActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
                                courseId));
              }
            });
          });

          alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
          alert.create().show();

        }else{

          itemView.getContext().startActivity(
                  new Intent(itemView.getContext(), GroupMessagingActivity.class)
                          .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
                          courseId));

        }
        return;
      }

      coursesRef.document(courseId)
              .collection("Attendees")
              .document(currentUid).get().addOnSuccessListener(
                      new OnSuccessListener<DocumentSnapshot>() {
        @Override
        public void onSuccess(DocumentSnapshot snapshot) {
          if(snapshot.exists()){

            if(!course.isHasStarted()){
              Toast.makeText(itemView.getContext(),
                      "This Course hasn't started yet!", Toast.LENGTH_SHORT).show();
              return;
            }

            itemView.getContext().startActivity(
                    new Intent(itemView.getContext(), GroupMessagingActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("messagingUid",
                            courseId));

          }else{

            if(!course.isHasEnded() && !course.isHasStarted()){

              final AlertDialog.Builder alert = new AlertDialog.Builder(itemView.getContext());
              alert.setTitle("Do you want to register for this course?");
              alert.setPositiveButton("Register", (dialog, which) -> {
                dialog.dismiss();
                registerInCourse(courseId,itemView.getContext());
              });

              alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
              alert.create().show();

            }else{

              Toast.makeText(itemView.getContext(), "You can't register in this course!" +
                      "the course already started", Toast.LENGTH_SHORT).show();
              
            }

          }
        }
      }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          Log.d("ttt","complete course details");
        }
      });

    }
  }

  private static void registerInCourse(String courseId, Context context){

    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Registering in course!");
    progressDialog.setCancelable(false);
    progressDialog.show();


    final HashMap<String, Object> attendeeMap = new HashMap<>();
    attendeeMap.put("userId",currentUid);
    attendeeMap.put("registrationTime",System.currentTimeMillis());

            coursesRef.document(courseId).collection("Attendees")
            .document(currentUid)
            .set(attendeeMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(Void aVoid) {

                Toast.makeText(context, "You were registered successfully!",
                        Toast.LENGTH_SHORT).show();

                progressDialog.dismiss();
              }
            }).addOnFailureListener(new OnFailureListener() {
      @Override
      public void onFailure(@NonNull Exception e) {
        Toast.makeText(context, "Course registration failed! Please try again",
                Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
      }
    });

  }

}
