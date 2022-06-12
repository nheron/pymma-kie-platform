##########################################################################
# Dockerfile that provides the image for JBoss Drools Workbench 6.5.0.Final
###########################################################################

####### BASE ############
FROM jboss/wildfly:23.0.2.Final


####### MAINTAINER ############
MAINTAINER "Nicolas Héron" "nicolas.heron@pymma-software.com"

####### ENVIRONMENT ############
ENV JBOSS_BIND_ADDRESS 0.0.0.0
ENV KIE_REPOSITORY https://repository.jboss.org/nexus/content/groups/public-jboss
ENV CHTIJBUG_REPOSITORY https://oss.sonatype.org/content/repositories/releases
ENV KIE_VERSION 7.17.0.Final
ENV KIE_CONTEXT_PATH kie-wb
ENV CHTIJBUG_VERSION 2.0.10
# Do NOT use demo neither examples by default in this image (no internet connection required).
ENV KIE_DEMO false
ENV JAVA_OPTS -Xms256m -Xmx4512m

####### Pymma Kie Realm #########


##com.pymmasoftware.kie-realm
RUN mkdir /opt/jboss/wildfly/modules/com
RUN mkdir /opt/jboss/wildfly/modules/com/pymmasoftware
RUN mkdir /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule
RUN mkdir /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main
ADD maven/pymma-kie-login-module.jar /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main/pymma-kie-loginmodule.jar
ADD maven/mongodb-driver.jar /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main/mongodb-driver.jar
ADD maven/mongodb-driver-core.jar /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main/mongodb-driver-core.jar
ADD maven/bson.jar /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main/bson.jar
ADD etc/module-loginmodule.xml /opt/jboss/wildfly/modules/com/pymmasoftware/pymma-kie-loginmodule/main/module.xml



####### DROOLS-WB ############

ADD maven/kie-wb.war  /opt/jboss/wildfly/standalone/deployments/$KIE_CONTEXT_PATH.war
#RUN curl $CHTIJBUG_REPOSITORY/org/chtijbug/drools/drools-framework-kie-wb-wars/$CHTIJBUG_VERSION/drools-framework-kie-wb-wars-$CHTIJBUG_VERSION.war -o /opt/jboss/wildfly/standalone/deployments/$KIE_CONTEXT_PATH.war

#RUN curl -o $HOME/$KIE_CONTEXT_PATH.war $KIE_REPOSITORY/org/kie/kie-drools-wb-distribution-wars/$KIE_VERSION/kie-drools-wb-distribution-wars-$KIE_VERSION-$KIE_CLASSIFIER.war && \
#unzip -q $HOME/$KIE_CONTEXT_PATH.war -d $JBOSS_HOME/standalone/deployments/$KIE_CONTEXT_PATH.war &&  \
#touch $JBOSS_HOME/standalone/deployments/$KIE_CONTEXT_PATH.war.dodeploy &&  \
# rm -rf $HOME/$KIE_CONTEXT_PATH.war


####### SCRIPTS ############
USER root
ADD etc/start_drools-wb.sh $JBOSS_HOME/bin/start_drools-wb.sh
RUN chown jboss:jboss $JBOSS_HOME/bin/start_drools-wb.sh
RUN chown -R jboss:jboss /opt/jboss/wildfly/modules/com/pymmasoftware


####### ENVIRONMENT ############
# Use demo and examples by default in this showcase image (internet connection required).
ENV KIE_DEMO false
ENV KIE_SERVER_PROFILE standalone-full-drools

####### EXPOSE INTERNAL JBPM GIT PORT ############
EXPOSE 8001

####### Drools Workbench CUSTOM CONFIGURATION ############
ADD etc/standalone-full-drools.xml $JBOSS_HOME/standalone/configuration/standalone-full-drools.xml
#ADD etc/application-users.properties $JBOSS_HOME/standalone/configuration/application-users.properties
#ADD etc/application-roles.properties $JBOSS_HOME/standalone/configuration/application-roles.properties

# Added files are chowned to root user, change it to the jboss one.
USER root
RUN chown jboss:jboss $JBOSS_HOME/standalone/configuration/standalone-full-drools.xml

# Switchback to jboss user
USER root
#RUN yum install -y git
RUN mkdir /home/lucene
RUN mkdir /home/niodir
RUN mkdir /home/kie-example
RUN mkdir /m2_kiewb
ADD settings.xml /m2_kiewb/settings.xml
RUN mkdir /m2_kiewb/repository
####### MVN REPO ############
# https://bugzilla.redhat.com/show_bug.cgi?id=1263738
#RUN mkdir -p /m2_kiewb/repository/org/guvnor/guvnor-asset-mgmt-project/$KIE_VERSION && \
#curl -o /m2_kiewb/repository/org/guvnor/guvnor-asset-mgmt-project/$KIE_VERSION/guvnor-asset-mgmt-project-$KIE_VERSION.jar $KIE_REPOSITORY/org/guvnor/guvnor-asset-mgmt-project/$KIE_VERSION/guvnor-asset-mgmt-project-$KIE_VERSION.jar


RUN chown jboss:jboss /home/lucene
RUN chown jboss:jboss /home/niodir
RUN chown jboss:jboss /home/kie-example
RUN chown jboss:jboss /m2_kiewb
RUN chown -R jboss:jboss /m2_kiewb/repository

#RUN cd /home/kie-example && git clone https://github.com/chtiJBUG/onboarding-nautic-project.git
#RUN cd /home/kie-example && git clone https://github.com/chtiJBUG/onboa rding-carinsurance-project.git
#RUN cd /home/kie-example && git clone https://github.com/chtiJBUG/onboarding-loyalty-project.git
####### EXPOSE INTERNAL JBPM GIT PORT ############
EXPOSE 8001
EXPOSE 8080
EXPOSE 50505
####### RUNNING DROOLS-WB ############
VOLUME /home/lucene
VOLUME /home/niodir
WORKDIR $JBOSS_HOME/bin/
CMD ["./start_drools-wb.sh"]