(defproject ptcount "0.0.1-SNAPSHOT"
  :description "A prototype to count votes of a potentially cyclic delegational tree, similar to Votorola."
  :url "http://zelea.com/project/votorola/home.xht"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  ; ensure enough memory for voter registry
  :jvm-opts ["-Xmx3000m"]
;  :repl-options {:port 4555}
;  :main ptcount.core
)
