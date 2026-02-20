---
skill: 보안 패턴 라이브러리
category: Security
patterns: Authentication, Authorization, Encryption, Validation
---

# Security Patterns Skill

Spring Security와 보안 베스트 프랙티스를 적용한 종합 보안 패턴 라이브러리입니다.

## 인증 (Authentication) 패턴

### JWT 기반 인증 시스템
```kotlin
@Component
class JwtAuthenticationProvider(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: UserDetailsService
) : AuthenticationProvider {
    
    override fun authenticate(authentication: Authentication): Authentication {
        val token = authentication.credentials as String
        
        if (!jwtTokenProvider.validateToken(token)) {
            throw BadCredentialsException("Invalid JWT token")
        }
        
        val username = jwtTokenProvider.getUsernameFromToken(token)
            ?: throw BadCredentialsException("Invalid JWT token")
        
        val userDetails = userDetailsService.loadUserByUsername(username)
        val authorities = jwtTokenProvider.getAuthoritiesFromToken(token)
        
        return JwtAuthenticationToken(userDetails, token, authorities)
    }
    
    override fun supports(authentication: Class<*>): Boolean {
        return JwtAuthenticationToken::class.java.isAssignableFrom(authentication)
    }
}

class JwtAuthenticationToken(
    private val principal: UserDetails,
    private val credentials: String,
    authorities: Collection<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {
    
    init {
        isAuthenticated = true
    }
    
    override fun getCredentials(): String = credentials
    override fun getPrincipal(): UserDetails = principal
}
```

### 다중 인증 방식 지원
```kotlin
@Component
class MultiFactorAuthenticationService(
    private val totpService: TOTPService,
    private val smsService: SMSService,
    private val emailService: EmailService,
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    enum class MFAMethod { TOTP, SMS, EMAIL }
    
    data class MFAChallenge(
        val userId: Long,
        val method: MFAMethod,
        val challengeId: String,
        val expiresAt: LocalDateTime
    )
    
    fun initiateChallenge(user: User, method: MFAMethod): MFAChallenge {
        val challengeId = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusMinutes(5)
        
        val challenge = MFAChallenge(user.id, method, challengeId, expiresAt)
        
        // Redis에 챌린지 저장 (5분 TTL)
        val cacheKey = "mfa:challenge:$challengeId"
        redisTemplate.opsForValue().set(
            cacheKey, 
            objectMapper.writeValueAsString(challenge),
            Duration.ofMinutes(5)
        )
        
        when (method) {
            MFAMethod.TOTP -> {
                // TOTP는 별도 전송 불필요 (사용자 앱에서 생성)
            }
            MFAMethod.SMS -> {
                val code = generateSecureCode()
                smsService.sendMFACode(user.phone, code)
                storeMFACode(challengeId, code)
            }
            MFAMethod.EMAIL -> {
                val code = generateSecureCode()
                emailService.sendMFACode(user.email, code)
                storeMFACode(challengeId, code)
            }
        }
        
        return challenge
    }
    
    fun verifyChallenge(challengeId: String, code: String): Boolean {
        val cacheKey = "mfa:challenge:$challengeId"
        val challengeJson = redisTemplate.opsForValue().get(cacheKey) 
            ?: throw MFAChallengeNotFoundException("Challenge not found: $challengeId")
        
        val challenge = objectMapper.readValue(challengeJson, MFAChallenge::class.java)
        
        if (LocalDateTime.now().isAfter(challenge.expiresAt)) {
            redisTemplate.delete(cacheKey)
            throw MFAChallengeExpiredException("Challenge expired: $challengeId")
        }
        
        val isValid = when (challenge.method) {
            MFAMethod.TOTP -> totpService.verifyCode(challenge.userId, code)
            MFAMethod.SMS, MFAMethod.EMAIL -> verifyStoredCode(challengeId, code)
        }
        
        if (isValid) {
            redisTemplate.delete(cacheKey)
            redisTemplate.delete("mfa:code:$challengeId")
        }
        
        return isValid
    }
    
    private fun generateSecureCode(): String {
        return (100000..999999).random().toString()
    }
    
    private fun storeMFACode(challengeId: String, code: String) {
        val hashedCode = BCryptPasswordEncoder().encode(code)
        redisTemplate.opsForValue().set(
            "mfa:code:$challengeId",
            hashedCode,
            Duration.ofMinutes(5)
        )
    }
    
    private fun verifyStoredCode(challengeId: String, code: String): Boolean {
        val hashedCode = redisTemplate.opsForValue().get("mfa:code:$challengeId") 
            ?: return false
        
        return BCryptPasswordEncoder().matches(code, hashedCode)
    }
}
```

