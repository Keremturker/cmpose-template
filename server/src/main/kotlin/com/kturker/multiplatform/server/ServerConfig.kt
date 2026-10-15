package com.kturker.multiplatform.server

private const val DEFAULT_PORT = 8080

data class ServerConfig(
    val port: Int,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String,
    val firebaseProjectId: String,
    val filesDir: String
) {
    // Keeps the password out of logs and exception messages.
    override fun toString() =
        "ServerConfig(port=$port, dbUrl=$dbUrl, dbUser=$dbUser, dbPassword=***, " +
            "firebaseProjectId=$firebaseProjectId, filesDir=$filesDir)"

    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): ServerConfig {
            fun req(name: String) = env[name]?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Missing environment variable $name")
            return ServerConfig(
                port = env["PORT"]?.toIntOrNull() ?: DEFAULT_PORT,
                dbUrl = req("DB_URL"),
                dbUser = req("DB_USER"),
                dbPassword = req("DB_PASSWORD"),
                firebaseProjectId = req("FIREBASE_PROJECT_ID"),
                filesDir = req("FILES_DIR")
            )
        }
    }
}
