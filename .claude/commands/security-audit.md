---
description: 보안 취약점을 스캔하고 보안 강화 방안을 제안합니다 (OWASP Top 10 기준)
---

# Security Audit Command

보안 취약점을 종합적으로 분석하고 개선 방안을 제시하는 명령어입니다.

## Usage

```bash
/security-audit [options]
```

## Examples

```bash
# 전체 보안 검사
/security-audit

# 특정 도메인만 검사
/security-audit --domain user

# OWASP Top 10 기준 검사
/security-audit --owasp

# 자동 수정 제안 포함
/security-audit --with-fixes
```

## What This Command Does

### 1. OWASP Top 10 보안 취약점 검사

#### A01 - Broken Access Control
```kotlin
// 🔍 Analyzing: Access Control Issues

❌ CRITICAL: Missing Authorization Check
File: UserController.kt:45
Code: @DeleteMapping("/{id}")
      fun deleteUser(@PathVariable id: Long)
Issue: No role-based access control
Fix: Add @PreAuthorize("hasRole('ADMIN') or principal.id == #id")

❌ HIGH: Insecure Direct Object Reference  
File: PostController.kt:67
Code: fun getPost(@PathVariable id: Long): Post
Issue: Users can access any post by guessing ID
Fix: Add ownership validation or access control

✅ PASS: JWT Token Validation
File: JwtAuthenticationFilter.kt
Status: Proper token validation implemented
```

#### A02 - Cryptographic Failures
```kotlin
// 🔍 Analyzing: Cryptographic Implementation

❌ CRITICAL: Hardcoded Secret Key
File: application.yml:15
Code: jwt.secret: "my-super-secret-key"
Issue: Secret key in source code
Fix: Use environment variable: ${JWT_SECRET}

❌ HIGH: Weak Password Hashing
File: PasswordEncoder.kt:23
Code: MessageDigest.getInstance("MD5")
Issue: MD5 is cryptographically broken
Fix: Use BCryptPasswordEncoder

⚠️  MEDIUM: Insufficient Key Length
File: CryptoService.kt:34
Code: KeyGenerator.getInstance("AES").init(128)
Issue: 128-bit AES may not be sufficient
Recommendation: Use 256-bit AES for better security

✅ PASS: HTTPS Enforcement
File: SecurityConfig.kt
Status: HSTS headers properly configured
```

#### A03 - Injection Attacks
```kotlin
// 🔍 Analyzing: Injection Vulnerabilities

❌ CRITICAL: SQL Injection Risk
File: UserRepository.kt:89
Code: entityManager.createQuery("SELECT u FROM User u WHERE u.email = '" + email + "'")
Issue: String concatenation in SQL query
Fix: Use parameterized queries

❌ HIGH: NoSQL Injection Risk
File: UserActivityRepository.kt:45
Code: mongoTemplate.find(Query.query(Criteria.where("userId").is(request.getUserId())))
Issue: Unvalidated user input in MongoDB query
Fix: Add input validation and sanitization

❌ MEDIUM: Command Injection Risk
File: FileService.kt:67
Code: Runtime.getRuntime().exec("convert " + userInput + " output.jpg")
Issue: User input in system command
Fix: Use parameterized commands or whitelist validation

✅ PASS: JPA Parameter Binding
File: PostRepository.kt
Status: Using @Query with proper parameter binding
```

#### A04 - Insecure Design
```kotlin
// 🔍 Analyzing: Design Security Issues

❌ HIGH: Missing Rate Limiting
File: AuthController.kt
Issue: No protection against brute force attacks
Fix: Add @RateLimit annotation on login endpoint

❌ MEDIUM: Insufficient Session Management
File: SecurityConfig.kt
Issue: Session not properly invalidated on logout
Fix: Implement proper session invalidation

⚠️  LOW: Missing Security Headers
File: SecurityConfig.kt
Issue: Some security headers not configured
Recommendation: Add X-Content-Type-Options, X-Frame-Options
```

