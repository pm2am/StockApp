package com.example.stockapp.data.csv

import com.example.stockapp.data.mapper.toIntradayInfo
import com.example.stockapp.data.remote.dto.IntradayInfoDto
import com.example.stockapp.domain.model.IntradayInfo
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntradayInfoParser @Inject constructor(): CSVParser<IntradayInfo>{
    override suspend fun parser(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            csvReader
                .readAll()
                .drop(1)
                .mapNotNull {  line ->
                    val timeStamp = line.getOrNull(0)?: return@mapNotNull null
                    val close = line.getOrNull((4))?: return@mapNotNull null
                    val dto = IntradayInfoDto(timeStamp, close.toDouble())
                    dto.toIntradayInfo()
                }
                .filter {
                    it.date.dayOfMonth == LocalDate.now().minusDays(4).dayOfMonth
                }
                .sortedBy { it.date.hour }
                .also {
                    csvReader.close()
                }
        }
    }
}