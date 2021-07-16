/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.downloader;

public interface DownloaderStatusCallback {
    void onDownloadStatusChanged(long id, Downloader.Status newStatus);
}
