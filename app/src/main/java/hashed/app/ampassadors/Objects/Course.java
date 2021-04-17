package hashed.app.ampassadors.Objects;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hashed.app.ampassadors.Activities.CourseActivity;
import hashed.app.ampassadors.R;

@IgnoreExtraProperties
public class Course implements Serializable {

  @PropertyName("courseId")
  private String courseId;
  @PropertyName("creatorId")
  private String creatorId;
  @PropertyName("title")
  private String title;
  @PropertyName("tutorNames")
  private List<String> tutorNames;
  @PropertyName("tutorId")
  private String tutorId;
  @PropertyName("startTime")
  private long startTime;
  @PropertyName("createdTime")
  private long createdTime;
  @PropertyName("duration")
  private int duration;
  @PropertyName("hasEnded")
  private boolean hasEnded;
  @PropertyName("hasStarted")
  private boolean hasStarted;
  @PropertyName("important")
  private boolean important;


  public Course() {
  }

  public Course(Map<String, Object> postMap) {

    this.courseId = (String) postMap.get("courseId");
    this.creatorId = (String) postMap.get("creatorId");
    this.title = (String) postMap.get("title");
    this.tutorNames = (List<String>) postMap.get("tutorNames");
    this.startTime = (long) postMap.get("startTime");
    this.createdTime = (long) postMap.get("createdTime");
    this.duration = (int) postMap.get("duration");
    this.hasEnded = (boolean) postMap.get("hasEnded");

  }

  public static void joinCourse(Course course,Context context){

    final String courseId = course.getCourseId();
    final String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    if(currentUid.equals(course.getTutorId()) || currentUid.equals(course.getCreatorId())){
      if (!course.isHasStarted()) {
        //coordinator or tutor can start the course

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Do you want to start this course?");
        alert.setPositiveButton("Start", (dialog, which) -> {
          dialog.dismiss();
          FirebaseFirestore.getInstance().collection("Courses")
                  .document(courseId).update("hasStarted",true)
                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      context.startActivity(new Intent(context, CourseActivity.class)
                                      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                      .putExtra("course", course));
                    }
                  });
        });

        alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        alert.create().show();

      }else{

        context.startActivity(new Intent(context, CourseActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("course", course));
      }
      return;
    }

    FirebaseFirestore.getInstance().collection("Courses").document(courseId)
            .collection("Attendees")
            .document(currentUid).get().addOnSuccessListener(
            new OnSuccessListener<DocumentSnapshot>() {
              @Override
              public void onSuccess(DocumentSnapshot snapshot) {
                if(snapshot.exists()){

                  if(!course.isHasStarted()){
                    Toast.makeText(context,
                            "This Course hasn't started yet!", Toast.LENGTH_SHORT).show();
                    return;
                  }

                  context.startActivity(new Intent(context, CourseActivity.class)
                                  .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                  .putExtra("course", course));

                }else{

                  if(!course.isHasEnded() && !course.isHasStarted()){

                    final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Do you want to register for this course?");
                    alert.setPositiveButton("Register", (dialog, which) -> {
                      dialog.dismiss();
                      registerInCourse(courseId,context,currentUid);
                    });

                    alert.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
                    alert.create().show();

                  }else{

                    Toast.makeText(context, "You can't register in this course!" +
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

  private static void registerInCourse(String courseId, Context context,String currentUid){

    final ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("Registering in course!");
    progressDialog.setCancelable(false);
    progressDialog.show();


    final HashMap<String, Object> attendeeMap = new HashMap<>();
    attendeeMap.put("userId",currentUid);
    attendeeMap.put("registrationTime",System.currentTimeMillis());

    FirebaseFirestore.getInstance().collection("Courses")
            .document(courseId)
            .collection("Attendees")
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


  public String getCreatorId() {
    return creatorId;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  public boolean isHasEnded() {
    return hasEnded;
  }

  public void setHasEnded(boolean hasEnded) {
    this.hasEnded = hasEnded;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public String getTutorId() {
    return tutorId;
  }

  public void setTutorId(String tutorId) {
    this.tutorId = tutorId;
  }

  public boolean isHasStarted() {
    return hasStarted;
  }

  public void setHasStarted(boolean hasStarted) {
    this.hasStarted = hasStarted;
  }

  public List<String> getTutorNames() {
    return tutorNames;
  }

  public void setTutorNames(List<String> tutorNames) {
    this.tutorNames = tutorNames;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }
}
