document.addEventListener('DOMContentLoaded', function () {
    if (window.AOS) {
        AOS.init({ duration: 600, once: true });
    }
    loadTrendingProducts();
    initDarkModeToggle();
    initPasswordToggles();
    initNavbarScrollEffect();
    initButtonRipple();
    initTiltEffect();
    initHeroParallax();
    initScrollReveal();
});

// Site-wide fade-up reveal: works on every page (cart, checkout, wishlist, etc.),
// not just where AOS attributes are manually added in markup.
function initScrollReveal() {
    const selector = '.glass-card, .product-card, .category-card, .section-title, ' +
        '.order-tracker, .method-choice, .address-choice, .filter-panel, .auth-card';
    const els = document.querySelectorAll(selector);
    if (!els.length) return;

    els.forEach(function (el, i) {
        if (el.hasAttribute('data-aos')) return; // AOS already handles this one
        el.classList.add('sm-reveal');
        el.style.transitionDelay = Math.min(i % 8, 6) * 0.06 + 's';
    });

    if (!('IntersectionObserver' in window)) {
        document.querySelectorAll('.sm-reveal').forEach(function (el) { el.classList.add('sm-in'); });
        return;
    }

    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('sm-in');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.12 });

    document.querySelectorAll('.sm-reveal').forEach(function (el) { observer.observe(el); });
}

// 3D tilt: cards rotate toward the cursor for a physical, layered feel
function initTiltEffect() {
    if (window.matchMedia('(pointer: coarse)').matches) return; // skip on touch devices
    const selector = '.product-card, .category-card, .glass-card';
    document.addEventListener('mousemove', function (e) {
        const card = e.target.closest(selector);
        if (!card) return;
        const rect = card.getBoundingClientRect();
        const px = (e.clientX - rect.left) / rect.width - 0.5;
        const py = (e.clientY - rect.top) / rect.height - 0.5;
        card.style.setProperty('--ry', (px * 10).toFixed(2) + 'deg');
        card.style.setProperty('--rx', (-py * 10).toFixed(2) + 'deg');
    });
    document.addEventListener('mouseout', function (e) {
        const card = e.target.closest(selector);
        if (!card) return;
        card.style.setProperty('--rx', '0deg');
        card.style.setProperty('--ry', '0deg');
    });
}

// Hero content drifts slightly opposite to the cursor for a subtle parallax feel
function initHeroParallax() {
    if (window.matchMedia('(pointer: coarse)').matches) return;
    const hero = document.querySelector('.hero-slider');
    if (!hero) return;
    hero.addEventListener('mousemove', function (e) {
        const rect = hero.getBoundingClientRect();
        const px = (e.clientX - rect.left) / rect.width - 0.5;
        const py = (e.clientY - rect.top) / rect.height - 0.5;
        hero.querySelectorAll('.hero-content').forEach(function (el) {
            el.style.setProperty('--px', (px * -14).toFixed(1) + 'px');
            el.style.setProperty('--py', (py * -10).toFixed(1) + 'px');
        });
    });
    hero.addEventListener('mouseleave', function () {
        hero.querySelectorAll('.hero-content').forEach(function (el) {
            el.style.setProperty('--px', '0px');
            el.style.setProperty('--py', '0px');
        });
    });
}

// Classic touch: navbar gains a shadow and tightens up once the page scrolls
function initNavbarScrollEffect() {
    const nav = document.querySelector('.navbar');
    if (!nav) return;
    const onScroll = function () {
        nav.classList.toggle('is-scrolled', window.scrollY > 12);
    };
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
}

