package hashed.app.ampassadors.Utils;

import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;

import java.util.concurrent.TimeUnit;

public class WorkRequester {


  public static void requestWork(Class<? extends Worker> worker, Context context, long startTime,
                                 Data data){

    final int delay = Math.round(startTime - System.currentTimeMillis()) / 1000;

    final WorkRequest uploadWorkRequest =
            new OneTimeWorkRequest.Builder(worker)
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .setInputData(data)
                    .build();

    WorkManager.getInstance(context).enqueue(uploadWorkRequest);

  }
}
