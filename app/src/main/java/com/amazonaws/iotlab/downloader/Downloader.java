/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.downloader;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.amazonaws.iotlab.R;
import com.amazonaws.iotlab.codesigner.CodeSigner;
import com.amazonaws.util.IOUtils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

public class Downloader {
    private final HashMap<Long, Status> tasks = new HashMap<>();
    private final Context mContext;
    private final DownloadManager mDownloadManager;
    private final String mDownloadPath;

    private boolean mIsEnableMd5 = true;
    private boolean mIsEnableCodeSign = true;

    public enum Status {
        Invalid,
        Downloading,
        Failed,
        Succeed
    }

    public Downloader(Context context) {
        mContext = context;
        mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        mDownloadPath = mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
    }

    public void startDownloading(String url, String fileName, DownloaderStatusCallback callback, String md5, String signature, int maxRetries) {
        if (isFileExists(fileName)) {
            if (isDownloadedFileValid(fileName, md5, signature)) {
                tasks.put(0L, Status.Invalid);
                setTaskStatus(0L, Status.Succeed, callback);
                return;
            } else {
                if (!deleteFile(fileName)) {
                    tasks.put(0L, Status.Invalid);
                    setTaskStatus(0L, Status.Failed, callback);
                    return;
                }
            }
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading Updates");
        request.setNotificationVisibility(VISIBILITY_VISIBLE | VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, fileName);
        final long id = mDownloadManager.enqueue(request);
        tasks.put(id, Status.Invalid);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = mDownloadManager.query(new DownloadManager.Query().setFilterById(id));

                if (cursor != null && cursor.moveToNext()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    cursor.close();

                    switch (status) {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            if (isDownloadedFileValid(fileName, md5, signature)) {
                                setTaskStatus(id, Status.Succeed, callback);
                                timer.cancel();
                                return;
                            }
                            deleteFile(fileName);
                        case DownloadManager.STATUS_FAILED:
                            if (maxRetries > 0) {
                                tasks.remove(id);
                                startDownloading(url, fileName, callback, md5, signature, maxRetries - 1);
                            } else {
                                setTaskStatus(id, Status.Failed, callback);
                            }
                            timer.cancel();
                            break;
                        case DownloadManager.STATUS_PENDING:
                        case DownloadManager.STATUS_PAUSED:
                        case DownloadManager.STATUS_RUNNING:
                            setTaskStatus(id, Status.Downloading, callback);
                            break;
                    }
                } else {
                    // User canceled downloading
                    setTaskStatus(id, Status.Failed, callback);
                    timer.cancel();
                }
            }
        }, 0, 2000);
    }

    private void setTaskStatus(long id, Status newStatus, DownloaderStatusCallback callback) {
        Status currentStatus = tasks.get(id);
        if (currentStatus != null && currentStatus != newStatus) {
            tasks.put(id, newStatus);
            if (callback != null) {
                callback.onDownloadStatusChanged(id, newStatus);
            }
        }
    }

    private boolean isFileExists(String fileName) {
        File file = new File(mDownloadPath + fileName);
        return file.exists();
    }

    private boolean deleteFile(String fileName) {
        File file = new File(mDownloadPath + fileName);
        return file.delete();
    }

    private boolean isDownloadedFileValid(String fileName, String md5, String signature) {
        boolean isMd5Passed = !mIsEnableMd5 || md5.equals(md5(fileName));
        boolean isSignatureVerifyPassed = !mIsEnableCodeSign || isSignatureVerifyPassed(fileName, signature);
        return isMd5Passed && isSignatureVerifyPassed;
    }

    private String md5(String fileName) {
        String md5Hex = "";
        try (InputStream is = Files.newInputStream(Paths.get(mDownloadPath + fileName))) {
            md5Hex = new String(Hex.encodeHex(DigestUtils.md5(is)));
        } catch (Exception ignored) {
        }
        return md5Hex;
    }

    private boolean isSignatureVerifyPassed(String fileName, String signature) {
        try (InputStream is = Files.newInputStream(Paths.get(mDownloadPath + fileName))) {
            byte[] data = IOUtils.toByteArray(is);
            return CodeSigner.isVerifyPassed(mContext.getResources().openRawResource(R.raw.ecdsasigner),
                    data, signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setIsEnableMd5(boolean mIsEnableMd5) {
        this.mIsEnableMd5 = mIsEnableMd5;
    }

    public void setIsEnableCodeSign(boolean mIsEnableCodeSign) {
        this.mIsEnableCodeSign = mIsEnableCodeSign;
    }
}
