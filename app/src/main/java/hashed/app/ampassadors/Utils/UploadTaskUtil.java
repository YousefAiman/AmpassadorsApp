package hashed.app.ampassadors.Utils;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

public class UploadTaskUtil {

  public static void
  cancelUploadTasks(Map<UploadTask, StorageTask<UploadTask.TaskSnapshot>> uploadTaskMap){

    if(uploadTaskMap!=null && !uploadTaskMap.isEmpty()){
      final UploadTask uploadTask = uploadTaskMap.keySet().iterator().next();

      uploadTask.removeOnSuccessListener(
              (OnSuccessListener<? super UploadTask.TaskSnapshot>) uploadTaskMap.get(uploadTask));

      uploadTask.addOnSuccessListener(taskSnapshot ->
              uploadTask.getSnapshot().getStorage().delete()
                      .addOnSuccessListener(v -> Log.d("ttt", "ref delete sucess")).
                      addOnFailureListener(e -> Log.d("ttt", "ref delete failed: " +
                              e.getMessage())));

    }
  }
}
