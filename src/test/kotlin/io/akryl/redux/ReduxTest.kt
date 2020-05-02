package io.akryl.redux

import io.akryl.component
import io.akryl.dom.html.text
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import react_test_renderer.ReactTestRenderer
import react_test_renderer.akt
import react_test_renderer.aktCreate
import utils.assertJsonEquals
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

private data class State(
    val count: Int
)

private sealed class Msg {
    object Increment : Msg()
    object Decrement : Msg()
    object Effect : Msg()
}

private object Cmd

private class EventEmitter<T> {
    private lateinit var event: (T) -> Unit

    operator fun invoke(event: (T) -> Unit) {
        this.event = event
    }

    fun emit(value: T) = event(value)
}

private fun reduxComponent(inc: EventEmitter<Unit>, dec: EventEmitter<Unit>, effect: EventEmitter<Unit>) = component {
    val state = useSelector<State>()
    val dispatch = useDispatch<Msg>()
    inc { dispatch(Msg.Increment) }
    dec { dispatch(Msg.Decrement) }
    effect { dispatch(Msg.Effect) }
    text(state.count.toString())
}

class ReduxTest {
    private var sideEffect = 0

    private val store = createStore(
        init = Pair(State(count = 0), null),
        update = { state: State, msg: Msg ->
            when (msg) {
                is Msg.Increment ->
                    Pair(State(count = state.count + 1), null)

                is Msg.Decrement ->
                    Pair(State(count = state.count - 1), null)

                is Msg.Effect ->
                    Pair(state, Cmd)
            }
        },
        execute = { _, dispatch ->
            sideEffect += 1
            console.log("sideEffect", sideEffect)
            dispatch(Msg.Increment)
        }
    )

    private val inc = EventEmitter<Unit>()
    private val dec = EventEmitter<Unit>()
    private val effect = EventEmitter<Unit>()

    private val root = ReactTestRenderer.aktCreate {
        store.provider(listOf(reduxComponent(inc, dec, effect)))
    }

    @Test
    fun testInit() {
        assertJsonEquals("0", root.toJSON())
        assertEquals(0, sideEffect)
    }

    @Test
    fun testAction() {
        ReactTestRenderer.akt { inc.emit(Unit) }
        assertJsonEquals("1", root.toJSON())
        assertEquals(0, sideEffect)

        ReactTestRenderer.akt { dec.emit(Unit) }
        assertJsonEquals("0", root.toJSON())
        assertEquals(0, sideEffect)
    }

    @Test
    fun testEffect() = GlobalScope.promise {
        ReactTestRenderer.akt {
            effect.emit(Unit)
        }
        Promise.resolve(Unit).await()
        assertJsonEquals("1", root.toJSON())
        assertEquals(1, sideEffect)

        ReactTestRenderer.akt { effect.emit(Unit) }
        Promise.resolve(Unit).await()
        assertJsonEquals("2", root.toJSON())
        assertEquals(2, sideEffect)
    }
}
