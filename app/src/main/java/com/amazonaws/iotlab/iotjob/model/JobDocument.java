/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.iotjob.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class JobDocument {
    @Expose
    public String operation;
    @Expose
    public ArrayList<FileDescription> files;
}
