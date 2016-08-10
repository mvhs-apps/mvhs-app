package net.mvla.mvhs.schedulecalendar.cache;

import android.content.Context;
import android.util.LruCache;

import com.jakewharton.disklrucache.DiskLruCache;

import net.mvla.mvhs.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DiskMemoryCache<T> {

    private LruCache<String, T> memoryCache;
    private String name;
    private Mapper<T> mapper;
    private DiskLruCache diskCache;
    private Context context;
    private int maxDiskBytes;

    public DiskMemoryCache(Context context, int maxDiskBytes, int maxMemoryItems, String name, Mapper<T> mapper) {
        this.context = context;
        this.maxDiskBytes = maxDiskBytes;
        this.name = name;
        this.mapper = mapper;

        memoryCache = new LruCache<>(maxMemoryItems);

        try {
            diskCache = initCache(context, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DiskLruCache initCache(Context context, String fileName) throws IOException {
        File cacheFile = new File(context.getCacheDir().getCanonicalPath() + File.separator + fileName);
        return DiskLruCache.open(cacheFile, BuildConfig.VERSION_CODE, 1, maxDiskBytes);
    }

    public void put(String key, T item) {
        memoryCache.put(key, item);
        try {
            DiskLruCache.Editor edit = diskCache.edit(key);
            OutputStream out = edit.newOutputStream(0);
            mapper.toStream(out, item);
            out.close();
            edit.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Observable<T> get(final String key, Func1<T, Boolean> filter) {
        Single<T> memory = Single.just(memoryCache.get(key));

        final Single<T> disk = Single.fromCallable(() -> {
            DiskLruCache.Snapshot snapshot = diskCache.get(key);

            InputStream inputStream = snapshot.getInputStream(0);
            T t = mapper.fromStream(inputStream);
            inputStream.close();
            return t;
        }).doOnSuccess(object -> memoryCache.put(key, object)).onErrorReturn(throwable -> null);

        return Single.concat(memory, disk)
                .subscribeOn(Schedulers.io())
                .takeFirst(filter);
    }

    public interface Mapper<T> {
        T fromStream(InputStream inputStream) throws IOException;

        void toStream(OutputStream outputStream, T item) throws IOException;
    }
}
