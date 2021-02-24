#!/bin/bash
# Author: purplexsu

function call_java() {
  project_dir="${HOME}/Project"
  web_dir="${HOME}/Drive/My Webs"
  lib=(ArticleGenerator.jar\
       Lib/jdom.jar\
       Lib/xercesImpl.jar\
       Lib/xml-apis.jar\
       Lib/commons-net-1.4.1.jar\
       Lib/commons-httpclient-3.1.jar\
       Lib/commons-codec-1.3.jar\
       Lib/commons-logging-1.1.1.jar)
  class_path=""
  for jar in "${lib[@]}"
  do
    class_path="${class_path}:${project_dir}/$jar"
  done
  java -cp "${class_path:1}" $1 "${web_dir}/PurpleHaze" "${web_dir}/PurpleTemplate"
}
