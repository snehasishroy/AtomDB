# AtomDb

### For running the server on command line:

```bash
cd atomdb-server && sh run.sh d
```
You can then connect remotely from intellij remotely, by setting up a remote configuration (Remote JVM debug), and connecting to the default 5005 port

### For running the server on IntelliJ:
You can run the service from Intellij Idea as well by setting the following in edit configurations

**Prerequisite**

When running service locally, local.yml config would be fetched.  
Pre requisite make sure you have zookeeper running, as the local.yml points to localhost:2181  
Alternatively you can also point to stage zookeeper, just make sure you don't block others.

Select type Application and choose the Main Applicaiton class from the server-module

1) *VM args*  
   `-DlocalConfig=true -Dcom.sun.security.enableAIAcaIssuers=true`

2) *Program args*  
   `server atomdb-server/config/local.yml`

3) *Environment variables*  
   `ZK_CONNECTION_STRING=stg-appzk001.phonepe.nb6:2181;HOST=stage-rosey-nb6.phonepe.com;PORT_8080=443;ROSEY_PATH=atomdb-server/config/rosey.yml`

Voila! Visit `localhost:8080/swagger/#` to check the housekeeping api.

### For Models dependency:

```xml
<dependency>
    <groupId>com.phonepe.platform</groupId>
    <artifactId>atomdb-models</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```