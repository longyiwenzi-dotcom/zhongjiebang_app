#!/usr/bin/env bash
set -euo pipefail

INDEX=/var/www/zhaozibo-resume/index.html
BACKUP="${INDEX}.before-zhongjiebang-demo"
test -f "$INDEX"
test -f "$BACKUP" || cp -a "$INDEX" "$BACKUP"

perl -0pi -e 's#<div class="actions"><a class="button primary project-cta" href="https://47\.103\.84\.70/zhongjiebang-demo/" target="_blank" rel="noopener">访问 Java 项目 <span>↗</span></a><a class="button" href="https://github.com/longyiwenzi-dotcom/zhongjiebang_app" target="_blank" rel="noopener">GitHub 源码 <span>↗</span></a></div>#<div class="actions"><a class="button primary project-cta" href="https://47.103.84.70/zhongjiebang-demo/" target="_blank" rel="noopener">访问 Java 项目 <span>↗</span></a><a class="button" href="java-project.html">技术与面试知识点 <span>↗</span></a><a class="button" href="https://github.com/longyiwenzi-dotcom/zhongjiebang_app" target="_blank" rel="noopener">GitHub 源码 <span>↗</span></a></div>#' "$INDEX"

grep -q '47.103.84.70/zhongjiebang-demo/' "$INDEX"
grep -q 'github.com/longyiwenzi-dotcom/zhongjiebang_app' "$INDEX"
grep -q 'java-project.html' "$INDEX"
perl -0pi -e 's/技术与面试知识点/技术架构与服务/g' "$INDEX"
printf 'RESUME_LINKS_OK\n'
