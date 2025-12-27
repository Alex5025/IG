terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 4.0"
    }
  }
}

provider "google" {
  # 請替換成您的 GCP Project ID
  project = "gcp-ig-482520"
  # 設定預設區域為台灣 (Tier 1 定價)
  region  = "asia-east1"
}

# 1. 定義 Cloud Run 服務
resource "google_cloud_run_v2_service" "default" {
  name     = "gcp-ig-webhook"
  location = "asia-east1"
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    containers {
      # 初始部署可以使用 Google 的範例映像檔，之後 CD 流程會覆蓋它
      image = "us-docker.pkg.dev/cloudrun/container/hello"

      resources {
        limits = {
          cpu    = "1000m"
          memory = "512Mi"
        }
      }

      # 環境變數將由 GitHub Actions (CD) 部署時注入
      # 這裡不需要定義，以免 Terraform 覆蓋掉 CD 的設定
    }
  }

  # 忽略環境變數與映像檔的變更
  # 這樣 Terraform 只負責管理基礎設施 (如 CPU/Memory/權限)，而不會去重設由 CD 部署的程式碼與變數
  lifecycle {
    ignore_changes = [
      template[0].containers[0].image,
      template[0].containers[0].env
    ]
  }
}

# 2. 設定公開存取權限 (允許未驗證的調用 - Webhook 需要)
resource "google_cloud_run_service_iam_member" "public_access" {
  service  = google_cloud_run_v2_service.default.name
  location = google_cloud_run_v2_service.default.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
