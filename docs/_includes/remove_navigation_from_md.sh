#!/bin/bash

find docs -name "*.md" -type f -print0 | while IFS= read -r -d '' file; do
    sed -i.bak 's/\.html//g' "$file"
    echo "Removed .html extensions from: $file"
done

# Clean up backup files
find docs -name "*.bak" -delete
