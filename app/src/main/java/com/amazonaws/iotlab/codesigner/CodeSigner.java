/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.codesigner;

import android.util.Base64;

import java.io.InputStream;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class CodeSigner {

    public static boolean isVerifyPassed(InputStream certIs, byte[] data, String signature) {
        boolean res = false;
        try {
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(certIs);
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(certificate);
            verifier.update(data);
            res = verifier.verify(Base64.decode(signature, Base64.NO_WRAP));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
