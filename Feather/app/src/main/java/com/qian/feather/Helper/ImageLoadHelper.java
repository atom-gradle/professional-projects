package com.qian.feather.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageLoadHelper {
    private LruCache<String, Bitmap> memoryCache;
    private ThreadPoolExecutor executor;

    public ImageLoadHelper(Context context) {
        // 初始化内存缓存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        // 初始化线程池
        int cpuCount = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(
                cpuCount + 1, cpuCount * 2 + 1,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(128));
    }

    public void displayImage(final String imagePath, final ImageView imageView,
                             final int reqWidth, final int reqHeight) {
        final String cacheKey = imagePath + "_" + reqWidth + "x" + reqHeight;
        imageView.setTag(cacheKey);

        // 先从内存缓存中获取
        //Bitmap bitmap = getBitmapFromMemCache(cacheKey);
        Bitmap bitmap = null;
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        // 异步加载
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 压缩图片
                Bitmap bitmap = compressImage(imagePath, reqWidth, reqHeight);

                // 缓存图片
                if (bitmap != null) {
                    //addBitmapToMemoryCache(cacheKey, bitmap);
                }

                // 更新UI
                imageView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (imageView.getTag().equals(cacheKey)) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        });
    }

    private Bitmap compressImage(String imagePath, int reqWidth, int reqHeight) {
        // 1. 获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // 2. 计算采样率
        //options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 3. 加载压缩后的图片
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeFile(imagePath, options);
    }

}