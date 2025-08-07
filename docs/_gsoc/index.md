---
layout: page
title: Google Summer of Code
permalink: /gsoc/
---

# JPF & Google Summer of Code

Below is a list of our GSoC project ideas and past pages:

{% for post in site.gsoc reversed %}
{% unless post.url == page.permalink %}
- [{{ post.title }}]({{ post.url | relative_url }})
  {% endunless %}
  {% endfor %}
