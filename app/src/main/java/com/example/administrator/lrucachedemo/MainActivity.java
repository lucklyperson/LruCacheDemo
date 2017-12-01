package com.example.administrator.lrucachedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private LruCache<String, Bitmap> mMemoryCache;
    private ImageView iv;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLruCache();
        iv = (ImageView) findViewById(R.id.iv);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
    }

    //初始化
    private void initLruCache() {
        //获得可用的最大内存，使用内存超过这个值会引起oom异常（KB单位）
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //设置缓存大小为最大内存的八分之一
        int cacheSize = maxMemory / 8;
        //实例化
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    //添加到缓存中
    private void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    //从缓存中读取图片
    private Bitmap getBitmapFromCache(String key) {
        return mMemoryCache.get(key);
    }

    private void loadBitmap(int resId, ImageView imageView) {
        Bitmap bm = getBitmapFromCache(String.valueOf(resId));
        if (bm == null) {
            BitmapWokerTask task = new BitmapWokerTask();
            task.execute(resId);
            Toast.makeText(this, "第一次加载检测到内存中不存在图片的缓存,稍候请进行第二次检测", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "第二次加载检测到内存中存在图片的缓存", Toast.LENGTH_SHORT).show();
            imageView.setImageBitmap(bm);
        }

    }

    private void test() {
        loadBitmap(R.mipmap.test, iv);
    }


    private class BitmapWokerTask extends AsyncTask<Integer, Void, Bitmap> {

        //在后台加载图片
        @Override
        protected Bitmap doInBackground(Integer... integers) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), integers[0]);
            addBitmapToCache(String.valueOf(integers[0]), bitmap);
            return bitmap;
        }

    }


}
