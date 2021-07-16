/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob;

import com.amazonaws.iotlab.iotjob.model.DescribeJobExecutionRequest;
import com.amazonaws.iotlab.iotjob.model.DescribeJobExecutionResponse;
import com.amazonaws.iotlab.iotjob.model.FileDescription;
import com.amazonaws.iotlab.iotjob.model.GetPendingJobExecutionsRequest;
import com.amazonaws.iotlab.iotjob.model.GetPendingJobExecutionsResponse;
import com.amazonaws.iotlab.iotjob.model.JobDocument;
import com.amazonaws.iotlab.iotjob.model.JobExecutionsChangedNotify;
import com.amazonaws.iotlab.iotjob.model.JobStatus;
import com.amazonaws.iotlab.iotjob.model.JobTopic;
import com.amazonaws.iotlab.iotjob.model.StartNextPendingJobExecutionRequest;
import com.amazonaws.iotlab.iotjob.model.StartNextPendingJobExecutionResponse;
import com.amazonaws.iotlab.iotjob.model.UpdateJobExecutionRequest;
import com.amazonaws.iotlab.iotjob.model.UpdateJobExecutionResponse;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class JobManager {
    private static final String TAG = "JobManager";
    private final JobTopic mJobTopic;
    private final JobUpdateCallback mJobUpdateCallback;
    private final JobDownloadCallback mJobDownloadCallback;
    private AWSIotMqttManager mMqttManager;

    private final ArrayList<Job> mJobs = new ArrayList<>();
    private final Gson mGson = new Gson();

    public interface JobUpdateCallback {
        void onJobsUpdate(ArrayList<Job> jobs);
    }

    public interface JobDownloadCallback {
        void onDownload(Job job);
    }

    public JobManager(AWSIotMqttManager mqttManager, String thingName,
                      JobUpdateCallback jobUpdateCallback, JobDownloadCallback jobDownloadCallback) {
        mJobTopic = new JobTopic(thingName);
        mJobUpdateCallback = jobUpdateCallback;
        mJobDownloadCallback = jobDownloadCallback;
        mMqttManager = mqttManager;
        JobExecutionsChangedNotify jobExecutionsChangedNotify = new JobExecutionsChangedNotify(mJobTopic);
        GetPendingJobExecutionsResponse getPendingJobExecutionsResponse = new GetPendingJobExecutionsResponse(mJobTopic);
        AWSIotMqttNewMessageCallback mqttNewMessageCallback = (topic, data) -> {
            String dataStr = new String(data, StandardCharsets.UTF_8);
            if (topic.equals(jobExecutionsChangedNotify.getNotifyTopic())) {
                updateJobs(mGson.fromJson(dataStr, JobExecutionsChangedNotify.class));
            } else if (topic.equals(getPendingJobExecutionsResponse.getAcceptedTopic())) {
                updateJobs(mGson.fromJson(dataStr, GetPendingJobExecutionsResponse.class));
            }
        };
        jobExecutionsChangedNotify.subscribe(mMqttManager, mqttNewMessageCallback);
        getPendingJobExecutionsResponse.subscribe(mMqttManager, mqttNewMessageCallback);
    }

    private void updateJobStatus(ArrayList<Job> jobs, JobStatus status) {
        Job existedJob;
        if (jobs == null) {
            return;
        }
        for (Job newJob : jobs) {
            newJob.setStatus(status);
            existedJob = Job.getByJobId(mJobs, newJob.getJobId());
            if (existedJob != null) {
                if ((existedJob.getStatus() == JobStatus.DOWNLOADED ||
                        existedJob.getStatus() == JobStatus.APPLYING ||
                        existedJob.getStatus() == JobStatus.DOWNLOADING) &&
                        status == JobStatus.IN_PROGRESS) {
                    return;
                }
                mJobs.remove(existedJob);
            }
            mJobs.add(newJob);
        }
    }

    private void updateJobDocument(String jobId, JobDocument document) {
        Job job = Job.getByJobId(mJobs, jobId);
        if (job == null) {
            return;
        }
        job.setJobDocument(document);
    }

    private void updateJobs(GetPendingJobExecutionsResponse response) {
        updateJobStatus(response.queuedJobs, JobStatus.QUEUED);
        updateJobStatus(response.inProgressJobs, JobStatus.IN_PROGRESS);

        for (Job job : mJobs) {
            describeJobExecution(job.getJobId());
        }

        if (mJobs.size() == 0) {
            if (mJobUpdateCallback != null) {
                mJobUpdateCallback.onJobsUpdate(mJobs);
            }
        }
    }

    private void updateJobs(JobExecutionsChangedNotify notify) {
        for (JobStatus status : JobStatus.values()) {
            updateJobStatus(notify.jobs.get(status), status);
        }

        if (mJobUpdateCallback != null) {
            mJobUpdateCallback.onJobsUpdate(mJobs);
        }
    }

    public void getPendingJobExecutions() {
        GetPendingJobExecutionsRequest request = new GetPendingJobExecutionsRequest(mJobTopic);
        request.publish(mMqttManager);
    }

    public void startNextPendingJobExecution() {
        StartNextPendingJobExecutionRequest request = new StartNextPendingJobExecutionRequest(mJobTopic);
        request.publish(mMqttManager);
    }

    public void describeJobExecution(String jobId) {
        DescribeJobExecutionRequest request = new DescribeJobExecutionRequest(mJobTopic);
        request.jobId = jobId;
        request.includeJobDocument = true;
        DescribeJobExecutionResponse describeJobExecutionResponse = new DescribeJobExecutionResponse(mJobTopic);
        describeJobExecutionResponse.jobId = jobId;
        describeJobExecutionResponse.subscribe(mMqttManager, (topic, data) -> {
            String dataStr = new String(data, StandardCharsets.UTF_8);
            if (topic.equals(describeJobExecutionResponse.getAcceptedTopic())) {
                DescribeJobExecutionResponse response = mGson.fromJson(dataStr, DescribeJobExecutionResponse.class);
                updateJobDocument(response.execution.getJobId(), response.execution.getJobDocument());
                if (mJobUpdateCallback != null) {
                    mJobUpdateCallback.onJobsUpdate(mJobs);
                }
            }
        });
        request.publish(mMqttManager);
    }

    public void updateJobExecution(String jobId, JobStatus newStatus) {
        Job job = Job.getByJobId(mJobs, jobId);
        if (job == null)
            return;
        // Local status will not update to cloud
        if (newStatus == JobStatus.DOWNLOADED ||
                newStatus == JobStatus.APPLYING ||
                newStatus == JobStatus.DOWNLOADING) {
            job.setStatus(newStatus);
            if (mJobUpdateCallback != null) {
                mJobUpdateCallback.onJobsUpdate(mJobs);
            }
            return;
        }
        UpdateJobExecutionRequest request = new UpdateJobExecutionRequest(mJobTopic);
        request.jobId = jobId;
        request.status = newStatus;
        request.includeJobDocument = true;
        request.includeJobExecutionState = true;
        UpdateJobExecutionResponse updateJobExecutionResponse = new UpdateJobExecutionResponse(mJobTopic);
        updateJobExecutionResponse.jobId = jobId;
        updateJobExecutionResponse.subscribe(mMqttManager, (topic, data) -> {
            String dataStr = new String(data, StandardCharsets.UTF_8);
            if (topic.equals(updateJobExecutionResponse.getAcceptedTopic())) {
                UpdateJobExecutionResponse response = mGson.fromJson(dataStr, UpdateJobExecutionResponse.class);
                job.setStatus(response.executionState.status);
                job.setJobDocument(response.jobDocument);
                if (mJobUpdateCallback != null) {
                    mJobUpdateCallback.onJobsUpdate(mJobs);
                }
                if (mJobDownloadCallback != null && job.getStatus() == JobStatus.IN_PROGRESS) {
                    mJobDownloadCallback.onDownload(job);
                }
            }
        });

        request.publish(mMqttManager);
    }

    public ArrayList<Job> getJobs() {
        return mJobs;
    }

    public void checkJobDownloadStatus(Job job) {
        for (FileDescription file : job.getJobDocument().files) {
            if (!file.isDownloaded)
                return;
        }
        updateJobExecution(job.getJobId(), JobStatus.DOWNLOADED);
    }
}