## 인가 (Authorization) 패턴

### 역할 기반 접근 제어 (RBAC)
```kotlin
@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val name: String,
    
    val description: String?,
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = [JoinColumn(name = "role_id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id")]
    )
    val permissions: MutableSet<Permission> = mutableSetOf()
)

@Entity
@Table(name = "permissions")
class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(unique = true, nullable = false)
    val name: String,
    
    val resource: String,
    val action: String,
    val description: String?
)

@Service
class AuthorizationService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository
) {
    
    fun hasPermission(userId: Long, resource: String, action: String): Boolean {
        val user = userRepository.findById(userId) ?: return false
        
        return user.roles.flatMap { it.permissions }
            .any { permission ->
                permission.resource == resource && permission.action == action
            }
    }
    
    fun hasRole(userId: Long, roleName: String): Boolean {
        val user = userRepository.findById(userId) ?: return false
        return user.roles.any { it.name == roleName }
    }
    
    fun getUserPermissions(userId: Long): Set<String> {
        val user = userRepository.findById(userId) ?: return emptySet()
        
        return user.roles.flatMap { it.permissions }
            .map { "${it.resource}:${it.action}" }
            .toSet()
    }
}

// 커스텀 보안 어노테이션
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("@authorizationService.hasPermission(authentication.principal.id, #resource, #action)")
annotation class RequiresPermission(
    val resource: String,
    val action: String
)

// 사용 예시
@RestController
@RequestMapping("/api/v1/users")
class UserController {
    
    @GetMapping
    @RequiresPermission(resource = "USER", action = "READ")
    fun getUsers(): List<UserResponse> {
        // 구현
    }
    
    @PostMapping
    @RequiresPermission(resource = "USER", action = "CREATE")
    fun createUser(@RequestBody request: CreateUserRequest): UserResponse {
        // 구현
    }
}
```

### 속성 기반 접근 제어 (ABAC)
```kotlin
@Component
class AttributeBasedAuthorizationService {
    
    data class AuthorizationContext(
        val subject: Subject,
        val resource: Resource,
        val action: String,
        val environment: Environment
    )
    
    data class Subject(
        val userId: Long,
        val roles: Set<String>,
        val department: String?,
        val level: Int
    )
    
    data class Resource(
        val type: String,
        val id: String?,
        val ownerId: Long?,
        val sensitivity: SecurityLevel,
        val department: String?
    )
    
    data class Environment(
        val ipAddress: String,
        val timestamp: LocalDateTime,
        val userAgent: String?,
        val isInOfficeHours: Boolean
    )
    
    enum class SecurityLevel { PUBLIC, INTERNAL, CONFIDENTIAL, SECRET }
    
    fun isAuthorized(context: AuthorizationContext): Boolean {
        val policies = getPoliciesForResource(context.resource.type)
        
        return policies.any { policy ->
            evaluatePolicy(policy, context)
        }
    }
    
    private fun evaluatePolicy(policy: Policy, context: AuthorizationContext): Boolean {
        return policy.conditions.all { condition ->
            evaluateCondition(condition, context)
        }
    }
    
    private fun evaluateCondition(condition: Condition, context: AuthorizationContext): Boolean {
        return when (condition.type) {
            ConditionType.ROLE_REQUIRED -> 
                context.subject.roles.contains(condition.value)
            
            ConditionType.OWNER_ONLY -> 
                context.resource.ownerId == context.subject.userId
            
            ConditionType.SAME_DEPARTMENT -> 
                context.subject.department == context.resource.department
            
            ConditionType.MINIMUM_LEVEL -> 
                context.subject.level >= condition.value.toInt()
            
            ConditionType.OFFICE_HOURS_ONLY -> 
                context.environment.isInOfficeHours
            
            ConditionType.IP_WHITELIST -> 
                isIPWhitelisted(context.environment.ipAddress, condition.value)
            
            ConditionType.SECURITY_CLEARANCE -> 
                hasSecurityClearance(context.subject, context.resource.sensitivity)
        }
    }
    
    private fun hasSecurityClearance(subject: Subject, resourceSensitivity: SecurityLevel): Boolean {
        val clearanceLevel = when {
            subject.roles.contains("SECURITY_ADMIN") -> SecurityLevel.SECRET
            subject.roles.contains("MANAGER") -> SecurityLevel.CONFIDENTIAL
            subject.roles.contains("EMPLOYEE") -> SecurityLevel.INTERNAL
            else -> SecurityLevel.PUBLIC
        }
        
        return clearanceLevel.ordinal >= resourceSensitivity.ordinal
    }
}
```

