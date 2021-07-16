/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

import com.amazonaws.iotlab.iotjob.Job;

import java.util.ArrayList;

public class JobsMessage extends OtaMessage {
    public ArrayList<Job> jobs;

    public JobsMessage(OtaMessageOperation operation, ArrayList<Job> jobs) {
        super(OtaMessageType.Jobs, operation);
        this.jobs = jobs;
    }
}
