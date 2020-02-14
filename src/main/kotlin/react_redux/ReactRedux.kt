package react_redux

import react.Component
import react.ReactNode
import redux.Action
import redux.Store

typealias Dispatch<A> = (action: A) -> Unit

@Suppress("unused")
class StoreProviderProps<T>(
    val store: T,
    val children: ReactNode?
)

@JsModule("react-redux")
@JsNonModule
external object ReactRedux {
    val Provider: Component<StoreProviderProps<Store<*, *>>>
    fun <S, R> useSelector(selector: (S) -> R, equalityFn: ((R, R) -> Boolean)?): R
    fun <S, A : Action> useStore(): Store<S, A>
    fun <A : Action> useDispatch(): Dispatch<A>
}
