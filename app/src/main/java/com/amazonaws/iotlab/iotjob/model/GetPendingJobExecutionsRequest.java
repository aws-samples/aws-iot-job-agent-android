/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

public class GetPendingJobExecutionsRequest extends JobRequest {
    @Expose
    public String clientToken;

    public GetPendingJobExecutionsRequest(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    protected String getPublishTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "get";
    }
}