// Subtle ripple on primary buttons for a more tactile, stylish feel
function initButtonRipple() {
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('.btn-gradient');
        if (!btn) return;
        const ripple = document.createElement('span');
        const rect = btn.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        ripple.style.cssText =
            'position:absolute;border-radius:50%;pointer-events:none;' +
            'background:rgba(255,255,255,0.45);transform:scale(0);' +
            'animation:sm-ripple 0.6s ease-out;' +
            'width:' + size + 'px;height:' + size + 'px;' +
            'left:' + (e.clientX - rect.left - size / 2) + 'px;' +
            'top:' + (e.clientY - rect.top - size / 2) + 'px;';
        btn.appendChild(ripple);
        setTimeout(function () { ripple.remove(); }, 650);
    });

    if (!document.getElementById('sm-ripple-style')) {
        const style = document.createElement('style');
        style.id = 'sm-ripple-style';
        style.textContent = '@keyframes sm-ripple { to { transform: scale(2.2); opacity: 0; } }';
        document.head.appendChild(style);
    }
}

// Product photo uploads live outside the DB (assets/img/products); until a
// given thumbnail file is actually placed there, swap the broken image for
// an inline placeholder instead of showing the browser's broken-image icon.
var PLACEHOLDER_IMG =
    'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="500">' +
        '<rect width="100%" height="100%" fill="#f0f0f2"/>' +
        '<text x="50%" y="50%" font-family="Poppins, sans-serif" font-size="18" ' +
        'fill="#94969f" text-anchor="middle" dy=".3em">Image coming soon</text>' +
        '</svg>'
    );

document.addEventListener('error', function (e) {
    var el = e.target;
    if (el.tagName === 'IMG' && el.src.indexOf('/assets/img/products/') !== -1 && el.src.indexOf(PLACEHOLDER_IMG) === -1) {
        el.src = PLACEHOLDER_IMG;
    }
}, true);

function loadTrendingProducts() {
    const container = document.getElementById('trendingProducts');
    if (!container) return;

    fetch('api/products/trending')
        .then(function (res) {
            if (!res.ok) throw new Error('Failed to load products');
            return res.json();
        })
        .then(function (products) {
            container.innerHTML = '';
            if (!products.length) {
                container.innerHTML = '<p class="text-muted">No trending products yet.</p>';
                return;
            }
            products.forEach(function (p) {
                container.insertAdjacentHTML('beforeend', renderProductCard(p));
            });
        })
        .catch(function () {
            // Leave skeletons if the endpoint isn't reachable yet (e.g. DB not seeded)
            container.innerHTML = '<p class="text-muted">Trending products will appear here once the catalog is seeded.</p>';
        });
}

function renderProductCard(p) {
    const hasDiscount = p.discountPercent && p.discountPercent > 0;
    return (
        '<div class="col-6 col-md-3">' +
        '  <a href="product?id=' + p.id + '" class="text-decoration-none text-dark">' +
        '    <div class="product-card">' +
        '      <img src="assets/img/products/' + (p.thumbnail || 'placeholder.jpg') + '" alt="' + escapeHtml(p.name) + '">' +
        '      <div class="info">' +
        '        <div class="brand">' + escapeHtml(p.brand || '') + '</div>' +
        '        <div class="name">' + escapeHtml(p.name) + '</div>' +
        '        <div class="price">' +
        '          <span class="final">\u20B9' + Number(p.finalPrice).toFixed(0) + '</span>' +
        (hasDiscount
            ? '<span class="original">\u20B9' + Number(p.price).toFixed(0) + '</span>' +
              '<span class="discount">' + p.discountPercent + '% off</span>'
            : '') +
        '        </div>' +
        '      </div>' +
        '    </div>' +
        '  </a>' +
        '</div>'
    );
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function initDarkModeToggle() {
    const toggle = document.getElementById('darkModeToggle');
    if (!toggle) return;
    toggle.addEventListener('click', function () {
        document.body.classList.toggle('dark-mode');
    });
}

function initPasswordToggles() {
    document.querySelectorAll('.toggle-password').forEach(function (icon) {
        icon.addEventListener('click', function () {
            const input = document.getElementById(icon.getAttribute('data-target'));
            if (!input) return;
            const isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            icon.classList.toggle('fa-eye', !isHidden);
            icon.classList.toggle('fa-eye-slash', isHidden);
        });
    });
}
