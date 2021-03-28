package hashed.app.ampassadors.Utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class JobWorker extends Worker {

  public JobWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
  }

  @NonNull
  @Override
  public Result doWork() {

    Log.d("ttt","doing work");

    return Result.success();
  }
}
