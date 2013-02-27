#!/bin/bash
# Author: purplexsu

function get_credential() {
  sed '/^\#/d' ~/.purple-haze | grep "$1" | tail -n 1 \
      | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
}

pushd "`pwd`"
relative_path=$1
if [ ! -z $relative_path ]
then
  tmp=`echo $relative_path | grep '/$'`
  if [ $? -ne 0 ]
  then
    relative_path=${relative_path}/
  fi
fi
ftp_user=`get_credential "ftp.username"`
ftp_pwd=`get_credential "ftp.password"`
cd "/Users/frankxue/My Webs/PurpleHaze/$relative_path"
ftp ftp://${ftp_user}:${ftp_pwd}@ftp.purplexsu.net/www/$relative_path
popd
