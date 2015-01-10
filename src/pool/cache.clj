(ns pool.cache
  (:refer-clojure :exclude [get]))

(defn- ignore [o & more])

(declare purge)

(defn- is-snoring?
  ; Checks if the element has reached the idle-timeout
  [celem idle-timeout]
  (if (empty? celem) false
    (and (> idle-timeout -1) (>= (- (System/currentTimeMillis) (:ts celem)) idle-timeout))))

(defn- touch-elem
  ; Updates timestamp for element
  [cache elem key]
  ((swap! cache assoc key (assoc elem :ts (System/currentTimeMillis))) key))

(defmacro shutdown-hook
  [& body]
  `(.addShutdownHook (Runtime/getRuntime) (Thread. (fn [] ~@body))))

(defn get-cache
  "Returns a new threadsafe cache for costly objects.
  Safeguards against multiple threads trying to create object for same key.
  Take a single arity function @make-fn takes key for object creation which.
  Other optional kwarg are
  :destroy double arity function which take key and object.
  :idle-timeout time in millis to refresh idle objects. -1 to ignore"
  [make-fn & {:keys [destroy idle-timeout] :or {destroy ignore idle-timeout -1}}]
  (let [cache {:cache (atom {}) :make make-fn :destroy destroy :idle-timeout idle-timeout}]
    (shutdown-hook
      (doseq [[key object] @(:cache cache)]
        (destroy key object)))
    cache))

(defn get
  "Get object associated with @key from @cache."
  [cache key]
  (let [cache* (:cache cache)
        make-fn (:make cache)
        idle-timeout (:idle-timeout cache)
        celem (@cache* key)]
    ; Double-Checked-Locking works as atom works like a volatile here.
    (if (and celem (not (is-snoring? celem idle-timeout)))
      (:elem (touch-elem cache* celem key))
      (locking cache
        (let [celem (@cache* key)]
          (if (and celem (not (is-snoring? celem idle-timeout)))
            (:elem (touch-elem cache* celem key))
            (do (when (is-snoring? celem idle-timeout) (purge cache key))
                (:elem ((swap! cache* assoc key {:elem (make-fn key) :ts (System/currentTimeMillis)}) key)))))))))

(defn purge
  [cache key]
  (let [cache* (:cache cache)
        destory-fn (:destroy cache)]
    ; lock?
    (when-let [object (@cache* key)]
      (destory-fn key object)
      (swap! cache* dissoc key))))

(defn exists?
  [cache key]
  (:elem ((-> cache :cache deref) key)))