#### A05 - Security Misconfiguration
```kotlin
// 🔍 Analyzing: Configuration Security

❌ CRITICAL: Debug Mode in Production
File: application-prod.yml:8
Code: logging.level.com.metamong: DEBUG
Issue: Debug logging in production
Fix: Set to INFO or WARN level

❌ HIGH: Default Credentials
File: application.yml:25
Code: spring.datasource.username: admin
      spring.datasource.password: admin
Issue: Default database credentials
Fix: Use strong, unique credentials

❌ MEDIUM: Permissive CORS Configuration
File: SecurityConfig.kt:45
Code: .allowedOrigins("*")
Issue: Allows requests from any origin
Fix: Specify allowed origins explicitly

⚠️  MEDIUM: Unnecessary Features Enabled
File: application.yml
Issue: H2 console enabled in production
Recommendation: Disable in production profile
```

### 2. 자동 보안 수정 제안

#### 2.1 Access Control 개선
```kotlin
// BEFORE: Insecure Controller
@RestController
class UserController(
    private val userService: UserService
) {
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) {
        userService.deleteUser(id)  // ❌ Anyone can delete any user
    }
    
    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): UserResponse {
        return userService.getUser(id)  // ❌ No access control
    }
}

// AFTER: Secure Controller
@RestController
@PreAuthorize("isAuthenticated()")
class UserController(
    private val userService: UserService
) {
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #id")
    fun deleteUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ) {
        userService.deleteUser(id)  // ✅ Only admin or owner can delete
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.id == #id or @userSecurity.canViewUser(#id)")
    fun getUser(
        @PathVariable id: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): UserResponse {
        return userService.getUser(id)  // ✅ Proper access control
    }
}

// Security Helper Component
@Component
class UserSecurity {
    fun canViewUser(targetUserId: Long): Boolean {
        val currentUser = SecurityContextHolder.getContext().authentication.principal as UserPrincipal
        return currentUser.id == targetUserId || currentUser.hasRole("ADMIN")
    }
}
```

#### 2.2 암호화 개선
```kotlin
// BEFORE: Weak Encryption
@Service
class CryptoService {
    private val secretKey = "my-secret-key"  // ❌ Hardcoded
    
    fun encrypt(data: String): String {
        val digest = MessageDigest.getInstance("MD5")  // ❌ Weak algorithm
        return digest.digest(data.toByteArray()).toString()
    }
}

// AFTER: Strong Encryption
@Service
class CryptoService(
    @Value("\${app.encryption.secret-key}") private val secretKey: String
) {
    private val key: SecretKey
    private val cipher: Cipher
    
    init {
        require(secretKey.isNotBlank()) { "Encryption secret key must not be empty" }
        val decodedKey = Base64.getDecoder().decode(secretKey)
        key = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        cipher = Cipher.getInstance("AES/GCM/NoPadding")  // ✅ Strong encryption
    }
    
    fun encrypt(plainText: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plainText.toByteArray())
        
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        
        return Base64.getEncoder().encodeToString(combined)
    }
    
    fun decrypt(encryptedText: String): String {
        val combined = Base64.getDecoder().decode(encryptedText)
        val iv = combined.sliceArray(0..11)
        val cipherText = combined.sliceArray(12 until combined.size)
        
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        
        return String(cipher.doFinal(cipherText))
    }
}
```

