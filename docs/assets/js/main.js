
document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    initMobileMenu();
    initHeaderScroll();
    initDropdowns();
    initCodeCopy();
    initSmoothScroll();
    initActiveLinks();
    initAnimations();

    function initMobileMenu() {
        const header = document.querySelector('.header-container');
        const body = document.body;
        
        if (!header) return;

        let toggle = document.querySelector('.mobile-toggle');
        if (!toggle && window.innerWidth <= 768) {
            toggle = document.createElement('button');
            toggle.className = 'mobile-toggle';
            toggle.setAttribute('aria-label', 'Toggle navigation');
            toggle.setAttribute('aria-expanded', 'false');
            toggle.innerHTML = '<span></span><span></span><span></span>';
            
            const headerActions = document.querySelector('.header-actions');
            if (headerActions) {
                header.appendChild(toggle);
            }
        }

        let mobileMenu = document.querySelector('.mobile-menu');
        if (!mobileMenu && window.innerWidth <= 768) {
            mobileMenu = createMobileMenu();
            body.appendChild(mobileMenu);
            
            const overlay = document.createElement('div');
            overlay.className = 'mobile-menu-overlay';
            body.appendChild(overlay);
        }

        if (toggle && mobileMenu) {
            toggle.addEventListener('click', function(e) {
                e.stopPropagation();
                const isOpen = mobileMenu.classList.contains('open');
                
                if (isOpen) {
                    closeMobileMenu();
                } else {
                    openMobileMenu();
                }
            });
        }

        function openMobileMenu() {
            if (mobileMenu && toggle) {
                mobileMenu.classList.add('open');
                toggle.classList.add('active');
                toggle.setAttribute('aria-expanded', 'true');
                document.querySelector('.mobile-menu-overlay')?.classList.add('open');
                body.style.overflow = 'hidden';
            }
        }

        function closeMobileMenu() {
            if (mobileMenu && toggle) {
                mobileMenu.classList.remove('open');
                toggle.classList.remove('active');
                toggle.setAttribute('aria-expanded', 'false');
                document.querySelector('.mobile-menu-overlay')?.classList.remove('open');
                body.style.overflow = '';
            }
        }

        document.querySelector('.mobile-menu-overlay')?.addEventListener('click', closeMobileMenu);

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') {
                closeMobileMenu();
            }
        });

        if (mobileMenu) {
            mobileMenu.querySelectorAll('a').forEach(link => {
                link.addEventListener('click', () => {
                    // Don't close for dropdown toggles
                    if (!link.classList.contains('dropdown-toggle')) {
                        closeMobileMenu();
                    }
                });
            });
        }
    }

    function createMobileMenu() {
        const menu = document.createElement('nav');
        menu.className = 'mobile-menu';
        menu.setAttribute('aria-label', 'Mobile navigation');
        
        const desktopNav = document.querySelector('.nav-menu');
        if (!desktopNav) return menu;
        
        const mobileNav = document.createElement('ul');
        mobileNav.className = 'mobile-menu-nav';
        
        desktopNav.querySelectorAll('> li').forEach(item => {
            const clone = item.cloneNode(true);
            
            if (clone.classList.contains('dropdown')) {
                clone.classList.add('mobile-dropdown');
                const toggle = clone.querySelector('a');
                if (toggle) {
                    toggle.addEventListener('click', (e) => {
                        e.preventDefault();
                        clone.classList.toggle('open');
                    });
                }
            }
            
            // Special styling for GSoC tab
            const link = clone.querySelector('a');
            if (link && link.classList.contains('ideas-tab')) {
                link.classList.add('mobile-ideas-tab');
            }
            
            mobileNav.appendChild(clone);
        });
        
        // Add GitHub link at the bottom
        const githubLink = document.createElement('a');
        githubLink.href = 'https://github.com/javapathfinder/jpf-core';
        githubLink.className = 'mobile-github-link';
        githubLink.innerHTML = '<i class="fab fa-github"></i> View on GitHub';
        
        menu.appendChild(mobileNav);
        menu.appendChild(githubLink);
        
        return menu;
    }

    function initHeaderScroll() {
        const header = document.querySelector('.site-header');
        if (!header) return;

        let lastScroll = 0;
        let ticking = false;

        function updateHeader() {
            const currentScroll = window.pageYOffset;
            
            if (currentScroll > 10) {
                header.classList.add('scrolled');
            } else {
                header.classList.remove('scrolled');
            }
            
            if (currentScroll > 100 && currentScroll > lastScroll && window.innerWidth > 768) {
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
            
            if (toggle && !toggle.querySelector('.arrow')) {
                const arrow = document.createElement('span');
                arrow.className = 'arrow';
                arrow.textContent = ' ▾';
                arrow.style.fontSize = '12px';
                arrow.style.opacity = '0.7';
                toggle.appendChild(arrow);
            }
        });
    }

    // Enhanced Copy Code Functionality
    function initCodeCopy() {
        document.querySelectorAll('pre').forEach(pre => {
            if (pre.querySelector('.copy-btn')) return;

            const button = document.createElement('button');
            button.className = 'copy-btn';
            button.innerHTML = '<i class="far fa-copy"></i> Copy';
            
            button.addEventListener('click', async () => {
                const code = pre.querySelector('code') || pre;
                const text = code.textContent;
                
                try {
                    await navigator.clipboard.writeText(text);
                    button.innerHTML = '<i class="fas fa-check"></i> Copied!';
                    button.style.background = 'var(--gradient-primary)';
                    button.style.color = 'white';
                    
                    setTimeout(() => {
                        button.innerHTML = '<i class="far fa-copy"></i> Copy';
                        button.style.background = '';
                        button.style.color = '';
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
                    
                    button.innerHTML = '<i class="fas fa-check"></i> Copied!';
                    setTimeout(() => {
                        button.innerHTML = '<i class="far fa-copy"></i> Copy';
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
                    
                    // Update URL
                    history.pushState(null, null, href);
                }
            });
        });
    }

    function initActiveLinks() {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.nav-menu a, .mobile-menu-nav a');
        
        navLinks.forEach(link => {
            if (!link.getAttribute('href')) return;
            
            const linkPath = link.pathname?.replace(/\/$/, '') || '';
            const pagePath = currentPath.replace(/\/$/, '');
            
            if (linkPath && (linkPath === pagePath || pagePath.startsWith(linkPath + '/'))) {
                link.classList.add('active');
                
                // Mark parent dropdown as active
                const parentDropdown = link.closest('.dropdown, .mobile-dropdown');
                if (parentDropdown) {
                    const dropdownToggle = parentDropdown.querySelector('> a');
                    if (dropdownToggle) dropdownToggle.classList.add('active');
                }
            }
        });
    }

    function initAnimations() {
        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        document.querySelectorAll('.content > h2, .content > h3, .content > p, .content > ul, .content > ol').forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            observer.observe(el);
        });
    }

    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            if (window.innerWidth <= 768) {
                initMobileMenu();
            } else {
                document.querySelector('.mobile-menu')?.remove();
                document.querySelector('.mobile-menu-overlay')?.remove();
                document.querySelector('.mobile-toggle')?.remove();
                document.body.style.overflow = '';
            }
        }, 250);
    });

    document.querySelectorAll('a').forEach(link => {
        if (link.getAttribute('href')?.startsWith('http') && !link.getAttribute('href').includes(window.location.hostname)) {
            link.setAttribute('target', '_blank');
            link.setAttribute('rel', 'noopener noreferrer');
        }
    });
});