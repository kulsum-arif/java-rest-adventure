FROM docker-local.artifactory-stg.synkrato.io/cicd-repo/java-base-image:2.1.1

ARG USER=app
ENV JAR_FILE="partner-service-1.26.3-SNAPSHOT.jar"
ARG WORK_DIR_PATH=/opt/apps/partner-service

WORKDIR $WORK_DIR_PATH

RUN sudo apk add -U unzip

RUN sudo chmod 777 $WORK_DIR_PATH

RUN mkdir -p -v $WORK_DIR_PATH/appdynamics

RUN curl -fL https://getcli.jfrog.io | sh
RUN chmod 755 jfrog
RUN mv jfrog $WORK_DIR_PATH

# Add AppDynamics
RUN curl -L -o artifact_config.txt 'https://artifactory-config-bucket.s3.amazonaws.com/artifact_config.txt'

RUN curl --insecure -u readonly:$(cat artifact_config.txt) -o AppDAgent.zip 'https://artifactory-stg.synkrato.io/artifactory/common/Appdynamics/AppServerAgent-1.8-22.11.0.34486.zip'
RUN unzip AppDAgent.zip -d $WORK_DIR_PATH/appdynamics/
RUN rm AppDAgent.zip
RUN rm artifact_config.txt

COPY /target/${JAR_FILE} ${JAR_FILE}
COPY /deployment/scripts/start.sh start.sh

RUN sudo chown -R $USER:$USER /opt/apps/

RUN sudo chmod 744 ${JAR_FILE}
RUN sudo chmod 744 start.sh

EXPOSE 8443
ENTRYPOINT ["sh", "./start.sh"]
