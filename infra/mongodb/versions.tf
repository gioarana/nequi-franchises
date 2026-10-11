terraform {
  required_version = ">= 1.6.0, < 2.0.0"

  required_providers {
    mongodbatlas = {
      source  = "mongodb/mongodbatlas"
      version = "~> 2.19.0"
    }
  }
}