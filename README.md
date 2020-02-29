# Akryl Redux

Akryl wrapper around [redux](https://github.com/reduxjs/redux) and [react-redux](https://github.com/reduxjs/react-redux) libraries, 
but it's more like [elm-architecture](https://guide.elm-lang.org/architecture/).

# Install

See full project set up in [akryl-core](https://github.com/akryl-kt/akryl-core) repository.

1. Add jcenter repository:

```gradle
repositories {
    jcenter()
    ...
}
```

2. Add dependencies:

```gradle
kotlin {
    sourceSets {
        main {
            dependencies {
                implementation "io.akryl:akryl-redux:0.+"
                implementation npm("redux", "4.0.5")
                implementation npm("react-redux", "7.1.3")
            }
        }
    }
}
```

# Quick Start

```kotlin
sealed class Msg {
    object Increment : Msg()
    object Decrement : Msg()
}

val store = createStore<Int, Msg, Nothing>(
    init = Pair(0, null),
    update = { state, msg ->
        when (msg) {
            is Msg.Increment -> Pair(state + 1, null)
            is Msg.Decrement -> Pair(state - 1, null)
        }
    },
    execute = { emptyList() } 
)

fun counter() = component {
    val count = useSelector<Int>()
    val dispatch = useDispatch<Msg>()

    Div(
        Button(text = "-", onClick = { dispatch(Msg.Decrement) }),
        Text(count.toString()),
        Button(text = "+", onClick = { dispatch(Msg.Increment) })
    )
}

val app = store.provider(listOf(
    counter()
))
```

# Documentation

The library provides global state management based on functional programming principles. 
It is useful for large applications where it is hard for components to communicate with each other directly.

Main concepts of the library are:

- State - an immutable object that contains the global application state.
- Store - a container that holds the current state.
- Message - an object that describes mutation to be applied to the state. 
The mutation must be synchronous and side-effects free.
- Command - an object that describes an external side-effect. 
It can be an HTTP request, write to localStorage or anything that changes the outer world.
- Update - a pure function that takes the current state and a message, and returns a new state and, optionally, a command.
- Executor - a suspended impure function that executes commands and emits messages.

```
+------------------------------------------------------------+
|                                                            |
|                                                            |
|             +---------+           +----------+             |
|             |         |   State   |          |             |
|             |         +---------->+   View   +-------------+
|             |         |           |          |
|   Message   |         |           +----------+
+------------>+  Store  |
|             |         |           +----------+
|             |         |    Cmd    |          |
|             |         +---------->+ Executor +-------------+
|             |         |           |          |             |
|             +---------+           +----------+             |
|                                                            |
|                                                            |
+------------------------------------------------------------+
```

## DevTools

Redux DevTools are fully compatible with `akryl-redux`. To connect them, add the following lines to the store creation:

```kotlin
val store = createStore<State, Msg, Cmd>(
    ...
    enhancer = js("window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()")
        .unsafeCast<StoreEnhancer<State, MsgAction<Msg>>>()
)
```


