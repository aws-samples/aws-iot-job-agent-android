/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.iotlab.iotjob.Job;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JobExecutionsChangedNotify implements Subscribable {
    public JobTopic jobTopic;
    public long timestamp;
    public Map<JobStatus, ArrayList<Job>> jobs;

    public JobExecutionsChangedNotify(JobTopic jobTopic) {
        this.jobTopic = jobTopic;
    }

    public String getNotifyTopic() {
        return jobTopic.JOB_TOPIC_PREFIX + "notify";
    }

    @Override
    public void subscribe(AWSIotMqttManager mqttManager, AWSIotMqttNewMessageCallback newMessageCallback) {
        mqttManager.subscribeToTopic(getNotifyTopic(), AWSIotMqttQos.QOS1, newMessageCallback);
    }
}
