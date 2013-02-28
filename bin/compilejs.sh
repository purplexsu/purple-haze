#!/bin/bash
# Author: purplexsu

function compile_js() {
  project_dir="${HOME}/Projects/Pro052"
  web_dir="${HOME}/My Webs"

  python ${project_dir}/closure-library/closure/bin/build/closurebuilder.py \
    --root=${project_dir}/closure-library/ \
    --root=${project_dir}/ArticleGenerator/javascript/ \
    --namespace="$1" \
    --compiler_jar="${project_dir}/compiler.jar" \
    --compiler_flags="--compilation_level=ADVANCED_OPTIMIZATIONS" \
    --output_mode=compiled \
    --output_file="${web_dir}/PurpleHaze/$2"
}

compile_js "net.purplexsu.ArticlePage" "article.js"
compile_js "net.purplexsu.PhotoPage" "photo.js"
compile_js "net.purplexsu.CommentPage" "comment.js"
