#!/bin/sh

# JDK路径
JAVA_HOME="/nemo/jdk1.8.0_141"

# Java主程序，也就是main(String[] args)方法类
APP_MAIN=org.bcos.evidence.utils.DeployRoute

# 启动Java应用程序
echo  "Starting $APP_MAIN ..."
$JAVA_HOME/bin/java -cp 'conf-route/:../../../lib/*' $APP_MAIN $@