#### 2.3 SQL Injection 방지
```kotlin
// BEFORE: Vulnerable to SQL Injection
@Repository
class UserRepositoryImpl(
    private val entityManager: EntityManager
) : UserRepository {
    fun findUsersByEmail(email: String): List<User> {
        val query = "SELECT u FROM User u WHERE u.email = '$email'"  // ❌ Vulnerable
        return entityManager.createQuery(query, User::class.java).resultList
    }
    
    fun searchUsers(keyword: String): List<User> {
        val sql = "SELECT * FROM users WHERE name LIKE '%" + keyword + "%'"  // ❌ Vulnerable
        return entityManager.createNativeQuery(sql, User::class.java).resultList as List<User>
    }
}

// AFTER: Safe from SQL Injection
@Repository
class UserRepositoryImpl(
    private val entityManager: EntityManager
) : UserRepository {
    fun findUsersByEmail(email: String): List<User> {
        return entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", 
            User::class.java
        )
        .setParameter("email", email)  // ✅ Parameterized query
        .resultList
    }
    
    fun searchUsers(keyword: String): List<User> {
        // Input validation
        require(keyword.length <= 100) { "Search keyword too long" }
        require(!keyword.contains(";", ignoreCase = true)) { "Invalid characters in keyword" }
        
        return entityManager.createNativeQuery(
            "SELECT * FROM users WHERE name LIKE :keyword",
            User::class.java
        )
        .setParameter("keyword", "%$keyword%")  // ✅ Parameterized query
        .resultList as List<User>
    }
}
```

### 3. 보안 테스트 자동 생성

#### 3.1 인증/인가 테스트
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class SecurityAuditTest(
    @Autowired val mockMvc: MockMvc
) : BehaviorSpec({
    
    Given("보안 엔드포인트 접근 테스트") {
        When("인증 없이 보호된 리소스에 접근하면") {
            val result = mockMvc.get("/api/v1/users/1")
            
            Then("401 Unauthorized 응답") {
                result.andExpect {
                    status { isUnauthorized() }
                }
            }
        }
        
        When("권한 없는 사용자가 관리자 API에 접근하면") {
            val userToken = "Bearer user-jwt-token"
            
            val result = mockMvc.delete("/api/v1/admin/users/1") {
                header("Authorization", userToken)
            }
            
            Then("403 Forbidden 응답") {
                result.andExpect {
                    status { isForbidden() }
                }
            }
        }
        
        When("다른 사용자의 개인정보에 접근하려 하면") {
            val userToken = "Bearer user-2-jwt-token"  // User ID: 2
            
            val result = mockMvc.get("/api/v1/users/1/profile") {  // Accessing User ID: 1
                header("Authorization", userToken)
            }
            
            Then("403 Forbidden 응답") {
                result.andExpect {
                    status { isForbidden() }
                }
            }
        }
    }
})
```

#### 3.2 입력 검증 테스트
```kotlin
class InputValidationSecurityTest : BehaviorSpec({
    
    Given("악성 입력값에 대한 방어 테스트") {
        When("SQL Injection 시도시") {
            val maliciousInput = "'; DROP TABLE users; --"
            
            Then("입력값이 거부된다") {
                shouldThrow<ValidationException> {
                    userService.searchUsers(maliciousInput)
                }
            }
        }
        
        When("XSS 스크립트 입력시") {
            val xssPayload = "<script>alert('XSS')</script>"
            
            val request = CreateUserRequest(
                email = "test@example.com",
                nickname = xssPayload,  // XSS attempt
                password = "password123"
            )
            
            Then("스크립트가 이스케이프되거나 거부된다") {
                val user = userService.createUser(request.toCommand())
                user.nickname shouldNotContain "<script>"
            }
        }
        
        When("과도하게 긴 입력값 제공시") {
            val longString = "a".repeat(10000)
            
            val request = CreateUserRequest(
                email = "test@example.com",
                nickname = longString,
                password = "password123"
            )
            
            Then("입력 길이 제한으로 거부된다") {
                shouldThrow<ValidationException> {
                    userService.createUser(request.toCommand())
                }
            }
        }
    }
})
```

### 4. 보안 설정 강화

#### 4.1 개선된 SecurityConfig
```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
) {
    
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }  // API 전용이므로 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                it.maximumSessions(1)  // ✅ 동시 세션 제한
                it.maxSessionsPreventsLogin(false)
            }
            .headers { headers ->
                headers
                    .frameOptions().deny()  // ✅ Clickjacking 방지
                    .contentTypeOptions().and()  // ✅ MIME 타입 스니핑 방지
                    .httpStrictTransportSecurity { hstsConfig ->
                        hstsConfig
                            .maxAgeInSeconds(31536000)  // 1년
                            .includeSubdomains(true)
                    }
            }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                it.accessDeniedHandler(customAccessDeniedHandler())
            }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.requestMatchers("/api/v1/public/**").permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                it.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                it.requestMatchers("/actuator/health").permitAll()
                it.requestMatchers("/actuator/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterBefore(
                rateLimitFilter(),  // ✅ Rate Limiting 추가
                JwtAuthenticationFilter::class.java
            )
            .build()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf(  // ✅ 특정 오리진만 허용
                "http://localhost:3000",
                "https://metamong.com"
            )
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
```

#### 4.2 Rate Limiting 구현
```kotlin
@Component
class RateLimitFilter : OncePerRequestFilter() {
    
