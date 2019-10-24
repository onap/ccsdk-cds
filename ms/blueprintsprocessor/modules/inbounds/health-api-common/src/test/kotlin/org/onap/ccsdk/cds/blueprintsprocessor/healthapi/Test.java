package org.onap.ccsdk.cds.blueprintsprocessor.healthapi;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration;
import org.onap.ccsdk.cds.blueprintsprocessor.db.mock.MockBlueprintProcessorCatalogServiceImpl;
import org.onap.ccsdk.cds.blueprintsprocessor.db.service.BlueprintCatalogServiceImpl;
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionComponent;
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ComponentScriptExecutor;
import org.onap.ccsdk.cds.blueprintsprocessor.services.workflow.mock.MockComponentConfiguration;
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebFluxTest
@ContextConfiguration(
        classes = {MockComponentConfiguration.class,BluePrintRuntimeService.class, BluePrintCoreConfiguration.class,MockBlueprintProcessorCatalogServiceImpl.class})
@ComponentScan(basePackages = {"org.onap.ccsdk.cds.blueprintsprocessor",
        "org.onap.ccsdk.cds.controllerblueprints"},
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {ResourceResolutionComponent.class,MockComponentConfiguration.class})})
@TestPropertySource(locations = {"classpath:application-test.properties"})
public class Test {


  @org.junit.Test
  public void test(){
    Assert.assertTrue(true);
  }
}
