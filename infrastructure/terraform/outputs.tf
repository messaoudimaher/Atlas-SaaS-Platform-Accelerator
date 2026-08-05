output "vpc_id" {
  description = "The ID of the provisioned AWS VPC"
  value       = aws_vpc.main.id
}

output "database_endpoint" {
  description = "Connection endpoint URL for the RDS PostgreSQL database instance"
  value       = aws_db_instance.postgres.endpoint
}

output "redis_primary_endpoint" {
  description = "Connection URL endpoint for Amazon ElastiCache Redis primary node"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "s3_bucket_name" {
  description = "Identifier name of the private S3 uploads storage bucket"
  value       = aws_s3_bucket.uploads.id
}
