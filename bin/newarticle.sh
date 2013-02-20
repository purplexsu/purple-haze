#!/bin/bash
# Author: purplexsu

script_dir=$(dirname $0)
. ${script_dir}/calljava.sh

call_java com.purplehaze.CommonArticleGenerator
