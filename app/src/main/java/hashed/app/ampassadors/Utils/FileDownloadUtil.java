package hashed.app.ampassadors.Utils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import hashed.app.ampassadors.R;

public class FileDownloadUtil {

  private final Context context;
  private final ImageView attachmentImage;
  private final String url,fileName;

  public FileDownloadUtil(Context context,
                          String url, String fileName,@Nullable ImageView attachmentImage){
    this.context = context;
    this.attachmentImage = attachmentImage;
    this.url = url;
    this.fileName = fileName;
  }

  public void showDownloadAlert(){

    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
    alertDialogBuilder.setMessage(context.getString(R.string.DownLoad_Asking));

    alertDialogBuilder.setPositiveButton(R.string.YES, (dialogInterface, i) -> {
      downloadFile();
    });

    alertDialogBuilder.setNegativeButton(R.string.No, (dialogInterface, i) -> {
      dialogInterface.dismiss();
    });

    alertDialogBuilder.show();

  }


  private void downloadFile() {

    DownloadManager.Request request;

    request = new DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription(context.getString(R.string.Download))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      request.setRequiresCharging(false);
    }

    DownloadManager downloadManager = (DownloadManager)
            context.getSystemService(Context.DOWNLOAD_SERVICE);

    request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,
            fileName);

    long downloadId = downloadManager.enqueue(request);

    if(attachmentImage!=null){
      attachmentImage.setImageResource(R.drawable.cancel_icon);
      attachmentImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          downloadManager.remove(downloadId);
          attachmentImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              showDownloadAlert();
            }
          });
        }
      });
    }

  }


}
