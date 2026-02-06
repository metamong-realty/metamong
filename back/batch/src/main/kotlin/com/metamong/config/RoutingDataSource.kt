package com.metamong.config

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager

class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any? =
        if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            "slave"
        } else {
            "master"
        }
}
