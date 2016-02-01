(ns vase-osv.routes
  (:require [datomic.api :as d]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.log :as log]
            [ring.util.response :as ring-resp]
            [vase.interceptor :as interceptor]
            [vase.config :as conf]
            [vase]))

(defn clj-ver
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::clj-ver))))

(defn health-check
  [request]
  (ring-resp/response "alive"))

(defn make-master-routes
  [vase-context-atom]
  `["/" {:get health-check} ^:interceptors [interceptor/attach-received-time
                                            interceptor/attach-request-id
                                            http/html-body
                                            ~(interceptor/bind-vase-context vase-context-atom)]
    ["/about" {:get clj-ver}]
    ^:vase/api-root ["/api" {:get vase/show-routes}
                     ^:interceptors [http/json-body
                                     interceptor/vase-error-ring-response]]])

(defn service-map
  ([] (service-map {}))
  ([service-overrides]
   (let [config       (conf/default-config)
         vase-context (atom (vase/map->Context {:config config}))]
     (vase/bootstrap-vase-context! vase-context (make-master-routes vase-context))
     (merge {:env                 :dev
             :vase/context        vase-context
             ::http/host          (or (config :host) "127.0.0.1")
             ::http/port          (or (config :port) 8080)
             ::http/type          :jetty
             ::http/join?         false
             ::http/allowed-origins ["http://localhost:3449"]
             ::http/resource-path "/public"
             ::http/routes        (if (config :enable-upsert)
                                    #(:routes @vase-context)
                                    (:routes @vase-context))}
            service-overrides))))
