(ns runtime-deps.core
  (:require [cemerick.pomegranate :refer [add-dependencies]])
  (:import [clojure.lang Reflector]))

(def dcl (clojure.lang.DynamicClassLoader.))

(defn dynamically-load-class! [class-loader class-name]
  (let [class-reader (clojure.asm.ClassReader. class-name)]
    (when class-reader
      (let [bytes (.-b class-reader)]
        (.defineClass class-loader class-name bytes "")))))

;; lein run redis.clients jedis 2.10.0 redis.clients.jedis.Jedis
(defn -main [& [group-id artifact-id version classname]]
  (add-dependencies :coordinates [[(symbol group-id artifact-id) version]]
                    :repositories (merge cemerick.pomegranate.aether/maven-central
                                         {"clojars" "https://clojars.org/repo"}))
  (dynamically-load-class! dcl classname)
  (let [jedis (Reflector/invokeConstructor
               (Class/forName classname)
               (into-array []))]
    (.set jedis "foo" "bar")
    (println "foo:" (.get jedis "foo"))))
