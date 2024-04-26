# Global/Common Parameters
locals {
  em_epc2_dev_east_private_ips  = ["10.216.98.96/27", "10.216.98.128/27", "10.216.98.160/27", "10.127.0.0/16", "10.64.130.0/32"]
  em_epc2_dev_west_private_ips  = ["10.200.97.96/27", "10.200.97.128/27", "10.200.97.160/27", "10.127.0.0/16", "10.64.130.0/32"]
  em_epc2_prod_east_private_ips = ["10.208.101.96/27", "10.208.101.128/27", "10.208.101.160/27"]
  em_epc2_prod_west_private_ips = ["10.193.101.96/27", "10.193.101.128/27", "10.193.101.160/27"]
  cop_service_role_name         = local.environment_zone == "non-prod" ? "COP_EPC2-Dev-East_srv_role20200318060711336200000005" : "COP_EPC2-Prod-East_srv_role20200429034658860100000007"
  db_name                       = "partnerdb"
  environment_zone              = (var.environment == "prod" || var.environment == "stg" || var.environment == "concept") ? "prod" : "non-prod"
  is_private_hosted_zone        = false
  kms_key_description           = "RDS KMS Key for ${var.environment} ${local.db_name}"
  master_db_kms_key_alias       = "alias/${var.environment}/rds/master/${local.db_name}"
  replica_db_kms_key_alias      = "alias/${var.environment}/rds/replica/${local.db_name}"
  port                          = 8443
  service_name                  = "epc-partner-service"
  ssl_policy                    = "ELBSecurityPolicy-TLS-1-2-2017-01"
  vpc_name                      = "elmae-default"
  s3_bucket_name                = "${var.environment}-em-partner-resources"
  cloudwatch_alert_topic        = "alert"

  vpn_private_ips = [
    "10.111.16.0/22",
    "10.16.16.0/22",
    "10.127.192.0/20",
    "10.115.192.0/19",
    "10.127.224.0/20"
  ]

  tags = {
    tag_compliance  = "Restricted-PII data"
    tag_costcenter  = "Engineering"
    tag_department  = "EPC Platform"
    tag_environment = var.environment
    tag_name        = "Partner Service"
    tag_owner       = "Chris Chan"
    tag_project     = "Partner Service"
    tag_tenant      = "multi"

  }

  # Active region parameters
  active_aws_region = "us-east-1"

  active_region_alb_security_group_list = [data.aws_security_group.active-em-security-group.id,
  data.aws_security_group.active-route53-healthcheck-security-group.id]

  active_region_alb_sg_egress_rule = [
    {
      security_group = data.aws_security_group.active-cop-ec2-security-group.id,
      from_port      = 0,
      to_port        = 65535,
      protocol       = "tcp",
      description    = "COP EC2 security group"
  }]

  master_db_ingress_security_group_list = [
    {
      port           = var.db_port
      protocol       = "tcp"
      security_group = data.aws_security_group.active-cop-ec2-security-group.id
      description    = "COP Cluster security group"
  }]

  active_region_cop_ecs_cluster_name = local.environment_zone == "non-prod" ? "COP_EPC2-Dev-East" : "COP_EPC2-Prod-East"

  aws_workspace_ips = [
    {
      cidr_block  = "35.160.159.128/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Offshore AWS Workspaces"
    },
    {
      cidr_block  = "35.161.41.6/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Offshore AWS Workspaces"
    },
    {
      cidr_block  = "54.218.187.104/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Internal Tool AWS Workspace"
  }]

  investorconnect_nonprod_ips = [
    {
      cidr_block  = "34.212.60.85/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "35.164.53.182/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "52.42.124.119/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.233.178.170/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.198.68.187/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.233.234.72/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.233.245.55/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    }
  ]

  investorconnect_prod_ips = [
    {
      cidr_block  = "34.235.246.16/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "35.169.206.51/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "52.44.6.113/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.210.217.137/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.212.188.30/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    },
    {
      cidr_block  = "34.216.5.170/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "Investor Connect"
    }
  ]

  em_eic_nonprod_east_vpc_ips = [
    {
      cidr_block  = "34.232.91.57/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC East non-prod IP"
    }]

  em_eic_nonprod_west_vpc_ips = [
    {
      cidr_block  = "54.200.224.37/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC West non-prod IP"
    }]

  em_eic_prod_east_vpc_ips = [
    {
      cidr_block  = "54.152.173.101/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC East prod IP"
    },
    {
      cidr_block  = "34.202.41.210/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC East prod IP"
    },
    {
      cidr_block  = "3.229.100.92/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC East prod IP"
    }]

  em_eic_prod_west_vpc_ips = [
    {
      cidr_block  = "100.20.4.164/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC West prod IP"
    },
    {
      cidr_block  = "54.184.63.96/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC West prod IP"
    },
    {
      cidr_block  = "100.20.26.203/32",
      from_port   = 443,
      to_port     = 443,
      protocol    = "tcp",
      description = "EIC West prod IP"
    }]


  em_ips_whitelist_map = {
    "non-prod" = concat(local.aws_workspace_ips, local.investorconnect_nonprod_ips, local.em_eic_nonprod_east_vpc_ips, local.em_eic_nonprod_west_vpc_ips)
    "prod"     = concat(local.aws_workspace_ips, local.investorconnect_prod_ips, local.em_eic_prod_east_vpc_ips, local.em_eic_prod_west_vpc_ips)
  }

  active_region_alb_cidr_ingress_rule  = local.em_ips_whitelist_map[local.environment_zone]
  passive_region_alb_cidr_ingress_rule = local.em_ips_whitelist_map[local.environment_zone]

  # Passive region parameters
  passive_aws_region = "us-west-2"

  passive_region_alb_security_group_list = var.deploy_to_passive_region ? [
    data.aws_security_group.passive-em-security-group[0].id,
  data.aws_security_group.passive-route53-healthcheck-security-group[0].id] : []

  passive_region_alb_sg_egress_rule = [
    {
      security_group = var.deploy_to_passive_region ? data.aws_security_group.passive-cop-ec2-security-group[0].id : ""
      from_port      = 0,
      to_port        = 65535,
      protocol       = "tcp",
      description    = "COP EC2 security group"
  }]

  replica_db_ingress_security_group_list = var.deploy_rds_replica ? [
    {
      port           = var.db_port
      protocol       = "tcp"
      security_group = data.aws_security_group.passive-cop-ec2-security-group[0].id
      description    = "COP Cluster security group"
  }] : []

  passive_region_cop_ecs_cluster_name = local.environment_zone == "non-prod" ? "COP_EPC2-Dev-West" : "COP_EPC2-Prod-West"

  # Remove the else part after migrating partner db for all environments
  db_parameter_group = [
    {
      name  = "client_encoding"
      value = "UTF8"
    },
    {
      name         = "rds.logical_replication"
      value        = "1"
      apply_method = "pending-reboot"
  }]

  ecs_task_role_principal_list = [
    "ec2.amazonaws.com",
    "ecs.amazonaws.com",
    "lambda.amazonaws.com",
    "ecs-tasks.amazonaws.com",
    "s3.amazonaws.com",
    "sns.amazonaws.com",
    "elasticache.amazonaws.com",
    "rds.amazonaws.com"
  ]

  ecs_task_role_policy_statement = <<EOF
          [{
            "Effect": "Allow",
            "Action": ["s3:*","s3:ListBucket"
              ],
            "Resource": [
              "arn:aws:s3:::non-prod-partner-service-s3-us-east-1",
              "arn:aws:s3:::non-prod-partner-service-s3-us-east-1/*",
              "arn:aws:s3:::non-prod-partner-service-s3-us-west-2",
              "arn:aws:s3:::non-prod-partner-service-s3-us-west-2/*",
              "arn:aws:s3:::prod-partner-service-s3-us-east-1",
              "arn:aws:s3:::prod-partner-service-s3-us-east-1/*",
              "arn:aws:s3:::prod-partner-service-s3-us-west-2",
              "arn:aws:s3:::prod-partner-service-s3-us-west-2/*",
              "arn:aws:s3:::${var.environment}-em-partner-resources",
              "arn:aws:s3:::${var.environment}-em-partner-resources/*"
            ]
          },
          {
            "Effect": "Allow",
            "Action": [
              "cloudwatch:DescribeAlarmHistory",
              "cloudwatch:DescribeAlarms",
              "cloudwatch:DescribeAlarmsForMetric",
              "cloudwatch:GetMetricData",
              "cloudwatch:ListMetrics",
              "logs:DescribeDestinations",
              "logs:DescribeLogGroups",
              "logs:DescribeLogStreams",
              "logs:DescribeMetricFilters",
              "logs:DescribeSubscriptionFilters",
              "logs:GetLogEvents"
            ],
            "Resource": "*"
          },
          {
            "Effect": "Allow",
            "Action": ["iam:CreateServiceLinkedRole", "iam:PutRolePolicy"],
            "Resource": "arn:aws:iam::*:role/aws-service-role/rds.amazonaws.com/AWSServiceRoleForRDS",
            "Condition": {
            	"StringLike": {
            		"iam:AWSServiceName": "rds.amazonaws.com"
            	}
            }
          },
          {
            "Effect": "Allow",
            "Action": ["iam:CreateServiceLinkedRole", "iam:PutRolePolicy"],
            "Resource": "arn:aws:iam::*:role/aws-service-role/elasticache.amazonaws.com/AWSServiceRoleForElastiCache*",
            "Condition": {
            	"StringLike": {
            		"iam:AWSServiceName": "elasticache.amazonaws.com"
            	}
            }
          }]
  EOF

  ecs_auto_scaling_task_role_principal_list = ["application-autoscaling.amazonaws.com"]

  ecs_auto_scaling_task_role_policy_statement = <<EOF
          [{
            "Effect": "Allow",
            "Action": [
              "application-autoscaling:DeleteScalingPolicy",
              "application-autoscaling:DeregisterScalableTarget",
              "application-autoscaling:DescribeScalableTargets",
              "application-autoscaling:DescribeScalingActivities",
              "application-autoscaling:DescribeScalingPolicies",
              "application-autoscaling:PutScalingPolicy",
              "application-autoscaling:RegisterScalableTarget",
              "application-autoscaling:DeleteScheduledAction",
              "application-autoscaling:DescribeScheduledActions",
              "application-autoscaling:PutScheduledAction",
              "cloudwatch:DescribeAlarms",
              "cloudwatch:PutMetricAlarm",
              "ecs:DescribeServices",
              "ecs:UpdateService"],
            "Resource": "*"
          }]
  EOF

  # Container Environment Variables
  active_region_container_environment_variables = {
    BUILD_NUMBER = var.build_number,
    DC_REGION               = var.dc_region,
    SPRING_DATASOURCE_URL = "jdbc:postgresql://${module.rds.output_rds_master_db_instance_endpoint}/partnerdb",
    SDLC_ENVIRONMENT = var.environment,
    AWS_ACCOUNT_ID = data.aws_caller_identity.current.account_id,
    AWS_REGION = data.aws_region.active.name,
    ENVIRONMENT_ZONE = local.environment_zone,
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
  }

  passive_region_container_environment_variables = {
    BUILD_NUMBER = var.build_number,
    DC_REGION               = var.dc_region,
    SPRING_DATASOURCE_URL = var.deploy_to_passive_region ? "jdbc:postgresql://${module.rds.output_rds_replica_db_instance_endpoint}/partnerdb" : "",
    SDLC_ENVIRONMENT = var.environment,
    AWS_ACCOUNT_ID = data.aws_caller_identity.current.account_id,
    AWS_REGION = data.aws_region.passive.name,
    ENVIRONMENT_ZONE = local.environment_zone,
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
  }

  # Autoscale down step adjustment
  autoscale_down_step_adjustment = [
    {
      metric_interval_lower_bound = -60
      metric_interval_upper_bound = 0
      scaling_adjustment          = -10
    },
    {
      metric_interval_lower_bound = -120
      metric_interval_upper_bound = -60
      scaling_adjustment          = -10
    },
    {
      metric_interval_lower_bound = -160
      metric_interval_upper_bound = -120
      scaling_adjustment          = -10
    },
    {
      metric_interval_upper_bound = -160
      scaling_adjustment          = -10
    }]

  # Autoscale up step adjustment
  autoscale_up_step_adjustment = [
    {
      metric_interval_lower_bound = 0
      metric_interval_upper_bound = 60
      scaling_adjustment          = 10
    },
    {
      metric_interval_lower_bound = 60
      metric_interval_upper_bound = 120
      scaling_adjustment          = 10
    },
    {
      metric_interval_lower_bound = 120
      scaling_adjustment          = 10
    }]

}
