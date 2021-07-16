/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

public abstract class JobResponse implements Subscribable {
    public final JobTopic jobTopic;

    public JobResponse(JobTopic jobTopic) {
        this.jobTopic = jobTopic;
    }

    public void subscribe(AWSIotMqttManager mqttManager, AWSIotMqttNewMessageCallback newMessageCallback) {
        mqttManager.subscribeToTopic(getAcceptedTopic(), AWSIotMqttQos.QOS1, newMessageCallback);
        mqttManager.subscribeToTopic(getRejectedTopic(), AWSIotMqttQos.QOS1, newMessageCallback);
    }

    public abstract String getAcceptedTopic();

    public abstract String getRejectedTopic();
}
