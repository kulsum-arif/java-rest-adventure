# Create/update parameter store resource

//Service-specific and environment-specific
module service_config {
  source                = "tfe.deploy.synkrato.io/Admin/ssm-config/aws"
  version               = "1.0.2"
  platform              = "java"
  environment           = var.environment
  region1               = local.active_aws_region
  region2               = local.passive_aws_region
  enable_region2        = var.deploy_to_passive_region
  service_name          = local.service_name
  region1_service_role  = module.ecs.output_active_ecs_task_role_name
  region2_service_role  = module.ecs.output_active_ecs_task_role_name
  overwrite             = true
  parameters            = {
    "spring/datasource/password" = {
      value             = var.db_password
    }
    "synkrato/em-services/webhook/security/token/oapi/client-secret" = {
      value             = var.webhook_oapi_client_secret
    }
    "synkrato/kafka/sasl-password" = {
      value             = var.kafka_producer_password
    }
  }
}
