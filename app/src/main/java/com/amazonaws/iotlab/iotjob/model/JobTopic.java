/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

public class JobTopic {
    public final String JOB_TOPIC_PREFIX;
    public final String THING_NAME;

    public JobTopic(String thingName) {
        this.THING_NAME = thingName;
        this.JOB_TOPIC_PREFIX = "$aws/things/" + THING_NAME + "/jobs/";
    }
}
