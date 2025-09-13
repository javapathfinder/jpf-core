/* JPF Core Documentation - Clean JavaScript */

document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    // Mobile Navigation
    const setupMobileNav = () => {
        const header = document.querySelector('.header-container');
        const nav = document.querySelector('.main-nav');
        
        if (!header || !nav) return;

        // Create mobile toggle if it doesn't exist
        let toggle = document.querySelector('.mobile-toggle');
        if (!toggle && window.innerWidth <= 768) {
            toggle = document.createElement('button');
            toggle.className = 'mobile-toggle';
            toggle.setAttribute('aria-label', 'Toggle navigation');
            toggle.innerHTML = '<span></span><span></span><span></span>';
            
            // Insert before nav
            header.insertBefore(toggle, nav);
        }

        if (toggle) {
            toggle.addEventListener('click', () => {
                toggle.classList.toggle('active');
                nav.classList.toggle('open');
                document.body.style.overflow = nav.classList.contains('open') ? 'hidden' : '';
            });

            // Close on outside click
            document.addEventListener('click', (e) => {
                if (!e.target.closest('.main-nav') && !e.target.closest('.mobile-toggle')) {
                    toggle.classList.remove('active');
                    nav.classList.remove('open');
                    document.body.style.overflow = '';
                }
            });

            // Close on escape
            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape' && nav.classList.contains('open')) {
                    toggle.classList.remove('active');
                    nav.classList.remove('open');
                    document.body.style.overflow = '';
                }
            });
        }
    };

    // Header Hide/Show on Scroll
    const setupHeaderScroll = () => {
        const header = document.querySelector('.site-header');
        if (!header) return;

        let lastScroll = 0;
        let ticking = false;

        const updateHeader = () => {
            const currentScroll = window.pageYOffset;
            
            // Only hide header when scrolling down past 100px
            if (currentScroll > 100 && currentScroll > lastScroll) {
                header.classList.add('hidden');
            } else {
                header.classList.remove('hidden');
            }
            
            lastScroll = currentScroll;
            ticking = false;
        };

        window.addEventListener('scroll', () => {
            if (!ticking) {
                window.requestAnimationFrame(updateHeader);
                ticking = true;
            }
        });
    };

    // Dropdown Menus
    const setupDropdowns = () => {
        const dropdowns = document.querySelectorAll('.dropdown');
        
        dropdowns.forEach(dropdown => {
            const toggle = dropdown.querySelector('a');
            
            // Add arrow indicator
            if (toggle && !toggle.querySelector('.arrow')) {
                toggle.innerHTML += ' <span class="arrow">▾</span>';
            }

            // Mobile: click to toggle
            if (window.innerWidth <= 768) {
                toggle?.addEventListener('click', (e) => {
                    e.preventDefault();
                    dropdown.classList.toggle('open');
                });
            }
        });
    };

    // Copy Code Functionality
    const setupCodeCopy = () => {
        document.querySelectorAll('pre').forEach(pre => {
            // Skip if button already exists
            if (pre.querySelector('.copy-btn')) return;

            const button = document.createElement('button');
            button.className = 'copy-btn';
            button.textContent = 'Copy';
            
            button.addEventListener('click', async () => {
                const code = pre.querySelector('code') || pre;
                const text = code.textContent;
                
                try {
                    await navigator.clipboard.writeText(text);
                    button.textContent = 'Copied!';
                    setTimeout(() => {
                        button.textContent = 'Copy';
                    }, 2000);
                } catch (err) {
                    // Fallback for older browsers
                    const textarea = document.createElement('textarea');
                    textarea.value = text;
                    textarea.style.position = 'fixed';
                    textarea.style.opacity = '0';
                    document.body.appendChild(textarea);
                    textarea.select();
                    document.execCommand('copy');
                    document.body.removeChild(textarea);
                    
                    button.textContent = 'Copied!';
                    setTimeout(() => {
                        button.textContent = 'Copy';
                    }, 2000);
                }
            });
            
            pre.appendChild(button);
        });
    };

    // Smooth Scrolling for Anchors
    const setupSmoothScroll = () => {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                if (href === '#') return;
                
                const target = document.querySelector(href);
                if (target) {
                    e.preventDefault();
                    const headerHeight = document.querySelector('.site-header')?.offsetHeight || 0;
                    const targetPosition = target.offsetTop - headerHeight - 20;
                    
                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                    
                    // Update URL
                    history.pushState(null, null, href);
                }
            });
        });
    };

    // Active Navigation Links
    const setupActiveLinks = () => {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-menu a');
        
        navLinks.forEach(link => {
            // Remove trailing slashes for comparison
            const linkPath = link.pathname.replace(/\/$/, '');
            const pagePath = currentPath.replace(/\/$/, '');
            
            if (linkPath === pagePath) {
                link.classList.add('active');
            }
        });
    };

    // Table of Contents Generation (if needed)
    const generateTOC = () => {
        const tocContainer = document.querySelector('.toc-container');
        const content = document.querySelector('.content');
        
        if (!tocContainer || !content) return;
        
        const headings = content.querySelectorAll('h2, h3');
        if (headings.length < 3) return; // Don't generate TOC for short pages
        
        const toc = document.createElement('nav');
        toc.className = 'table-of-contents';
        toc.innerHTML = '<h4>Contents</h4>';
        
        const list = document.createElement('ul');
        
        headings.forEach((heading, index) => {
            // Add ID if missing
            if (!heading.id) {
                heading.id = 'section-' + index;
            }
            
            const item = document.createElement('li');
            const link = document.createElement('a');
            link.href = '#' + heading.id;
            link.textContent = heading.textContent;
            
            if (heading.tagName === 'H3') {
                item.style.marginLeft = '20px';
            }
            
            item.appendChild(link);
            list.appendChild(item);
        });
        
        toc.appendChild(list);
        tocContainer.appendChild(toc);
    };

    // Handle window resize
    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            setupMobileNav();
            setupDropdowns();
        }, 250);
    });

    // Initialize everything
    setupMobileNav();
    setupHeaderScroll();
    setupDropdowns();
    setupCodeCopy();
    setupSmoothScroll();
    setupActiveLinks();
    generateTOC();
});