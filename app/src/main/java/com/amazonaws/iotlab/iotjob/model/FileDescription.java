/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

public class FileDescription {
    @Expose
    public String fileName;
    @Expose
    public String fileVersion;
    @Expose
    public String md5;
    @Expose
    public String signature;
    @Expose
    public FileSource fileSource;
    public boolean isDownloaded = false;
}
