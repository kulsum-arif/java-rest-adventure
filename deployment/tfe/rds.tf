# Define KMS Key for RDS Master
resource "aws_kms_key" "master-rds-kms-key" {
  provider            = aws.active
  description         = local.kms_key_description
  enable_key_rotation = true
  is_enabled          = true
  tags                = local.tags
}

# Define Alias for RDS Master KMS Key
resource "aws_kms_alias" "master-rds-kms-key-alias" {
  provider      = aws.active
  name          = local.master_db_kms_key_alias
  target_key_id = aws_kms_key.master-rds-kms-key.arn
}

# Define KMS Key for RDS Replica
resource "aws_kms_key" "replica-rds-kms-key" {
  count               = var.deploy_rds_replica ? 1 : 0
  provider            = aws.passive
  description         = local.kms_key_description
  enable_key_rotation = true
  is_enabled          = true
  tags                = local.tags
}

# Define Alias for RDS Replica KMS Key
resource "aws_kms_alias" "replica-rds-kms-key-alias" {
  count         = var.deploy_rds_replica ? 1 : 0
  provider      = aws.passive
  name          = local.replica_db_kms_key_alias
  target_key_id = aws_kms_key.replica-rds-kms-key[0].arn
}

#Deploy RDS for Partner service
module "rds" {
  source                           = "tfe.deploy.synkrato.io/Admin/rds-postgres/aws"
  version                          = "3.1.0"
  providers                        = {
    aws.master  = aws.active
    aws.replica = aws.passive
  }
  environment                      = var.environment
  master_aws_region                = local.active_aws_region
  replica_aws_region               = local.passive_aws_region
  master_vpc_id                    = data.aws_vpc.active-elmae-default.id
  replica_vpc_id                   = var.deploy_rds_replica ? data.aws_vpc.passive-elmae-default[0].id : ""
  master_subnet_ids                = data.aws_subnet_ids.active-private-subnet.ids
  replica_subnet_ids               = var.deploy_rds_replica ? data.aws_subnet_ids.passive-private-subnet[0].ids : []
  master_db_sg_ingress_rule        = local.master_db_ingress_security_group_list
  replica_db_sg_ingress_rule       = local.replica_db_ingress_security_group_list
  db_parameter_group               = local.db_parameter_group
  db_port                          = var.db_port
  iops                             = var.iops
  backup_retention_period          = var.db_backup_retention_period
  allocated_storage                = var.db_allocated_storage
  preferred_backup_window          = var.db_preferred_backup_window
  db_name                          = local.db_name
  master_kms_key_id                = aws_kms_key.master-rds-kms-key.arn
  replica_kms_key_id               = var.deploy_rds_replica ? aws_kms_key.replica-rds-kms-key[0].arn : ""
  master_username                  = var.master_db_username
  master_password                  = var.master_db_password
  db_instance_class                = var.db_instance_class
  db_instance_identifier           = var.db_instance_identifier
  multi_az                         = var.db_multi_az
  deploy_rds_replica               = var.deploy_rds_replica
  engine_version                   = "14.4"
  engine_family                    = "postgres14"
  final_snapshot_identifier_prefix = "final"
  skip_final_snapshot              = false
  deletion_protection              = true
  storage_encrypted                = true
  tags                             = local.tags
  apply_immediately                = true # Enable for any urgent upgrades
  auto_minor_version_upgrade       = false
  allow_major_version_upgrade      = true
  snapshot_identifier              = var.snapshot_identifier
}

# Add EM VPN Private IPs as Ingress to Master RDS Security Group
resource aws_security_group_rule em-vpn-ips-to-master-rds-security-group-ingress {
  count             = local.environment_zone == "non-prod" ? 1 : 0
  provider          = aws.active
  security_group_id = module.rds.output_master_rds_security_group_id
  type              = "ingress"
  from_port         = var.db_port
  to_port           = var.db_port
  protocol          = "tcp"
  cidr_blocks       = local.vpn_private_ips
  description       = "EM VPN Private IPs"
}

# Add EM VPN Private IPs as Ingress to Master RDS Security Group
resource aws_security_group_rule em-vpn-ips-to-replica-rds-security-group-ingress {
  count             = var.deploy_rds_replica && local.environment_zone == "non-prod"? 1 : 0
  provider          = aws.passive
  security_group_id = module.rds.output_replica_rds_security_group_id
  type              = "ingress"
  from_port         = var.db_port
  to_port           = var.db_port
  protocol          = "tcp"
  cidr_blocks       = local.vpn_private_ips
  description       = "EM VPN Private IPs"
}


# Add EPC East Region VPC Private IPs  as Ingress to Master RDS Security Group
resource aws_security_group_rule em-epc-vpc-private-ips-to-master-rds-security-group-ingress {
  provider          = aws.active
  security_group_id = module.rds.output_master_rds_security_group_id
  type              = "ingress"
  from_port         = var.db_port
  to_port           = var.db_port
  protocol          = "tcp"
  cidr_blocks       = local.environment_zone == "non-prod" ? local.em_epc2_dev_east_private_ips : local.em_epc2_prod_east_private_ips
  description       = "EM EPC VPC Private IPs"
}

# Add EPC West Region VPC Private IPs  as Ingress to Replica RDS Security Group
resource aws_security_group_rule em-epc-vpc-private-ips-to-replica-rds-security-group-ingress {
  count             = var.deploy_rds_replica ? 1 : 0
  provider          = aws.passive
  security_group_id = module.rds.output_replica_rds_security_group_id
  type              = "ingress"
  from_port         = var.db_port
  to_port           = var.db_port
  protocol          = "tcp"
  cidr_blocks       = local.environment_zone == "non-prod" ? local.em_epc2_dev_west_private_ips : local.em_epc2_prod_west_private_ips
  description       = "EM EPC VPC Private IPs"
}
