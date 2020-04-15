package org.onap.ccsdk.cds.sdclistener.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(SdcListenerDto.class)
@SpringBootTest(classes = {SdcListenerDto.class})
public class SdcListenerDtoTest {

    //@Autowired
    private SdcListenerDto listenerConfiguration;

    @Test
    public void testCdsSdcListenerDto() {
        assertEquals(listenerConfiguration.getDistributionId(), "1234");
        assertEquals(listenerConfiguration.getArtifactUrl(), "\"/sdc/v1/artifact\"");
    }

}
