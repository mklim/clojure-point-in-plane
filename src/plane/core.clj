(ns plane.core
  (:require [plane.build :refer [build]]
            [plane.query :refer [query]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def cli-options
  [["-o" "--output OFILE" "Output results file (default plane_results.txt)"
    :default "plane_results.txt"
    :parse-fn #(str %)]
   ;; A non-idempotent option
   ["-i" "--input POINTS" "Location of input query file (default points.txt)"
    :default "points.txt"]
   ["-c" "--inputcoords COORDS" "Location of raw map input file"
    :default "data/gr_planes.txt"]
   ["-p" "--inputmap IMAP" "Location of parsed map input file "
    :default "data/gr_slabs.clj"]
   ["-s" "--outputmap OMAP" "Location of parsed map output file "
    :default "data/gr_slabs.clj"]
   ["-h" "--help" "Print this usage message."]])

(defn usage [options-summary]
  (->> ["Find the Grand Rapids plane of latitude and longitude points."
        ""
        "Usage: plane [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  find          Search for planes in a parsed map."
        "  build         Parse a new plane map."
        ""
        "Refer to the Readme for more information"]
        (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  status)

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit (usage summary) 0)
      (not= (count arguments) 1) (exit (usage summary) 1)
      errors (exit (error-msg errors) 1))
      (case (first arguments)
          "find" (query options)
          "build" (build options)
          (exit 1 (usage summary)))))