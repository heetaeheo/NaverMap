package com.project.navermap.data.datasource.restaurant


import com.project.navermap.data.network.FoodApiService
import com.project.navermap.di.annotation.dispatchermodule.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RestaurantDataSourceImpl @Inject constructor(
    private val foodApiService: FoodApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RestaurantDataSource {

    override suspend fun getItemsByRestaurantId(id: Long) = withContext(ioDispatcher) {
        val response = foodApiService.getRestaurantFoods(id)
        if (response.isSuccessful) {
            response.body()!!
        } else {
            emptyList()
        }
    }
}