## 데이터 암호화 패턴

### 필드 레벨 암호화
```kotlin
@Component
class FieldLevelEncryption(
    @Value("\${app.encryption.key}") private val encryptionKey: String
) {
    
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val secretKey = SecretKeySpec(encryptionKey.toByteArray(), "AES")
    
    fun encrypt(plaintext: String): EncryptedValue {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val ciphertext = cipher.doFinal(plaintext.toByteArray())
        val iv = cipher.iv
        
        return EncryptedValue(
            data = Base64.getEncoder().encodeToString(ciphertext),
            iv = Base64.getEncoder().encodeToString(iv)
        )
    }
    
    fun decrypt(encryptedValue: EncryptedValue): String {
        val spec = GCMParameterSpec(128, Base64.getDecoder().decode(encryptedValue.iv))
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val ciphertext = Base64.getDecoder().decode(encryptedValue.data)
        val plaintext = cipher.doFinal(ciphertext)
        
        return String(plaintext)
    }
}

@Embeddable
data class EncryptedValue(
    @Column(name = "encrypted_data", columnDefinition = "TEXT")
    val data: String,
    
    @Column(name = "encryption_iv")
    val iv: String
)

@Converter
class EncryptedStringConverter(
    private val fieldLevelEncryption: FieldLevelEncryption
) : AttributeConverter<String?, EncryptedValue?> {
    
    override fun convertToDatabaseColumn(attribute: String?): EncryptedValue? {
        return attribute?.let { fieldLevelEncryption.encrypt(it) }
    }
    
    override fun convertToEntityAttribute(dbData: EncryptedValue?): String? {
        return dbData?.let { fieldLevelEncryption.decrypt(it) }
    }
}

// 사용 예시
@Entity
class UserEntity(
    @Id val id: Long,
    
    val email: String, // 평문
    
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "phone_number")
    var phoneNumber: String?, // 암호화
    
    @Convert(converter = EncryptedStringConverter::class)
    @Column(name = "social_security_number")
    var ssn: String? // 암호화
)
```

### 키 관리 시스템
```kotlin
@Service
class KeyManagementService(
    private val keyRotationScheduler: KeyRotationScheduler,
    private val auditService: AuditService
) {
    
    private val masterKey = SecretKeySpec("your-master-key".toByteArray(), "AES")
    private val keyStore = mutableMapOf<String, EncryptionKey>()
    
    data class EncryptionKey(
        val keyId: String,
        val algorithm: String,
        val keyData: ByteArray,
        val createdAt: LocalDateTime,
        val version: Int,
        val isActive: Boolean
    )
    
    fun getCurrentKey(purpose: String): EncryptionKey {
        val keyId = "$purpose:current"
        return keyStore[keyId] ?: createNewKey(purpose)
    }
    
    fun getKeyById(keyId: String): EncryptionKey? {
        return keyStore[keyId]
    }
    
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 자정
    fun rotateKeys() {
        keyStore.keys.filter { it.endsWith(":current") }.forEach { keyId ->
            val purpose = keyId.substringBefore(":")
            val currentKey = keyStore[keyId]!!
            
            // 이전 키를 비활성화
            keyStore[keyId] = currentKey.copy(isActive = false)
            
            // 새 키 생성
            createNewKey(purpose)
            
            auditService.logKeyRotation(purpose, currentKey.keyId)
        }
    }
    
    private fun createNewKey(purpose: String): EncryptionKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val newKey = keyGenerator.generateKey()
        
        val encryptionKey = EncryptionKey(
            keyId = "$purpose:${UUID.randomUUID()}",
            algorithm = "AES",
            keyData = newKey.encoded,
            createdAt = LocalDateTime.now(),
            version = getNextVersion(purpose),
            isActive = true
        )
        
        keyStore["$purpose:current"] = encryptionKey
        keyStore[encryptionKey.keyId] = encryptionKey
        
        return encryptionKey
    }
    
    private fun getNextVersion(purpose: String): Int {
        return keyStore.values
            .filter { it.keyId.startsWith("$purpose:") }
            .maxOfOrNull { it.version }
            ?.plus(1) ?: 1
    }
}
```

## 입력 검증 및 새니타이제이션

