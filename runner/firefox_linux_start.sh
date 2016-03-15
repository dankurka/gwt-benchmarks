java -jar selenium-server-standalone-2.46.0.jar -role node \
  -hub http://100.107.70.41:4444/grid/register \
  -forcedBrowserMode chrome -maxSession 1 \
  -browser browserName=firefox,maxInstances=1,platform=LINUX
