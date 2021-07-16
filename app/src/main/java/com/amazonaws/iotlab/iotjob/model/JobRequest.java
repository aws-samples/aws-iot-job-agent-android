/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JobRequest implements Publishable {
    protected final JobTopic jobTopic;
    protected final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public JobRequest(JobTopic jobTopic) {
        this.jobTopic = jobTopic;
    }

    protected abstract String getPublishTopic();

    @Override
    public void publish(AWSIotMqttManager mqttManager) {
        mqttManager.publishString(gson.toJson(this),
                getPublishTopic(), AWSIotMqttQos.QOS1);
    }
}
