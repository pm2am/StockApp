package com.example.stockapp.data.repo

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.stockapp.data.csv.CSVParser
import com.example.stockapp.data.local.StockDatabase
import com.example.stockapp.data.mapper.toCompanyListing
import com.example.stockapp.data.mapper.toCompanyListingEntity
import com.example.stockapp.data.remote.StockApi
import com.example.stockapp.domain.model.CompanyListing
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
    private val companyListingParser: CSVParser<CompanyListing>
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
                val response = api.getListings(StockApi.API_KEY)
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

}