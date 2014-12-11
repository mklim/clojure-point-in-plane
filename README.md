# plane

Slab implementation of the point in plane problem.

## Installation

Extract the contents of the executable directory anywhere. Requires Java (https://www.java.com/en/download/).

## Usage

    $ java -jar plane-0.1.0-standalone.jar [options] [build | find]

To find all the results for an input file:

    $ java -jar plane-0.1.0-standalone.jar -i <input_file_location> find

The default input file location is "points.txt."

Input file format:

  * One x/y coordinate per line
  * Coordinates in the form of x,y (note the lack of spaces)

The program relies on a parsed map of all the planes in the area.

    $ java -jar plane-0.1.0-standalone.jar -c <input_coords_file_location> build

Format for a plane coordinates file:

  * One point OR plane name per line.
  * plane names must be followed by a colon.
  * Lat/long points must be in the format lat,long.


## Options
  * -o, --output                                         Output results file (default plane_results.txt)
  * -i, --input POINTS        points.txt                 Location of input query file (default points.txt)
  * -c, --inputcoords COORDS  data/gr_planes.txt  Location of raw map input file
  * -p, --inputmap IMAP       data/gr_slabs.clj          Location of parsed map input file
  * -s, --outputmap OMAP      data/gr_slabs.clj          Location of parsed map output file
  * -h, --help                                           Print this usage message.



## Examples

Loading input points from "mypoints.txt" and writing the results to "myresults.txt"

    $ java -jar plane-0.1.0-standalone.jar -i "mypoints.txt" -o "myresults.txt" find

Parsing a new plane map from "newplanes.txt" and saving it in the default location.

    $ java -jar plane-0.1.0-standalone.jar -c "newplanes.txt" build

Parsing a new plane map from "newplanes.txt" and saving it as "newparsedmap.txt".

    $ java -jar plane-0.1.0-standalone.jar -c "newplanes.txt" -s "newparsedmap.txt" build

Loading input points from "newpoints.txt", a new plane map called "newparsedmap.txt", and getting the results:

    $ java -jar plane-0.1.0-standalone.jar -i "mypoints.txt" -p "newparsedmap.txt" find

## Testing

Running tests require the source code and that lein (https://github.com/technomancy/leiningen) and clojure (http://clojure.org/) be installed. Once installed, navigate to the home directory of the source code and run:

    $ lein test

## License

Distributed under the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php).