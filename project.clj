(defproject todo-backend-pedestal-vase "0.0.1-SNAPSHOT"
  :description      "Example of Todo Backend running as Vase microservice"
  :url              "https://github.com/mtnygard/todo-backend-pedestal-vase"
  :license          {:name "Eclipse Public License"
                     :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies     [[org.clojure/clojure                            "1.7.0-RC2"]
                     [org.clojure/core.async                         "0.1.346.0-17112a-alpha" :exclusions [[org.clojure/tools.analyzer.jvm]]]

                     [io.pedestal/pedestal.service                   "0.4.1-SNAPSHOT" :exclusions [[org.slf4j/slf4j-api]
                                                                                                   [com.cognitect/transit-clj]
                                                                                                   [com.fasterxml.jackson.core/jackson-core]
                                                                                                   [cheshire]]]
                     [io.pedestal/pedestal.jetty                     "0.4.1-SNAPSHOT" :exclusions [org.clojure/clojure]]
                     [com.cognitect/vase                             "0.1.0-SNAPSHOT"]
                     [com.cognitect/transit-clj                      "0.8.275"]
                     [com.fasterxml.jackson.core/jackson-annotations "2.6.0"]

                     [ns-tracker                                     "0.3.0"]
                     [environ                                        "1.0.0"]

                     ;; Datomic
                     [com.datomic/datomic-free                       "0.9.5201" :exclusions [[joda-time]
                                                                                             [org.slf4j/slf4j-nop]
                                                                                             [org.slf4j/slf4j-log4j12]
                                                                                             [com.fasterxml.jackson.core/jackson-annotations]]]
                     [io.rkn/conformity                              "0.3.5" :exclusions [com.datomic/datomic-free]]

                     ;; Logging
                     [org.slf4j/slf4j-api                            "1.7.12"]
                     [ch.qos.logback/logback-classic                 "1.1.3" :exclusions [[org.slf4j/slf4j-api]]]
                     [org.slf4j/jul-to-slf4j                         "1.7.12"]
                     [org.slf4j/jcl-over-slf4j                       "1.7.12"]
                     [org.slf4j/log4j-over-slf4j                     "1.7.12"]]
  :min-lein-version "2.0.0"
  :resource-paths   ["resources" "config"]
  :main ^{:skip-aot true} vase-osv
  :profiles         {:uberjar {:aot [vase-osv]}
                     :dev     {:aliases      {"dumbrepl" ["trampoline" "run" "-m" "clojure.main/main"]}
                               :dependencies [[criterium "0.4.3"]
                                              [thunknyc/profile "0.5.2"]
                                              [org.clojure/tools.trace "0.7.8"]
                                              [org.clojure/tools.namespace "0.2.10"]
                                              [org.clojure/test.check "0.8.0-alpha3"]]}}
  :plugins          [[lein-marginalia "0.8.0" :exclusions [[org.clojure/clojure]
                                                           ;; Use the tools.reader from `cljfmt`
                                                           [org.clojure/tools.reader]]]
                     [codox           "0.8.12" :exclusions [[org.clojure/clojure]]]
                     [lein-cljfmt     "0.1.10" :exclusions [[org.clojure/clojure]]]])
