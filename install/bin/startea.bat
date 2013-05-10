set scriptdir=%~dp0
set libdir=%scriptdir%\..\lib\optional\openspaces
set confdir=%scriptdir%\..\config\

start/b "xap" %scriptdir%\gs-agent.bat gsa.gsc 1 gsa.gsm 1
set groovy=%scriptdir%\..\tools\groovy\bin\groovy

set CLASSPATH=
for %%J in (%scriptdir%\..\lib\required\*.jar) do (
  call %scriptdir%\cpappend.bat %%J%
)

rem ---------------------
rem wait for XAP to start
rem ---------------------

@call %groovy% waitforxap.groovy

rem ---------------------
rem deploy
rem ---------------------

@call %scriptdir%/gs.bat deploy -properties %confdir%\ea-config.properties %libdir%\analytics-dyna-pu.jar
@call %scriptdir%/gs.bat deploy -properties %confdir%\ea-config.properties %libdir%\analytics-rest.war

@start/b "webui" %scriptdir%/gs-webui.bat

ping -n 5 127.0.0.1 >nul

start /b "" "http://localhost:8099"
