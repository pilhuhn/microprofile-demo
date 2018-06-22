#!/bin/sh

#set -x

while true 
do
   END=$((RANDOM % 10 +1 ))
   echo $END
   for i in `seq $END`
   do
      curl http://localhost:8080/hello/par &
   done
   sleep 62
done
