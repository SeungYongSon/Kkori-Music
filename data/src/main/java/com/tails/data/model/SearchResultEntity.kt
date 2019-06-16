package com.tails.data.model

import com.tails.domain.entity.SearchResult
import javax.inject.Inject

data class SearchResultEntity(val resultList: List<String>,
                              val nextPageToken: String) : ModelEntity()

class SearchResultMapper @Inject constructor(): EntityMapper<SearchResult, SearchResultEntity> {

    override fun mapToDomain(entity: SearchResultEntity): SearchResult =
        SearchResult(entity.resultList, entity.nextPageToken)

    override fun mapToEntity(model: SearchResult): SearchResultEntity =
        SearchResultEntity(model.resultList, model.nextPageToken)

}