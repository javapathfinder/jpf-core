---
layout: default  
title: Google Summer of Code
permalink: /gsoc/
---

# JPF & Google Summer of Code

Below is a list of our GSoC project ideas and past pages:

{% for post in site.gsoc %}
{% unless post.url == page.url %}
- [{{ post.title }}]({{ post.url | relative_url }})
  {% endunless %}
  {% endfor %}
