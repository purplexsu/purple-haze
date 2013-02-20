#!/bin/bash
# Author: purplexsu

function call_java() {
  project_dir="${HOME}/Projects/Pro052"
  web_dir="${HOME}/My Webs"
  lib=(ArticleGenerator.jar\
       jdom.jar\
       xercesImpl.jar\
       xml-apis.jar\
       commons-net-1.4.1.jar\
       commons-httpclient-3.1.jar\
       commons-codec-1.3.jar\
       commons-logging-1.1.1.jar)
  class_path=""
  for jar in "${lib[@]}"
  do
    class_path="${class_path}:${project_dir}/$jar"
  done
  java -cp "${class_path:1}" $1 "${web_dir}/PurpleHaze" "${web_dir}/PurpleTemplate"
}
