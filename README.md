# pool

Clojure wrapper for Apache Commons Pool.

##Install

[![Clojars Project](http://clojars.org/pool/latest-version.svg)](http://clojars.org/pool)

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

## Caching
Caching although a different concept is provided in the library for caching
objects which are threadsafe in themselves and thus can be shared instead of
pooled, but are very costly to create.

`get-cache` and `get` are the only available methods to work with.  `get-cache`
take a single arity function on key for object creation.

```clojure
(require '[pool.cache :as c])
(def cache (c/get-cache (fn [k] (print "once!") (Thread/sleep 1000) k)))
(c/get cache 1) ;=> once! 1

; Safeguards against multiple threads.
(defn double-write [x] (future (c/get cache x)) (future (c/get cache x)))
(double-write 5) ;=> once! ...
```

## License

Copyright © 2014 kul

Distributed under the Eclipse Public License.
