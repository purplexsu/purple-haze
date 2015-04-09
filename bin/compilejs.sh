#!/bin/bash
# Author: purplexsu

project_dir="${HOME}/Projects/Pro052"
web_dir="${HOME}/My Webs"

function compile_js() {
  python ${project_dir}/closure-library/closure/bin/build/closurebuilder.py \
    --root=${project_dir}/closure-library/ \
    --root=${project_dir}/ArticleGenerator/javascript/ \
    --namespace="$1" \
    --compiler_jar="${project_dir}/compiler.jar" \
    --compiler_flags="--compilation_level=ADVANCED_OPTIMIZATIONS" \
    --output_mode="$3" \
    --output_file="${web_dir}/PurpleHaze/$2"
}

output_mode="compiled"
if [ $# -gt 0 -a "$1" == "debug" ]
then
  output_mode="script"
fi

compile_js "net.purplexsu.ArticlePage" "article.js" "${output_mode}"
compile_js "net.purplexsu.ColumnPage" "column.js" "${output_mode}"
compile_js "net.purplexsu.CommentPage" "comment.js" "${output_mode}"
compile_js "net.purplexsu.DivisionPage" "division.js" "${output_mode}"
compile_js "net.purplexsu.IndexPage" "index.js" "${output_mode}"
compile_js "net.purplexsu.PhotoPage" "photo.js" "${output_mode}"

if [ "${output_mode}" != "script" ]
then
  read -p "Upload?[y|n] " -n 1
  if [ $REPLY == "y" ]
  then
    python ${project_dir}/ArticleGenerator/bin/upload.py --type j
  fi
fi

