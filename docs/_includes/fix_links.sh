#!/bin/bash
# fix_links.sh

find docs -name "*.md" -type f | while read -r file; do
    sed -i.bak '
        # Convert .md links to .html
        s/](\([^)]*\)\.md)/](\1.html)/g

        # Optional: Add ./ prefix to links that do not start with / or ./
        s/]\([^/.]\)/](.\/\1/g
    ' "$file"

    echo "Fixed links in: $file"
done