### XSS 방지 필터
```kotlin
@Component
class XSSProtectionFilter : Filter {
    
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain
    ) {
        val wrappedRequest = XSSRequestWrapper(request as HttpServletRequest)
        chain.doFilter(wrappedRequest, response)
    }
}

class XSSRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    
    private val xssPatterns = listOf(
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL),
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL)
    )
    
    override fun getParameterValues(parameter: String): Array<String>? {
        val values = super.getParameterValues(parameter)
        return values?.map { sanitizeXSS(it) }?.toTypedArray()
    }
    
    override fun getParameter(parameter: String): String? {
        val value = super.getParameter(parameter)
        return value?.let { sanitizeXSS(it) }
    }
    
    private fun sanitizeXSS(value: String): String {
        var sanitized = value
        
        xssPatterns.forEach { pattern ->
            sanitized = pattern.matcher(sanitized).replaceAll("")
        }
        
        // HTML 엔티티 인코딩
        sanitized = StringEscapeUtils.escapeHtml4(sanitized)
        
        return sanitized
    }
}
```

### SQL 인젝션 방지
```kotlin
@Component
class SQLInjectionValidator {
    
    private val sqlKeywords = listOf(
        "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
        "EXEC", "EXECUTE", "UNION", "DECLARE", "CAST", "CONVERT",
        "SCRIPT", "JAVASCRIPT", "VBSCRIPT", "ONLOAD", "ONMOUSEOVER",
        "STYLE", "EXPRESSION", "APPLET", "META", "XML", "BLINK",
        "LINK", "IMPORT", "EMBED", "OBJECT", "FRAME", "FRAMESET",
        "IFRAME", "LAYER", "ILAYER", "BGSOUND"
    )
    
    private val sqlPatterns = listOf(
        Pattern.compile("(\\s|^)(select|insert|update|delete|drop|create|alter)\\s", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\s|^)(union|exec|execute)\\s", Pattern.CASE_INSENSITIVE),
        Pattern.compile("--", Pattern.CASE_INSENSITIVE),
        Pattern.compile("/\\*.*\\*/", Pattern.CASE_INSENSITIVE or Pattern.DOTALL),
        Pattern.compile("'\\s*(or|and)\\s*'", Pattern.CASE_INSENSITIVE),
        Pattern.compile("'\\s*(or|and)\\s*\\d+\\s*=\\s*\\d+", Pattern.CASE_INSENSITIVE)
    )
    
    fun validateInput(input: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        // SQL 키워드 검사
        sqlKeywords.forEach { keyword ->
            if (input.contains(keyword, ignoreCase = true)) {
                errors.add("SQL 키워드가 감지되었습니다: $keyword")
            }
        }
        
        // SQL 패턴 검사
        sqlPatterns.forEach { pattern ->
            if (pattern.matcher(input).find()) {
                errors.add("SQL 인젝션 패턴이 감지되었습니다")
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.valid()
        } else {
            ValidationResult.invalid(errors)
        }
    }
    
    fun sanitizeInput(input: String): String {
        var sanitized = input
        
        // 위험한 문자 이스케이프
        sanitized = sanitized.replace("'", "''")
        sanitized = sanitized.replace("\"", "\"\"")
        sanitized = sanitized.replace(";", "\\;")
        sanitized = sanitized.replace("--", "\\--")
        
        return sanitized
    }
}

// 검증 어노테이션
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SQLInjectionConstraintValidator::class])
annotation class NoSQLInjection(
    val message: String = "SQL 인젝션 공격이 감지되었습니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class SQLInjectionConstraintValidator(
    private val validator: SQLInjectionValidator
) : ConstraintValidator<NoSQLInjection, String> {
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        val result = validator.validateInput(value)
        return result.isValid
    }
}
```

## 감사 로깅 (Audit Logging)

