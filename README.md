# pool

Clojure wrapper for Apache Commons Pool.

##Install

```
[![Clojars Project](http://clojars.org/pool/latest-version.svg)](http://clojars.org/pool)
```

## Usage

```clojure
(use 'pool.core)
```

### Object Pool
`get-pool` takes a no argument function for object creation.
```clojure
(def pool (get-pool (fn [] :heavy-object)))
```
Next, `borrow` and `return` from pool.

```clojure
(borrow pool) ;=> :heavy-object
(return pool :heavy-object)
```

###Keyed Object Pool
`get-keypool` takes a single arity function for object creation.
```clojure
(def pool (get-keypool (fn [k] (str :heavy k))))

(borrow pool :object) ;=> ":heavy:object"
; Return key and object
(return pool :object ":heavy:object")
```

`get-pool` and `get-keypool` takes optional kwargs for `destory`, `validate`,
`activate` and `passivate` for finer control of object creation and deletion.

## License

Copyright © 2014 kul

Distributed under the Eclipse Public License.
