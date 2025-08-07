---
layout: page
title: Google Summer of Code
permalink: /gsoc/
---

# JPF & Google Summer of Code

Below is a list of our GSoC project ideas and past pages:

{% assign gsoc_docs = site.collections.gsoc.docs | sort: "date" | reverse %}
{% for post in gsoc_docs %}
{% unless post.relative_path == page.relative_path %}
- [{{ post.data.title }}]({{ post.url | relative_url }})
  {% endunless %}
  {% endfor %}
