(ns plane.build
  (:gen-class))


(defn build
    "Builds and saves decomposed slab map from list of lats and longs by plane"
    [options]
    (declare load-map)
    (declare save-slabs)
    (def inloc (get options :inputcoords))
    (def outloc (get options :outputmap))
    (println (str "Loading plane map " inloc "..."))
    (def hoods (load-map inloc))
    (if(false? hoods)
      (do
        (println "ERR: Couldn't load map. Check file location.\nNot OK!")
        1)
      (do
        (println (str "Slicing map into slabs and saving to " outloc "..."))
        (if (true? (save-slabs hoods outloc))
          (do
            (println "OK, done!")
            0)
          (do
            (println (str "ERR: Couldn't save slab file. Make sure that " outloc " is a valid file and that you have write permissions to the directory (see Readme).\nNot OK!"))
            1)))))


; functions to handle loading plane structure
(defn load-map
  "Loads map of planes from file"
  [floc]
  (declare load-map-helper)
  (try
    (with-open [rdr (clojure.java.io/reader floc)]
      (load-map-helper (map #(.trim %) (line-seq rdr)) {}))
    (catch Exception ex 'false)))

(defn load-map-helper
  "Cycles through plane data file and organizes into list of planes"
  [rawf, hoods]
  (declare build-hood)
  (if (or (empty? rawf) (nil? rawf))
    ;(build-hood (rest rawf) nil)
    hoods
    (if(not (nil? (re-find #"[a-zA-Z ]+" (first rawf))))
       (load-map-helper (rest rawf) (merge hoods (hash-map (re-find #"[a-zA-Z \-]+" (first rawf)) (build-hood (rest rawf) nil))))
       (load-map-helper (rest rawf) hoods))))

(defn build-hood
  "Gets data for a specific plane"
  [rawf, pts]
  (declare get-xrange)
  ;exit recursion if eof or the next plane name
  (if (or (empty? rawf) (nil? rawf) (= "" (first rawf)) (not (nil? (re-find #"[a-zA-Z ]+" (first rawf)))))
      (hash-map :xrange (get-xrange pts), :xs (reverse (map #(first %) pts)) :ys (reverse (map #(second %) pts)))
      (build-hood (rest rawf) (conj pts (map #(bigdec %) (re-seq #"[\-\.0-9]+" (first rawf)))))))

(defn get-xrange
  "Return the min and max x values for a series of xy points"
  [pts]
  (def xs (map #(first %) pts))
  [(apply min xs) (apply max xs)])


; functions to handle splicing map from planes
(defn save-slabs
  "Slices a plane map into slabs and saves the results"
  [hoods, fout]
  (declare build-slab-walls)
  (declare build-slabs)
  (def walls (build-slab-walls hoods))
  ; (println fout)
  (def slabs (str (build-slabs nil walls hoods)))
  (try
    (spit fout (with-out-str (pr slabs))) 'true
  (catch Exception ex 'false)))

(defn build-slab-walls
  "Get x coordinates for slabs for a loaded map"
  [hoods]
  (distinct (sort (flatten (map #(get % :xs) (vals hoods))))))

(defn build-slabs
  "Walks through slab walls and gets segments for each slab"
  [slabs, walls, hoods]
  (declare find-hoods-in-slab)
  (declare find-segments-in-slab)
  (if (= 1 (count walls)) ;last wall is just rightmost boundary, don't need to include it
    slabs
    (merge slabs
           (hash-map (first walls)
                     (hash-map :segments (remove nil? (find-segments-in-slab (first walls) (second walls) hoods nil))
                               :rbound (second walls)))
           (build-slabs slabs (rest walls) hoods))))

(defn find-segments-in-slab
  "Walks through hoods and finds all segments for a specific slab"
  [lbound, rbound, lefthoods, matchinghoods]
  (declare get-segment)
  (if (nil? (keys lefthoods))
    matchinghoods
    (find-segments-in-slab
      lbound
      rbound
      (rest lefthoods)
      (conj matchinghoods
        (get-segment lbound rbound (first lefthoods))))))

(defn get-segment
  "Returns segment for hood if it falls into the passed slab"
  [lbound, rbound, hood]
  (declare get-segment-helper)
  (def hood-range (get (val hood) :xrange))
  (if (<= (first hood-range) lbound rbound (second hood-range))
    (do
      (def edges (get-segment-helper lbound rbound (get (val hood) :xs) (get (val hood) :ys)))
      (def segment (hash-map :nick (key hood) :lines edges))
      segment)
    'nil))

(defn get-segment-helper
  "Walks through the edges of a matching hood and returns those that cross slab"
  [lbound, rbound, xs, ys]
  (declare get-slope)
  (if(= (count xs) 1) ;last one in list is just endpoint, don't need to count
    'nil
    (do
      (def edge (list (first xs) (second xs)))
      (if (and (<= (apply min edge) lbound rbound (apply max edge))
               (not= (first edge) (second edge))) ;in case of perfectly vertical lines
          (cons (hash-map :m (get-slope (first edge) (second edge) (first ys) (second ys))
                          :x (first edge)
                          :y (first ys))
                (get-segment-helper lbound rbound (rest xs) (rest ys)))
        (get-segment-helper lbound rbound (rest xs) (rest ys))))))

(defn get-slope
  "Returns the slope for a line segment between two given points"
  [x1, x2, y1, y2]
  (with-precision 24 :rounding CEILING (/ (- y2 y1) (- x2 x1))))