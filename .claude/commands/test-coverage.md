---
description: 테스트 커버리지를 체크하고 부족한 부분을 식별하여 테스트를 추가합니다
---

# Test Coverage Command

코드 커버리지를 분석하고 부족한 테스트를 식별하는 명령어입니다.

## Usage

```bash
/test-coverage [options]
```

## Examples

```bash
# 전체 프로젝트 커버리지 체크
/test-coverage

# 특정 패키지만 체크
/test-coverage --package com.metamong.domain.user

# 커버리지 리포트와 함께 개선 제안
/test-coverage --with-suggestions

# 임계값 지정
/test-coverage --threshold 80
```

## What This Command Does

### 1. 현재 커버리지 분석

#### Gradle 커버리지 실행
```bash
./gradlew test jacocoTestReport
```

#### 커버리지 리포트 분석
```kotlin
// Coverage Analysis Result
================================
📊 Test Coverage Report
================================

Overall Coverage: 76.3%
 - Line Coverage: 78.5% (Target: 80%)
 - Branch Coverage: 72.1% (Target: 70%)
 - Method Coverage: 85.2% (Target: 90%)

❌ Below Threshold:
 - Line Coverage: -1.5% (Need to improve)

📁 Package Coverage:
 - com.metamong.domain.user: 89.2% ✅
 - com.metamong.application.user: 71.3% ❌
 - com.metamong.infra: 62.5% ❌
 - com.metamong.presentation: 82.1% ✅

🔍 Files with Low Coverage:
1. UserService.kt: 65.3%
2. UserRepositoryImpl.kt: 58.7%
3. PaymentService.kt: 42.1%
```

### 2. 누락된 테스트 식별

#### 커버되지 않은 코드 분석
```kotlin
// UserService.kt - Line 45-52 (Not Covered)
fun deleteUser(userId: Long) {
    val user = userRepository.findById(userId)
        ?: throw UserNotFoundException("User not found: $userId")
    
    if (user.hasActiveOrders()) {  // ❌ Not tested
        throw CannotDeleteUserException("User has active orders")
    }
    
    userRepository.delete(user)  // ❌ Not tested
}

// UserService.kt - Line 78-85 (Partially Covered)
fun updateUserStatus(userId: Long, status: UserStatus) {
    val user = userRepository.findById(userId)
        ?: throw UserNotFoundException("User not found: $userId")
    
    when (status) {  // ✅ Covered
        UserStatus.SUSPENDED -> {
            user.suspend()  // ❌ Not tested
            notificationService.sendSuspensionNotice(user.email)  // ❌ Not tested
        }
        UserStatus.ACTIVE -> user.activate()  // ✅ Covered
        UserStatus.INACTIVE -> user.deactivate()  // ❌ Not tested
    }
}
```

### 3. 자동 테스트 생성

#### 누락된 단위 테스트 생성
```kotlin
// Generated Test: UserServiceTest.kt
class UserServiceTest : BehaviorSpec({
    val fixture = kotlinFixture()
    val mockUserRepository = mockk<UserRepository>()
    val mockNotificationService = mockk<NotificationService>()
    
    val userService = UserService(
        userRepository = mockUserRepository,
        notificationService = mockNotificationService
    )
    
    // 🔧 Generated for uncovered code: deleteUser
    Given("활성 주문이 있는 사용자를 삭제하려 할 때") {
        val userId = 1L
        val userWithOrders = fixture<User> {
            property(User::id) { userId }
        }
        
        every { mockUserRepository.findById(userId) } returns userWithOrders
        every { userWithOrders.hasActiveOrders() } returns true
        
        When("사용자 삭제를 시도하면") {
            Then("CannotDeleteUserException이 발생한다") {
                shouldThrow<CannotDeleteUserException> {
                    userService.deleteUser(userId)
                }
            }
        }
    }
    
    Given("활성 주문이 없는 사용자를 삭제할 때") {
        val userId = 1L
        val user = fixture<User> {
            property(User::id) { userId }
        }
        
        every { mockUserRepository.findById(userId) } returns user
        every { user.hasActiveOrders() } returns false
        every { mockUserRepository.delete(user) } just Runs
        
        When("사용자 삭제를 실행하면") {
            userService.deleteUser(userId)
            
            Then("사용자가 삭제된다") {
                verify { mockUserRepository.delete(user) }
            }
        }
    }
    
    // 🔧 Generated for uncovered branch: updateUserStatus
    Given("사용자 상태를 SUSPENDED로 변경할 때") {
        val userId = 1L
        val user = fixture<User> {
            property(User::id) { userId }
            property(User::email) { "test@example.com" }
        }
        
        every { mockUserRepository.findById(userId) } returns user
        every { user.suspend() } just Runs
        every { mockNotificationService.sendSuspensionNotice(any()) } just Runs
        
        When("상태 변경을 실행하면") {
            userService.updateUserStatus(userId, UserStatus.SUSPENDED)
            
            Then("사용자가 정지되고 알림이 발송된다") {
                verify { user.suspend() }
                verify { mockNotificationService.sendSuspensionNotice("test@example.com") }
            }
        }
    }
    
    Given("사용자 상태를 INACTIVE로 변경할 때") {
        val userId = 1L
        val user = fixture<User> {
            property(User::id) { userId }
        }
        
        every { mockUserRepository.findById(userId) } returns user
        every { user.deactivate() } just Runs
        
        When("상태 변경을 실행하면") {
            userService.updateUserStatus(userId, UserStatus.INACTIVE)
            
            Then("사용자가 비활성화된다") {
                verify { user.deactivate() }
            }
        }
    }
})
```

