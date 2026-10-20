# El user pool es el "tenant" de la guía 1.2.3: el directorio donde viven los
# usuarios y, a la vez, el servidor de autorización que emite los tokens.
resource "aws_cognito_user_pool" "pool" {
  name = "dsy1107-vacaciones-2"

  user_pool_tier = "ESSENTIALS"

  # El correo es el nombre de usuario, como en cualquier CIAM.
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_uppercase = true
    require_numbers   = true
    require_symbols   = false
  }


  # Solo un administrador crea usuarios. Con auto-registro esto sería false.
  admin_create_user_config {
    allow_admin_create_user_only = true
  }

  lambda_config {
    pre_token_generation_config {
      lambda_arn      = aws_lambda_function.user_token_ms.arn
      lambda_version  = "V2_0"
    }
  }
}

resource "aws_cognito_user_pool_domain" "hosted_ui" {
  domain       = "dsy1107-vacaciones-2"
  user_pool_id = aws_cognito_user_pool.pool.id

  # 1 = Hosted UI clásica. La versión 2 (Managed Login) exige definir un
  # branding style o la pantalla de login queda en blanco.
  managed_login_version = 1
}


# Nuestro futuro front en React es un CLIENTE PÚBLICO: su código se descarga
# completo en el navegador, así que no puede guardar un secreto. Por eso
# generate_secret = false y por eso el flujo será Authorization Code + PKCE.
resource "aws_cognito_user_pool_client" "spa" {
  name         = "spa-react"
  user_pool_id = aws_cognito_user_pool.pool.id

  generate_secret = false

  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  supported_identity_providers         = ["COGNITO"]
  allowed_oauth_scopes                 = ["openid", "email", "profile", "aws.cognito.signin.user.admin"]

  # Debe coincidir EXACTAMENTE con el redirect_uri que envíe la aplicación,
  # incluida la barra final. Es el error número uno de esta actividad.
  callback_urls = ["http://localhost:5173/",  "${local.url_amplify}/",]
  logout_urls   = ["http://localhost:5173/",  "${local.url_amplify}/",]

  # ALLOW_USER_PASSWORD_AUTH se habilita solo para poder probar por consola en
  # el paso 7. En el paso 10 se quita: una app nunca debe ver la contraseña.
  explicit_auth_flows = ["ALLOW_USER_PASSWORD_AUTH", "ALLOW_REFRESH_TOKEN_AUTH"]

  # Tokens cortos a propósito: que expiren durante la clase es parte del ejercicio.
  access_token_validity = 60
  id_token_validity     = 60

  token_validity_units {
    access_token = "minutes"
    id_token     = "minutes"
  }

}

#un usuario de prueba, ya confirmado y con contraseña definitiva
# sin esto habría que crearlo a mano en la consola antes de cada demo

#usuario solicitante 1 (employee side)
resource "aws_cognito_user" "demo" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = "test@duoc.cl"
  password     = "Duoc2026"

  attributes = {
    email          = "test@duoc.cl"
    email_verified = true
  name = "carla" }

  #No enviar correo de invitación: el usuario es ficticio
  message_action = "SUPPRESS"
}

#usuario solicitante 2 (employee side)
resource "aws_cognito_user" "demo_solicitante2" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = "solicitante@duoc.cl"
  password     = "Duoc2026"

  attributes = {
    email          = "solicitante@duoc.cl"
    email_verified = true
    name           = "Solicitante Demo 2"
  }

  message_action = "SUPPRESS"
}



#usuario aprobador (employer side)
resource "aws_cognito_user" "demo_aprobador" {
  user_pool_id = aws_cognito_user_pool.pool.id
  username     = "aprobador@duoc.cl"
  password     = "Duoc2026"

  attributes = {
    email          = "aprobador@duoc.cl"
    email_verified = true
    name           = "Aprobador Demo"
  }

  message_action = "SUPPRESS"
}

resource "aws_cognito_user_in_group" "demo_aprobador" {
  user_pool_id = aws_cognito_user_pool.pool.id
  group_name   = aws_cognito_user_group.aprobadores.name
  username     = aws_cognito_user.demo_aprobador.username
}


# -----------------------------------------------------------------------------
# El resource server: declara que los scopes EXISTEN. No los concede a nadie.
# -----------------------------------------------------------------------------
resource "aws_cognito_resource_server" "solicitudes" {
  identifier   = "solicitudes"
  name         = "API de solicitudes"
  user_pool_id = aws_cognito_user_pool.pool.id

  scope {
    scope_name        = "read"
    scope_description = "Consultar solicitudes"
  }
  scope {
    scope_name        = "write"
    scope_description = "Crear, editar y eliminar solicitudes propias"
  }
    scope {
    scope_name        = "aprobar"
    scope_description = "Aprobar o rechazar solicitudes"
  }
}

# -----------------------------------------------------------------------------
# Los grupos: aqui vive el permiso de cada persona.
# -----------------------------------------------------------------------------
resource "aws_cognito_user_group" "solicitantes" {
  name         = "solicitantes"
  user_pool_id = aws_cognito_user_pool.pool.id
  description  = "Puede crear y gestionar sus propias solicitudes"
}

resource "aws_cognito_user_group" "aprobadores" {
  name         = "aprobadores"
  user_pool_id = aws_cognito_user_pool.pool.id
  description  = "Puede aprobar o rechazar solicitudes"
}

resource "aws_cognito_user_in_group" "demo_solicitante" {
  user_pool_id = aws_cognito_user_pool.pool.id
  group_name   = aws_cognito_user_group.solicitantes.name
  username     = aws_cognito_user.demo.username
}

resource "aws_cognito_user_in_group" "demo_solicitante2" {
  user_pool_id = aws_cognito_user_pool.pool.id
  group_name   = aws_cognito_user_group.solicitantes.name
  username     = aws_cognito_user.demo_solicitante2.username
}




#This creates nothing on AWS. But it makes terraform output print a value Terraform already knows.
# application.yaml and AWS CLI commands will need this.
output "cognito_pool_id" {
  value = aws_cognito_user_pool.pool.id
}

output "cognito_issuer" {
  value = "https://${aws_cognito_user_pool.pool.endpoint}"
}