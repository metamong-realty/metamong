package com.metamong.infra.persistence.notification.repository

import com.metamong.application.notification.dto.NotificationDto
import com.metamong.domain.apartment.model.QApartmentComplexEntity
import com.metamong.domain.apartment.model.QApartmentTradeEntity
import com.metamong.domain.apartment.model.QApartmentUnitTypeEntity
import com.metamong.domain.subscription.model.NotificationStatus
import com.metamong.domain.subscription.model.QNotificationEventEntity
import com.metamong.support.QuerydslRepositorySupport
import com.querydsl.core.types.Projections
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class NotificationEventRepositoryCustomImpl :
    QuerydslRepositorySupport(com.metamong.domain.subscription.model.NotificationEventEntity::class.java),
    NotificationEventRepositoryCustom {
    private val notification = QNotificationEventEntity.notificationEventEntity
    private val trade = QApartmentTradeEntity.apartmentTradeEntity
    private val unitType = QApartmentUnitTypeEntity.apartmentUnitTypeEntity
    private val complex = QApartmentComplexEntity.apartmentComplexEntity

    override fun findNotificationsWithDetail(
        userId: Long,
        pageable: Pageable,
    ): List<NotificationDto> =
        queryFactory
            .select(
                Projections.constructor(
                    NotificationDto::class.java,
                    notification.id,
                    complex.id,
                    complex.nameRaw,
                    unitType.exclusivePyeong,
                    trade.price.longValue(),
                    trade.contractDate,
                    notification.status,
                    notification.createdAt,
                ),
            ).from(notification)
            .leftJoin(trade)
            .on(trade.id.eq(notification.tradeId))
            .leftJoin(unitType)
            .on(unitType.id.eq(trade.unitTypeId))
            .leftJoin(complex)
            .on(complex.id.eq(unitType.complexId))
            .where(notification.userId.eq(userId))
            .orderBy(notification.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

    override fun markAsRead(
        userId: Long,
        notificationId: Long,
    ): Long =
        queryFactory
            .update(notification)
            .set(notification.status, NotificationStatus.SENT)
            .where(
                notification.id.eq(notificationId),
                notification.userId.eq(userId),
            ).execute()

    override fun markAllAsRead(userId: Long): Long =
        queryFactory
            .update(notification)
            .set(notification.status, NotificationStatus.SENT)
            .where(
                notification.userId.eq(userId),
                notification.status.eq(NotificationStatus.PENDING),
            ).execute()
}
