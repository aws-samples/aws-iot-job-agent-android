/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.iotlab.MainActivity;
import com.amazonaws.iotlab.R;
import com.amazonaws.iotlab.message.OtaMessage;
import com.amazonaws.iotlab.message.OtaMessageOperation;
import com.amazonaws.iotlab.message.SimpleMessage;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfigFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SwitchMaterial switchMd5 = view.findViewById(R.id.switchMd5);
        SwitchMaterial switchCodeSign = view.findViewById(R.id.switchCodeSign);
        switchMd5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sendOtaMessage(new SimpleMessage(OtaMessageOperation.EnableMd5Check));
            } else {
                sendOtaMessage(new SimpleMessage(OtaMessageOperation.DisableMd5Check));
            }
        });
        switchCodeSign.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                sendOtaMessage(new SimpleMessage(OtaMessageOperation.EnableCodeSignCheck));
            } else {
                sendOtaMessage(new SimpleMessage(OtaMessageOperation.DisableCodeSignCheck));
            }
        });
    }

    private void sendOtaMessage(OtaMessage msg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.sendOtaMessage(msg);
        }
    }
}