package hashed.app.ampassadors.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import hashed.app.ampassadors.Adapters.PrivateMessagingAdapter;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.app.ActivityCompat.startActivityForResult;

public class Files {

  public static final int
          TEXT = 1,
          IMAGE = 2,
          AUDIO = 3,
          VIDEO = 4,
          FILE = 5;


  public final static int EXTERNAL_STORAGE_PERMISSION = 1,
                            PICK_IMAGE = 10;

  public final static String MESSAGE_IMAGE_REF = "Messaging-images/";

  final static String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

  private final Activity activity;

  public Files(Activity activity){
    this.activity = activity;
  }

  public void startImageFetchIntent(){

    if(checkStoragePermissions()){

      final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
      i.setType("image/*");
      activity.startActivityForResult(Intent.createChooser(i, "Select Image"), PICK_IMAGE);

    }

  }


  private boolean checkStoragePermissions(){

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            activity.checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

      Log.d("ttt", "requesting location persmission");

      activity.requestPermissions(permissions, EXTERNAL_STORAGE_PERMISSION);

     return false;
    }

    return true;
  }


}