### 보안 이벤트 감사 시스템
```kotlin
@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Enumerated(EnumType.STRING)
    val eventType: SecurityEventType,
    
    val userId: Long?,
    
    val sessionId: String?,
    
    val ipAddress: String?,
    
    val userAgent: String?,
    
    @Column(columnDefinition = "TEXT")
    val eventDetails: String?,
    
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    @Enumerated(EnumType.STRING)
    val severity: SecuritySeverity
)

enum class SecurityEventType {
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,
    PASSWORD_CHANGE,
    ACCOUNT_LOCKED,
    PERMISSION_DENIED,
    DATA_ACCESS,
    DATA_MODIFICATION,
    SUSPICIOUS_ACTIVITY,
    MFA_CHALLENGE,
    MFA_SUCCESS,
    MFA_FAILURE
}

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Service
class AuditService(
    private val auditLogRepository: AuditLogRepository,
    private val alertService: AlertService
) {
    
    @Async
    fun logSecurityEvent(
        eventType: SecurityEventType,
        userId: Long? = null,
        sessionId: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null,
        details: Map<String, Any> = emptyMap()
    ) {
        val auditLog = AuditLog(
            eventType = eventType,
            userId = userId,
            sessionId = sessionId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            eventDetails = objectMapper.writeValueAsString(details),
            severity = determineSeverity(eventType)
        )
        
        auditLogRepository.save(auditLog)
        
        // 심각도가 높은 이벤트는 즉시 알림
        if (auditLog.severity in listOf(SecuritySeverity.HIGH, SecuritySeverity.CRITICAL)) {
            alertService.sendSecurityAlert(auditLog)
        }
    }
    
    fun detectAnomalousActivity(userId: Long): List<SecurityAnomaly> {
        val recentEvents = auditLogRepository.findByUserIdAndTimestampAfter(
            userId = userId,
            timestamp = LocalDateTime.now().minusHours(24)
        )
        
        val anomalies = mutableListOf<SecurityAnomaly>()
        
        // 비정상적인 로그인 실패 패턴
        val failedLogins = recentEvents.filter { it.eventType == SecurityEventType.LOGIN_FAILURE }
        if (failedLogins.size > 10) {
            anomalies.add(SecurityAnomaly(
                type = AnomalyType.EXCESSIVE_LOGIN_FAILURES,
                severity = SecuritySeverity.HIGH,
                description = "24시간 내 ${failedLogins.size}회의 로그인 실패"
            ))
        }
        
        // 비정상적인 IP 주소 패턴
        val uniqueIPs = recentEvents.mapNotNull { it.ipAddress }.distinct()
        if (uniqueIPs.size > 5) {
            anomalies.add(SecurityAnomaly(
                type = AnomalyType.MULTIPLE_IP_ACCESS,
                severity = SecuritySeverity.MEDIUM,
                description = "24시간 내 ${uniqueIPs.size}개의 서로 다른 IP에서 접근"
            ))
        }
        
        // 업무시간 외 접근
        val offHoursAccess = recentEvents.filter { isOfficeHours(it.timestamp) }
        if (offHoursAccess.isNotEmpty()) {
            anomalies.add(SecurityAnomaly(
                type = AnomalyType.OFF_HOURS_ACCESS,
                severity = SecuritySeverity.LOW,
                description = "업무시간 외 ${offHoursAccess.size}회 접근"
            ))
        }
        
        return anomalies
    }
    
    private fun determineSeverity(eventType: SecurityEventType): SecuritySeverity {
        return when (eventType) {
            SecurityEventType.LOGIN_SUCCESS, 
            SecurityEventType.LOGOUT,
            SecurityEventType.DATA_ACCESS -> SecuritySeverity.LOW
            
            SecurityEventType.LOGIN_FAILURE,
            SecurityEventType.MFA_CHALLENGE,
            SecurityEventType.MFA_SUCCESS -> SecuritySeverity.MEDIUM
            
            SecurityEventType.PERMISSION_DENIED,
            SecurityEventType.MFA_FAILURE,
            SecurityEventType.DATA_MODIFICATION -> SecuritySeverity.HIGH
            
            SecurityEventType.ACCOUNT_LOCKED,
            SecurityEventType.PASSWORD_CHANGE,
            SecurityEventType.SUSPICIOUS_ACTIVITY -> SecuritySeverity.CRITICAL
        }
    }
    
    private fun isOfficeHours(timestamp: LocalDateTime): Boolean {
        val hour = timestamp.hour
        val dayOfWeek = timestamp.dayOfWeek
        
        return dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY && hour in 9..18
    }
}

data class SecurityAnomaly(
    val type: AnomalyType,
    val severity: SecuritySeverity,
    val description: String
)

enum class AnomalyType {
    EXCESSIVE_LOGIN_FAILURES,
    MULTIPLE_IP_ACCESS,
    OFF_HOURS_ACCESS,
    UNUSUAL_DATA_ACCESS_PATTERN,
    PRIVILEGE_ESCALATION_ATTEMPT
}
```

## 보안 모니터링 및 알림

