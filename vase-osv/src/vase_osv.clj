(ns vase-osv
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ns-tracker.core :refer [ns-tracker]]
            [vase-osv.config :refer [config]]
            [vase-osv.util :as util]
            [vase-osv.routes :as routes]
            [vase-osv.db :as db])
  (:gen-class))

(def config-map (util/edn-resource "system.edn"))

(defn conf
  ([k]
  (config config-map k))
  ([k not-found]
   (config config-map k not-found)))

(defonce modified-namespaces
  (if (conf :prod)
    (constantly nil)
    (ns-tracker ["src"])))

(defonce datomic-uri (conf :datomic-uri (db/new-db-uri)))

(defn server [service-overrides]
  (http/create-server (routes/service-map service-overrides)))

(defn start [& args]
  (db/bootstrap! datomic-uri)
  (let [service-overrides (apply hash-map args)
        server (server service-overrides)]
    (http/start server)))

(defn stop [serv]
  (if serv
    (http/stop serv)
    serv))

(defn restart [serv]
  (if serv
    (http/start (stop serv))
    serv))

(defn -main [& args]
  (start ::http/join? true
         :env         :prod))
