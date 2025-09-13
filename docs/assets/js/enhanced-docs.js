document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    initMobileNav();
    initHeaderScroll();
    initDropdowns();
    initCodeCopy();
    initSmoothScroll();
    initActiveLinks();

    // Mobile Navigation
    function initMobileNav() {
        const header = document.querySelector('.header-container');
        const nav = document.querySelector('.main-nav');
        const headerActions = document.querySelector('.header-actions');
        
        if (!header || !nav) return;

        let toggle = document.querySelector('.mobile-toggle');
        if (!toggle && window.innerWidth <= 768) {
            toggle = document.createElement('button');
            toggle.className = 'mobile-toggle';
            toggle.setAttribute('aria-label', 'Toggle navigation');
            toggle.innerHTML = '<span></span><span></span><span></span>';
            
            header.insertBefore(toggle, headerActions);
        }

        if (toggle) {
            toggle.addEventListener('click', () => {
                toggle.classList.toggle('active');
                nav.classList.toggle('open');
                document.body.style.overflow = nav.classList.contains('open') ? 'hidden' : '';
            });

            document.addEventListener('click', (e) => {
                if (!e.target.closest('.main-nav') && !e.target.closest('.mobile-toggle')) {
                    closeMobileNav();
                }
            });

            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') {
                    closeMobileNav();
                }
            });
        }

        function closeMobileNav() {
            if (toggle && nav) {
                toggle.classList.remove('active');
                nav.classList.remove('open');
                document.body.style.overflow = '';
            }
        }
    }

    function initHeaderScroll() {
        const header = document.querySelector('.site-header');
        if (!header) return;

        let lastScroll = 0;
        let ticking = false;

        function updateHeader() {
            const currentScroll = window.pageYOffset;
            
            if (currentScroll > 100 && currentScroll > lastScroll) {
                header.classList.add('hidden');
            } else {
                header.classList.remove('hidden');
            }
            
            lastScroll = currentScroll;
            ticking = false;
        }

        window.addEventListener('scroll', () => {
            if (!ticking) {
                window.requestAnimationFrame(updateHeader);
                ticking = true;
            }
        });
    }

    function initDropdowns() {
        const dropdowns = document.querySelectorAll('.dropdown');
        
        dropdowns.forEach(dropdown => {
            const toggle = dropdown.querySelector('a');
            
            // Add arrow if not present
            if (toggle && !toggle.querySelector('.dropdown-arrow')) {
                const arrow = document.createElement('span');
                arrow.className = 'dropdown-arrow';
                arrow.textContent = ' ▾';
                toggle.appendChild(arrow);
            }

            // Mobile click behavior
            if (window.innerWidth <= 768 && toggle) {
                toggle.addEventListener('click', (e) => {
                    e.preventDefault();
                    dropdown.classList.toggle('open');
                });
            }
        });
    }

    function initCodeCopy() {
        document.querySelectorAll('pre').forEach(pre => {
            if (pre.querySelector('.copy-btn')) return;

            const button = document.createElement('button');
            button.className = 'copy-btn';
            button.textContent = 'Copy';
            
            button.addEventListener('click', async () => {
                const code = pre.querySelector('code') || pre;
                const text = code.textContent;
                
                try {
                    await navigator.clipboard.writeText(text);
                    button.textContent = '✓ Copied';
                    button.style.background = 'var(--color-teal-600)';
                    setTimeout(() => {
                        button.textContent = 'Copy';
                        button.style.background = '';
                    }, 2000);
                } catch (err) {
                    // Fallback
                    const textarea = document.createElement('textarea');
                    textarea.value = text;
                    textarea.style.position = 'fixed';
                    textarea.style.opacity = '0';
                    document.body.appendChild(textarea);
                    textarea.select();
                    document.execCommand('copy');
                    document.body.removeChild(textarea);
                    
                    button.textContent = '✓ Copied';
                    setTimeout(() => {
                        button.textContent = 'Copy';
                    }, 2000);
                }
            });
            
            pre.appendChild(button);
        });
    }

    function initSmoothScroll() {
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function(e) {
                const href = this.getAttribute('href');
                if (href === '#') return;
                
                const target = document.querySelector(href);
                if (target) {
                    e.preventDefault();
                    const headerHeight = 80;
                    const targetPosition = target.offsetTop - headerHeight;
                    
                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                    
                    history.pushState(null, null, href);
                }
            });
        });
    }

    function initActiveLinks() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-menu a');
        
        navLinks.forEach(link => {
            const linkPath = link.pathname?.replace(/\/$/, '') || '';
            const pagePath = currentPath.replace(/\/$/, '');
            
            if (linkPath && (linkPath === pagePath || pagePath.startsWith(linkPath + '/'))) {
                link.classList.add('active');
                
                // Mark parent dropdown as active
                const parentDropdown = link.closest('.dropdown');
                if (parentDropdown) {
                    const dropdownToggle = parentDropdown.querySelector('a');
                    if (dropdownToggle) dropdownToggle.classList.add('active');
                }
            }
        });
    }

    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            initMobileNav();
            initDropdowns();
        }, 250);
    });
});