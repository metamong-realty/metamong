package com.metamong.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server


@OpenAPIDefinition(
    servers = [Server(
        url = "https://metamong-server-production.up.railway.app",
        description = "Default Server URL"
    )]
)
class OpenApiConfig