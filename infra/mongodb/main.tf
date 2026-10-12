resource "mongodbatlas_project" "nequi" {
  name   = var.atlas_project_name
  org_id = var.atlas_org_id
}

resource "mongodbatlas_advanced_cluster" "nequi" {
  project_id   = mongodbatlas_project.nequi.id
  name         = "nequi-franchises-cluster"
  cluster_type = "REPLICASET"

  replication_specs = [
    {
      region_configs = [
        {
          provider_name         = "TENANT"
          backing_provider_name = "AWS"
          region_name           = "US_EAST_1"
          priority              = 7

          electable_specs = {
            instance_size = "M0"
          }
        }
      ]
    }
  ]
}

resource "mongodbatlas_database_user" "api" {
  project_id         = mongodbatlas_project.nequi.id
  username           = var.database_username
  password           = var.database_password
  auth_database_name = "admin"

  roles {
    role_name     = "readWrite"
    database_name = "nequi_franchises"
  }

  scopes {
    name = mongodbatlas_advanced_cluster.nequi.name
    type = "CLUSTER"
  }
}

resource "mongodbatlas_project_ip_access_list" "local" {
  project_id = mongodbatlas_project.nequi.id
  ip_address = var.local_ip
  comment    = "Local API development"
}

resource "mongodbatlas_project_ip_access_list" "render" {
  for_each = var.render_outbound_cidrs

  project_id = mongodbatlas_project.nequi.id
  cidr_block = each.value
  comment    = "Acceso de la API desplegada en Render"
}