package com.mivideo.mifm.download.support;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.Future;

public class URLConnectionWorker implements DownloadWorker, FileDownloadImpl {
    protected boolean debug = DownloadAdapter.Companion.getDebug();
    protected String url, dir, key;
    protected int requsetTimes = 0;
    protected int bufferSize = 4096;

    protected final int READ_TIME_OUT = 10 * 60 * 1000; // millis
    protected final int CONNECT_TIME_OUT = 20 * 1000; // millis

    protected Future future;
    protected boolean interrupt;
    protected InterruptReason interruptReason;
    protected DownloadError error;

    public URLConnectionWorker(String url, String dir, String key, String suffix, String md5) {
        this.url = url;
        this.dir = dir;
        this.key = key;

        this.suffix = suffix;
        this.md5 = md5;
        if (debug) {
            Log.d("FILEDOWNLOAD", "1|url|" + url + "|dir|" + dir + "|key|" + key + "|suffix|" + suffix + "|md5|" + md5);
        }
    }

    protected String suffix, md5;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getFileName() {
        return suffix == null ? key : key + "." + suffix;
    }

    protected boolean running = false;
    protected long startTimeStamp = System.currentTimeMillis();

    @Override
    public void run() {
        running = true;
        startTimeStamp = System.currentTimeMillis();
        notifyDownloadStart(key, url);
        boolean success = false;
        String savePath = null;
        RandomAccessFile randomAccessFile = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        while (requsetTimes >= 0 && requsetTimes < 3) {
            if (interrupt && interruptReason != null) {
                error = DownloadError.parse(interruptReason);
                notifyDownloadFailure(key, url, error, requsetTimes + ".interrupt for " + error.name());
                notifyDownloadClear(key, false, url, null, error);
                return;
            }

            long totalSize = -1;
            if (Patterns.WEB_URL.matcher(url).matches()) {
                totalSize = getDownloadFileSize(url);
                if (totalSize <= 0) {
                    if (requsetTimes == 0) {
                        error = DownloadError.TOTAL_SIZE_ILLEGAL;
                    }
                    requsetTimes++;
                    notifyDownloadFailure(key, url, error, "totalSize <= 0 size=" + totalSize);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            String path = isFileExist(dir, getFileName(), totalSize, md5, "check exist");
            //如果有此文件返回
            if (!TextUtils.isEmpty(path)) {
                Log.d("FILEDOWNLOAD", "file already exist --- " + key + ", " + path);
                notifyDownloadSuccess(key, url, path);
                notifyDownloadClear(key, true, url, path, null);
                return;
            }
            long completeSize = getlocalCacheFileSize(dir, getFileName());
            if (completeSize > totalSize) {
                requsetTimes++;
                error = DownloadError.COMPLETE_SIZE_ILLEGAL;
                notifyDownloadFailure(key, url, error, "completeSize > totalSize, completeSize=" + completeSize + ", totalSize=" + totalSize);
                try {
                    Thread.sleep(1000L);
                    new File(dir, getFileName()).delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                continue;
            } else if (completeSize == totalSize && completeSize > 0) {
                path = isFileExist(dir, getFileName(), totalSize, md5, "check completed local cache");
                if (!TextUtils.isEmpty(path)) {
                    notifyDownloadSuccess(key, url, path);
                    notifyDownloadClear(key, true, url, path, null);
                    return;
                } else {
                    requsetTimes++;
                    error = DownloadError.FILE_VERIFY_FAIL;
                    notifyDownloadFailure(key, url, error, "completeSize == totalSize, completeSize=" + completeSize + ", totalSize=" + totalSize + ", but verify fail");
                    try {
                        Thread.sleep(1000L);
                        new File(dir, getFileName()).delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }
            if (interrupt && interruptReason != null) {
                error = DownloadError.parse(interruptReason);
                notifyDownloadFailure(key, url, error, requsetTimes + ".interrupt for " + error.name());
                notifyDownloadClear(key, false, url, null, error);
                return;
            }
            try {
                URL downloadUrl = new URL(url);
                conn = (HttpURLConnection) downloadUrl.openConnection();
//                conn.setInstanceFollowRedirects(false);
                conn.setConnectTimeout(CONNECT_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                String range = "bytes=" + completeSize + "-" + (totalSize - 1);
                conn.setRequestProperty("Range", range);
                int responseCode = conn.getResponseCode();
                if (responseCode == 301 || responseCode == 302) {
//                    String warnning = " Illegal ResponseCode " + responseCode + " is it a hijack?";
//                    Log.d("URLC", url + warnning);
//                    ImageDownloadManager.getInstance().notifyDownloadFailure(url, warnning);
//                    throw new IllegalStateException(url + warnning);
                }
                is = conn.getInputStream();
                File target = new File(dir, getFileName());
                randomAccessFile = new RandomAccessFile(target.getAbsolutePath(), "rwd");
                randomAccessFile.seek(completeSize);
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                int origin_percent = 0;
                while ((length = is.read(buffer)) != -1) {
                    boolean runnableInterrupt = interrupt;
                    boolean threadInterrupt = Thread.interrupted();
                    if (runnableInterrupt || threadInterrupt) {
                        Log.d("FILEDOWNLOAD", "===Interrupt===|threadInterrupt|" + threadInterrupt + "|runnableInterrupt|" + runnableInterrupt + "|@|" + System.currentTimeMillis());
                        notifyDownloadCancel(key, url);
                        error = DownloadError.parse(interruptReason);
                        notifyDownloadClear(key, false, url, null, error);
                        return;
                    }
                    randomAccessFile.write(buffer, 0, length);
                    completeSize += length;
                    int progress = (int) (((double) (completeSize) / totalSize) * 100);
                    if (progress - origin_percent >= 1) {
                        origin_percent = progress;
                        notifyDownloadProgress(key, url, Math.max(0, Math.min(progress, 99)), completeSize, totalSize);
                    }
                }
                requsetTimes = -1;
                success = true;
                path = isFileExist(dir, getFileName(), totalSize, md5, "check new download");
                if (!TextUtils.isEmpty(path)) {
                    notifyDownloadSuccess(key, url, path);
                    notifyDownloadClear(key, true, url, path, null);
                    return;
                } else {
                    error = DownloadError.FILE_VERIFY_FAIL;
                    notifyDownloadFailure(key, url, error, "download complete but verify fail, should not happen");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (interrupt && interruptReason != null) {
                    error = DownloadError.parse(interruptReason);
                    notifyDownloadFailure(key, url, error, requsetTimes + ".interrupt for " + error.name());
                    notifyDownloadClear(key, false, url, null, error);
                    return;
                }
                if (e instanceof ConnectTimeoutException) {
                    error = DownloadError.CONNECTION_ERROR;
                } else if (e instanceof SocketTimeoutException) {
                    error = DownloadError.CONNECTION_ERROR;
                } else if (e instanceof SocketException) {
                    error = DownloadError.CONNECTION_ERROR;
                } else if (e instanceof FileNotFoundException) {
                    error = DownloadError.SERVER_ERROR;
                }
                else {
                    error = DownloadError.OTHER_ERROR;
                }
                notifyDownloadFailure(key, url, error, "Exception:" + e.getMessage() + "|dir|" + dir + "|filename|" + getFileName() + "|" + e.getClass().getSimpleName());
                if (requsetTimes >= 2) {
                    notifyDownloadFailure(key, url, error, "requestTimes >= 2");
                }
            } finally {
                requsetTimes++;
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d("FILEDOWNLOAD", "requsetTimes enough " + url + ", " + savePath + ", " + success);
        notifyDownloadClear(key, success, url, savePath, error);
    }

    @Override
    public long getDownloadFileSize(String inputUrl) {
        HttpURLConnection conn = null;
        long size = -1;
        try {
            URL url = new URL(inputUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            size = conn.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
            size = -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return size;
    }

    @Override
    public String isFileExist(String dir, String fileName, long targetSize) {
        File file = new File(dir, fileName);
        if (file != null && file.exists()) {
            long fileSize = file.length();
            if (targetSize == fileSize) {
                return file.getPath();
            }
        }
        return null;
    }

    protected String isFileExist(String dir, String fileName, long targetSize, String md5, String desc) {
        Log.d("FILEDOWNLOAD", "isFileExist(" + desc + ")|" + fileName + "|" + targetSize + "|" + md5);
        if (targetSize > 0 && (md5 == null || md5.length() == 0)) {
            // 大小合法但是md5缺失，只检查大小
            return isFileExist(dir, fileName, targetSize);
        } else if (targetSize > 0 && md5 != null && md5.length() > 0) {
            // 检查md5
            File file = new File(dir, fileName);
            if (file != null) {
                Log.d("FILEDOWNLOAD", fileName + " current length " + file.length());
            }
            boolean validate = DownloadUtil.Companion.verifyBinaryFile(file.getAbsolutePath(), md5);
            Log.d("FILEDOWNLOAD", "verifyBinaryFile - " + validate);
            if (validate) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public long getlocalCacheFileSize(String dir, String fileName) {
        long fileSize = 0;
        File dF = new File(dir, fileName);
        if (dF.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(dF);
                fileSize = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
                fileSize = 0;
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return fileSize;
    }

    @Override
    public void setFuture(Future future) {
        this.future = future;
    }

    @Override
    public Future getFuture() {
        return this.future;
    }

    @Override
    public void setInterrupt(InterruptReason reason) {
        this.interrupt = true;
        this.interruptReason = reason;
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public long startTimeStamp() {
        return startTimeStamp;
    }

    @Override
    public void notifyDownloadFailure(String key, String url, DownloadError error, String message) {
        DownloadManager.Companion.get().notifyDownloadFailure(key, url, error, message);
    }

    @Override
    public void notifyDownloadWaiting(String key, String url) {
        DownloadManager.Companion.get().notifyDownloadWaiting(key, url);
    }

    @Override
    public void notifyDownloadStart(String key, String url) {
        DownloadManager.Companion.get().notifyDownloadStart(key, url);
    }

    @Override
    public void notifyDownloadCancel(String key, String url) {
        DownloadManager.Companion.get().notifyDownloadCancel(key, url);
    }

    @Override
    public void notifyDownloadProgress(String key, String url, int progress, long completeSize, long totalSize) {
        DownloadManager.Companion.get().notifyDownloadProgress(key, url, progress, completeSize, totalSize);
    }

    @Override
    public void notifyDownloadSuccess(String key, String url, String path) {
        DownloadManager.Companion.get().notifyDownloadSuccess(key, url, path);
    }

    @Override
    public void notifyDownloadClear(String key, boolean success, String url, String path, DownloadError error) {
        DownloadManager.Companion.get().notifyDownloadClear(key, success, url, path, error);
    }
}