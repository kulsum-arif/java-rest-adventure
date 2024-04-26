#!/bin/bash

# Set working directory
EPC_WORKING_DIR=/opt/apps/partner-service

mkdir "${EPC_WORKING_DIR}"/certs

cp ${EM_CERT} ${EPC_WORKING_DIR}/certs/ELLIE-MAE-ROOT.cer
cp ${EM_KAFKA_KEYSTORE} ${EPC_WORKING_DIR}/certs/${SDLC_ENVIRONMENT}.kafka.client.truststore.jks

# We are creating openssl base64 random text and using first 20chars as storepass for keytool
SECRETKEY=$(openssl rand -base64 20)

# Below is the command to generate p12 keystore file using keytool with following info. alias name = epc, commonname=PartnerService and valid for 2years
keytool -genkeypair -alias epc -dname "CN=PartnerService" -keyalg RSA -keysize 2048 -storepass "${SECRETKEY}" -storetype PKCS12 \
  -keystore "${EPC_WORKING_DIR}"/certs/epc.p12 -validity 365

JVM_ARGS="-Xmx2048M
    -XX:+PrintGCDateStamps
    -verbose:gc
    -Xloggc:${EPC_WORKING_DIR}/partner-service-gc.log
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=${EPC_WORKING_DIR}"

######################################
# AppDynamics
######################################
ACCOUNT_NAME='synkrato-nonprod'
ACCOUNT_ACCESS_KEY='i1dv5bkq8g0p'
CONTROLLER_HOSTNAME='synkrato-nonprod.saas.appdynamics.com'
CERTIFICATE_VALIDATION='false'
if [[ "${SDLC_ENVIRONMENT}" == "prod" ]]; then
  ACCOUNT_NAME='synkrato-prod'
  ACCOUNT_ACCESS_KEY='7fc85cc1-cf7e-4f2c-8a3f-7b0964169b3b'
  CONTROLLER_HOSTNAME='synkrato-prod.saas.appdynamics.com'
  CERTIFICATE_VALIDATION='true'
fi

UNIQUE_HOST_ID="$(sed -rn '1s#.*/##; 1s/(.{12}).*/\1/p' /proc/self/cgroup)"
echo "UNIQUE_HOST_ID=$UNIQUE_HOST_ID"
APPDYNAMICS="-javaagent:/opt/apps/partner-service/appdynamics/javaagent.jar -Dappdynamics.agent.nodeName=$UNIQUE_HOST_ID
  -Dappdynamics.controller.hostName=$CONTROLLER_HOSTNAME
  -Dappdynamics.controller.port=443 -Dappdynamics.controller.ssl.enabled=true
  -Dappdynamics.force.default.ssl.certificate.validation=$CERTIFICATE_VALIDATION
  -Dappdynamics.agent.accountName=$ACCOUNT_NAME
  -Dappdynamics.agent.applicationName="EPC2-$SDLC_ENVIRONMENT"
  -Dappdynamics.agent.tierName="epc-partner-service"
  -Dappdynamics.agent.accountAccessKey=${ACCOUNT_ACCESS_KEY}
  -Dappdynamics.jvm.shutdown.mark.node.as.historical=true"

exec java ${JVM_ARGS} ${APPDYNAMICS} \
  -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector \
  -Dnet.logstash.log4j.JSONEventLayoutV1.UserFields="project:partner-service,
                                                       region:${AWS_REGION},
                                                       environment:${SDLC_ENVIRONMENT},
                                                       account_id:${AWS_ACCOUNT_ID}" \
  -jar ${EPC_WORKING_DIR}/${JAR_FILE} \
  --spring.config.location=classpath:application.yml,classpath:common-application.yml \
  --spring.profiles.active=${SDLC_ENVIRONMENT} \
  --aws.region=${AWS_REGION} \
  --server.ssl.key-store-password=${SECRETKEY}
