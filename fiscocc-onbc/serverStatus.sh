#!/bin/sh

# 从server.env文件中获取JAVA_HOME的配置
TEMP_JAVA_HOME=`cat server.env | grep JAVA_HOME | cut -d':' -f4`
# JDK路径
JAVA_HOME="${TEMP_JAVA_HOME}"

# Java主程序，也就是main(String[] args)方法类
APP_MAIN=org.bcos.fiscocc.onbc.Application

tradePortalPID=0

getTradeProtalPID(){
    javaps=`$JAVA_HOME/bin/jps -l | grep $APP_MAIN`
    if [ -n "$javaps" ]; then
        tradePortalPID=`echo $javaps | awk '{print $1}'`
    else
        tradePortalPID=0
    fi
}

getServerStatus(){
    getTradeProtalPID
    echo "==============================================================================================="
    if [ $tradePortalPID -ne 0 ]; then
        echo "$APP_MAIN is running(PID=$tradePortalPID)"
        echo "==============================================================================================="
    else
        echo "$APP_MAIN is not running"
        echo "==============================================================================================="
    fi
}

# 调用查看命令
getServerStatus