(ns pool.cache
  (:refer-clojure :exclude [get]))

(defn- ignore [o & more])

(defmacro shutdown-hook
  [& body]
  `(.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] ~@body))))

(defn get-cache
  "Returns a new threadsafe cache for costly objects.
  Safeguards against multiple threads trying to create object for same key.
  Take a single arity function @make-fn takes key for object creation which.
  Other optional kwarg are
  :destroy double arity function which take key and object."
  [make-fn & {:keys [destroy] :or {destroy ignore}}]
  (let [cache {:cache (atom {}) :make make-fn}]
    (shutdown-hook
      (doseq [[key object] @(:cache cache)]
        (destroy key object)))
    cache))

(defn get
  "Get object associated with @key from @cache."
  [cache key]
  (let [cache* (:cache cache)
        make-fn (:make cache)]
    ; Double-Checked-Locking works as atom works like a volatile here.
    (if-let [object (@cache* key)]
      object
      (locking cache
        (if-let [object (@cache* key)]
          object
          ((swap! cache* assoc key (make-fn key)) key))))))
