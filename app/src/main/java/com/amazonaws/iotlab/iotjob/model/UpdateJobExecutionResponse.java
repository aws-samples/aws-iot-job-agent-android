/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

public class UpdateJobExecutionResponse extends JobResponse {
    public String jobId;
    @Expose
    public long timestamp;
    @Expose
    public JobDocument jobDocument;
    @Expose
    public JobExecutionState executionState;

    public UpdateJobExecutionResponse(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    public String getAcceptedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/update/accepted";
    }

    @Override
    public String getRejectedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/update/rejected";
    }
}
