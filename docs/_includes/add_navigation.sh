#!/bin/bash
# add_navigation.sh

find docs -name "*.md" -type f | while read -r file; do
    # Check if file already has the include
    if ! grep -q "{% include navigation.html %}" "$file"; then
        # Create temporary file with navigation added after front matter
        awk '
        /^---$/ && !seen_front_matter {
            if (NR == 1) {
                print;
                front_matter = 1;
                next
            } else if (front_matter) {
                print;
                print "";
                print "{% include navigation.html %}";
                print "";
                seen_front_matter = 1;
                next
            }
        }
        { print }
        END {
            if (!seen_front_matter && !front_matter) {
                print "{% include navigation.html %}"
            }
        }
        ' "$file" > "$file.tmp" && mv "$file.tmp" "$file"

        echo "Added navigation to: $file"
    fi
done