/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.iotlab.iotjob.Job;

import java.util.ArrayList;

public class GetPendingJobExecutionsResponse extends JobResponse {

    public String clientToken;
    public long timestamp;
    public ArrayList<Job> inProgressJobs;
    public ArrayList<Job> queuedJobs;

    public GetPendingJobExecutionsResponse(JobTopic jobTopic) {
        super(jobTopic);
    }

    @Override
    public String getAcceptedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "get/accepted";
    }

    @Override
    public String getRejectedTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "get/rejected";
    }
}
