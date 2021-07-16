/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

public final class SimpleMessage extends OtaMessage {
    public SimpleMessage(OtaMessageOperation operation) {
        super(OtaMessageType.Simple, operation);
    }
}
