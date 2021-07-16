/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

public enum OtaMessageOperation {
    Register,
    Unregister,
    RequestCredentials,
    SetCredentials,
    EnableMd5Check,
    DisableMd5Check,
    EnableCodeSignCheck,
    DisableCodeSignCheck,
    GetJob,
    StartJob,
    RejectJob,
    ApplyJob,
    CompleteJob,
    FailJob,
    Notify
}
