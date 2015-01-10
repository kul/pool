(ns pool.cache-test
  (:require [clojure.test :refer :all]
            [pool.cache :as c]))

; Initialize Cache
(def cache
  (c/get-cache
    (fn [k] (let [ts (System/currentTimeMillis)]
              (println (str "Inititalized: " k " at: " ts)) ts))
    :destroy (fn [k v] (println "Destroyed: " k) true)))

; Initialize Cache with idle timeout
(def icache
  (c/get-cache
    (fn [k] (let [ts (System/currentTimeMillis)]
              (println (str "Inititalized: " k " at: " ts)) ts))
    :destroy (fn [k v] (println "Destroyed: " k) true) :idle-timeout 2000))

(deftest test-cache
  ; Should be possible to add new elements
  (let [ts (c/get cache "first")]
    ; Basic check for value
    (is (>= (System/currentTimeMillis) ts))
    ; create drift
    (Thread/sleep 500)
    ; Same element key should not be re-initialized
    (is (= ts (c/get cache "first")))
    ; Added element should be present
    (is (= ts (c/exists? cache "first")))
    ; Should be possible to drop existing element
    (is (= {} (c/purge cache "first")))
    ; Object should no longer exist
    (is (nil? (c/exists? cache "first")))
    ; Purge should be a no-op
    (is (nil? (c/purge cache "first")))))

(deftest idle-timeout
  ; Should be possible to add new elements
  (let [lts (c/get icache "leech")
        bts (c/get icache "bird")]
    ; Basic check for value
    (is (>= (System/currentTimeMillis) lts))
    (is (>= (System/currentTimeMillis) bts))
    ; create drift
    (Thread/sleep 500)
    ; Added element should be present and not re-initialized
    (is (= lts (c/exists? icache "leech")))
    (is (= lts (c/get icache "leech")))
    (is (= bts (c/exists? icache "bird")))
    (is (= bts (c/get icache "bird")))
    (Thread/sleep 1000)
    ; Same element key should not be re-initialized
    ; but access timestamp should be updated
    (is (= lts (c/get icache "leech")))
    (Thread/sleep 1000)
    ; Accessed element should be present
    (is (= lts (c/exists? icache "leech")))
    ; Accessed element should not be re-initialized
    (is (= lts (c/exists? icache "leech")))
    ; Bird should be re-initialized
    (is (< bts (c/get icache "bird")))
    ; Should be possible to drop existing element
    (is (c/purge icache "leech"))
    (is (c/purge icache "bird"))
    ; Object should no longer exist
    (is (nil? (c/exists? icache "leech")))
    (is (nil? (c/exists? icache "bird")))
    ; Purge should be a no-op
    (is (nil? (c/purge cache "leech")))
    (is (nil? (c/purge cache "bird")))))
