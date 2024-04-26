# s3 bucket creation
resource "aws_s3_bucket" "epc_resources_bucket" {
  provider = aws.active
  bucket   = local.s3_bucket_name
  acl      = "private"

  tags = {
    Name        = local.tags.tag_name
    Project     = local.tags.tag_project
    Department  = local.tags.tag_department
    Environment = local.environment_zone
  }
}

# uploading multiple files to s3
resource "aws_s3_bucket_object" "epc_resources_billing_object" {
  for_each = fileset(path.module, "schemas/**/*.json")

  provider = aws.active
  bucket   = aws_s3_bucket.epc_resources_bucket.bucket
  key      = each.value
  source   = "${path.module}/${each.value}"
  etag     = filemd5(each.value)
}
