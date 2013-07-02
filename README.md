# Easy Analytics For Hadoop (and others)

## Vision

This goal of this project is creating an easy to use analytics "kit" for Hadoop
and other big data stores utilizing XAP.  The following use case illustrates
the ultimate goal:

1. User already has an installed Hadoop (or other) cluster.
* User desires to compute real time analytics on data flowing to the Hadoop cluster.
* User downloads XAP lite (or a XAP eval version) and unzips it on a node that
has network access to the Hadoop cluster.
* User downloads the "kit" and deploys it on the XAP node.  Should be as simple
as unzipping.
* User performs a single configuration step that:
 1. Defines the connection details to the Hadoop cluster/Name node
 * Defines (optionally with reasonable default) the input data tokenization
 * Defines the desired analytics to gather
 * Defines (optionally with reasonable default) the file name strategy for HDFS.
 * Defines (optionally with reasonable default) the serialization method for HDFS file entries.
* System must be dynamic in nature to avoid complexity of processing unit construction.
* System must provide simple ways to:
 1. Insert data.  No knowledge of XAP should be required.
 * Dynamically deploy new aggregators and aggregator logic.  No XAP knowledge should be required.
 * Retrieve/view aggregator results without XAP knowledge or APIs.

## Design

The repo contains several projects that serve the goals above.  I decided to use REST for all user interactions with XAP.  This spans data input through system configuration and aggregator querying.  The projects:

* **hdfs-archiver** - an archiver implementation that supports the serialization and
HDFS file naming strategies mentioned above.  As of this writing, the archiver isn't
dynamic (must be configured via pu.xml), which will have to be changed to support
the simplicity goals.

* **analytics-rest** - the REST API implementation that serves as the user interaction
point to the system.  The API itself is dynamic, in the sense that input events
are parsed by user code defined in Groovy.  One of the REST API calls deploys the
groovy code for parsing input records into tokens/tuples in XAP.  Ultimately, this
code should be defined in the master configuration discussed earlier.

* **analytics-dyna** - this project implements the dynamic aggregation infrastructure
that will ultimately run in XAP.  It supports dynamically spawning dynamic defined
containers that execute user defined logic for performing aggregations.

* **analytics-dyna-pu** - the is the deployment unit for the analytics engine.  Contains no code of it's own, but deploys code found in _analytics-dyna_.

* **dynamic-grid** - this project contains code that serves as the basis for the dynamic execution in the system.  Providers of updatable logic are consumers of this project.

* **install** - this project contains scripts needed to create an overlay on a XAP installation that provides for easy startup and deployment.  It includes a script to create a zip file to contain the overlay, plus jars and scripts that get placed into various XAP directories, including the master configuration described above.

## quick-start guide

1. Download and install **hadoop** from http://hadoop.apache.org/releases.html . $HADOOP_HOME/sbin/start-all.sh...
* Download and install **gigaspaces** from http://www.gigaspaces.com/LatestProductVersion to $JSHOMEDIR (location of unzipped gigaspaces directory)

