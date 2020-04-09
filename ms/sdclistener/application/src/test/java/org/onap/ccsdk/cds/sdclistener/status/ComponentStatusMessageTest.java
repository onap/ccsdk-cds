package org.onap.ccsdk.cds.sdclistener.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.sdclistener.status.ComponentStatusMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ComponentStatusMessageTest.class})
public class ComponentStatusMessageTest {
    ComponentStatusMessage componentStatusMsg= new ComponentStatusMessage();

    @Test
    public void testComponentStatusMessage() {
        componentStatusMsg.setComponentName("Test");
        componentStatusMsg.setArtifactUrl("/sdc/v1/artifact");
        componentStatusMsg.setConsumerID("cds-id-local");
        componentStatusMsg.setDistributionID("1");
        componentStatusMsg.setTimeStamp(01022020);
        assertEquals(componentStatusMsg.getComponentName(), "Test");
        assertEquals(componentStatusMsg.getArtifactURL(), "/sdc/v1/artifact");
        assertEquals(componentStatusMsg.getConsumerID(), "cds-id-local");
        assertEquals(componentStatusMsg.getDistributionID(), "1");
        assertEquals(componentStatusMsg.getTimeStamp(), 01022020);
        assertEquals(componentStatusMsg.getTimestamp(), 01022020);
    }
}
