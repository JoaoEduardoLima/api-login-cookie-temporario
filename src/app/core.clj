(ns app.core
  (:require
   [compojure.core :refer [defroutes context GET POST PUT DELETE]]
   [compojure.route :as route]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cookies :refer [wrap-cookies]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.middleware.params :refer [wrap-params]])
  (:gen-class))

;; -----------------------------
(def salved-tokens (atom {}))

;; -----------------------------
(defn create-token []
  (format "%x%d" (rand-int 1000000) (rand-int 1000000)))

(defn id-password-ok? [id password]
  (cond 
    (and (= id "test") (= password "123"))    {:user-valid? true, :role "user"}
    (and (= id "admin") (= password "admin")) {:user-valid? true, :role "admin"}))

;; -----------------------------
(defn handler-health [_req]
  {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, health!"}})

(defn handler-unauthorized [_req]
  {:status 401 :headers {"Content-Type" "application/json"} :body {:msg "Unauthorized"}})

(defn get-handler-login [_req]
  {:status 200 :headers {"Content-Type" "text/html"} :body (slurp "src/app/index.html")})

(defn post-handler-login [req]
  (let [params (:params req)
        {:keys [user-valid? role]} (id-password-ok? (get params "id") (get params "password"))]
    (if user-valid?
      (let [token (create-token)
            ip (:remote-addr req)
            _ (swap! salved-tokens assoc ip {:token token :role role})]
       {:status 200 
        :headers {"Content-Type" "application/json"}
        :body {:msg "Login successful!"} 
        :cookies {"token" {:value token, :max-age 120}}})
      (handler-unauthorized req))))

;; -----------------------------
(defn middleware-auth [handler]
  (fn [req]
    (let [ip (:remote-addr req)
          token (get-in req [:cookies "token" :value])
          token-salvo (get-in @salved-tokens [ip :token])]
      (if (and token (= token token-salvo))
        (handler req)
        (handler-unauthorized req)))))

(defn middleware-auth-admin [handler]
  (fn [req]
    (let [ip (:remote-addr req)
          token (get-in req [:cookies "token" :value])
          token-salvo (get-in @salved-tokens [ip :token])
          user-role (get-in @salved-tokens [ip :role])]
      (if (and token (= token token-salvo) (= user-role "admin"))
        (handler req)
        (handler-unauthorized req)))))

;; -----------------------------
(defroutes app
  (GET  "/health" [] handler-health)
  (GET  "/login"  [] get-handler-login)
  (POST "/login"  [] post-handler-login)
  (middleware-auth
   (context "/api/v1" []
     (GET    "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, GET!"   }})
     (POST   "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, POST!"  }})
     (PUT    "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, PUT!"   }})
     (DELETE "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, DELETE!"}})))
  (middleware-auth-admin
   (context "/api/v1/admin" []
     (GET    "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, GET! (ADMIN)"   }})
     (POST   "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, POST! (ADMIN)"  }})
     (PUT    "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, PUT! (ADMIN)"   }})
     (DELETE "/" [] {:status 200 :headers {"Content-Type" "application/json"} :body {:msg "Ok, DELETE! (ADMIN)"}})))
  (route/not-found "<h1>Page not found</h1>"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (jetty/run-jetty (-> app
                       (wrap-params :params)
                       (wrap-json-body {:keywords? true})
                       wrap-json-response
                       wrap-multipart-params
                       wrap-cookies)
                   {:port 3010}))