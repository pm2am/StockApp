package com.example.stockapp.domain.repo

import com.example.stockapp.domain.model.CompanyListing
import com.example.stockapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface StockRepository {

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>>
}
