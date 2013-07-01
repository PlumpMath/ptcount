(ns ptcount.core
  (:require [clojure.set :as set]))


(defn remove-voter
  "Takes a registry map and removes mailish voter
   from candidate cand. Returns new registry."
  [registry cand voter]
  (if (registry cand)
    (update-in registry [cand :voters] (fn [vtrs] (set/difference vtrs #{voter})))
    registry))


(defn add-voter
  "Takes a registry map and adds mailish voter
   to candidate cand. Returns new registry."
  [registry cand voter]
  (if (registry cand)
    (update-in registry [cand :voters] conj voter)
    registry))


(defn vote
  "Cast vote for voter to candidate in registry."
  [registry voter candidate]
  (let [{old-voter voter} registry
        {old-cand :candidate} old-voter]
    (-> registry
        (remove-voter old-cand voter)
        (add-voter candidate voter)
        (assoc voter (if old-voter (assoc old-voter :candidate candidate)
                         {:voter voter :candidate candidate :voters #{}})))))


(defn count-votes
  "Counts all votes for voter in registry.
   Handles cycles correctly."
  ([registry voter]
     (count-votes registry voter #{}))
  ([registry voter path]
     (if (path voter) 0
         (reduce + 1
                 (map #(count-votes registry % (conj path voter))
                      ((registry voter) :voters))))))


(defn scenario1
  "Cast votes for max-cnd candidate levels and
   10 times as many voters at the leafs. Returns new registry."
  ([registry]
     (scenario1 registry 0 1000 "V"))
  ([registry cnds max-cnds prefix]
     (let [cand (str prefix cnds)
           ensure-cand #(if-not (% cand) (vote % cand nil) %)
           new-registry (reduce #(vote %1 (str prefix cnds %2) cand)
                                (ensure-cand registry)
                                (range 0 10))]
       (if (< cnds max-cnds) (recur new-registry (inc cnds) max-cnds prefix)
           new-registry))))


(comment
  (def test-registry (atom {}))
  ((vote @test-registry "Some2 MailCom" "Some MailCom") "Some2 MailCom")

  ; load more than 5 million votes equally distributed by 10 voters per candidate
  ; each letter in upper and lower case marks an individual end-candidate and its voters
  ; each end-candidate holds 11111 votes
  ; takes ~45 secs here
  (time (swap! test-registry (fn [old] (reduce #(scenario1 %1 0 10000 %2) old
                                              "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))))
  ; total > 5 million voters = hash-map entries (nested maps)
  (count @test-registry)

  ; test counts, complexity \in O(log n), practically instant
  (count-votes @test-registry "c2")
  (@test-registry "c20000")

  ; create cycle
  (swap! test-registry #(vote % "c2" "c20000"))
  ; count again, still the same ...
  (count-votes @test-registry "c2"))
