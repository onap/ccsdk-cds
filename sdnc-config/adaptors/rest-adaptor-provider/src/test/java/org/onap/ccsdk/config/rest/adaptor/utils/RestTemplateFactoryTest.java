package org.onap.ccsdk.config.rest.adaptor.utils;

import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorServiceImpl;

@SuppressWarnings("squid:S2187")
public class RestTemplateFactoryTest {
    
    public static void main(String[] args) {
        
        String propertyFile = RestTemplateFactoryTest.class.getClassLoader().getResource(".").getPath();
        System.out.println(" Property : " + propertyFile);
        
        try {
            ConfigRestAdaptorServiceImpl configRestAdaptorServiceImpl = new ConfigRestAdaptorServiceImpl(propertyFile);
            String restconfResponse = genericRestGetMDSALOperation(args, configRestAdaptorServiceImpl);
            System.out.println("RestTemplateFactoryTest.main Completed with response :" + restconfResponse);
        } catch (ConfigRestAdaptorException e) {
            e.printStackTrace();
        }
    }
    
    public static String genericRestGetMDSALOperation(String[] args,
            ConfigRestAdaptorServiceImpl configRestAdaptorServiceImpl) throws ConfigRestAdaptorException {
        String path = "config/Dummy-API:services/service-list/dummy-1234";
        String restconfResponse = configRestAdaptorServiceImpl.getResource(ConfigRestAdaptorConstants.SELECTOR_RESTCONF,
                path, String.class);
        return restconfResponse;
    }
    
}
