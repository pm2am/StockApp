package com.example.stockapp.data.mapper

import com.example.stockapp.data.local.CompanyListingEntity
import com.example.stockapp.data.remote.dto.CompanyInfoDto
import com.example.stockapp.domain.model.CompanyInfo
import com.example.stockapp.domain.model.CompanyListing

fun CompanyListingEntity.toCompanyListing(): CompanyListing {
    return CompanyListing(
        name = name,
        symbol = symbol,
        exchange = exchange
    )
}

fun CompanyListing.toCompanyListingEntity(): CompanyListingEntity {
    return CompanyListingEntity(
        name = name,
        symbol = symbol,
        exchange = exchange
    )
}

fun CompanyInfoDto.toCompanyInfo(): CompanyInfo {
    return CompanyInfo(
        symbol = symbol?: "",
        description = description?: "",
        name = name?: "",
        country = country?: "",
        industry = industry?: ""
    )
}
