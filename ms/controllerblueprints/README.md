Application VM Arguments :

-DappName=ControllerBluePrints
-Dms_name=org.onap.ccsdk.apps.controllerblueprints
-DappVersion=1.0.0
-Dlogging.config=etc/logback.xml
-Dspring.config.location=opt/app/onap/config/
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/sdnctl
-Dspring.datasource.username=sdnctl
-Dspring.datasource.password=sdnctl
-Dcontrollerblueprints.loadInitialData=true
-Dspring.profiles.active=dev

