document.addEventListener('DOMContentLoaded', function() {
    const dropdownTriggers = document.querySelectorAll('.nav-menu > li.dropdown > a');

    dropdownTriggers.forEach(trigger => {
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const parent = this.parentNode;
            parent.classList.toggle('open');
            this.setAttribute('aria-expanded', parent.classList.contains('open') ? 'true' : 'false');
        });
    });

    document.addEventListener('click', function(e) {
        document.querySelectorAll('.nav-menu > li.dropdown.open').forEach(openItem => {
            if (!openItem.contains(e.target)) {
                openItem.classList.remove('open');
                const trigger = openItem.querySelector('a');
                if (trigger) trigger.setAttribute('aria-expanded', 'false');
            }
        });
    });
});
