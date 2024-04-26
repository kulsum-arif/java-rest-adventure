variable active_region_desired_count {
  description = "The number of simultaneous tasks to be run on the ECS cluster in the active region"
  type        = number
  default     = 1
}

variable active_region_max_count {
  description = "Active region maximum capacity limit to scale out in response to changing demand"
  type        = number
  default     = 1
}

variable active_region_min_count {
  description = "Active region minimum capacity limit to scale out in response to changing demand"
  type        = number
  default     = 1
}

variable build_number {
  description = "Current build number for this deployment"
  type        = string
  default     = ""
}

variable certificate_domain_name {
  description = "A domain name for which the certificate should be issued"
  type        = string
  default     = ""
}

variable container_registry_url {
  description = "Container registry url for docker image such as JFrog artifactory path and AWS ECR"
  type        = string
}

variable cpu {
  description = "The minimum number of CPU units to reserve for the container"
  type        = number
}

variable "db_allocated_storage" {
  description = "The db allocated storage size, specified in gigabytes (GB)"
  type        = number
}

variable "db_backup_retention_period" {
  description = "The number of days during which automatic DB snapshots are retained"
  type        = number
}

variable "db_instance_class" {
  description = "The name of the compute and memory capacity classes of the DB instance"
  type        = string
}

variable "db_instance_identifier" {
  description = "A name for the DB instance. AWS CloudFormation converts it to lowercase"
  type        = string
}

variable "db_multi_az" {
  description = "Specifies if the database instance is a multiple Availability Zone deployment"
  type        = bool
}

variable "db_port" {
  description = "The port on which the DB accepts connections"
  type        = number
}

variable "db_preferred_backup_window" {
  description = "The daily time range during which automated backups are performed if automated backups are enabled"
  type        = string
}

variable "deploy_rds_replica" {
  description = "Specifies if RDS replica needs to be deployed in the non-primary region"
  type        = bool
  default     = false
}

variable deploy_to_passive_region {
  description = "Specifies if ECS resources should be created in the passive region"
  type        = bool
  default     = false
}

variable docker_image {
  description = "Docker Image URL"
  type        = string
}

variable enable_route53 {
  description = "Specifies if route53 should be enabled. This requires alb to be enabled"
  type        = bool
  default     = true
}

variable "environment" {
  description = "Application environment such as non-prod and prod"
  type        = string
}

variable "hosted_zone_name" {
  description = "The name of the domain for the hosted zone where the Type A record set should be added"
  type        = string
}

variable hosted_zone_record_name {
  description = "The hosted zone record name"
  type        = string
  default     = ""
}

variable "iops" {
  description = "The number of I/O operations per second (IOPS) that the database provisions"
  type        = number
}

variable "master_db_username" {
  description = "The master user name for the DB instance"
  type        = string
}

variable "master_db_password" {
  description = "The master password for the DB instance"
  type        = string
}

variable passive_region_desired_count {
  description = "The number of simultaneous tasks to be run on the ECS cluster in the passive region"
  type        = number
  default     = 1
}

variable passive_region_max_count {
  description = "Passive region maximum capacity limit to scale out in response to changing demand"
  type        = number
  default     = 1
}

variable passive_region_min_count {
  description = "Passive region minimum capacity limit to scale out in response to changing demand"
  type        = number
  default     = 1
}

variable soft_memory_limit {
  description = "Container Soft Memory Limit"
  type        = number
  default     = 512
}

variable webhook_oapi_client_secret {
  description = "Webhook OAPI Client Secret"
  type        = string
}

variable db_password {
  description = "Database password"
  type        = string
}

variable dc_region {
  description = "Data Center location for Kafka"
  type        = string
}

variable kafka_bootstrap_servers {
  description = "Kafka bootstrap server endpoint"
  type        = string
}

variable kafka_producer_password {
  description = "Kafka Producer password"
  type        = string
}

variable log_retention_days {
  description = "Cloudwatch log retention in days"
  type        = number
  default     = 1
}

variable request_count_autoscale_up_alarm_threshold {
  description = "ALB request count autoscale up alarm threshold"
  type        = string
  default     = "200"
}

variable request_count_autoscale_down_alarm_threshold {
  description = "ALB request count autoscale down alarm threshold"
  type        = string
  default     = "190"
}

variable snapshot_identifier {
  description = "Create database from this snapshot. Used during recovering from DR"
  type        = string
  default     = null
}

variable request_count_autoscale_up_alarm_statistic {
  description = "Request count based autoscale up alarm statistic"
  type        = string
  default     = "Sum"
}

variable request_count_autoscale_down_alarm_statistic {
  description = "Request count based autoscale down alarm statistic"
  type        = string
  default     = "Sum"
}

variable health_check_interval {
  description = "Health Check Interval In Seconds"
  type        = number
  default     = 40
}

variable health_check_timeout {
  description = "Health Check timeout In Seconds"
  type        = number
  default     = 30
}

variable health_check_healthy_threshold {
  description = "Health Check Healthy Threshold"
  type        = number
  default     = 2
}

variable health_check_unhealthy_threshold {
  description = "Health Check Unhealthy Threshold"
  type        = number
  default     = 4
}
