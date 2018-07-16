(ns occupy.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require ;;[taoensso.timbre :refer [info]]
            [cljs-http.client :as http]
            [dommy.core :as d]
            [hipo.core :as hipo]
            [cemerick.url    :as url]
            [cljs.core.async :as async :refer [<! >! chan]]
            [occupy.links :refer [dashboard-url]]))



;; todo: add hiccups (test which is smaller)
;; check how libraries affect the size of program
;; move timbre's info behind an internal api


(def internet (chan))


(defn table-el [{columns :columns rows :rows :as args}]
  (hipo/create
   [:table
    [:tbody
     [:tr.table-header
      (for [c columns]
        [:td c])]
     (for [r rows]
       [:tr
        (for [d r]
          [:td d])])]]))




(defn get-url [url]
;;  (info "getting" url)
  (let [c (chan)]
    (go
      (let [{data :body} (<! (http/get url {:with-credentials? false}))]
        (>! c data)))
    c))



(defn coerce-article [data]
  (map #(into [] (clojure.string/split % #"\t"))
       (-> data
           (clojure.string/split #"\r\n"))))



(defn append-article [article]
  (-> (d/sel1 :#occupy-grid)
      (d/append! (table-el {:columns (first article)
                            :rows (rest article)}))))



(defn fetch-articles [starting-url]
  (go
    (let [article-urls (filter (partial re-find #"http.?://")
                               (-> (<! (get-url starting-url))
                                   (clojure.string/split #"\r\n")))]

;;      (info "article urls" article-urls)

      (let [article-chans (map get-url article-urls)]
        (when-not (empty? article-urls)
          (while true
            (let [[article] (async/alts! article-chans)]
;;              (info "got article" article)
              (-> article coerce-article append-article))))))))



(defn ^:export -main [& args]
  (fetch-articles dashboard-url))
