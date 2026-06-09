package com.example.data.repository

import com.example.data.database.StockEntity

object MarketMapper {
    fun mapDtoToEntity(dto: StockDto): StockEntity {
        return StockEntity(
            symbol = dto.symbol,
            companyName = dto.companyName,
            eps = dto.eps,
            dps = dto.dps,
            lastPrice = dto.lastPrice,
            volume = dto.volume,
            pe = dto.pe,
            updatedAt = dto.lastUpdateTime
        )
    }

    fun mapDtosToEntities(dtos: List<StockDto>): List<StockEntity> {
        return dtos.map { mapDtoToEntity(it) }
    }
}
