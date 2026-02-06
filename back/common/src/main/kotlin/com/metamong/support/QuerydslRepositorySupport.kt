package com.metamong.support

import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import kotlin.properties.Delegates

class QuerydslRepositorySupport(
    domainClass: Class<*>,
) : QuerydslRepositorySupport(domainClass) {
    protected var queryFactory: JPAQueryFactory by Delegates.notNull()

    @PersistenceContext
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
        queryFactory = JPAQueryFactory(entityManager)
    }
}
