/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

public class StartNextPendingJobExecutionResponse extends JobResponse {
    public StartNextPendingJobExecutionResponse(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    public String getAcceptedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "start-next/accepted";
    }

    @Override
    public String getRejectedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "start-next/rejected";
    }
}
