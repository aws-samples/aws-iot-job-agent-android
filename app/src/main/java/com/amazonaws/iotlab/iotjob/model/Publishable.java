/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;

public interface Publishable {
    void publish(AWSIotMqttManager mqttManager);
}
