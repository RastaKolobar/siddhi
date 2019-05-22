/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.siddhi.core.stream;

import java.util.HashMap;
import java.util.Map;

/**
 * Service related information to the deployment
 */
public class ServiceDeploymentInfo {

    private ServiceProtocol serviceProtocol = ServiceProtocol.TCP;
    private SourceStrategy sourceStrategy = SourceStrategy.PULL;
    private boolean secured = false;
    private int port;
    private Map<String, String> deploymentProperties = new HashMap<>();

    /**
     * @param serviceProtocol the protocol used by the service
     * @param port            the port of the service
     * @param secured         is the service protocol secured
     * @param sourceStrategy  the source strategy
     */
    public ServiceDeploymentInfo(ServiceProtocol serviceProtocol, int port, boolean secured,
                                 SourceStrategy sourceStrategy) {
        this.serviceProtocol = serviceProtocol;
        this.port = port;
        this.secured = secured;
        this.sourceStrategy = sourceStrategy;
    }

    /**
     * Service related information to the deployment
     *
     * @param port           the port of the service
     * @param secured        is the service protocol secured
     * @param sourceStrategy the source strategy
     */
    public ServiceDeploymentInfo(int port, boolean secured, SourceStrategy sourceStrategy) {
        this.port = port;
        this.secured = secured;
        this.sourceStrategy = sourceStrategy;
    }

    public ServiceDeploymentInfo() {
    }

    public ServiceProtocol getServiceProtocol() {
        return serviceProtocol;
    }

    public void setServiceProtocol(ServiceProtocol serviceProtocol) {
        this.serviceProtocol = serviceProtocol;
    }

    public boolean isSecured() {
        return secured;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public SourceStrategy getSourceStrategy() {
        return sourceStrategy;
    }

    public void setSourceStrategy(SourceStrategy sourceStrategy) {
        this.sourceStrategy = sourceStrategy;
    }

    public Map<String, String> getDeploymentProperties() {
        return deploymentProperties;
    }

    public void addDeploymentProperties(Map<String, String> deploymentProperties) {
        for (Map.Entry<String, String> entry : deploymentProperties.entrySet()) {
            this.deploymentProperties.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Service protocols
     */
    public enum ServiceProtocol {
        TCP, UDP, SCTP,
    }

    /**
     * Source strategy
     */
    public enum SourceStrategy {
        PUSH, PULL
    }
}
