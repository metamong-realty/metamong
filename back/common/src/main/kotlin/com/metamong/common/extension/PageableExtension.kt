package com.metamong.common.extension

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun Pageable?.defaultPageable(
    page: Int = 0,
    size: Int = 10,
    sort: String = "id",
    direction: Sort.Direction = Sort.Direction.DESC,
): Pageable = this ?: PageRequest.of(page, size, Sort.by(direction, sort))
