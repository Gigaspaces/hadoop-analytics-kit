import org.openspaces.admin.Admin
import org.openspaces.admin.AdminFactory

def admin=new AdminFactory().addGroup("gigaspaces-9.5.1-XAPPremium-ga").createAdmin()

def found=admin.getGridServiceManagers().waitFor(1)

if(!found){
 println "No GSM Found: FATAL"
 System.exit(-1)
}

println "found GSM"

found=admin.getGridServiceContainers().waitFor(1)

if(!found){
 println "No GSC Found: FATAL"
 System.exit(-1)
}

println "found GSC"

//Done
admin.close()
