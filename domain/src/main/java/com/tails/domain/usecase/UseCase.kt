package com.tails.domain.usecase

abstract class UseCase<in Params, out T> where T : Any {

    abstract fun createObservable(params: Params): T

    abstract fun onCleared()

}