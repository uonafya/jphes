
# JPHES on DHIS 2

DHIS 2 is a flexible information system for data capture, management, validation, analytics and visualization.

The software is open source and released under the [BSD license](https://opensource.org/licenses/BSD-2-Clause).

For documentation please visit the [doc page](https://www.dhis2.org/documentation/).

To can download pre-built WAR files from the [contiunuous integration server](http://ci.dhis2.org/).

You can explore various demos of DHIS 2 in the [play environment](https://play.dhis2.org/).

For general info please visit the [project web page](https://www.dhis2.org/).

## Build process

This repository contains the source code for the server-side component of DHIS 2, which is developed in [Java](https://www.java.com/en/) and built with [Maven](https://maven.apache.org/). 

To build it you must first install the root POM file, navigate to the dhis-web directory and then build the web POM file.

<pre>
cd dhis-2
mvn install
cd dhis-web
mvn install
</pre>

## Project under development

JPHES partner data attribution, running on dhis2.
