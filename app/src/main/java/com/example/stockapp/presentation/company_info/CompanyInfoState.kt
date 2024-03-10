package com.example.stockapp.presentation.company_info

import com.example.stockapp.domain.model.CompanyInfo
import com.example.stockapp.domain.model.IntradayInfo

data class CompanyInfoState(
    val stockInfo: List<IntradayInfo> = emptyList(),
    val companyInfo: CompanyInfo? = null,
    val isLoading: Boolean = false,
    var error: String? = null
)
