# Partner Service 
## Build & Deployment Status 
|  Resource  |                                                                                                          Status                                                                                                           |
|:----------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|  Service   | <a href='https://jenkins.dco.elmae/view/EPC2/job/epc-ci-cd-pipeline/job/partner-service/job/master/'><img src='https://jenkins.dco.elmae/view/EPC2/job/epc-ci-cd-pipeline/job/partner-service/job/master/badge/icon'></a> |
| Deployment | [![Build Status](https://jenkins.dco.elmae/view/EPC2/job/partner-service/job/deploy/job/tf-deploy-partner-dev/badge/icon)](https://jenkins.dco.elmae/view/EPC2/job/partner-service/job/deploy/job/tf-deploy-partner-dev/) |

## Additional Links

|   Env   |                             Health                              |                                     Swagger                                     |
|:-------:|:---------------------------------------------------------------:|:-------------------------------------------------------------------------------:|
|   DEV   |  [Health](https://api.partner.dev.epc2.rd.synkrato.io/health)   |  [Swagger](https://api.partner.dev.epc2.rd.synkrato.io/swagger-ui/index.html)   |
|   QA    |   [Health](https://api.partner.qa.epc2.rd.synkrato.io/health)   |   [Swagger](https://api.partner.qa.epc2.rd.synkrato.io/swagger-ui/index.html)   |
|   INT   |  [Health](https://api.partner.int.epc2.rd.synkrato.io/health)   |  [Swagger](https://api.partner.int.epc2.rd.synkrato.io/swagger-ui/index.html)   |
|   PEG   |  [Health](https://api.partner.peg.epc2.rd.synkrato.io/health)   |  [Swagger](https://api.partner.peg.epc2.rd.synkrato.io/swagger-ui/index.html)   |
|   STG   |   [Health](https://api.partner.stg.epc2.ellielabs.com/health)   |   [Swagger](https://api.partner.stg.epc2.ellielabs.com/swagger-ui/index.html)   |
|   R2T   |   [Health](https://api.partner.r2t.epc2.ellielabs.com/health)   |   [Swagger](https://api.partner.r2t.epc2.ellielabs.com/swagger-ui/index.html)   |
| CONCEPT | [Health](https://api.partner.concept.epc2.ellielabs.com/health) | [Swagger](https://api.partner.concept.epc2.ellielabs.com/swagger-ui/index.html) |
|  PROD   |   [Health](https://api.partner.epc2.ellieservices.com/health)   |   [Swagger](https://api.partner.epc2.ellieservices.com/swagger-ui/index.html)   |

## Development

### Getting Started

```
git clone git@githubdev.dco.elmae:synkrato/partner-service.git
cd partner-service
git checkout YOUR_BRANCH_OR_TAG
```

### Build and Run Tests

```
mvn clean install
```

### Create a Docker Image

```
mvn docker:build -DdockerRepo=cicd-repo -DpushImageTag -DdockerImageTags=YOUR_TAG
```

### Running Locally

```
java -classpath YOUR_CLASSPATH com.synkrato.services.partner.PartnerServiceApplication \
--spring.config.location=classpath:application.yml,classpath:common-application.yml,/PATH_TO_SECRETS/secrets.yml
```
