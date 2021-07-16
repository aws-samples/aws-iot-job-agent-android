/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class UpdateJobExecutionRequest extends JobRequest {
    public String jobId;

    @Expose
    public Long executionNumber;
    @Expose
    public HashMap<String, String> statusDetails;
    @Expose
    public Boolean includeJobExecutionState;
    @Expose
    public Integer expectedVersion;
    @Expose
    public Boolean includeJobDocument;
    @Expose
    public JobStatus status;
    @Expose
    public Long stepTimeoutInMinutes;
    @Expose
    public String clientToken;

    public UpdateJobExecutionRequest(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    protected String getPublishTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/update";
    }
}
