package com.metamong.batch.jobs.publicdata.reader

import com.metamong.external.publicdata.dto.RegionCode
import org.springframework.batch.item.ItemReader
import org.springframework.stereotype.Component

@Component
class RegionCodeItemReader {
    /**
     * 매 호출마다 새로운 상태를 가진 Reader를 생성
     * 각 Step에서 독립적인 상태를 유지하기 위해 상태를 Reader 내부에 캡슐화
     */
    fun createReader(): ItemReader<RegionCode> {
        val regionCodes = RegionCode.getTargetRegions()
//        val regionCodes = listOf(RegionCode("11", "11215", "광진구"))
        var currentIndex = 0

        return ItemReader {
            if (currentIndex < regionCodes.size) {
                regionCodes[currentIndex++]
            } else {
                null
            }
        }
    }
}
