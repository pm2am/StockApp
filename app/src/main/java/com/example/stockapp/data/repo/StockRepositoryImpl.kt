package com.example.stockapp.data.repo

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.stockapp.data.csv.CSVParser
import com.example.stockapp.data.local.StockDatabase
import com.example.stockapp.data.mapper.toCompanyInfo
import com.example.stockapp.data.mapper.toCompanyListing
import com.example.stockapp.data.mapper.toCompanyListingEntity
import com.example.stockapp.data.remote.StockApi
import com.example.stockapp.domain.model.CompanyInfo
import com.example.stockapp.domain.model.CompanyListing
import com.example.stockapp.domain.model.IntradayInfo
import com.example.stockapp.domain.repo.StockRepository
import com.example.stockapp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val api: StockApi,
    private val db: StockDatabase,
    private val companyListingParser: CSVParser<CompanyListing>,
    private val intradayInfoParser: CSVParser<IntradayInfo>
): StockRepository {

    private val dao = db.dao

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> {
        return flow {
            emit(Resource.Loading(true))
            val localListing = dao.searchCompanyListing(query)
            emit(Resource.Success(
                data = localListing.map {
                    it.toCompanyListing()
                }
            ))
            val isDbEmpty = localListing.isEmpty() && query.isBlank()
            val shouldJustLoadFromCache = !isDbEmpty && !fetchFromRemote
            if (shouldJustLoadFromCache) {
                emit(Resource.Loading(false))
                return@flow
            }
            val remoteListing = try {
                val response = api.getListings()
                companyListingParser.parser(response.byteStream())
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't parse data"))
                null
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
                null
            }
            remoteListing?.let {  listings ->
                dao.clearCompanyListings()
                dao.insertCompanyListing(
                    listings.map {
                        it.toCompanyListingEntity()
                    }
                )
                emit(Resource.Success(
                    data = dao.searchCompanyListing("")
                        .map { it.toCompanyListing() }
                ))
                emit(Resource.Loading(false))
            }
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getIntraDayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol)
            val res = intradayInfoParser.parser(response.byteStream())
            Resource.Success(res)
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val res = api.getCompanyInfo(symbol)
            Resource.Success(res.toCompanyInfo())
        } catch (e: IOException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        } catch (e: HttpException) {
            e.printStackTrace()
            Resource.Error(
                message = "Couldn't load intraday info"
            )
        }
    }

}
