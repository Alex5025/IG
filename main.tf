terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 4.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# 1. 啟用必要的 Google Cloud APIs
resource "google_project_service" "run_api" {
  service = "run.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "artifact_registry_api" {
  service = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

# 2. 建立 Artifact Registry Repository (存放 Docker Image)
resource "google_artifact_registry_repository" "repo" {
  location      = var.region
  repository_id = var.repository_id
  description   = "Docker repository for GCP IG Webhook"
  format        = "DOCKER"

  depends_on = [google_project_service.artifact_registry_api]
}

# 3. 定義 Cloud Run 服務
resource "google_cloud_run_v2_service" "default" {
  name     = var.service_name
  location = var.region
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

  depends_on = [google_project_service.run_api]
}

# 2. 設定公開存取權限 (允許未驗證的調用 - Webhook 需要)
resource "google_cloud_run_service_iam_member" "public_access" {
  service  = google_cloud_run_v2_service.default.name
  location = google_cloud_run_v2_service.default.location
  role     = "roles/run.invoker"
  member   = "allUsers"
}
