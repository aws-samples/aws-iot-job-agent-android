/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.iotlab.iotjob.Job;
import com.google.gson.annotations.Expose;

public class DescribeJobExecutionResponse extends JobResponse {
    public String jobId;

    @Expose
    public long timestamp;
    @Expose
    public Job execution;

    public DescribeJobExecutionResponse(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    public String getAcceptedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/get/accepted";
    }

    @Override
    public String getRejectedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/get/rejected";
    }
}
