---
name: security-checklist
description: 보안 체크리스트와 구현 가이드. API 보안, 데이터 보호, 인증/인가 구현 시 참조
---

# 보안 체크리스트

Metamong 프로젝트의 보안 구현 가이드와 체크리스트입니다.

## API 보안

### JWT 토큰 검증
```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)
        
        token?.let {
            runCatching {
                // 토큰 유효성 검증
                jwtTokenProvider.validateToken(it)
                val authentication = jwtTokenProvider.getAuthentication(it)
                SecurityContextHolder.getContext().authentication = authentication
            }.onFailure { exception ->
                logger.error("JWT 검증 실패", exception)
                response.sendError(HttpStatus.UNAUTHORIZED.value())
                return
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun extractToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())
    
    fun createToken(userId: Long, roles: List<String>): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    
    fun validateToken(token: String): Boolean {
        return runCatching {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        }.getOrElse { false }
    }
}
```

### Rate Limiting
```kotlin
@Component
class RateLimitingInterceptor(
    private val redisTemplate: RedisTemplate<String, Int>,
) : HandlerInterceptor {
    
    companion object {
        private const val RATE_LIMIT = 100  // 요청 제한 수
        private const val TIME_WINDOW = 60  // 시간 창 (초)
    }
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val clientId = getClientId(request)
        val key = "rate_limit:$clientId"
        
        val currentCount = redisTemplate.opsForValue().increment(key) ?: 1
        
        if (currentCount == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(TIME_WINDOW))
        }
        
        if (currentCount > RATE_LIMIT) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.writer.write("Rate limit exceeded")
            return false
        }
        
        response.setHeader("X-RateLimit-Limit", RATE_LIMIT.toString())
        response.setHeader("X-RateLimit-Remaining", (RATE_LIMIT - currentCount).toString())
        
        return true
    }
    
    private fun getClientId(request: HttpServletRequest): String {
        // IP 또는 사용자 ID 기반으로 클라이언트 식별
        return request.remoteAddr
    }
}
```

### SQL Injection 방지
```kotlin
// ✅ 파라미터 바인딩 사용 (안전)
@Repository
class UserRepository(
    private val jdbcTemplate: JdbcTemplate,
) {
    fun findByEmail(email: String): User? {
        val sql = "SELECT * FROM users WHERE email = ?"
        return jdbcTemplate.queryForObject(sql, email) { rs, _ ->
            User(
                id = rs.getLong("id"),
                email = rs.getString("email"),
                name = rs.getString("name"),
            )
        }
    }
}

// ❌ 문자열 연결 (위험)
fun findByEmailUnsafe(email: String): User? {
    val sql = "SELECT * FROM users WHERE email = '$email'"  // SQL Injection 위험!
    // ...
}

// ✅ QueryDSL 사용 (안전)
fun searchUsers(name: String?): List<User> {
    return queryFactory
        .selectFrom(user)
        .where(name?.let { user.name.contains(it) })  // 자동으로 파라미터 바인딩
        .fetch()
}
```

### XSS 방지
```kotlin
// 입력값 검증
@RestController
class CommentController {
    @PostMapping("/comments")
    fun createComment(
        @Valid @RequestBody request: CreateCommentRequest,
    ): CommentResponse {
        // HTML 태그 제거
        val sanitizedContent = Jsoup.clean(
            request.content,
            Whitelist.none(),  // 모든 HTML 태그 제거
        )
        
        return commentService.create(sanitizedContent)
    }
}

// DTO 검증
data class CreateCommentRequest(
    @field:NotBlank
    @field:Size(max = 1000)
    @field:Pattern(
        regexp = "^[^<>]*$",  // < > 문자 금지
        message = "HTML 태그는 허용되지 않습니다",
    )
    val content: String,
)
```

### CSRF 보호
```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { csrf ->
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringAntMatchers("/api/public/**")  // 공개 API는 제외
            }
            .build()
    }
}
```

---

## 데이터 보안

### 비밀번호 암호화
```kotlin
@Configuration
class PasswordConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(12)  // strength 12 권장
    }
}

@Service
class UserService(
    private val passwordEncoder: PasswordEncoder,
) {
    fun createUser(request: CreateUserRequest): User {
        val hashedPassword = passwordEncoder.encode(request.password)
        
        return User(
            email = request.email,
            password = hashedPassword,  // 암호화된 비밀번호 저장
        )
    }
    
    fun authenticate(email: String, password: String): Boolean {
        val user = userRepository.findByEmail(email) ?: return false
        return passwordEncoder.matches(password, user.password)
    }
}
```

