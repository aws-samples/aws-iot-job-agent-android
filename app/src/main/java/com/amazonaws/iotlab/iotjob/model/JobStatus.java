/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

public enum JobStatus {
    IN_PROGRESS,
    DOWNLOADING,
    DOWNLOADED,
    APPLYING,
    FAILED,
    QUEUED,
    TIMED_OUT,
    SUCCEEDED,
    CANCELED,
    REJECTED,
    REMOVED
}
