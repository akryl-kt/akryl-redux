package redux

typealias SubscriptionDisposer = () -> Unit

typealias StoreEnhancer<S, A> = (
    next: StoreEnhancerStoreCreator<S, A>
) -> StoreEnhancerStoreCreator<S, A>

typealias StoreEnhancerStoreCreator<S, A> = (
    reducer: (state: S?, action: A) -> S,
    preloadedState: S?
) -> Store<S, A>

external interface Store<S, A : Action> {
    fun getState(): S
    fun subscribe(subscription: () -> Unit): SubscriptionDisposer
    fun dispatch(action: A)
}

external interface Action {
    val type: String
}

@JsModule("redux")
@JsNonModule
external object Redux {
    fun <S, A : Action> createStore(
        reducer: (state: S?, action: A) -> S,
        preloadedState: S? = definedExternally,
        enhancer: StoreEnhancer<S, A>? = definedExternally
    ): Store<S, A>
}
