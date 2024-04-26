data aws_caller_identity current {
  provider = aws.active
}

data aws_iam_role cop_service_role {
  provider = aws.active
  name     = local.cop_service_role_name
}

data aws_region active {
  provider = aws.active
}

data aws_region passive {
  provider = aws.passive
}

data aws_security_group active-cop-ec2-security-group {
  provider = aws.active
  vpc_id   = data.aws_vpc.active-elmae-default.id

  tags = {
    Name = local.active_region_cop_ecs_cluster_name
  }

}

data aws_security_group active-em-security-group {
  provider = aws.active
  vpc_id   = data.aws_vpc.active-elmae-default.id

  tags = {
    tag_name        = "em-epc-whitelist-ips"
    tag_environment = local.environment_zone
  }

}

data aws_security_group active-route53-healthcheck-security-group {
  provider = aws.active
  vpc_id   = data.aws_vpc.active-elmae-default.id

  tags = {
    tag_name        = "route53"
    tag_environment = local.environment_zone
  }

}

data aws_security_group passive-cop-ec2-security-group {
  count    = var.deploy_to_passive_region ? 1 : 0
  provider = aws.passive
  vpc_id   = data.aws_vpc.passive-elmae-default[0].id

  tags = {
    Name = local.passive_region_cop_ecs_cluster_name
  }

}

data aws_security_group passive-em-security-group {
  count    = var.deploy_to_passive_region ? 1 : 0
  provider = aws.passive
  vpc_id   = data.aws_vpc.passive-elmae-default[0].id

  tags = {
    tag_name        = "em-epc-whitelist-ips"
    tag_environment = local.environment_zone
  }

}

data aws_security_group passive-route53-healthcheck-security-group {
  count    = var.deploy_to_passive_region ? 1 : 0
  provider = aws.passive
  vpc_id   = data.aws_vpc.passive-elmae-default[0].id

  tags = {
    tag_name        = "route53"
    tag_environment = local.environment_zone
  }

}

data aws_subnet_ids active-private-subnet {
  provider = aws.active
  vpc_id   = data.aws_vpc.active-elmae-default.id
  tags     = {
    Name = "Private*"
  }
}

data aws_subnet_ids passive-private-subnet {
  count    = var.deploy_rds_replica ? 1 : 0
  provider = aws.passive
  vpc_id   = data.aws_vpc.passive-elmae-default[0].id
  tags     = {
    Name = "Private*"
  }
}

data aws_vpc active-elmae-default {
  provider = aws.active
  filter {
    name   = "tag-value"
    values = [
      local.vpc_name]
  }
}

data aws_vpc passive-elmae-default {
  count    = (var.deploy_to_passive_region || var.deploy_rds_replica) ? 1 : 0
  provider = aws.passive
  filter {
    name   = "tag-value"
    values = [
      local.vpc_name]
  }
}

data aws_sns_topic active-region-cloudwatch-alert-topic {
  provider = aws.active
  name     = local.cloudwatch_alert_topic
}

data aws_sns_topic passive-region-cloudwatch-alert-topic {
  provider = aws.passive
  name     = local.cloudwatch_alert_topic
}
