scriptdir=`dirname $0`
libdir=$scriptdir/../lib/optional/openspaces
confdir=$scriptdir/../config

$scriptdir/gs-agent.sh gsa.gsc 1 gsa.gsm 1 &
groovy=$scriptdir/../tools/groovy/bin/groovy

CLASSPATH=
for J in `ls $scriptdir/../lib/required/*.jar`; do
  CLASSPATH=$J:$CLASSPATH
done
export CLASSPATH

# ---------------------
# wait for XAP to start
# ---------------------

$groovy waitforxap.groovy

# ---------------------
# deploy
# ---------------------

$scriptdir/gs.sh deploy -properties $confdir/ea-config.properties $libdir/analytics-dyna-pu.jar
$scriptdir/gs.sh deploy -properties $confdir/ea-config.properties $libdir/analytics-rest.war

$scriptdir/gs-webui.sh

