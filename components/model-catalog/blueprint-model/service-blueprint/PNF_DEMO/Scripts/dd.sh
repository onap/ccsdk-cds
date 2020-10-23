#!/bin/bash
# Author: abdelmuhaimen.seaudi@orange.com
# Usage: name this script as dd.sh, and put the data dictionaries list JSON as dd.json in the same directory, and run dd.sh
# dd.sh will read the dictionary list JSON from dd.json, will output to stdout the number of Defintions found, and will start pushing them one by one to CDS DB

CDS_PORT=$1
JSON_FILE=$2

if [ -z ${CDS_PORT} ] || [ -z ${JSON_FILE} ] || [ ! -f ${JSON_FILE} ]
  then
    echo "Usage : $0 <CDS UI Port> <Data Dict JSON file>"
    exit 1
fi

l=`jq '.|length' ${JSON_FILE}`
echo "Found $l Dictionary Definition Entries"
i=0
while [ $i -lt $l ]
do
  echo "i = $i"
  d=`jq ".[$i]" ${JSON_FILE}`
  echo $d
  #REPLACE <cds-ui> with the IP Address of ONAP
  curl -k -O "https://localhost:${CDS_PORT}/resourcedictionary/save" -v -H 'Content-type: application/json' -d"$d"
  sleep 1
  echo -e "\n*****************************************\n"
  i=$(( $i + 1 ))
done