    private val rateLimiter = CacheBuilder.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build<String, AtomicInteger>()
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = getClientIp(request)
        val count = rateLimiter.getIfPresent(clientIp) ?: AtomicInteger(0)
        
        if (count.incrementAndGet() > 100) {  // 분당 100회 제한
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("""{"error":"Rate limit exceeded"}""")
            return
        }
        
        rateLimiter.put(clientIp, count)
        filterChain.doFilter(request, response)
    }
    
    private fun getClientIp(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr
    }
}
```

### 5. 보안 모니터링

#### 5.1 보안 이벤트 로깅
```kotlin
@Component
class SecurityEventLogger {
    
    private val securityLogger = LoggerFactory.getLogger("SECURITY")
    
    @EventListener
    fun handleAuthenticationFailure(event: AuthenticationFailureBadCredentialsEvent) {
        val request = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val ip = request.request.remoteAddr
        
        securityLogger.warn(
            "Authentication failure: username={}, ip={}, timestamp={}", 
            event.authentication.name,
            ip,
            LocalDateTime.now()
        )
        
        // 너무 많은 실패시 IP 차단
        checkForBruteForceAttack(ip)
    }
    
    @EventListener
    fun handleAccessDenied(event: AuthorizationDeniedEvent) {
        securityLogger.warn(
            "Access denied: user={}, resource={}, timestamp={}",
            event.authentication.name,
            event.resource,
            LocalDateTime.now()
        )
    }
    
    private fun checkForBruteForceAttack(ip: String) {
        // Redis를 사용한 IP 기반 차단 로직
        val failureCount = redisTemplate.opsForValue().increment("failed_login:$ip") ?: 0
        
        if (failureCount > 10) {
            // IP 차단
            redisTemplate.opsForValue().set("blocked_ip:$ip", "blocked", Duration.ofHours(1))
            securityLogger.error("IP blocked due to brute force: {}", ip)
        }
    }
}
```

## 보안 체크리스트

### 🔐 인증/인가
- [ ] JWT 토큰 검증 구현
- [ ] Role 기반 접근 제어
- [ ] API 엔드포인트별 권한 설정
- [ ] Session 관리 보안
- [ ] 브루트 포스 공격 방지

### 🛡️ 암호화
- [ ] 비밀번호 BCrypt 해싱
- [ ] 민감정보 AES 암호화  
- [ ] HTTPS 강제 사용
- [ ] 암호화 키 안전 관리
- [ ] Salt 사용한 해싱

### 🚫 입력 검증
- [ ] SQL Injection 방지
- [ ] XSS 공격 방지
- [ ] CSRF 보호
- [ ] Path Traversal 방지
- [ ] Command Injection 방지

### ⚙️ 설정 보안
- [ ] Production 환경 Debug 모드 비활성화
- [ ] 기본 크리덴셜 변경
- [ ] 불필요한 기능 비활성화
- [ ] 보안 헤더 설정
- [ ] CORS 적절한 설정

### 📊 모니터링
- [ ] 보안 이벤트 로깅
- [ ] 실패한 로그인 시도 추적
- [ ] 비정상 접근 패턴 감지
- [ ] 보안 메트릭 수집
- [ ] 알림 시스템 구축