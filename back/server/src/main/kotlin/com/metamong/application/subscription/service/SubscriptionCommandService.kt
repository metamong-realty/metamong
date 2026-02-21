package com.metamong.application.subscription.service

import com.metamong.application.subscription.request.CreateSubscriptionRequestDto
import com.metamong.application.subscription.request.UpdateSubscriptionRequestDto
import com.metamong.application.subscription.response.SubscriptionResponse
import com.metamong.common.exception.CommonException
import com.metamong.common.vo.LegalCode
import com.metamong.domain.subscription.exception.SubscriptionException
import com.metamong.domain.subscription.model.SubscriptionEntity
import com.metamong.domain.subscription.model.SubscriptionType
import com.metamong.infra.persistence.apartment.repository.ApartmentComplexRepository
import com.metamong.infra.persistence.region.repository.RegionLegalCodeRepository
import com.metamong.infra.persistence.subscription.repository.SubscriptionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@Transactional
class SubscriptionCommandService(
    private val subscriptionRepository: SubscriptionRepository,
    private val apartmentComplexRepository: ApartmentComplexRepository,
    private val regionLegalCodeRepository: RegionLegalCodeRepository,
) {
    fun create(
        userId: Long,
        dto: CreateSubscriptionRequestDto,
    ): SubscriptionResponse {
        val count = subscriptionRepository.countByUserId(userId)
        if (count >= MAX_SUBSCRIPTIONS) {
            throw SubscriptionException.LimitExceeded()
        }

        validateByType(dto)

        val entity = dto.toEntity(userId)
        return SubscriptionResponse.from(subscriptionRepository.save(entity))
    }

    fun update(
        id: Long,
        userId: Long,
        dto: UpdateSubscriptionRequestDto,
    ): SubscriptionResponse {
        val subscription = findById(id)
        validateOwnership(subscription, userId)

        if (subscription.type == SubscriptionType.REGION || subscription.type == SubscriptionType.CONDITION) {
            dto.regionCode?.let { validateRegionCode(it) }
        }
        if (subscription.type == SubscriptionType.CONDITION) {
            val hasCondition =
                (dto.exclusiveArea != null) || (dto.minPrice != null) || (dto.maxPrice != null)
            if (!hasCondition && dto.regionCode == null) {
                throw SubscriptionException.ConditionRequired()
            }
            validatePriceRange(dto.minPrice, dto.maxPrice)
        }

        subscription.update(
            regionCode = dto.regionCode ?: subscription.regionCode,
            exclusiveArea = dto.exclusiveArea,
            minPrice = dto.minPrice,
            maxPrice = dto.maxPrice,
            isActive = dto.isActive,
        )
        return SubscriptionResponse.from(subscription)
    }

    fun delete(
        id: Long,
        userId: Long,
    ) {
        val subscription = findById(id)
        validateOwnership(subscription, userId)
        subscriptionRepository.delete(subscription)
    }

    private fun findById(id: Long): SubscriptionEntity =
        subscriptionRepository.findByIdOrNull(id)
            ?: throw SubscriptionException.NotFound()

    private fun validateOwnership(
        subscription: SubscriptionEntity,
        userId: Long,
    ) {
        if (subscription.userId != userId) {
            throw SubscriptionException.AccessDenied()
        }
    }

    private fun validateByType(dto: CreateSubscriptionRequestDto) {
        when (dto.type) {
            SubscriptionType.COMPLEX -> {
                requireNotNull(dto.apartmentComplexId) {
                    throw CommonException.InvalidParameter()
                }
                require(apartmentComplexRepository.existsById(dto.apartmentComplexId)) {
                    throw CommonException.ResourceNotFound()
                }
            }
            SubscriptionType.REGION -> {
                requireNotNull(dto.regionCode) {
                    throw CommonException.InvalidParameter()
                }
                validateRegionCode(dto.regionCode)
            }
            SubscriptionType.CONDITION -> {
                requireNotNull(dto.regionCode) {
                    throw CommonException.InvalidParameter()
                }
                validateRegionCode(dto.regionCode)
                val hasCondition =
                    (dto.exclusiveArea != null) || (dto.minPrice != null) || (dto.maxPrice != null)
                if (!hasCondition) {
                    throw SubscriptionException.ConditionRequired()
                }
                validatePriceRange(dto.minPrice, dto.maxPrice)
            }
        }
    }

    private fun validateRegionCode(regionCode: String) {
        val legalCode =
            runCatching { LegalCode(regionCode) }.getOrNull()
                ?: throw SubscriptionException.InvalidRegionCode()
        if (!regionLegalCodeRepository.existsByLegalCode(legalCode)) {
            throw SubscriptionException.InvalidRegionCode()
        }
    }

    private fun validatePriceRange(
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
    ) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw SubscriptionException.InvalidPriceRange()
        }
    }

    companion object {
        private const val MAX_SUBSCRIPTIONS = 20
    }
}
