/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.amazonaws.iotlab.R;
import com.amazonaws.iotlab.fragment.JobFragment;
import com.amazonaws.iotlab.fragment.ConfigFragment;

import java.util.ArrayList;

public class IoTFragmentAdapter extends FragmentStateAdapter {
    private final ArrayList<Fragment> fragments;
    private final ArrayList<String> titles;

    public IoTFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);

        fragments = new ArrayList<>();
        fragments.add(new JobFragment());
        fragments.add(new ConfigFragment());

        titles = new ArrayList<>();
        titles.add(fragmentActivity.getString(R.string.job_fragment_name));
        titles.add(fragmentActivity.getString(R.string.config_fragment_name));
    }

    public String getTitle(int position) {
        return titles.get(position);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

}
