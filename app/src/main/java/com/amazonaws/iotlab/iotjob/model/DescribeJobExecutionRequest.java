/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

public class DescribeJobExecutionRequest extends JobRequest {
    public String jobId;

    @Expose
    public Long executionNumber;
    @Expose
    public Boolean includeJobDocument;
    @Expose
    public String clientToken;

    public DescribeJobExecutionRequest(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    protected String getPublishTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + jobId + "/get";
    }
}
