#!/bin/bash
# remove_navigation_from_md.sh

find docs -name "*.md" -type f | while read -r file; do
    # Remove the navigation include line
    sed -i.bak '/{% include navigation.html %}/d' "$file"
    echo "Removed navigation include from: $file"
done

# Clean up backup files
find docs -name "*.bak" -delete