### 실시간 위협 탐지
```kotlin
@Component
class ThreatDetectionSystem(
    private val redisTemplate: RedisTemplate<String, String>,
    private val alertService: AlertService
) {
    
    @EventListener
    fun onSecurityEvent(event: SecurityEvent) {
        when (event.type) {
            SecurityEventType.LOGIN_FAILURE -> handleLoginFailure(event)
            SecurityEventType.PERMISSION_DENIED -> handlePermissionDenied(event)
            SecurityEventType.SUSPICIOUS_ACTIVITY -> handleSuspiciousActivity(event)
            else -> {} // 다른 이벤트는 현재 처리하지 않음
        }
    }
    
    private fun handleLoginFailure(event: SecurityEvent) {
        val key = "login_failures:${event.ipAddress}"
        val count = redisTemplate.opsForValue().increment(key) ?: 1
        redisTemplate.expire(key, Duration.ofMinutes(15))
        
        when {
            count >= 5 -> {
                // IP 차단
                blockIP(event.ipAddress, Duration.ofHours(1))
                alertService.sendAlert(
                    AlertLevel.HIGH,
                    "IP ${event.ipAddress}에서 5회 이상 로그인 실패로 차단되었습니다"
                )
            }
            count >= 3 -> {
                alertService.sendAlert(
                    AlertLevel.MEDIUM,
                    "IP ${event.ipAddress}에서 ${count}회 로그인 실패가 감지되었습니다"
                )
            }
        }
    }
    
    private fun handlePermissionDenied(event: SecurityEvent) {
        val key = "permission_denied:${event.userId}"
        val count = redisTemplate.opsForValue().increment(key) ?: 1
        redisTemplate.expire(key, Duration.ofMinutes(10))
        
        if (count >= 3) {
            alertService.sendAlert(
                AlertLevel.HIGH,
                "사용자 ${event.userId}가 10분 내 ${count}회 권한 없는 리소스에 접근 시도했습니다"
            )
        }
    }
    
    private fun handleSuspiciousActivity(event: SecurityEvent) {
        // 즉시 보안팀에 알림
        alertService.sendAlert(
            AlertLevel.CRITICAL,
            "의심스러운 활동이 감지되었습니다: ${event.details}"
        )
        
        // 선택적으로 계정 임시 잠금
        if (event.userId != null) {
            temporarilyLockAccount(event.userId)
        }
    }
    
    private fun blockIP(ipAddress: String, duration: Duration) {
        redisTemplate.opsForValue().set(
            "blocked_ip:$ipAddress",
            LocalDateTime.now().toString(),
            duration
        )
    }
    
    private fun temporarilyLockAccount(userId: Long) {
        redisTemplate.opsForValue().set(
            "locked_account:$userId",
            LocalDateTime.now().toString(),
            Duration.ofHours(2)
        )
    }
}

@Component
class SecurityAlertService(
    private val emailService: EmailService,
    private val slackService: SlackService
) {
    
    enum class AlertLevel { LOW, MEDIUM, HIGH, CRITICAL }
    
    fun sendAlert(level: AlertLevel, message: String, details: Map<String, Any> = emptyMap()) {
        val alert = SecurityAlert(
            level = level,
            message = message,
            details = details,
            timestamp = LocalDateTime.now()
        )
        
        when (level) {
            AlertLevel.LOW -> logAlert(alert)
            AlertLevel.MEDIUM -> {
                logAlert(alert)
                slackService.sendToSecurityChannel(alert)
            }
            AlertLevel.HIGH -> {
                logAlert(alert)
                slackService.sendToSecurityChannel(alert)
                emailService.sendToSecurityTeam(alert)
            }
            AlertLevel.CRITICAL -> {
                logAlert(alert)
                slackService.sendToSecurityChannel(alert)
                emailService.sendToSecurityTeam(alert)
                // 추가적으로 SMS나 전화 알림도 가능
            }
        }
    }
    
    private fun logAlert(alert: SecurityAlert) {
        logger.warn("Security Alert [${alert.level}]: ${alert.message}")
    }
}

data class SecurityAlert(
    val level: SecurityAlertService.AlertLevel,
    val message: String,
    val details: Map<String, Any>,
    val timestamp: LocalDateTime
)
```

이러한 보안 패턴들을 적용하면 Spring Boot 애플리케이션에서 포괄적인 보안 체계를 구축할 수 있습니다. 각 패턴은 특정 보안 위협에 대응하도록 설계되었으며, 조합하여 사용할 때 더욱 강력한 보안 시스템을 만들 수 있습니다.