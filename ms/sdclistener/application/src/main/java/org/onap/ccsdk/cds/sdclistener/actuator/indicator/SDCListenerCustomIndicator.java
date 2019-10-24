package org.onap.ccsdk.cds.sdclistener.actuator.indicator;


import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.SDCListenerHealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

@Component
public class SDCListenerCustomIndicator extends AbstractHealthIndicator {

  @Autowired
  private SDCListenerHealthCheck sDCListenerHealthCheck;

  /*@Throws(Exception::class)
  override fun doHealthCheck(builder: Health.Builder) {
    val (status, checks) = sDCListenerHealthCheck!!.retrieveEndpointExecutionStatus()
    if (status == HealthCheckStatus.UP) {
      builder.up()
    } else {
      builder.down()
    }
    builder.withDetail("Services", checks)
  }*/

  @Override
  protected void doHealthCheck(Builder builder) {
    HealthApiResponse healthAPIResponse = sDCListenerHealthCheck.retrieveEndpointExecutionStatus();
    if (healthAPIResponse.getStatus() == HealthCheckStatus.UP) {
      builder.up();
    } else {
      builder.down();
    }
    builder.withDetail("Services", healthAPIResponse.getChecks());
  }
}
