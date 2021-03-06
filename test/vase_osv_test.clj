(ns vase-osv-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [vase-osv.test-helpers :refer :all]))

(deftest home-page-test
  (is (.contains
       (:body (response-for (test-service) :get "/"))
       "Hello, World!"))
  (is (=
       (:headers (response-for (test-service) :get "/"))
       {"Content-Type" "text/plain"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"})))
