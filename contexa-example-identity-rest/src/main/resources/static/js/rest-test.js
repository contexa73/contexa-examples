(function() {
    'use strict';

    var els = {
        username: document.getElementById('username'),
        password: document.getElementById('password'),
        requestPreview: document.getElementById('request-preview'),
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline'),
        loginDot: document.getElementById('login-dot'),
        loginStatus: document.getElementById('login-status')
    };

    var state = { authenticated: false };

    function getTimeStr() {
        return new Date().toLocaleTimeString('en-US', { hour12: false });
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    function addTimeline(type, msg) {
        var entry = document.createElement('div');
        entry.className = 'timeline-entry ' + type;
        entry.innerHTML =
            '<span class="timeline-time">' + getTimeStr() + '</span>' +
            '<span class="timeline-msg">' + escapeHtml(msg) + '</span>';
        els.timeline.appendChild(entry);
        els.timeline.scrollTop = els.timeline.scrollHeight;
    }

    function getStatusClass(status) {
        if (status >= 200 && status < 300) return 'status-200';
        if (status === 401) return 'status-401';
        if (status === 403) return 'status-403';
        return 'status-500';
    }

    function renderResponse(status, data) {
        els.responseStatus.innerHTML =
            '<span class="response-status ' + getStatusClass(status) + '">HTTP ' + status + '</span>';
        els.responseBody.textContent = JSON.stringify(data, null, 2);
    }

    function updateLoginState(loggedIn, username) {
        state.authenticated = loggedIn;
        els.loginDot.className = 'login-dot ' + (loggedIn ? 'connected' : 'disconnected');
        els.loginStatus.textContent = loggedIn ? ('Authenticated: ' + username) : 'Not authenticated';
    }

    // Update request preview on input change
    function updatePreview() {
        var u = els.username.value || 'user';
        var p = els.password.value || 'password';
        els.requestPreview.textContent =
            'POST /api/auth/login\n' +
            'Content-Type: application/x-www-form-urlencoded\n\n' +
            'username=' + u + '&password=' + p;
    }

    els.username.addEventListener('input', updatePreview);
    els.password.addEventListener('input', updatePreview);

    async function restLogin() {
        var username = els.username.value.trim();
        var password = els.password.value.trim();

        if (!username || !password) {
            addTimeline('error', 'Username and password required');
            return;
        }

        addTimeline('info', 'REST Login: POST /api/auth/login (username=' + username + ')');

        try {
            var body = 'username=' + encodeURIComponent(username) + '&password=' + encodeURIComponent(password);
            var response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                credentials: 'same-origin',
                body: body
            });

            var data;
            try {
                data = await response.json();
            } catch (e) {
                data = { status: response.status, redirected: response.redirected, url: response.url };
            }

            renderResponse(response.status, data);

            if (response.ok || response.redirected) {
                updateLoginState(true, username);
                addTimeline('success', 'Login successful - session established');
            } else {
                updateLoginState(false, null);
                addTimeline('error', 'Login failed - HTTP ' + response.status);
            }
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
        }
    }

    async function getProfile() {
        addTimeline('info', 'GET /api/profile');
        try {
            var response = await fetch('/api/profile', {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });

            var data;
            try { data = await response.json(); } catch (e) { data = { raw: await response.text() }; }

            renderResponse(response.status, data);

            if (response.ok) {
                updateLoginState(true, data.username || 'unknown');
                addTimeline('success', 'Profile loaded: ' + (data.username || 'unknown'));
            } else {
                addTimeline('error', 'Profile failed - HTTP ' + response.status);
            }
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
        }
    }

    async function healthCheck() {
        addTimeline('info', 'GET /api/health');
        try {
            var response = await fetch('/api/health', {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var data = await response.json();
            renderResponse(response.status, data);
            addTimeline('success', 'Health: ' + data.status);
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Health check failed: ' + err.message);
        }
    }

    async function logout() {
        addTimeline('info', 'Logging out...');
        try {
            await fetch('/logout', { method: 'POST', credentials: 'same-origin' });
            updateLoginState(false, null);
            renderResponse(200, { message: 'Logged out successfully' });
            addTimeline('success', 'Logged out');
        } catch (err) {
            addTimeline('error', 'Logout error: ' + err.message);
        }
    }

    window.App = {
        restLogin: restLogin,
        getProfile: getProfile,
        healthCheck: healthCheck,
        logout: logout
    };
})();
