(ns pool.core
  (:import [org.apache.commons.pool2 PooledObject
            PooledObjectFactory KeyedPooledObjectFactory]
           [org.apache.commons.pool2.impl
            GenericKeyedObjectPool GenericObjectPool DefaultPooledObject
            BaseGenericObjectPool]))

(defn- ignore [o & more])

(defn get-pool
  "Returns a pool with user provided methods for handling objects.
  Other kwargs can be passed for finer control but are optional.

  `make` must be a function with no arguments and rest kwargs are function with
  single arity for the object.
  `activate` and `passivate` are meant to be mutating functions for any effect
  on the pooled object.
  `validate` must return truthly value."
  [make & {:keys [destroy validate activate passivate]
      :or {destroy ignore validate (fn [o] true)
           activate ignore passivate ignore}}]
  (let [object-factory (proxy [PooledObjectFactory] []
                         (makeObject [] (DefaultPooledObject. (make)))
                         (destroyObject [^PooledObject o] (destroy (.getObject o)))
                         (validateObject [^PooledObject o] (validate (.getObject o)))
                         (activateObject [^PooledObject o] (activate (.getObject o)))
                         (passivateObject [^PooledObject o] (passivate (.getObject o))))]
    (doto (GenericObjectPool. object-factory)
          (.setTestOnBorrow true))))

(defn get-keypool
  "Returns a keypool with user provided methods for handling objects.
  Other kwargs can be passed for finer control but are optional.

  `make` must be function with single arguments for key and rest kwargs are
  function with two arity for key and the object."
  [make & {:keys [destroy validate activate passivate]
      :or {destroy ignore validate (fn [k o] true)
           activate ignore passivate ignore}}]
  (let [object-factory (proxy [KeyedPooledObjectFactory] []
                         (makeObject [k] (DefaultPooledObject. (make k)))
                         (destroyObject [k ^PooledObject o] (destroy k (.getObject o)))
                         (validateObject [k ^PooledObject o] (validate k (.getObject o)))
                         (activateObject [k ^PooledObject o] (activate k (.getObject o)))
                         (passivateObject [k ^PooledObject o] (passivate k (.getObject o))))]
    (doto (GenericKeyedObjectPool. object-factory)
          (.setTestOnBorrow true))))

(defn borrow
  ([^GenericObjectPool pool] (.borrowObject pool))
  ([^GenericKeyedObjectPool pool key] (.borrowObject pool key)))

(defn return
  ([^GenericObjectPool pool object] (.returnObject pool object))
  ([^GenericKeyedObjectPool pool key object] (.returnObject pool key object)))

(defn add
  "Adds an object to pool and passivate it. Returns nil."
  ([^GenericObjectPool pool] (.addObject pool))
  ([^GenericKeyedObjectPool pool key] (.addObject pool key)))

(defn clear
  "Clear idle objects in pool."
  ([pool] (.clear pool))
  ([^GenericKeyedObjectPool pool key] (.clear pool key)))

(defn close
  ([^BaseGenericObjectPool pool] (.close pool)))
