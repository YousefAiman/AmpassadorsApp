package hashed.app.ampassadors.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Files {

  public static final int
          TEXT = 1,
          IMAGE = 2,
          AUDIO = 3,
          VIDEO = 4,
          DOCUMENT = 5,
          ZOOM = 6;


  public final static int EXTERNAL_STORAGE_PERMISSION = 1,
          EXTERNAL_STORAGE_WRITE_PERMISSION = 2,
          PICK_IMAGE = 10,
          PICK_VIDEO = 11,
          PICK_FILE = 12,
          WRITE_FILE = 13;

  public final static String MESSAGE_IMAGE_REF = "Messaging-images/",
          MESSAGE_RECORD_REF = "Messaging-recordings/",
          MESSAGE_VIDEO_REF = "Messaging-videos/",
          MESSAGE_DOCUMENT_REF = "Messaging-documents/",
          POST_IMAGE_REF = "Post-images/",
          POST_VIDEO_REF = "Post-videos/",
          POST_THUMBNAIL_REF = "Post-thumbnails/",
          POST_DOCUMENT_REF = "Post-documents/";


  public final static int MAX_VIDEO_LENGTH = 30;
  public final static int MAX_FILE_SIZE = 5;

  private final static String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
          Manifest.permission.WRITE_EXTERNAL_STORAGE};

  private static final String[] supportedMimeTypes = {"application/pdf", "application/msword",
          "text/*", "application/mspowerpoint", "application/vnd.ms-excel", "application/zip"};

  public static void startImageFetchIntent(Activity activity) {

    if (checkStoragePermissions(activity, IMAGE)) {

      final Intent i = new Intent(Intent.ACTION_GET_CONTENT);
      i.setType("image/*");
      activity.startActivityForResult(Intent.createChooser(i, "Select Image"), PICK_IMAGE);

    }

  }


  public static void startDocumentFetchIntent(Activity activity) {

    if (checkStoragePermissions(activity, PICK_FILE)) {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.setType("*/*");
      intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes);
      intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

      activity.startActivityForResult(intent, PICK_FILE);
    }
  }


  public static void startVideoFetchIntent(Activity activity) {

    if (checkStoragePermissions(activity, PICK_VIDEO)) {

      Intent intent;
      if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
      } else {
        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI);
      }


      intent.setType("video/*");
      intent.setAction(Intent.ACTION_GET_CONTENT);
      intent.putExtra("return-data", true);
      intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

      activity.startActivityForResult(intent, PICK_VIDEO);


//      activity.startActivityForResult(Intent.createChooser(
//              new Intent(Intent.ACTION_GET_CONTENT)
//                      .setType("video/*")
//                      .addCategory(Intent.CATEGORY_OPENABLE),"Select Video"), PICK_VIDEO);
    }

  }

  private static boolean checkStoragePermissions(Activity activity, int requestCode) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

      if (activity.checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          //doesn't need to request write persmission
          activity.requestPermissions(new String[]{permissions[0]}, requestCode);
        } else {
          //needs to request write persmission
          activity.requestPermissions(permissions, requestCode);

        }

        return false;
      } else {
        return true;
      }

    }
    return true;
  }

  public static boolean checkStorageWritePermission(Activity activity) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            activity.checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED) {
      Log.d("ttt", "requesting location persmission");


      activity.requestPermissions(permissions, EXTERNAL_STORAGE_WRITE_PERMISSION);

      return false;
    }

    return true;
  }


  public static boolean isFromGooglePhotos(Uri uri) {
    return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
  }


  private static Cursor getFileCursor(Context context, Uri fileUri) {

    final ContentResolver contentResolver = context.getContentResolver();


    return contentResolver.query(fileUri
            , null,
            null,
            null,
            null);
  }


  public static double getFileSizeInMB(Context context, Uri uri) {
    String fileSize;
    try (Cursor cursor = getFileCursor(context, uri)) {
      if (cursor != null && cursor.moveToFirst()) {
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        if (!cursor.isNull(sizeIndex)) {
          fileSize = cursor.getString(sizeIndex);
          cursor.close();
          return Long.parseLong(fileSize) / 1e+6;
        }
      }
    }
    return 0;
  }

  public static Map<String, Object> getFileInfo(Context context, Uri fileUri) {


    final Map<String, Object> fileInfoMap = new HashMap<>();

    try (Cursor cursor = getFileCursor(context, fileUri)) {
      if (cursor != null && cursor.moveToFirst()) {

        final int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        final int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        final String fileType = context.getContentResolver().getType(fileUri);


        if (!cursor.isNull(sizeIndex) &&
                !cursor.isNull(nameIndex)) {

          final String fileName = cursor.getString(nameIndex);
          final long fileSize = cursor.getLong(sizeIndex);

          fileInfoMap.put("fileName", fileName);
          fileInfoMap.put("fileSize", fileSize / 1e+6);
          fileInfoMap.put("fileType", fileType);


          cursor.close();
        }
      }
    }


    return fileInfoMap;
  }


  public static FileDownloadTask downloadFile(Activity activity,
                                              String attachmentUrl, String fileName) {

    if (checkStoragePermissions(activity, WRITE_FILE)) {

      File filePath = new File(Environment.DIRECTORY_DOWNLOADS, fileName);

      boolean fileWasCreated;
      if (!filePath.exists()) {
        fileWasCreated = filePath.mkdirs();
      } else {
        fileWasCreated = true;
      }

      if (fileWasCreated) {
        return FirebaseStorage.getInstance().getReferenceFromUrl(attachmentUrl).getFile(filePath);
      } else {
        return null;
      }
    }

    return null;
  }


  public static Intent getFileLaunchIntentFromUri(String uriPath) {

    Log.d("ttt", "uriString: " + uriPath);

    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

    final Uri launchUri = Uri.fromFile(new File(uriPath));

    if (uriPath.contains(".doc") || uriPath.contains(".docx")) {
      intent.setDataAndType(launchUri, "application/msword");
    } else if (uriPath.contains(".pdf")) {
      intent.setDataAndType(launchUri, "application/pdf");
    } else if (uriPath.contains(".ppt") || uriPath.contains(".pptx")) {
      intent.setDataAndType(launchUri, "application/vnd.ms-powerpoint");
    } else if (uriPath.contains(".xls") || uriPath.contains(".xlsx")) {
      intent.setDataAndType(launchUri, "application/vnd.ms-excel");
    } else if (uriPath.contains(".zip") || uriPath.contains(".rar")) {
      intent.setDataAndType(launchUri, "application/x-wav");
    }

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    return intent;
  }
}
