---
layout: page
title: Google Summer of Code
permalink: /gsoc/
---


# JPF & Google Summer of Code

Below is a list of our GSoC project ideas and past pages:

{% for post in site.gsoc reversed %}
- [{{ post.title }}]({{ post.url }})
  {% endfor %}
