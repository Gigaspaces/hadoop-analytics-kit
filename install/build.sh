rm -rf work
mkdir work

rm -f install-old.zip
mv -f install.zip install-old.zip

cp -r bin config work
mkdir -p work/lib/optional/openspaces

cp ../analytics-dyna-pu/target/*.jar work/lib/optional/openspaces/analytics-dyna-pu.jar
cp ../analytics-rest/target/*.war work/lib/optional/openspaces/analytics-rest.war

cd work
zip -r ../install.zip *
