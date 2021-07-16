/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

public class OtaMessage {
    public static int MSG_WHAT = 0x15000;
    protected static String MSG_BUNDLE_KEY = "OtaMessage";

    public final OtaMessageType type;
    public final OtaMessageOperation operation;

    public OtaMessage(OtaMessageType type, OtaMessageOperation operation) {
        this.type = type;
        this.operation = operation;
    }

    protected Bundle createBundle() {
        Bundle bundle = new Bundle();

        bundle.putString(MSG_BUNDLE_KEY, new Gson().toJson(this));
        return bundle;
    }

    public Message serialize(@Nullable Handler handler, @Nullable Messenger replyTo) {
        Message msg = Message.obtain(handler, OtaMessage.MSG_WHAT);
        msg.replyTo = replyTo;
        msg.setData(createBundle());
        return msg;
    }

    public static OtaMessage deserialize(Message msg) {
        if (msg.what != MSG_WHAT) {
            return null;
        }

        Bundle bundle = msg.getData();

        if (bundle == null) {
            return null;
        }
        OtaMessage otaMessage = new Gson().fromJson(bundle.getString(MSG_BUNDLE_KEY), OtaMessage.class);

        if (otaMessage.type == OtaMessageType.Simple)
            return new Gson().fromJson(bundle.getString(MSG_BUNDLE_KEY), SimpleMessage.class);

        if (otaMessage.type == OtaMessageType.Response)
            return new Gson().fromJson(bundle.getString(MSG_BUNDLE_KEY), ResponseMessage.class);

        if (otaMessage.type == OtaMessageType.Credentials)
            return new Gson().fromJson(bundle.getString(MSG_BUNDLE_KEY), CredentialMessage.class);

        if (otaMessage.type == OtaMessageType.Jobs)
            return new Gson().fromJson(bundle.getString(MSG_BUNDLE_KEY), JobsMessage.class);

        return otaMessage;
    }

    public static void sendMessage(Messenger messenger, Message msg) {
        try {
            messenger.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
