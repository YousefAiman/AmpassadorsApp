package hashed.app.ampassadors.Utils;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import hashed.app.ampassadors.R;

public class VideoDataSourceFactory implements DataSource.Factory {
  private static final long maxFileSize = 100 * 1024 * 1024,
          maxCacheSize = 15 * 1024 * 1024;
  private final Context context;
  private final DefaultDataSourceFactory defaultDatasourceFactory;


  public VideoDataSourceFactory(Context context) {
    super();
    this.context = context;

    String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));

    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();

    defaultDatasourceFactory = new DefaultDataSourceFactory(this.context,
            bandwidthMeter,
            new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
  }

  public static void clearVideoCache(Context context) {

    Log.d("exoPlayerPlayback", "clearing video cache");

    try {
      File dir = new File(context.getCacheDir(), "media");
      deleteDir(dir);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static boolean deleteDir(File dir) {
    Log.d("exoPlayerPlayback", "trying to delete dir");
    if (dir != null && dir.isDirectory()) {
      Log.d("exoPlayerPlayback", "dir != null && dir.isDirectory()");
      final String[] children = dir.list();
      if (children != null) {
        for (String child : children) {
          Log.d("exoPlayerPlayback", "found child: " + child);
          boolean success = deleteDir(new File(dir, child));
          if (!success) {
            Log.d("exoPlayerPlayback", "failed to deleted: " + child);
            return false;
          }
        }
      }
      return dir.delete();
    } else if (dir != null && dir.isFile()) {
      Log.d("exoPlayerPlayback", "dir!= null && dir.isFile()");
      return dir.delete();
    } else {
      return false;
    }
  }

  @Override
  public DataSource createDataSource() {

    return new CacheDataSource(
            VideoCache.getInstance(context),
            defaultDatasourceFactory.createDataSource(),
            new FileDataSource(),
            new CacheDataSink(VideoCache.getInstance(context), maxFileSize),
            CacheDataSource.FLAG_BLOCK_ON_CACHE |
                    CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
  }
}
