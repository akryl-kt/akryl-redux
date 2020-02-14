package io.akryl.redux

import io.akryl.ComponentScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import react.React
import react.ReactElement
import react_redux.Dispatch
import react_redux.ReactRedux
import react_redux.StoreProviderProps
import redux.Action
import redux.Redux
import redux.Store
import redux.StoreEnhancer

fun Store<*, *>.provider(children: List<ReactElement<*>>) =
    React.createElement(
        ReactRedux.Provider,
        StoreProviderProps(store = this, children = null),
        *children.toTypedArray()
    )

@Suppress("unused")
fun <S, R> ComponentScope.useSelector(selector: (S) -> R, equalityFn: ((R, R) -> Boolean)? = undefined): R =
    ReactRedux.useSelector(selector, equalityFn)

@Suppress("unused")
fun <S> ComponentScope.useSelector(): S =
    ReactRedux.useSelector<S, S>({ it }, undefined)

@Suppress("unused")
fun <S, A : Action> ComponentScope.useStore(): Store<S, A> =
    ReactRedux.useStore()

@Suppress("unused")
fun <Msg> ComponentScope.useDispatch(): Dispatch<Msg> {
    val inner = ReactRedux.useDispatch<MsgAction<Msg>>()
    return { msg -> dispatchMsg(inner, msg) }
}

interface MsgAction<out Msg> : Action {
    val payload: Msg?
}

fun <State, Msg, Cmd> createStore(
    init: Pair<State, Cmd?>,
    update: (State, Msg) -> Pair<State, Cmd?>,
    execute: suspend (Cmd) -> List<Msg>,
    enhancer: StoreEnhancer<State, MsgAction<Msg>>? = undefined
): Store<State, MsgAction<Msg>> {
    val (initState, initCmd) = init
    var dispatch: Dispatch<Msg>? = null

    fun process(cmd: Cmd) {
        GlobalScope.launch {
            execute(cmd).forEach { msg ->
                dispatch?.invoke(msg)
            }
        }
    }

    val reducer = { state: State?, msg: MsgAction<Msg> ->
        val oldState = state ?: initState
        val (newState, cmd) = msg.payload
            ?.let { update(oldState, it) }
            ?: Pair(oldState, null)
        if (cmd != null) process(cmd)
        newState
    }

    val store = Redux.createStore(
        reducer = reducer,
        enhancer = enhancer
    )

    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    dispatch = { msg -> dispatchMsg(store::dispatch, msg) }

    if (initCmd != null) process(initCmd)

    return store
}

@Suppress("UNUSED_PARAMETER")
private fun <Msg> dispatchMsg(dispatch: Dispatch<MsgAction<Msg>>, msg: Msg) {
    val action = js("{type: Object.getPrototypeOf(msg).constructor.name, payload: msg}")
        .unsafeCast<MsgAction<Msg>>()
    dispatch(action)
}
