/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob;

import androidx.annotation.NonNull;

import com.amazonaws.iotlab.iotjob.model.JobDocument;
import com.amazonaws.iotlab.iotjob.model.JobStatus;

import java.util.ArrayList;

public class Job {

    private String jobId;
    private long queuedAt;
    private long lastUpdatedAt;
    private long executionNumber;
    private long versionNumber;
    private JobStatus status;
    private JobDocument jobDocument;

    public String getJobId() {
        return jobId;
    }

    public long getQueuedAt() {
        return queuedAt;
    }

    public long getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public long getExecutionNumber() {
        return executionNumber;
    }

    public long getVersionNumber() {
        return versionNumber;
    }

    public JobDocument getJobDocument() {
        return jobDocument;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public void setJobDocument(JobDocument jobDocument) {
        this.jobDocument = jobDocument;
    }

    public boolean isExist(ArrayList<Job> jobs) {
        if (jobs == null) {
            return false;
        }
        for (Job job : jobs) {
            if (job.jobId.equals(this.jobId)) {
                return true;
            }
        }
        return false;
    }

    public static Job getByJobId(ArrayList<Job> jobs, String jobId) {
        if (jobs == null) {
            return null;
        }
        for (Job job : jobs) {
            if (job.jobId.equals(jobId)) {
                return job;
            }
        }
        return null;
    }
}
