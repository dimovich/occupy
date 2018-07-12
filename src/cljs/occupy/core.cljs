(ns occupy.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [taoensso.timbre :refer [info]]
            [cljs-http.client :as http]
            [dommy.core :as d]
            [cemerick.url    :as url]
            [cljs.core.async :as async :refer [<! >! chan]]
            [occupy.links :refer [dashboard-url]]))



;; todo: add hiccups


(def internet (chan))


(defn get-url [url]
  (info "getting" url)
  (let [c (chan)]
    (go
      (let [{data :body} (<! internet #_(http/get url {:with-credentials? false}))]
        (>! c data)))
    c))



(defn coerce-article [data]
  (-> data
      (clojure.string/split #"\r\n")))



(defn append-article [article]
  (let [el (d/create-element :pre)]
    (d/set-text! el article)
    (-> (d/sel1 :#occupy-grid)
        (d/append! el))))




(defn fetch-articles [starting-url]
  (go
    (let [article-urls (filter (partial re-find #"http.?://")
                               (-> (<! (get-url starting-url))
                                   (clojure.string/split #"\r\n")))]

      (info "article urls" article-urls)

      (let [article-chans (map get-url article-urls)]
        (when-not (empty? article-urls)
          (while true
            (let [article (async/alts! article-chans)]
              (info "got article" article)
              (-> article append-article))))))))



(defn ^:export -main [& args])
