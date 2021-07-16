/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.iotlab.downloader.Downloader;
import com.amazonaws.iotlab.iotjob.Job;
import com.amazonaws.iotlab.iotjob.JobManager;
import com.amazonaws.iotlab.iotjob.model.FileDescription;
import com.amazonaws.iotlab.iotjob.model.JobStatus;
import com.amazonaws.iotlab.message.CredentialMessage;
import com.amazonaws.iotlab.message.JobsMessage;
import com.amazonaws.iotlab.message.OtaMessage;
import com.amazonaws.iotlab.message.OtaMessageOperation;
import com.amazonaws.iotlab.message.OtaMessageType;
import com.amazonaws.iotlab.message.ResponseMessage;
import com.amazonaws.iotlab.message.SimpleMessage;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OtaService extends Service {
    private static final String TAG = "Lab OTA Service";
    private static final int MAX_RECONNECT_TIMES = 2;

    private IncomingHandler mHandler;
    private Messenger mMessenger;
    private List<Messenger> mClients;

    private AWSCredentialsProvider mCredentialProvider;
    private AWSIotMqttManager mMqttManager;
    private boolean isMqttConnected = false;
    private String mClientId;
    private int mReconnectTimes = 0;
    private JobManager mJobManager;
    private Downloader mDownloader;

    private class IncomingHandler extends Handler {

        public IncomingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage");
            if (msg.what != OtaMessage.MSG_WHAT) {
                Log.e(TAG, "Received unsupported message");
                return;
            }
            OtaMessage otaMessage = OtaMessage.deserialize(msg);
            if (otaMessage == null) {
                return;
            }
            if (otaMessage.type == OtaMessageType.Simple) {
                handleSimple(msg.replyTo, (SimpleMessage) otaMessage);
            } else if (otaMessage.type == OtaMessageType.Credentials) {
                handleCredentials((CredentialMessage) otaMessage);
            } else if (otaMessage.type == OtaMessageType.Jobs) {
                handleJobs(msg.replyTo, (JobsMessage) otaMessage);
            }
        }
    }

    private void handleSimple(Messenger client, SimpleMessage msg) {
        switch (msg.operation) {
            case Register:
                registerClient(client);
                if (mClients.size() == 1 && !isMqttConnected) {
                    requestCredentials(client);
                }
                break;
            case Unregister:
                unregisterClient(client);
                break;
            case GetJob:
                getJobs(client);
            case EnableMd5Check:
                setMd5Check(client, true);
                break;
            case DisableMd5Check:
                setMd5Check(client, false);
                break;
            case EnableCodeSignCheck:
                setCodeSignCheck(client, true);
                break;
            case DisableCodeSignCheck:
                setCodeSignCheck(client, false);
                break;
        }
    }

    private void handleJobs(Messenger client, JobsMessage msg) {
        switch (msg.operation) {
            case StartJob:
                startJobs(client, msg.jobs);
                break;
            case ApplyJob:
                applyJobs(client, msg.jobs);
                break;
            case CompleteJob:
                completeJobs(client, msg.jobs);
            case RejectJob:
                rejectJobs(client, msg.jobs);
            case FailJob:
                failJobs(client, msg.jobs);
                break;
        }
    }

    private void handleCredentials(CredentialMessage msg) {
        setCredentials(msg);
        connectToIoT();
    }

    private void registerClient(Messenger client) {
        if (client == null)
            return;

        mClients.add(client);
        sendResponse(client, OtaMessageOperation.Register, true);
    }

    private void unregisterClient(Messenger client) {
        if (client == null)
            return;

        mClients.remove(client);
        sendResponse(client, OtaMessageOperation.Unregister, true);
    }


    private void requestCredentials(Messenger client) {
        OtaMessage.sendMessage(client,
                new SimpleMessage(OtaMessageOperation.RequestCredentials)
                        .serialize(mHandler, null));
    }

    private void setCredentials(CredentialMessage msg) {
        AWSCredentials credentials;

        if (msg.sessionToken.equals("")) {
            credentials = new BasicAWSCredentials(msg.accessKeyId, msg.accessKeySecret);
        } else {
            credentials = new BasicSessionCredentials(msg.accessKeyId, msg.accessKeySecret, msg.sessionToken);
        }

        mCredentialProvider = new StaticCredentialsProvider(credentials);
        mMqttManager = new AWSIotMqttManager(msg.clientId, msg.endpoint);
        mClientId = msg.clientId;
    }

    private void getJobs(Messenger client) {
        if (isMqttConnected) {
            mJobManager.getPendingJobExecutions();
            sendResponse(client, OtaMessageOperation.GetJob, true);
        } else {
            sendResponse(client, OtaMessageOperation.GetJob, false);
        }
    }

    private void startJobs(Messenger client, ArrayList<Job> jobs) {
        if (isMqttConnected) {
            for (Job job : jobs) {
                mJobManager.updateJobExecution(job.getJobId(), JobStatus.IN_PROGRESS);
            }
            sendResponse(client, OtaMessageOperation.StartJob, true);
        } else {
            sendResponse(client, OtaMessageOperation.StartJob, false);
        }
    }

    private void applyJobs(Messenger client, ArrayList<Job> jobs) {
        for (Job job : jobs) {
            mJobManager.updateJobExecution(job.getJobId(), JobStatus.APPLYING);
        }
        sendResponse(client, OtaMessageOperation.ApplyJob, true);
    }

    private void completeJobs(Messenger client, ArrayList<Job> jobs) {
        if (isMqttConnected) {
            for (Job job : jobs) {
                mJobManager.updateJobExecution(job.getJobId(), JobStatus.SUCCEEDED);
            }
            sendResponse(client, OtaMessageOperation.CompleteJob, true);
        } else {
            sendResponse(client, OtaMessageOperation.CompleteJob, false);
        }
    }

    private void rejectJobs(Messenger client, ArrayList<Job> jobs) {
        if (isMqttConnected) {
            for (Job job : jobs) {
                mJobManager.updateJobExecution(job.getJobId(), JobStatus.REJECTED);
            }
            sendResponse(client, OtaMessageOperation.RejectJob, true);
        } else {
            sendResponse(client, OtaMessageOperation.RejectJob, false);
        }
    }

    private void failJobs(Messenger client, ArrayList<Job> jobs) {
        if (isMqttConnected) {
            for (Job job : jobs) {
                mJobManager.updateJobExecution(job.getJobId(), JobStatus.FAILED);
            }
            sendResponse(client, OtaMessageOperation.FailJob, true);
        } else {
            sendResponse(client, OtaMessageOperation.FailJob, false);
        }
    }

    private void setMd5Check(Messenger client, boolean isEnabled) {
        mDownloader.setIsEnableMd5(isEnabled);
        if (isEnabled) {
            sendResponse(client, OtaMessageOperation.EnableMd5Check, true);
        } else {
            sendResponse(client, OtaMessageOperation.DisableMd5Check, true);
        }
    }

    private void setCodeSignCheck(Messenger client, boolean isEnabled) {
        mDownloader.setIsEnableCodeSign(isEnabled);
        if (isEnabled) {
            sendResponse(client, OtaMessageOperation.EnableCodeSignCheck, true);
        } else {
            sendResponse(client, OtaMessageOperation.DisableCodeSignCheck, true);
        }
    }

    private void sendResponse(Messenger client, OtaMessageOperation operation, boolean isSuccess) {
        OtaMessage.sendMessage(client,
                new ResponseMessage(operation, isSuccess)
                        .serialize(mHandler, null));
    }

    private void sendJobsMessage(Messenger client, ArrayList<Job> jobs) {
        OtaMessage.sendMessage(client,
                new JobsMessage(OtaMessageOperation.Notify, jobs)
                        .serialize(mHandler, null));
    }

    private void initJobManager() {
        if (mJobManager == null) {
            mJobManager = new JobManager(mMqttManager, mClientId, jobs -> {
                for (Messenger client : mClients) {
                    sendJobsMessage(client, jobs);
                }
            }, (job) -> {
                for (FileDescription file : job.getJobDocument().files) {
                    mDownloader.startDownloading(file.fileSource.url, file.fileName, (id, newStatus) -> {
                        if (newStatus == Downloader.Status.Succeed) {
                            file.isDownloaded = true;
                            mJobManager.checkJobDownloadStatus(job);
                        } else if (newStatus == Downloader.Status.Failed) {
                            mJobManager.updateJobExecution(job.getJobId(), JobStatus.FAILED);
                        } else if (newStatus == Downloader.Status.Downloading) {
                            mJobManager.updateJobExecution(job.getJobId(), JobStatus.DOWNLOADING);
                        }
                    }, file.md5, file.signature, 3);
                }
            });
        }
    }

    private void connectToIoT() {
        mReconnectTimes = 0;
        mMqttManager.connect(mCredentialProvider, (status, throwable) -> {
            switch (status) {
                case Connected:
                    isMqttConnected = true;
                    if (mClients.size() != 0) {
                        sendResponse(mClients.get(0), OtaMessageOperation.SetCredentials, true);
                    }
                    mReconnectTimes = 0;
                    initJobManager();
                    break;
                case Connecting:
                    isMqttConnected = false;
                    Log.i(TAG, "MQTT Connecting");
                    break;
                case Reconnecting:
                    isMqttConnected = false;
                    Log.i(TAG, "MQTT Reconnecting");
                    mReconnectTimes++;
                    if (mReconnectTimes >= MAX_RECONNECT_TIMES) {
                        mMqttManager.disconnect();
                        if (mClients.size() != 0) {
                            sendResponse(mClients.get(0), OtaMessageOperation.SetCredentials, false);
                            requestCredentials(mClients.get(0));
                        }
                    }
                    break;
                default:
                    isMqttConnected = false;
                    Log.e(TAG, "MQTT connection error: " + status);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new IncomingHandler(Looper.getMainLooper());
        mMessenger = new Messenger(mHandler);
        mClients = Collections.synchronizedList(new ArrayList<>());
        mDownloader = new Downloader(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}