#!/bin/sh

IP=${1}
PORT=${2}

if [[ ! $IP || ! $PORT ]] ; then
    echo "Usage: sh ${0} ip port"
    echo "eg: sh ${0} 10.0.0.1 8501"
    exit 1
fi

#dbUser
DBUSER="user"
#dbPass
PASSWD="pass"
#dbName
DBNAME="name"

#connect to database then execute init
cat fiscocc-onbc.list | mysql --user=$DBUSER --password=$PASSWD --host=$IP --database=$DBNAME --port=$PORT --default-character-set=utf8;

exit
