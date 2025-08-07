---
title: "JPF Workshops"
layout: page
permalink: /workshops/
---

# JPF Workshops

Browse our annual workshop information:

{% for post in site.workshops reversed %}
- [{{ post.title }}]({{ post.url }})
  {% endfor %}
