#!/bin/bash

find docs -name "*.md" -type f | while read -r file; do
    if ! grep -q "{% include navigation.html %}" "$file"; then
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