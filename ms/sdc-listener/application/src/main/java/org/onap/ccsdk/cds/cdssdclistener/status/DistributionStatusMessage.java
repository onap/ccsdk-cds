/*
 * Copyright © 2019 Bell Canada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.cds.cdssdclistener.status;

import org.onap.sdc.api.consumer.IFinalDistrStatusMessage;
import org.onap.sdc.utils.DistributionStatusEnum;

public class DistributionStatusMessage  implements IFinalDistrStatusMessage {

    public String componentName;

    public String consumerID;

    public String distributionID;

    public DistributionStatusEnum status;

    @Override
    public String getDistributionID() {
        return distributionID;
    }

    @Override
    public long getTimestamp() {
        return 0;
    }

    @Override
    public DistributionStatusEnum getStatus() {
        return status;
    }

    @Override
    public String getConsumerID() {
        return consumerID;
    }

    @Override
    public String getComponentName() {
        return componentName;
    }
}
