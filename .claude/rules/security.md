# Security Guidelines

## 🔒 필수 보안 체크리스트

### 인증/인가
- [ ] JWT 토큰 검증 구현
- [ ] 토큰 만료 시간 설정 (Access: 1시간, Refresh: 7일)
- [ ] Role 기반 접근 제어 (RBAC)
- [ ] API 엔드포인트별 권한 설정
- [ ] 토큰 블랙리스트 관리

### 입력 검증
- [ ] 모든 입력값 검증 (@Valid, @Validated)
- [ ] SQL Injection 방지 (파라미터 바인딩)
- [ ] XSS 방지 (HTML 이스케이핑)
- [ ] Path Traversal 방지
- [ ] Command Injection 방지

### 암호화
- [ ] 비밀번호 BCrypt 암호화
- [ ] 민감정보 AES 암호화
- [ ] HTTPS 통신 강제
- [ ] 암호화 키 안전한 관리 (AWS Secrets Manager)

### 보안 헤더
- [ ] CORS 설정
- [ ] CSP (Content Security Policy)
- [ ] X-Frame-Options
- [ ] X-Content-Type-Options
- [ ] Strict-Transport-Security

## Spring Security 설정

### SecurityConfig
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
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            }
            .authorizeHttpRequests {
                it.requestMatchers("/api/v1/auth/**").permitAll()
                it.requestMatchers("/api/v1/public/**").permitAll()
                it.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }
    
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:3000")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            maxAge = 3600
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
    
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
```

## JWT 구현

### JWT Provider
```kotlin
@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-validity}") private val accessTokenValidity: Long,
    @Value("\${jwt.refresh-token-validity}") private val refreshTokenValidity: Long
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())
    
    fun createAccessToken(userId: Long, roles: List<String>): String {
        val now = Date()
        val expiry = Date(now.time + accessTokenValidity)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }
    
    fun createRefreshToken(userId: Long): String {
        val now = Date()
        val expiry = Date(now.time + refreshTokenValidity)
        
        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact()
    }
    
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            logger.error { "Invalid JWT token: ${e.message}" }
            false
        }
    }
    
    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            
        return claims.subject.toLong()
    }
}
```

### JWT Filter
```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)
        
        token?.let {
            if (jwtProvider.validateToken(it)) {
                val userId = jwtProvider.getUserIdFromToken(it)
                val userDetails = userDetailsService.loadUserById(userId)
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                
                SecurityContextHolder.getContext().authentication = authentication
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        return if (header?.startsWith("Bearer ") == true) {
            header.substring(7)
        } else null
    }
}
```

## 권한 관리

### Method Security
```kotlin
@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val userService: UserService
) {
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(): List<UserDto> {
        return userService.getAllUsers()
    }
    
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') and @userSecurity.isNotSelf(#id)")
    fun deleteUser(@PathVariable id: Long) {
        userService.deleteUser(id)
    }
}

