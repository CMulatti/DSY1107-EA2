variable "estudiante" {
  description = "Tu apellido en minúsculas. Hace únicos varios recursos."
  type        = string
  default     = "grupoxx-mulatti-parcial2"
}

variable "aws_region" {
  description = "Región donde se despliega todo."
  type        = string
  default     = "us-east-1"
}

variable "db_nombre" {
  type    = string
  default = "dsy1107"
}

variable "db_usuario" {
  type    = string
  default = "postgres" //database's master username
}

variable "db_password" {
  type      = string
  sensitive = true
  default   = "PSWD1234" //of our own choosing
}

variable "db_instancia" {
  type    = string
  default = "db.t3.micro"
}

variable "db_almacenamiento_gb" {
  type    = number
  default = 20
}

variable "db_version" {
  type    = string
  default = "17"
}

variable "origenes_postgres" {
  type    = list(string)
  default = ["0.0.0.0/0"]
}