### 민감정보 마스킹
```kotlin
// 로깅 시 민감정보 마스킹
data class UserDto(
    val id: Long,
    val email: String,
    val name: String,
) {
    override fun toString(): String {
        return "UserDto(id=$id, email=${maskEmail(email)}, name=$name)"
    }
    
    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return "***"
        
        val localPart = parts[0]
        val maskedLocal = if (localPart.length > 2) {
            "${localPart.take(2)}***"
        } else {
            "***"
        }
        
        return "$maskedLocal@${parts[1]}"
    }
}

// Response에서 민감정보 제외
@JsonIgnoreProperties(value = ["password", "ssn"])
data class UserResponse(
    val id: Long,
    val email: String,
    val name: String,
    @JsonIgnore val password: String,  // JSON 직렬화에서 제외
    @JsonIgnore val ssn: String,       // 주민번호 제외
)
```

### 감사 로그
```kotlin
@Aspect
@Component
class AuditLoggingAspect {
    private val logger = KotlinLogging.logger {}
    
    @AfterReturning(
        pointcut = "@annotation(Auditable)",
        returning = "result",
    )
    fun logAuditTrail(joinPoint: JoinPoint, result: Any?) {
        val user = SecurityContextHolder.getContext().authentication?.name ?: "anonymous"
        val method = joinPoint.signature.name
        val args = joinPoint.args.joinToString()
        
        logger.info {
            "AUDIT: User=$user, Method=$method, Args=$args, Result=$result"
        }
        
        // DB에 감사 로그 저장
        auditRepository.save(
            AuditLog(
                userId = user,
                action = method,
                details = args,
                timestamp = LocalDateTime.now(),
            )
        )
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditable

// 사용
@Service
class PaymentService {
    @Auditable
    fun processPayment(request: PaymentRequest): PaymentResult {
        // 중요한 결제 처리 - 감사 로그 자동 기록
    }
}
```

### PII 암호화
```kotlin
@Entity
class User(
    @Id val id: Long,
    
    val email: String,
    
    @Convert(converter = CryptoConverter::class)
    val ssn: String,  // 주민번호 암호화 저장
    
    @Convert(converter = CryptoConverter::class)
    val phoneNumber: String,  // 전화번호 암호화 저장
)

@Component
class CryptoConverter(
    @Value("\${encryption.key}") private val encryptionKey: String,
) : AttributeConverter<String, String> {
    
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val key = SecretKeySpec(encryptionKey.toByteArray(), "AES")
    
    override fun convertToDatabaseColumn(attribute: String?): String? {
        return attribute?.let { encrypt(it) }
    }
    
    override fun convertToEntityAttribute(dbData: String?): String? {
        return dbData?.let { decrypt(it) }
    }
    
    private fun encrypt(data: String): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }
    
    private fun decrypt(data: String): String {
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(data))
        return String(decrypted)
    }
}
```

---

## 인증/인가

### Spring Security 설정
```kotlin
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    
    @Bean
    fun filterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
    ): SecurityFilterChain {
        return http
            .cors { }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .antMatchers("/api/public/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            )
            .exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint { _, response, _ ->
                        response.sendError(HttpStatus.UNAUTHORIZED.value())
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.sendError(HttpStatus.FORBIDDEN.value())
                    }
            }
            .build()
    }
}
```

### Method-level Security
```kotlin
@Service
class UserService {
    // 본인 또는 관리자만 접근 가능
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    fun getUserDetails(userId: Long): UserDto {
        return userRepository.findById(userId)?.toDto()
            ?: throw UserNotFoundException(userId)
    }
    
    // 관리자만 접근 가능
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(userId: Long) {
        userRepository.deleteById(userId)
    }
    
    // 결과 필터링
    @PostFilter("filterObject.ownerId == authentication.principal.id")
    fun getUserDocuments(): List<Document> {
        return documentRepository.findAll()
    }
}
```

---

## 보안 체크리스트

### 개발 단계
- [ ] 입력값 검증 구현
- [ ] SQL Injection 방지 (파라미터 바인딩)
- [ ] XSS 방지 (HTML 이스케이핑)
- [ ] CSRF 토큰 구현
- [ ] 비밀번호 암호화 (BCrypt)
- [ ] JWT 토큰 만료 시간 설정
- [ ] Rate Limiting 구현
- [ ] 민감정보 마스킹

### 테스트 단계
- [ ] 보안 테스트 케이스 작성
- [ ] Penetration Testing
- [ ] OWASP Top 10 체크
- [ ] 의존성 취약점 스캔

### 배포 단계
- [ ] HTTPS 적용
- [ ] 보안 헤더 설정
- [ ] 에러 메시지 정보 노출 방지
- [ ] 로깅 및 모니터링 설정
- [ ] 백업 및 복구 계획

### 운영 단계
- [ ] 정기적인 보안 패치
- [ ] 접근 로그 모니터링
- [ ] 이상 행동 탐지
- [ ] 보안 사고 대응 절차