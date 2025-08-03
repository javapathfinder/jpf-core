#!/bin/bash
# fix_image_paths.sh

find docs -name "*.md" -type f | while read -r file; do
    # Replace GitHub repository URLs with relative paths
    sed -i.bak 's|https://github.com/javapathfinder/jpf-core/blob/master/docs/graphics/|{{ site.baseurl }}/graphics/|g' "$file"

    # Also handle any other variations
    sed -i.bak 's|https://github.com/javapathfinder/jpf-core/raw/master/docs/graphics/|{{ site.baseurl }}/graphics/|g' "$file"

    # Remove the {align=center width=800} syntax that doesn't work in Jekyll
    sed -i.bak 's|{align=center width=[0-9]*}||g' "$file"

    echo "Fixed image paths in: $file"
done

# Clean up backup files
find docs -name "*.bak" -delete
