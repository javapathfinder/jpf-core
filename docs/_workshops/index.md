---
layout: page
title: JPF Workshops
permalink: /workshops/
---

{% include Sidebar.md %}

# JPF Workshops

Browse our annual workshop information:

{% for post in site.workshops reversed %}
- [{{ post.title }}]({{ post.url }})
  {% endfor %}
