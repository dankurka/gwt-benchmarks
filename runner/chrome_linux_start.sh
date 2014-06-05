java -jar selenium-server-standalone-2.40.0.jar -role node \
  -hub http://172.28.82.238:4444/grid/register \
  -forcedBrowserMode chrome -maxSession 1 \
  -Dwebdriver.chrome.driver=chromedriver_linux_x64_2.10
