(ns maze.core
  (:require [clojure.core.async :refer [>! <! >!! <!! go chan]]
            [org.httpkit.client :as http]
            [clojure.data.json :as json]))

; state of the known maze that map cell to its shortest path from start in string form
(def state (atom {:pending true}))

(defn- http-get
  ([path param]
    (http-get path param nil))
  ([path param fn]
   (let [url (str "https://challenge.flipboard.com/" path)
         qparam (if-let [id (@state :mazeid)]
                  {:query-params (assoc param :s id)})
         opt (merge {:keepalive 30000} qparam)]
     (if (nil? fn)
       @(http/get url opt)
       (http/get url opt fn)))))

(defn- complete [path]
  (swap! state #(assoc % :pending false))
  (->> (:start-time @state)
       (- (System/currentTimeMillis))
       (println "result=" path "taking "))
  (->> (http-get "check"  {:guess path} nil)
       :success
       (if (println "Verified!")
         (println "Oops, check failed."))))

(def query-cell
  (memoize
    (fn [cell]
      (let [body (:body (http-get "step" cell nil))
            value (if (= "Invalid request." body)
                    {:valid false}
                    (json/read-str body :key-fn keyword))]
        value))))

(defn- set-path
  "calculate and set the shortest path from cell to start
   stop if done, return cell value"
  [cell {:keys [letter adjacent end] :as value}]
  (let [npaths (filter some? (map @state adjacent))
        path (if (empty? npaths)
               letter
               (str (apply min-key count npaths) letter))]
    (println "found " cell " path " (pr-str path))
    (swap! state #(assoc % cell path))
    (when end
      (complete path)))
  value)

(defn step [cell]
  (when (:pending @state)
    (->> (query-cell cell)
         (set-path cell)
         :adjacent
         (filter #(not (contains? @state %)))
         (map step)
         (doall))))

(defn- crawl []
  (let [ch (chan)]
    (doseq [_ (range 10)]
      (go
        (while (and (:pending @state)
                    ;cell is still valid with letter
                    ((query-cell (<! ch)) :letter)))))
    (go (loop [cells (for [b (range) x (range b)] {:x x :y (- b x)})]
          (when (:pending @state)
            (>! ch (first cells))
            (recur (rest cells)))))))

(defn -main [& args]
  (->> (http-get "start" {} nil)
       (:opts)
       (:url)
       (re-matches #".*s=([\d\.]*)&.*")
       (last)
       (assoc {:start-time (System/currentTimeMillis)} :mazeid)
       (partial merge) ; (fn [a] (merge {:a 1}  a))
       (swap! state))
  (crawl)
  (step {:x 0 :y 0}))