variable "aws_region" {
  description = "The target AWS Region for deployment"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "The target environment namespace"
  type        = string
  default     = "production"
}

variable "cluster_name" {
  description = "Name of the target Amazon EKS Kubernetes Cluster"
  type        = string
  default     = "atlas-saas-cluster"
}

variable "db_instance_class" {
  description = "RDS PostgreSQL node database instance size"
  type        = string
  default     = "db.r6g.large"
}

variable "db_username" {
  description = "Master administrative username for RDS PostgreSQL"
  type        = string
  default     = "atlas_db_admin"
}

variable "db_password" {
  description = "Master administrative password for RDS PostgreSQL"
  type        = string
  sensitive   = true
}

variable "redis_node_type" {
  description = "Amazon ElastiCache Redis replication node capacity class"
  type        = string
  default     = "cache.t4g.medium"
}
