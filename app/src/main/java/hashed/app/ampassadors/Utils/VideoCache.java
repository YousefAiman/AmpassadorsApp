package hashed.app.ampassadors.Utils;

import android.content.Context;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

public class VideoCache {

    private static SimpleCache cache = null;
    private static final long maxCacheSize = 15 * 1024 * 1024;

    private VideoCache(){}

    public static boolean isNull(){
      return cache == null;
    }

    public static SimpleCache getInstance(Context context){
      new LeastRecentlyUsedCacheEvictor(maxCacheSize);

      if(cache == null){
        cache = new SimpleCache(
                new File(context.getCacheDir(), "media"),
                new LeastRecentlyUsedCacheEvictor(maxCacheSize),
                new ExoDatabaseProvider(context));
      }
      return(cache);
    }

}
