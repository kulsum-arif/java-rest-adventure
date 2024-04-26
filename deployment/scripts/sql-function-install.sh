#!/bin/bash

# Run sql-function-install.sh once per AWS account
VPC_NAME="elmae-default"
LOG_MESSAGE="PARTNER_SERVICE_MSG="
echo "${LOG_MESSAGE}Deployment begins for PostgreSQL execution function installation"

# 1. Check input params - 1.SDLC_ENVIRONMENT 2.AWS Region
if [ "$#" -lt 2 ]; then
    echo "${LOG_MESSAGE}ERROR: Parameter expected: SDLC_ENVIRONMENT [dev/qa/int/peg/stg/concept/r2t/prod], AWS Region"
    exit 1
fi

#2. Initialize SDLC_Environment and Aws Region
SDLC_ENVIRONMENT=$1
AWS_REGION=$2

#3. Initialize Environment Zone
case "$SDLC_ENVIRONMENT" in
"dev" | "qa" | "int" | "peg")
    ENVIRONMENT_ZONE='non-prod'
    ;;
"stg" | "concept" | "r2t" | "prod")
    ENVIRONMENT_ZONE='prod'
    ;;
esac

VPC_ID=$(aws ec2 describe-vpcs --region $AWS_REGION --filters Name=tag-value,Values="${VPC_NAME}" --query Vpcs[0].VpcId --output text)
PRIVATE_SUBNET_IDS=$(aws --region ${AWS_REGION} ec2 describe-subnets --filters "Name=tag:Name,Values=Private0*,Private1*,Private2*" "Name=vpc-id,Values=${VPC_ID}" --query "Subnets[*].SubnetId" --output text | tr '	' ',')
BASTION_SECURITY_GROUP=$(aws ec2 describe-security-groups --region $AWS_REGION --filters Name=tag-key,Values=Name Name=tag-value,Values=BastionSecurityGroup --query SecurityGroups[0].GroupId --output text)

echo "${LOG_MESSAGE}SDLC_ENVIRONMENT: ${SDLC_ENVIRONMENT}, AWS_REGION: ${AWS_REGION} ENVIRONMENT_ZONE: ${ENVIRONMENT_ZONE}"

#3. Initialize Stack Names
if [ "${ENVIRONMENT_ZONE}" == "prod" ]; then
    CF_PARTNER_SQL_RESOURCE_STACK_NAME="PartnerSvcSqlFunctionResourcesStack${AWS_REGION_NAME}"
else
    CF_PARTNER_SQL_RESOURCE_STACK_NAME="nonProdPartnerSvcSqlFunctionResourcesStack${AWS_REGION_NAME}"
fi

#4. Check and assign create/update stack command for cloudformation partner sql function resource template
if aws cloudformation describe-stacks --region $AWS_REGION --stack-name $CF_PARTNER_SQL_RESOURCE_STACK_NAME; then
    CF_PARTNER_SQL_RESOURCE_STACK_COMMAND="update-stack"
    CF_PARTNER_SQL_RESOURCE_STACK_COMMAND_WAIT_COMMAND="stack-update-complete"
else
    CF_PARTNER_SQL_RESOURCE_STACK_COMMAND="create-stack"
    CF_PARTNER_SQL_RESOURCE_STACK_COMMAND_WAIT_COMMAND="stack-create-complete"
fi

echo "${LOG_MESSAGE}Partner SQL Function Resource Stack commands: CF_PARTNER_SQL_RESOURCE_STACK_COMMAND: ${CF_PARTNER_SQL_RESOURCE_STACK_COMMAND} ,
    command: ${CF_PARTNER_SQL_RESOURCE_STACK_COMMAND_WAIT_COMMAND} , wait command: ${CF_PARTNER_SQL_RESOURCE_STACK_COMMAND_WAIT_COMMAND}"

#5 Replace param values in SQL Function Exec param file
sed -e "s#%ENVIRONMENT_ZONE%#${ENVIRONMENT_ZONE}#;s#%SDLC_ENVIRONMENT%#${SDLC_ENVIRONMENT}#;s#%PRIVATE_SUBNET_IDS%#${PRIVATE_SUBNET_IDS}#;
        s#%BASTION_SECURITY_GROUP%#${BASTION_SECURITY_GROUP}#;" \
    ./deployment/aws/params/sql-function-exec-params.json >./deployment/aws/params/sql-function-exec-params-v1.json

#5. Execute AWS CloudFormation template for Partner Service S3 Resources stack
if aws cloudformation $CF_PARTNER_SQL_RESOURCE_STACK_COMMAND --region $AWS_REGION --stack-name $CF_PARTNER_SQL_RESOURCE_STACK_NAME \
    --parameters file://./deployment/aws/params/sql-function-exec-params-v1.json \
    --capabilities CAPABILITY_NAMED_IAM \
    --template-body file://./deployment/aws/cloudformation-partner-sql-exec-function-resources.yml; then
    echo "${LOG_MESSAGE}Waiting for the $CF_PARTNER_SQL_RESOURCE_STACK_NAME  $CF_PARTNER_SQL_RESOURCE_STACK_COMMAND to complete"
    aws cloudformation wait $CF_PARTNER_SQL_RESOURCE_STACK_COMMAND_WAIT_COMMAND --stack-name $CF_PARTNER_SQL_RESOURCE_STACK_NAME --region $AWS_REGION
    echo "${LOG_MESSAGE}$CF_PARTNER_SQL_RESOURCE_STACK_COMMAND on $CF_PARTNER_SQL_RESOURCE_STACK_NAME is now complete"
fi

echo "${LOG_MESSAGE}Deployment ends for PostgreSQL execution function installation"
