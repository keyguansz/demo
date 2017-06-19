package k.core.util.kil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import k.core.util.DJITextUtil;
import k.core.util.KLogUtil;
import k.core.util.KUtils;
import k.httpd.c.cons.Config;
import k.httpd.c.cons.IActionSet;

import static k.httpd.c.cons.IActionSet.Download.level;


/**
 * @author : key.guan @ 2017/6/15 17:33
 * @desc
 * @ref:
 * @better:mIsDiskLruCacheCreated不要了吧？ IO_BUFFER_SIZE局域网络很好，是不是可以把这缓存开大点？
 * @qa:DISK_CACHE_INDEX作用是啥？ Snapshot为啥不是直接文件的方式？我要拷贝文件出来怎么办？？？
 */
public final class KRawImgLoader {
    public interface CallBack{
        void onLoading(int progress);
    }
    private static final String TAG = "KRawImgLoader";
    private static final int MESSAGE_UPDATE_UI = 1;
    private static final int MSG_LOADING = 2;
    private static final int CPU_COUNT = Runtime.getRuntime()
            .availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static final ThreadFactory sThreadFactory = new ThreadFactory(){
        private final AtomicInteger mCnt = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "KRawImgLoader#"+mCnt.getAndIncrement());
        }
    };
    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),sThreadFactory
    );
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_LOADING){
                LoaderResult result = (LoaderResult)msg.obj;
                TextView textView = result.mtextView;
                String oldUrl = (String) textView.getTag(TAG_KEY_URI);
                if (result.mUri.equals(oldUrl)) {
                    textView.setText("progess="+msg.arg1);
                } else {
                    LOG_W("set image mBitmap,but url has changed, ignored!");
                }
            }
        };
    };
        
    private DiskLruCache mDiskLruCache;

    private static final int TAG_KEY_URI = 9999121;
    private static final int TAG_KEY_URI_LOading = 9999122;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX = 1;
    private boolean mIsDiskLruCacheCreated = false;

    private static KRawImgLoader ins = new KRawImgLoader();

    public static KRawImgLoader getIns() {
        return ins;
    }

    public boolean init(Context ctx) {
        ctx = ctx.getApplicationContext();
        //sdcard/Android/data/pkname/KimageLoader
        File diskCacheDir = getDiskCacheDir(ctx, TAG);
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }
    /**
     * load mBitmap from memory cache or disk cache or network async, then bind mtextView and mBitmap.
     * NOTE THAT: should run in UI Thread
     * @param uri http url
     * @param textView mBitmap's bind object
     */
  
    
    public void setTextView(final String url, final TextView textView) {
        textView.setTag(TAG_KEY_URI, url);//全局唯一的方法。
        Runnable loadTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = load(url);
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(textView, url);
                   // mMainHandler.sendMessage(mMainHandler.obtainMessage(0, result));
                    mMainHandler.obtainMessage(MESSAGE_UPDATE_UI, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadTask);
    }

    private KRawImgLoader() {
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSize() * statFs.getAvailableBlocks();
    }

    private Bitmap load(String uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        try {
            bitmap = loadFromDisk(uri,reqWidth, reqHeight);
            if (bitmap != null) {
                LOG_D("loadBitmapFromDisk,url:" + uri);
                return bitmap;
            }
            bitmap = loadFromNet(uri, reqWidth, reqHeight);
            LOG_D("loadBitmapFromHttp,url:" + uri);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            LOG_W("encounter error, DiskLruCache is not created.");
            bitmap = loadFileFromNet(uri);
        }
        return bitmap;
    }
    

    private Bitmap loadFromDisk(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.w(TAG, "load mBitmap from UI Thread, it's not recommended!");
        }
        if (mDiskLruCache == null) {
            return null;
        }

        Bitmap bitmap = null;
        String key = hashKey(url);
        return bitmap;
    }

    //http
    private Bitmap loadFromNet(String url, int reqWidth, int reqHeight) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if (mDiskLruCache == null) {//没有磁盘缓存，下载下来也没有用！
            LOG("mDiskLruCache == null");
            return null;
        }
        String key = hashKey(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downFileToStream(url, outputStream)) {
                editor.commit();
            } else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadFromDisk(url, reqWidth, reqHeight);
    }

    private Bitmap loadFromNet(String urlStr) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        Bitmap bitmap = null;
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(httpURLConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            LOG_D("Error in downloadBitmap: " + e);
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            KUtils.close(in);
        }
        return bitmap;
    }
    private Bitmap loadFileFromNet(String path,String level) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        Bitmap bitmap = null;
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream in = null;
        try {
            HashMap<String,String> parmMap = new HashMap<>(4);
            parmMap.put(IActionSet.Download.path, path);
            parmMap.put(level, level);
            final URL url = new URL(genParam(Config.SERVER_IP + IActionSet.Download.DO, parmMap));
            httpURLConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(httpURLConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            LOG_D("Error in downloadBitmap: " + e);
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            KUtils.close(in);
        }
        return bitmap;
    }
    private static String genParam(String url, HashMap<String,String> parmMap){
        StringBuilder sb = new StringBuilder(url);
        sb.append("?");
        for (String k :parmMap.keySet()){
            sb.append(k).append("=").append(parmMap.get(k)).append("&");
        }
        return sb.substring(0,sb.length()-1);
    }

    private boolean downToStream(String urlStr,
                                 OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            final URL url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int len;
            while ((len = in.read()) != -1) {
                out.write(len);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            LOG("downloadBitmap failed." + ex);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            KUtils.close(out);
            KUtils.close(in);
        }
        return false;
    }
    /**
     *@desc   带参数特殊下载方法
     *@ref:
     *@author : key.guan @ 2017/6/19 10:43
     */
    private boolean downFileToStream(String path,
                                 OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            HashMap<String,String> parmMap = new HashMap<>(4);
            parmMap.put(IActionSet.Download.path, path);
            parmMap.put(level, level);
            final URL url = new URL(genParam(Config.SERVER_IP + IActionSet.Download.DO, parmMap));
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int len;
            while ((len = in.read()) != -1) {
                mMainHandler.obtainMessage(MSG_LOADING, result).sendToTarget();
                out.write(len);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            LOG("downloadBitmap failed." + ex);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            KUtils.close(out);
            KUtils.close(in);
        }
        return false;
    }
    public static String downToStr( String urlStr )
    {
        String htmlStr = "";
        HttpURLConnection conn = null;
        InputStream inStream = null;
        boolean LoadFlagErrFlag = false;
        try
        {
            // URL url= new URL(Constant.REMOTE_ALBUM_PATH+"test2.html"); 模拟;
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setDoInput(true);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                LOG_D("http fail fail fail fail");
                conn.disconnect();
                conn = null;
                return "";
            }
            inStream = conn.getInputStream();
            int len = 1024;
            if (len <= 0){
                return "";
            }
            byte[] htmlBuffer = new byte[len];
            int offset = 0;
            int numRead = 0;
            while (offset < htmlBuffer.length&& (numRead = inStream.read(htmlBuffer, offset,htmlBuffer.length - offset)) >= 0)
            {
                offset += numRead;
            }

         //   log.d("parse response "," listHtml size = " + conn.getContentLength()+ " hasread = " + htmlBuffer.length);
            htmlStr = DJITextUtil.getString(htmlBuffer, "UTF-8");
          //  log.d(htmlStr);
          //  inStream.close();
            return htmlStr;
        } catch (IOException e)
        {
            e.printStackTrace();
        }finally {
            if(conn!=null)conn.disconnect();
            KUtils.close(inStream);

        }
      return "";
    }
/**
 *@desc  hash文件名作，后缀不变
 *@author : key.guan @ 2017/6/19 17:26
 */
    private String hashKey(String url) {
        int pos = url.lastIndexOf('.');
        String ext = url.substring(pos+1);
        url = url.substring(0, pos);
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey+"_raw."+ext;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    
    private static class LoaderResult {
        public TextView mtextView;
        public String mUri;
        public Bitmap mBitmap;

        public LoaderResult(TextView textView, String uri) {
            this.mUri = uri;
            this.mtextView = textView;
        }
    }

    public static void test() {
        LOG("" + "" + Runtime.getRuntime().maxMemory() / 1024);

    }

    public static void LOG(String log) {
        KLogUtil.E(TAG, log);
    }

    public static void LOG_D(String log) {
        KLogUtil.E(TAG, log);
    }

    public static void LOG_W(String log) {
        KLogUtil.E(TAG, log);
    }

}