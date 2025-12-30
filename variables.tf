variable "project_id" {
  description = "The GCP Project ID"
  type        = string
}

variable "region" {
  description = "The GCP Region"
  type        = string
  default     = "asia-east1"
}

variable "repository_id" {
  description = "The Artifact Registry Repository ID"
  type        = string
  default     = "gcp-ig-repo"
}

variable "service_name" {
  description = "The Cloud Run Service Name"
  type        = string
  default     = "gcp-ig-webhook"
}
