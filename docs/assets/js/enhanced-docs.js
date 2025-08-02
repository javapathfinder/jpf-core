// Enhanced Documentation JavaScript with smooth animations and interactions
document.addEventListener('DOMContentLoaded', function() {

    // Enhanced dropdown functionality
    initializeDropdowns();

    // Smooth scrolling for navigation links
    initializeSmoothScrolling();

    // Enhanced search functionality
    initializeSearch();

    // Table of contents generation
    generateTableOfContents();

    // Scroll-based animations
    initializeScrollAnimations();

    // Mobile menu toggle
    initializeMobileMenu();

    // Loading states
    initializeLoadingStates();

    // Copy code functionality
    initializeCodeCopy();
});

function initializeDropdowns() {
    const dropdownTriggers = document.querySelectorAll('.nav-menu > li.dropdown > a');

    dropdownTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();

            const parent = this.parentNode;
            const isOpen = parent.classList.contains('open');

            // Close all other dropdowns
            document.querySelectorAll('.nav-menu > li.dropdown.open').forEach(openItem => {
                if (openItem !== parent) {
                    openItem.classList.remove('open');
                    openItem.querySelector('a').setAttribute('aria-expanded', 'false');
                }
            });

            // Toggle current dropdown
            parent.classList.toggle('open');
            this.setAttribute('aria-expanded', !isOpen);

            // Add smooth animation
            const dropdown = parent.querySelector('.dropdown-menu');
            if (parent.classList.contains('open')) {
                dropdown.style.display = 'block';
                setTimeout(() => {
                    dropdown.style.opacity = '1';
                    dropdown.style.transform = 'translateY(0)';
                }, 10);
            } else {
                dropdown.style.opacity = '0';
                dropdown.style.transform = 'translateY(-10px)';
                setTimeout(() => {
                    dropdown.style.display = 'none';
                }, 300);
            }
        });
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.dropdown')) {
            document.querySelectorAll('.nav-menu > li.dropdown.open').forEach(openItem => {
                openItem.classList.remove('open');
                const trigger = openItem.querySelector('a');
                if (trigger) trigger.setAttribute('aria-expanded', 'false');

                const dropdown = openItem.querySelector('.dropdown-menu');
                dropdown.style.opacity = '0';
                dropdown.style.transform = 'translateY(-10px)';
                setTimeout(() => {
                    dropdown.style.display = 'none';
                }, 300);
            });
        }
    });
}

function initializeSmoothScrolling() {
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
}

function initializeSearch() {
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.querySelector('.search-input');

    if (searchForm && searchInput) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const query = searchInput.value.trim();
            if (query) {
                // Add loading state
                const button = this.querySelector('.search-button');
                const originalContent = button.innerHTML;
                button.innerHTML = '<div class="loading"></div>';

                // Simulate search (replace with actual search implementation)
                setTimeout(() => {
                    button.innerHTML = originalContent;
                    // Implement actual search functionality here
                    console.log('Searching for:', query);
                }, 1000);
            }
        });

        // Search suggestions (placeholder)
        searchInput.addEventListener('input', function() {
            const query = this.value.trim();
            if (query.length > 2) {
                // Add search suggestions functionality here
            }
        });
    }
}

function generateTableOfContents() {
    const tocContainer = document.getElementById('table-of-contents');
    if (!tocContainer) return;

    const mainContent = document.querySelector('.main-content .content');
    if (!mainContent) return;

    const headings = mainContent.querySelectorAll('h2, h3, h4');
    if (headings.length === 0) return;

    const toc = document.createElement('ul');
    toc.className = 'toc-list';

    headings.forEach((heading, index) => {
        // Add id to heading if it doesn't have one
        if (!heading.id) {
            heading.id = `heading-${index}`;
        }

        const li = document.createElement('li');
        li.className = `toc-item toc-${heading.tagName.toLowerCase()}`;

        const link = document.createElement('a');
        link.href = `#${heading.id}`;
        link.textContent = heading.textContent;
        link.className = 'toc-link';

        // Smooth scroll to heading
        link.addEventListener('click', function(e) {
            e.preventDefault();
            heading.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });

        li.appendChild(link);
        toc.appendChild(li);
    });

    tocContainer.appendChild(toc);
}

function initializeScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, observerOptions);

    // Observe elements that should animate on scroll
    document.querySelectorAll('h2, h3, .footer-section, .main-content > p').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
}

function initializeMobileMenu() {
    // Add mobile menu toggle button if needed
    const nav = document.querySelector('.main-navigation');
    if (window.innerWidth <= 768 && nav) {
        const toggleButton = document.createElement('button');
        toggleButton.className = 'mobile-menu-toggle';
        toggleButton.innerHTML = '☰';
        toggleButton.setAttribute('aria-label', 'Toggle navigation menu');

        const navContainer = nav.querySelector('.nav-container');
        navContainer.insertBefore(toggleButton, navContainer.firstChild);

        toggleButton.addEventListener('click', function() {
            const navMenu = document.querySelector('.nav-menu');
            navMenu.classList.toggle('mobile-open');
        });
    }
}

function initializeLoadingStates() {
    // Add loading states for external links
    document.querySelectorAll('a[href^="http"]').forEach(link => {
        link.addEventListener('click', function() {
            if (!this.target || this.target === '_self') {
                this.style.opacity = '0.7';
                this.style.pointerEvents = 'none';

                setTimeout(() => {
                    this.style.opacity = '1';
                    this.style.pointerEvents = 'auto';
                }, 2000);
            }
        });
    });
}

function initializeCodeCopy() {
    document.querySelectorAll('pre').forEach(pre => {
        const copyButton = document.createElement('button');
        copyButton.className = 'copy-code-btn';
        copyButton.innerHTML = '📋 Copy';
        copyButton.style.cssText = `
            position: absolute;
            top: 8px;
            right: 8px;
            background: var(--secondary-color);
            color: white;
            border: none;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 12px;
            cursor: pointer;
            opacity: 0;
            transition: opacity 0.3s ease;
        `;

        pre.style.position = 'relative';
        pre.appendChild(copyButton);

        pre.addEventListener('mouseenter', () => {
            copyButton.style.opacity = '1';
        });

        pre.addEventListener('mouseleave', () => {
            copyButton.style.opacity = '0';
        });

        copyButton.addEventListener('click', async function() {
            const code = pre.querySelector('code') || pre;
            try {
                await navigator.clipboard.writeText(code.textContent);
                this.innerHTML = 'Copied!';
                setTimeout(() => {
                    this.innerHTML = 'Copy';
                }, 2000);
            } catch (err) {
                console.error('Failed to copy code:', err);
                this.innerHTML = 'Failed';
                setTimeout(() => {
                    this.innerHTML = 'Copy';
                }, 2000);
            }
        });
    });
}

// Utility function for smooth animations
function animateElement(element, animation, duration = 300) {
    return new Promise(resolve => {
        element.style.transition = `all ${duration}ms ease`;
        Object.assign(element.style, animation);
        setTimeout(resolve, duration);
    });
}