@Component
class UserSecurity {
    fun isNotSelf(userId: Long): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        val currentUserId = (authentication.principal as UserPrincipal).id
        return currentUserId != userId
    }
}
```

## 입력값 검증

### DTO Validation
```kotlin
data class CreateUserRequest(
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,
    
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자여야 합니다")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9가-힣]+$",
        message = "닉네임은 한글, 영문, 숫자만 가능합니다"
    )
    val nickname: String,
    
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$",
        message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    val password: String
)
```

### Custom Validator
```kotlin
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NoSqlInjectionValidator::class])
annotation class NoSqlInjection(
    val message: String = "SQL Injection 위험이 있습니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NoSqlInjectionValidator : ConstraintValidator<NoSqlInjection, String> {
    private val sqlKeywords = listOf(
        "SELECT", "INSERT", "UPDATE", "DELETE", "DROP",
        "UNION", "WHERE", "OR", "AND", "--", "/*", "*/"
    )
    
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        
        val upperValue = value.uppercase()
        return !sqlKeywords.any { upperValue.contains(it) }
    }
}
```

## 암호화

### 비밀번호 암호화
```kotlin
@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository
) {
    fun register(request: RegisterRequest): UserDto {
        val encodedPassword = passwordEncoder.encode(request.password)
        
        val user = User(
            email = request.email,
            password = encodedPassword,
            nickname = request.nickname
        )
        
        return UserDto.from(userRepository.save(user))
    }
    
    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BadCredentialsException("Invalid credentials")
            
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }
        
        // JWT 토큰 생성
        return createTokens(user)
    }
}
```

### 민감정보 암호화
```kotlin
@Component
class CryptoService(
    @Value("\${crypto.secret-key}") private val secretKey: String
) {
    private val key: SecretKey
    private val cipher: Cipher
    
    init {
        val decodedKey = Base64.getDecoder().decode(secretKey)
        key = SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        cipher = Cipher.getInstance("AES/GCM/NoPadding")
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

## Rate Limiting

### Redis 기반 Rate Limiter
```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val key: String = "",
    val limit: Int = 10,
    val duration: Int = 60
)

@Aspect
@Component
class RateLimitAspect(
    private val redisTemplate: RedisTemplate<String, String>
) {
    
    @Around("@annotation(rateLimit)")
    fun rateLimit(joinPoint: ProceedingJoinPoint, rateLimit: RateLimit): Any? {
        val key = getRateLimitKey(joinPoint, rateLimit)
        val current = redisTemplate.opsForValue().increment(key) ?: 0
        
        if (current == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimit.duration.toLong()))
        }
        
        if (current > rateLimit.limit) {
            throw RateLimitExceededException("Rate limit exceeded")
        }
        
        return joinPoint.proceed()
    }
    
    private fun getRateLimitKey(joinPoint: ProceedingJoinPoint, rateLimit: RateLimit): String {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            .request
        val ip = request.remoteAddr
        val path = request.requestURI
        
        return "rate_limit:$ip:$path"
    }
}
```

## 보안 로깅

### 감사 로그
```kotlin
@Aspect
@Component
class AuditLogAspect(
    private val auditLogRepository: AuditLogRepository
) {
    
    @AfterReturning(
        pointcut = "@annotation(Audited)",
        returning = "result"
    )
    fun audit(joinPoint: ProceedingJoinPoint, result: Any?) {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = (authentication?.principal as? UserPrincipal)?.id
        
        val auditLog = AuditLog(
            userId = userId,
            action = joinPoint.signature.name,
            resource = joinPoint.target.javaClass.simpleName,
            ip = getClientIp(),
            timestamp = LocalDateTime.now(),
            result = "SUCCESS"
        )
        
        auditLogRepository.save(auditLog)
    }
    
    private fun getClientIp(): String {
        val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes)
            .request
        return request.getHeader("X-Forwarded-For") ?: request.remoteAddr
    }
}
```

## 보안 테스트

### Security 테스트
```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest(
    @Autowired val mockMvc: MockMvc
) : BehaviorSpec({
    
    Given("인증되지 않은 사용자가") {
        When("보호된 엔드포인트에 접근하면") {
            val result = mockMvc.get("/api/v1/users/profile")
            
            Then("401 Unauthorized 응답") {
                result.andExpect {
                    status { isUnauthorized() }
                }
            }
        }
    }
    
    Given("유효한 토큰을 가진 사용자가") {
        val token = "Bearer valid-jwt-token"
        
        When("보호된 엔드포인트에 접근하면") {
            val result = mockMvc.get("/api/v1/users/profile") {
                header("Authorization", token)
            }
            
            Then("200 OK 응답") {
                result.andExpect {
                    status { isOk() }
                }
            }
        }
    }
})
```

## 보안 체크리스트

### 코드 리뷰시 확인사항
- [ ] 하드코딩된 시크릿 없음
- [ ] SQL Injection 방지
- [ ] XSS 방지
- [ ] CSRF 보호
- [ ] 적절한 인증/인가
- [ ] 입력값 검증
- [ ] 에러 메시지에 민감정보 노출 없음
- [ ] 로깅에 민감정보 없음
- [ ] 암호화 적용
- [ ] Rate Limiting 적용