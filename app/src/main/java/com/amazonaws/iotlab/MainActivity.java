/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.amazonaws.iotlab.adapter.IoTFragmentAdapter;
import com.amazonaws.iotlab.fragment.JobFragment;
import com.amazonaws.iotlab.message.CredentialMessage;
import com.amazonaws.iotlab.message.JobsMessage;
import com.amazonaws.iotlab.message.OtaMessage;
import com.amazonaws.iotlab.message.OtaMessageOperation;
import com.amazonaws.iotlab.message.OtaMessageType;
import com.amazonaws.iotlab.message.ResponseMessage;
import com.amazonaws.iotlab.message.SimpleMessage;
import com.amazonaws.iotlab.service.OtaService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private JobFragment jobFragment;
    private IoTFragmentAdapter mIoTFragmentAdapter;

    private IncomingHandler mHandler;
    private Messenger mService;
    private Messenger mMessenger;
    private ServiceConnection mServiceConnection;

    private static final String mClientId = "android-ota-thing";
    private static final String mEndpoint = "";
    private static final String mAccessKeyId = "";
    private static final String mAccessKeySecret = "";

    private class IncomingHandler extends Handler {

        public IncomingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            if (msg.what != OtaMessage.MSG_WHAT) {
                Log.e(TAG, "Received unsupported message");
                return;
            }

            OtaMessage otaMessage = OtaMessage.deserialize(msg);
            if (otaMessage == null) {
                return;
            }
            if (otaMessage.type == OtaMessageType.Simple) {
                handleSimple((SimpleMessage) otaMessage);
            } else if (otaMessage.type == OtaMessageType.Response) {
                handleResponse((ResponseMessage) otaMessage);
            } else if (otaMessage.type == OtaMessageType.Jobs) {
                handleJobs((JobsMessage) otaMessage);
            }
        }
    }

    public void sendOtaMessage(OtaMessage msg) {
        OtaMessage.sendMessage(mService, msg.serialize(mHandler, mMessenger));
    }

    private void handleSimple(SimpleMessage msg) {
        if (msg.operation == OtaMessageOperation.RequestCredentials) {
            CredentialMessage credentialMessage = CredentialMessage.Builder.aCredentialMessage()
                    .withClientId(mClientId)
                    .withEndpoint(mEndpoint)
                    .withAccessKeyId(mAccessKeyId)
                    .withAccessKeySecret(mAccessKeySecret)
                    .build();

            MainActivity.this.sendOtaMessage(credentialMessage);
        }
    }

    private void handleJobs(JobsMessage msg) {
        if (msg.operation == OtaMessageOperation.Notify) {
            jobFragment = (JobFragment) getSupportFragmentManager().getFragments().get(0);
            jobFragment.updateJobs(msg.jobs);
        }
    }

    private void handleResponse(ResponseMessage msg) {
        switch (msg.operation) {
            case Register:
                if (msg.isSuccess) {
                    makeToast("Registered to ota service");
                } else {
                    makeToast("Unable to register to ota service");
                }
                break;
            case Unregister:
                if (msg.isSuccess) {
                    makeToast("Unregistered from ota service");
                } else {
                    makeToast("Unable to unregister to ota service");
                }

                break;
            case SetCredentials:
                if (msg.isSuccess) {
                    makeToast("Connected to AWS IoT");
                } else {
                    makeToast("Invalid credentials");
                }
                break;
            case GetJob:
                if (!msg.isSuccess) {
                    makeToast("GetJob Failed");
                    jobFragment = (JobFragment) getSupportFragmentManager().getFragments().get(0);
                    jobFragment.updateJobs(null);
                }
        }
    }

    private void makeToast(String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 mViewPager = findViewById(R.id.view_pager);
        TabLayout mTabLayout = findViewById(R.id.tab_layout);
        mIoTFragmentAdapter = new IoTFragmentAdapter(this);
        mViewPager.setAdapter(mIoTFragmentAdapter);
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> tab.setText(mIoTFragmentAdapter.getTitle(position))).attach();

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = new Messenger(service);
                mHandler = new IncomingHandler(Looper.getMainLooper());
                mMessenger = new Messenger(mHandler);
                sendOtaMessage(new SimpleMessage(OtaMessageOperation.Register));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                mMessenger = null;
            }
        };
        bindService(new Intent(this, OtaService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            sendOtaMessage(new SimpleMessage(OtaMessageOperation.Unregister));
            unbindService(mServiceConnection);
        }
        stopService(new Intent(this, OtaService.class));
    }
}