(defproject fractals "0.1.0-SNAPSHOT"
  :description "Explore the Mandelbrot fractal."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apfloat/apfloat "1.6.3"]
                 [quil "2.6.0"]]
  :jvm-opts ["-Dcom.sun.management.jmxremote"
           "-Dcom.sun.management.jmxremote.ssl=false"
           "-Dcom.sun.management.jmxremote.authenticate=false"
           "-Dcom.sun.management.jmxremote.port=43210"]
  :main ^:skip-aot fractals.core)
