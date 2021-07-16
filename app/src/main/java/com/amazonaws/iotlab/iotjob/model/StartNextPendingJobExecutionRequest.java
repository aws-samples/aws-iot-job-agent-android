/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

import java.util.HashMap;

public class StartNextPendingJobExecutionRequest extends JobRequest {
    @Expose
    public long stepTimeoutInMinutes;
    @Expose
    public String clientToken;
    @Expose
    public HashMap<String, String> statusDetails;

    public StartNextPendingJobExecutionRequest(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    protected String getPublishTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "start-next";
    }
}
