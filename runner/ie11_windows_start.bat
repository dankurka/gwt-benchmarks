java -jar selenium-server-standalone-2.52.0.jar -role node ^
  -hub http://100.107.70.41:4444/grid/register ^
  -forcedBrowserMode chrome -maxSession 1 ^
  -browser browserName="internet explorer,version=11,maxInstances=1,platform=WINDOWS" ^
  -Dwebdriver.ie.driver=IEDriverServer_x64_2.46.0.exe
