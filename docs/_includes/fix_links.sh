#!/bin/bash
# Updated fix_links.sh

find docs -name "*.md" -type f | while read -r file; do
    sed -i.bak '
        # Fix double parentheses in links: ](./(path) -> ](path
        s/](\.\/(/.]\(/g

        # Convert .md links to .html
        s/](\([^)]*\)\.md)/](\1.html)/g

        # Fix relative paths for section links
        s/](install\//](install\//g
        s/](intro\//](intro\//g
        s/](jpf-core\//](jpf-core\//g
        s/](user\//](user\//g
        s/](papers\//](papers\//g
    ' "$file"

    echo "Fixed links in: $file"
done
