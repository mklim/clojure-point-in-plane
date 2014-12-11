(ns plane.query
  (:gen-class))

(defn query
  "Load a list of points from a file and print found hoods to file"
  [options]
  (declare find-hoods, load-pts, load-slabs, prettify)
  (def pts (load-pts (get options :input)))
  (def slabs (load-slabs (get options :inputmap)))
  (if
    (or (false? pts) (false? slabs))
    (do
      (println "ERR: loading files. Please check the path and try again.") 1)
    (do
      (def hoods (prettify (find-hoods pts slabs) 1))
      (println "Results Found!\n" hoods "Saving to file...")
      (try
        (do
          (spit (get options :output) hoods)
          (println (str "OK! Results saved to " (get options :output)))
          0)
      (catch Exception ex
        (println (str "ERR: Couldn't save results! Check that you have permission to read and write "
                      "to " (get options :output)))
        1)))))

(defn prettify
  "Take a list of hoods and format for file reading"
  [hoods, i]
  (if (or (nil? hoods) (empty? hoods))
    'nil
    (str (str "Point " i ": " (first hoods) "\n") (prettify (rest hoods) (inc i)))))


; loading functions
(defn load-slabs
  "Read a slab file (created by build()) and return its content as a map"
  [floc]
  (println (str "Loading data from " floc "..."))
  (try
    (read-string (read-string (slurp floc)))
    (catch Exception ex 'false)))

(defn load-pts
  "Reads an input file of query coordinates and returns it as list"
  [inploc]
  (declare parse-lines)
  (try
    (println (str "Loading query points from " inploc "..."))
    (with-open [rdr (clojure.java.io/reader inploc)]
      (parse-lines (line-seq rdr) nil))
    (catch Exception ex 'false)))

(defn parse-lines
  "Returns a list of x,y points for a list of input lines"
  [rawf, lines]
  (declare parse-lines-helper)
  (if (or (empty? rawf) (nil? rawf))
    (if (empty? (remove nil? lines)) 'false lines)
    (parse-lines (rest rawf) (conj lines (parse-lines-helper (first rawf))))))

(defn parse-lines-helper
  "Returns a list of x,y coordinates for a valid line, nil otherwise"
  [line]
  (def pts
    (first
      (map #(clojure.string/split % #",") (re-seq #"[\-\.0-9]+,[\-\.0-9]+" line))))
  (if (nil? pts)
    pts
    (map #(bigdec %) pts)))


; query functions
(defn find-hoods
  "Returns a list of planes found in a series of points"
  [pts, slabs]
  (declare find-slab find-segment)
  (println "Searching for results...")
  (def xbounds (apply sorted-set (set (keys slabs))))
  (reverse
    (map
      #(do
        (def found-slab (find-slab (first %)  xbounds slabs))
        (if (nil? found-slab)
          "<none>"
          (find-segment %(get-in slabs [found-slab :segments])))) pts)))

(defn find-slab
  "Returns slab that a given x coord is in, none if nothing found"
  [x, bounds, slabs]
  (if (or (< x (first bounds)) (> x (last bounds)))
    'nil
    (last (subseq bounds <= x))))

(defn find-segment
  "Returns name of hood of xy coord, none if not found in segment"
  [pt, segments]
  (declare get-segments)
  (def sorted-segments (get-segments (first pt) segments))
  (def ybounds (apply sorted-set (keys sorted-segments)))
  (def y (second pt))
  (if (or (< y (first ybounds)) (> y (get-in sorted-segments [(last ybounds) :rbound])))
    "<none>"
    (do
      (def segmatch (last (subseq ybounds <= y)))
      (if (<= y (get-in sorted-segments [segmatch :rbound]))
        (get-in sorted-segments [segmatch :nick])
        "<none>"))))

(defn get-segments
  "Returns the y bounds for all hoods in a segment, organized by lbound"
  [x, segments]
  (declare get-segment-bounds)
  (apply merge
    (flatten
      (map #(hash-map(first (get-segment-bounds x %)) (hash-map :nick (get % :nick) :rbound (second (get-segment-bounds x %)))) segments))))

(defn get-segment-bounds
  "Returns the y bounds for specific hood in a segment"
  [x, segment]
  (sort (map #(+ (get % :y) (* (get % :m) (- x (get % :x)))) (get segment :lines))))