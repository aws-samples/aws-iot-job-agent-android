/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.BlendModeColorFilterCompat;
import androidx.core.graphics.BlendModeCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amazonaws.iotlab.MainActivity;
import com.amazonaws.iotlab.R;
import com.amazonaws.iotlab.message.JobsMessage;
import com.amazonaws.iotlab.message.OtaMessage;
import com.amazonaws.iotlab.message.OtaMessageOperation;
import com.amazonaws.iotlab.message.SimpleMessage;
import com.amazonaws.iotlab.iotjob.Job;
import com.amazonaws.iotlab.iotjob.model.JobStatus;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class JobFragment extends Fragment {
    private ArrayList<Job> mJobs;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private JobRecyclerViewAdapter mJobRecyclerViewAdapter;

    public void updateJobs(@Nullable ArrayList<Job> jobs) {
        if (jobs != null) {
            mJobs.clear();
            mJobs.addAll(jobs);
            mJobRecyclerViewAdapter.notifyDataSetChanged();
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void sendOtaMessage(OtaMessage msg) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.sendOtaMessage(msg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_job, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mJobs = new ArrayList<>();
        RecyclerView mJobsRecyclerView = view.findViewById(R.id.jobsRecyclerView);
        mJobsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mJobRecyclerViewAdapter = new JobRecyclerViewAdapter();
        mJobsRecyclerView.setAdapter(mJobRecyclerViewAdapter);
        mJobsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mSwipeRefreshLayout = view.findViewById(R.id.jobs);
        mSwipeRefreshLayout.setOnRefreshListener(() -> sendOtaMessage(new SimpleMessage(OtaMessageOperation.GetJob)));
    }

    private class JobRecyclerViewAdapter extends RecyclerView.Adapter<JobRecyclerViewAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView jobIdTextView;
            TextView jobStatusTextView;
            ImageButton menuButton;
            int index;

            public ViewHolder(View view) {
                super(view);
                jobIdTextView = view.findViewById(R.id.jobNameTextView);
                jobStatusTextView = view.findViewById(R.id.jobStatusTextView);
                menuButton = view.findViewById(R.id.menuButton);
                menuButton.setOnClickListener(this::showMenu);
            }

            private void showMenu(View v) {
                PopupMenu menu = new PopupMenu(getContext(), v);
                menu.inflate(R.menu.menu);
                switch (mJobs.get(index).getStatus()) {
                    case QUEUED:
                        menu.getMenu().findItem(R.id.startJob).setVisible(true);
                        menu.getMenu().findItem(R.id.rejectJob).setVisible(true);
                        break;
                    case IN_PROGRESS:
                    case DOWNLOADING:
                        menu.getMenu().findItem(R.id.startJob).setVisible(true);
                        menu.getMenu().findItem(R.id.failJob).setVisible(true);
                        break;
                    case DOWNLOADED:
                        menu.getMenu().findItem(R.id.applyJob).setVisible(true);
                        menu.getMenu().findItem(R.id.completeJob).setVisible(true);
                        break;
                    case APPLYING:
                        menu.getMenu().findItem(R.id.completeJob).setVisible(true);
                        menu.getMenu().findItem(R.id.failJob).setVisible(true);
                        break;
                }
                menu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.startJob) {
                        sendOtaMessage(new JobsMessage(OtaMessageOperation.StartJob, new ArrayList<>(mJobs.subList(index, index + 1))));
                        return true;
                    } else if (item.getItemId() == R.id.rejectJob) {
                        sendOtaMessage(new JobsMessage(OtaMessageOperation.RejectJob, new ArrayList<>(mJobs.subList(index, index + 1))));
                        return true;
                    } else if (item.getItemId() == R.id.applyJob) {
                        sendOtaMessage(new JobsMessage(OtaMessageOperation.ApplyJob, new ArrayList<>(mJobs.subList(index, index + 1))));
                        return true;
                    } else if (item.getItemId() == R.id.completeJob) {
                        sendOtaMessage(new JobsMessage(OtaMessageOperation.CompleteJob, new ArrayList<>(mJobs.subList(index, index + 1))));
                        return true;
                    } else if (item.getItemId() == R.id.failJob) {
                        sendOtaMessage(new JobsMessage(OtaMessageOperation.FailJob, new ArrayList<>(mJobs.subList(index, index + 1))));
                        return true;
                    }

                    return false;
                });
                menu.show();
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_list_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Job job = mJobs.get(position);
            holder.index = position;
            holder.jobIdTextView.setText(job.getJobId());
            holder.jobStatusTextView.setText(job.getStatus().toString());
            if (job.getStatus() == JobStatus.QUEUED) {
                holder.jobStatusTextView.getBackground()
                        .setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GRAY, BlendModeCompat.SRC_ATOP));
            } else if (job.getStatus() == JobStatus.IN_PROGRESS || job.getStatus() == JobStatus.APPLYING
                    || job.getStatus() == JobStatus.DOWNLOADING || job.getStatus() == JobStatus.DOWNLOADED) {
                holder.jobStatusTextView.getBackground()
                        .setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.BLUE, BlendModeCompat.SRC_ATOP));
            } else if (job.getStatus() == JobStatus.SUCCEEDED) {
                holder.jobStatusTextView.getBackground()
                        .setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.GREEN, BlendModeCompat.SRC_ATOP));
            } else if (job.getStatus() == JobStatus.FAILED || job.getStatus() == JobStatus.REJECTED) {
                holder.jobStatusTextView.getBackground()
                        .setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(Color.RED, BlendModeCompat.SRC_ATOP));
            }
        }

        @Override
        public int getItemCount() {
            return mJobs.size();
        }
    }
}
