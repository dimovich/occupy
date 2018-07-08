(ns occupy.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]
            [occupy.links :refer [dashboard-url]]))


(def data (atom nil))

(defn get-url [url]
  (go
    (reset! data
            (-> (<! (http/get url {:with-credentials? false}))
                :body
                (clojure.string/split #"\r\n")))))


(defn ^:export -main [& args])
