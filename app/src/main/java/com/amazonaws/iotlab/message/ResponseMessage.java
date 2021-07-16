/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

public final class ResponseMessage extends OtaMessage {
    public final boolean isSuccess;

    public ResponseMessage(OtaMessageOperation operation, boolean isSuccess) {
        super(OtaMessageType.Response, operation);
        this.isSuccess = isSuccess;
    }
}
