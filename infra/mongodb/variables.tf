variable "atlas_org_id" {
  description = "ID de la organización de MongoDB Atlas"
  type        = string
}

variable "atlas_project_name" {
  description = "Nombre del proyecto de MongoDB Atlas"
  type        = string
  default     = "nequi-franchises"
}

variable "database_username" {
  description = "Usuario de base de datos de la API"
  type        = string
  default     = "nequi-api"
}

variable "database_password" {
  description = "Contraseña del usuario de base de datos"
  type        = string
  sensitive   = true
}

variable "local_ip" {
  description = "IP pública autorizada para conectar la API local"
  type        = string
}

variable "render_outbound_cidrs" {
  description = "Rangos de salida del servicio de Render"
  type        = set(string)
  default     = []
}