#### Repository 통합 테스트 생성
```kotlin
// Generated Test: UserRepositoryTest.kt
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserRepositoryIntegrationTest(
    @Autowired val userRepository: UserRepository,
    @Autowired val testEntityManager: TestEntityManager
) : BehaviorSpec({
    
    beforeEach {
        userRepository.deleteAll()
        testEntityManager.clear()
    }
    
    // 🔧 Generated for uncovered Repository methods
    Given("복잡한 조건으로 사용자 검색할 때") {
        // Test data setup
        val activeUser = createUserMockEntity(
            fixture = fixture,
            email = "active@test.com",
            status = UserStatus.ACTIVE,
            createdAt = LocalDateTime.now().minusDays(5)
        )
        
        val suspendedUser = createUserMockEntity(
            fixture = fixture,
            email = "suspended@test.com",
            status = UserStatus.SUSPENDED,
            createdAt = LocalDateTime.now().minusDays(10)
        )
        
        userRepository.saveAll(listOf(activeUser, suspendedUser))
        testEntityManager.flush()
        
        When("활성 사용자만 검색하면") {
            val filter = UserSearchFilter(
                status = UserStatus.ACTIVE,
                startDate = LocalDateTime.now().minusDays(7)
            )
            
            val result = userRepository.searchUsers(filter, Pageable.unpaged())
            
            Then("활성 사용자만 반환된다") {
                result.content.size shouldBe 1
                result.content[0].email shouldBe "active@test.com"
            }
        }
    }
})
```

### 4. 브랜치 커버리지 개선

#### 조건부 로직 테스트 생성
```kotlin
// Original Code with poor branch coverage
fun calculateDiscount(user: User, amount: BigDecimal): BigDecimal {
    return when {
        user.isVip() && amount > BigDecimal(100000) -> amount.multiply(BigDecimal("0.15"))
        user.isVip() -> amount.multiply(BigDecimal("0.10"))
        amount > BigDecimal(50000) -> amount.multiply(BigDecimal("0.05"))
        else -> BigDecimal.ZERO
    }
}

// Generated tests for all branches
class DiscountCalculationTest : BehaviorSpec({
    Given("할인 계산 시나리오") {
        val mockUser = mockk<User>()
        
        When("VIP 사용자가 10만원 이상 구매시") {
            every { mockUser.isVip() } returns true
            val amount = BigDecimal("150000")
            
            val discount = calculateDiscount(mockUser, amount)
            
            Then("15% 할인이 적용된다") {
                discount shouldBe BigDecimal("22500.00")
            }
        }
        
        When("VIP 사용자가 10만원 미만 구매시") {
            every { mockUser.isVip() } returns true
            val amount = BigDecimal("50000")
            
            val discount = calculateDiscount(mockUser, amount)
            
            Then("10% 할인이 적용된다") {
                discount shouldBe BigDecimal("5000.00")
            }
        }
        
        When("일반 사용자가 5만원 이상 구매시") {
            every { mockUser.isVip() } returns false
            val amount = BigDecimal("60000")
            
            val discount = calculateDiscount(mockUser, amount)
            
            Then("5% 할인이 적용된다") {
                discount shouldBe BigDecimal("3000.00")
            }
        }
        
        When("일반 사용자가 5만원 미만 구매시") {
            every { mockUser.isVip() } returns false
            val amount = BigDecimal("30000")
            
            val discount = calculateDiscount(mockUser, amount)
            
            Then("할인이 적용되지 않는다") {
                discount shouldBe BigDecimal.ZERO
            }
        }
    }
})
```

### 5. API 테스트 커버리지

#### Controller 테스트 생성
```kotlin
// Generated for uncovered API endpoints
@WebMvcTest(UserController::class)
class UserControllerCoverageTest(
    @Autowired val mockMvc: MockMvc,
    @MockkBean val userService: UserService
) : BehaviorSpec({
    
    // 🔧 Generated for uncovered error scenarios
    Given("사용자 상태 변경 API") {
        When("존재하지 않는 사용자 ID로 요청시") {
            every { 
                userService.updateUserStatus(999L, UserStatus.SUSPENDED) 
            } throws UserNotFoundException("User not found: 999")
            
            val result = mockMvc.patch("/api/v1/users/999/status") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status": "SUSPENDED"}"""
            }
            
            Then("404 Not Found 응답") {
                result.andExpect {
                    status { isNotFound() }
                    jsonPath("$.code") { value("USER_NOT_FOUND") }
                }
            }
        }
        
        When("유효하지 않은 상태값으로 요청시") {
            val result = mockMvc.patch("/api/v1/users/1/status") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"status": "INVALID_STATUS"}"""
            }
            
            Then("400 Bad Request 응답") {
                result.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.code") { value("VALIDATION_ERROR") }
                }
            }
        }
    }
})
```

