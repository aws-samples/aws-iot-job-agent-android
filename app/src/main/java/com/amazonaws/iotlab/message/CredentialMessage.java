/*
  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
  SPDX-License-Identifier: Apache-2.0.
 */
package com.amazonaws.iotlab.message;

public class CredentialMessage extends OtaMessage {
    public String clientId;
    public String endpoint;
    public String accessKeyId;
    public String accessKeySecret;
    public String sessionToken;

    public CredentialMessage(String clientId, String endpoint, String accessKeyId, String accessKeySecret, String sessionToken) {
        super(OtaMessageType.Credentials, OtaMessageOperation.SetCredentials);
        this.clientId = clientId;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.sessionToken = sessionToken;
    }

    public static final class Builder {
        public String clientId = "";
        public String endpoint = "";
        public String accessKeyId = "";
        public String accessKeySecret = "";
        public String sessionToken = "";

        private Builder() {
        }

        public static Builder aCredentialMessage() {
            return new Builder();
        }

        public Builder withClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder withAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
            return this;
        }

        public Builder withAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
            return this;
        }

        public Builder withSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
            return this;
        }

        public CredentialMessage build() {
            return new CredentialMessage(clientId, endpoint, accessKeyId, accessKeySecret, sessionToken);
        }
    }
}
