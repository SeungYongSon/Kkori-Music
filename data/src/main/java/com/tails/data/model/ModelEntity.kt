package com.tails.data.model

import com.tails.domain.entity.Model

open class ModelEntity

interface EntityMapper<M : Model, ME : ModelEntity> {
    fun mapToDomain(entity: ME): M

    fun mapToEntity(model: M): ME
}