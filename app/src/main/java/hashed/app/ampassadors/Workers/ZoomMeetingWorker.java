package hashed.app.ampassadors.Workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class ZoomMeetingWorker extends Worker {

  public ZoomMeetingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {

    final Data inputData = getInputData();

    final String groupId = inputData.getString("groupId");
    final String messageId = inputData.getString("messageId");

    if(groupId!=null){

      if(messageId!=null){
        FirebaseDatabase.getInstance().getReference().child("GroupMessages")
                .child(groupId).child("Messages").child(messageId).child("zoomMeeting")
                .child("status").setValue("ended");
      }

      FirebaseFirestore.getInstance().collection("Meetings")
              .document(groupId).update("currentZoomMeeting",null);

    }else{
      return Result.failure();
    }

    Log.d("ttt","doing work");

    return Result.success();
  }
}