### 6. 성능 테스트 추가

#### 부하 테스트
```kotlin
class UserServicePerformanceTest : BehaviorSpec({
    
    Given("대량 사용자 처리 성능 테스트") {
        When("1000명 사용자를 동시에 생성할 때") {
            val executor = Executors.newFixedThreadPool(50)
            val latch = CountDownLatch(1000)
            val results = Collections.synchronizedList(mutableListOf<Boolean>())
            val startTime = System.currentTimeMillis()
            
            repeat(1000) { index ->
                executor.submit {
                    try {
                        val user = userService.createUser(
                            CreateUserCommand(
                                email = "user$index@test.com",
                                nickname = "User$index",
                                password = "password123!"
                            )
                        )
                        results.add(user != null)
                    } catch (e: Exception) {
                        results.add(false)
                    } finally {
                        latch.countDown()
                    }
                }
            }
            
            latch.await(60, TimeUnit.SECONDS)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            Then("60초 내에 완료되고 95% 이상 성공") {
                duration shouldBeLessThan 60000
                val successRate = results.count { it } / 1000.0
                successRate shouldBeGreaterThan 0.95
            }
        }
    }
})
```

## 커버리지 개선 전략

### 1. 우선순위 기반 개선
```kotlin
// High Priority (Critical Business Logic)
- Payment processing: Current 45% → Target 85%
- User authentication: Current 62% → Target 90%
- Order management: Current 38% → Target 80%

// Medium Priority (Important Features)
- Notification system: Current 71% → Target 80%
- Search functionality: Current 56% → Target 75%

// Low Priority (Utility Functions)
- Logging utilities: Current 23% → Target 60%
- Cache management: Current 45% → Target 65%
```

### 2. 테스트 타입별 전략
```kotlin
// Unit Tests (목표: 85%)
- Service layer 비즈니스 로직
- Domain model 검증 로직
- Utility 함수

// Integration Tests (목표: 70%)
- Repository 복합 쿼리
- External API 연동
- Configuration 검증

// E2E Tests (목표: 60%)
- Critical user journeys
- Payment flows
- Authentication flows
```

### 3. 자동화된 커버리지 체크

#### GitHub Actions 통합
```yaml
# .github/workflows/coverage.yml
name: Coverage Check
on: [push, pull_request]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run tests with coverage
        run: ./gradlew test jacocoTestReport
      - name: Check coverage threshold
        run: |
          COVERAGE=$(grep -o 'Total.*[0-9]\+%' build/reports/jacoco/test/html/index.html | grep -o '[0-9]\+')
          if [ $COVERAGE -lt 80 ]; then
            echo "Coverage $COVERAGE% is below threshold 80%"
            exit 1
          fi
```

#### Pre-commit Hook 추가
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "Running coverage check..."
./gradlew test jacocoTestReport

COVERAGE=$(grep -o 'Total.*[0-9]\+%' build/reports/jacoco/test/html/index.html | grep -o '[0-9]\+')

if [ $COVERAGE -lt 80 ]; then
    echo "❌ Coverage $COVERAGE% is below threshold 80%"
    echo "Please add more tests before committing"
    exit 1
fi

echo "✅ Coverage check passed: $COVERAGE%"
```

## 커버리지 리포트 해석

### HTML 리포트 분석
```
build/reports/jacoco/test/html/index.html

📊 Coverage Summary:
- Instructions: 76.3% (실제 실행된 바이트코드 비율)
- Branches: 72.1% (조건문 분기 커버리지)
- Lines: 78.5% (라인 커버리지)
- Methods: 85.2% (메서드 커버리지)
- Classes: 92.1% (클래스 커버리지)

🔍 상세 분석:
- 빨간색: 커버되지 않은 코드
- 노란색: 부분적으로 커버된 코드 (일부 분기만 테스트)
- 초록색: 완전히 커버된 코드
```

### XML 리포트 CI/CD 통합
```xml
<!-- build/reports/jacoco/test/jacocoTestReport.xml -->
<report>
  <counter type="LINE" missed="245" covered="868"/>
  <counter type="BRANCH" missed="89" covered="231"/>
  <counter type="METHOD" missed="15" covered="87"/>
</report>
```

## 커버리지 개선 체크리스트

### Before 커밋
- [ ] 새 코드의 테스트 작성 완료
- [ ] 전체 커버리지 80% 이상 유지
- [ ] 브랜치 커버리지 70% 이상 유지
- [ ] Critical path 100% 커버

### 정기 점검 (주간)
- [ ] 커버리지 트렌드 분석
- [ ] 낮은 커버리지 모듈 개선 계획 수립
- [ ] 테스트 품질 리뷰 (의미있는 테스트인지)
- [ ] 성능 테스트 결과 검토