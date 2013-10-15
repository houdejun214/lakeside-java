#!/bin/sh
# by houdejun
action=$1

#name of the process
name='lakeside-berkeleydb'
curfiles='ls'
classpath='.'
date=$(date +%Y-%m-%d)	# string of date time
hostname=$(hostname -s) 	# hostname
basedir=$(dirname $0) 		# dir of the current shell script
cd $basedir
pidfile=$basedir/$name-$hostname.pid
logofile=logs/output-$date.log
logofile=logs/output-$date-$hostname.log
mkdir -p ${basedir}/logs
#start getting the calsspath
for jarfile in $(ls lib/*.jar);
do
classpath="${classpath}:$jarfile"	
done
export CLASSPATH=$classpath
shift
cmd="java -Xms256m -Xmx5120m com.hajo.Main $@"

case "$action" in

start)

# start the program..
if (test -f $pidfile);then
    cat $pidfile | while read line
    do
      echo $line  
      if !(test -z $line); then
	  echo "stoping existed program"
	  echo "kill "$line
	  kill $line
      fi
    done
fi

 echo "starting program..."
 echo "$cmd"
 nohup $cmd >$logofile 2>&1 &
 pid=$!
 #disown $pid
 #echo "pid is $pid"
 echo $pid>$pidfile
 tail -f $logofile
 ;;

stop)

# stop the program only
if (test -f $pidfile);then
   cat $pidfile | while read line
      do
      echo $line  
      if !(test -z $line); then
          echo "stoping existed program"
          echo "kill "$line
          kill $line
      fi
    done
else
   echo "there isn't running program to stop"
fi
 ;;

*)
  echo 'arguments can only be start or stop'
  ;;

esac

exit 0
