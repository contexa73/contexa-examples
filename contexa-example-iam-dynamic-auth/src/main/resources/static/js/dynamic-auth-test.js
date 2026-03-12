(function() {
    'use strict';

    var els = {
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline'),
        currentUser: document.getElementById('current-user'),
        currentRoles: document.getElementById('current-roles')
    };

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
        if (status === 423) return 'status-423';
        return 'status-500';
    }

    function renderResponse(status, data) {
        els.responseStatus.innerHTML =
            '<span class="response-status ' + getStatusClass(status) + '">HTTP ' + status + '</span>';
        els.responseBody.textContent = JSON.stringify(data, null, 2);
    }

    function getResultClass(status) {
        if (status >= 200 && status < 300) return 'result-allow';
        if (status === 401) return 'result-challenge';
        if (status === 403) return 'result-block';
        return 'result-block';
    }

    function getResultLabel(status) {
        if (status >= 200 && status < 300) return 'ALLOW';
        if (status === 401) return 'CHALLENGE';
        if (status === 403) return 'BLOCK';
        if (status === 423) return 'ESCALATE';
        return 'ERROR';
    }

    function updateMatrix(level, status) {
        var statusCell = document.getElementById('matrix-' + level);
        var resultCell = document.getElementById('matrix-' + level + '-result');
        if (statusCell && resultCell) {
            statusCell.textContent = status;
            statusCell.className = getResultClass(status);
            resultCell.textContent = getResultLabel(status);
            resultCell.className = getResultClass(status);
        }
    }

    async function callApi(url, label) {
        addTimeline('info', label);
        addTimeline('info', 'GET ' + url);

        try {
            var response = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });

            var data;
            try {
                data = await response.json();
            } catch (e) {
                data = { raw: 'Non-JSON response', status: response.status };
            }

            renderResponse(response.status, data);

            if (response.ok) {
                addTimeline('success', 'ALLOW - HTTP ' + response.status);
            } else if (response.status === 401) {
                addTimeline('warning', 'CHALLENGE - HTTP 401');
            } else if (response.status === 403) {
                addTimeline('error', 'BLOCK - HTTP 403');
            } else {
                addTimeline('error', 'Error - HTTP ' + response.status);
            }

            return response.status;
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
            return 0;
        }
    }

    async function loadProfile() {
        addTimeline('info', 'Loading user profile...');
        try {
            var response = await fetch('/api/profile', {
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            if (response.ok) {
                var data = await response.json();
                els.currentUser.textContent = data.user || data.username || 'Unknown';
                var authorities = data.authorities || [];
                els.currentRoles.textContent = 'Authorities: ' + (authorities.length > 0 ? authorities.join(', ') : 'none');
                renderResponse(response.status, data);
                addTimeline('success', 'Profile loaded: ' + (data.user || data.username));
            } else {
                els.currentUser.textContent = 'Not authenticated';
                els.currentRoles.textContent = '-';
                addTimeline('warning', 'Not authenticated - HTTP ' + response.status);
            }
        } catch (err) {
            addTimeline('error', 'Failed to load profile: ' + err.message);
        }
    }

    async function testResource(level) {
        var id = document.getElementById('input-' + level + '-id').value || 'doc-1';
        var status = await callApi('/api/resources/' + level + '/' + id,
            'Testing ' + level.toUpperCase() + ' resource (id=' + id + ')');
        updateMatrix(level, status);
    }

    async function testAll() {
        addTimeline('info', '--- Testing all 4 security levels ---');
        var levels = ['public', 'normal', 'sensitive', 'admin'];
        for (var i = 0; i < levels.length; i++) {
            var level = levels[i];
            var id = document.getElementById('input-' + level + '-id').value || 'doc-1';
            var status = await callApi('/api/resources/' + level + '/' + id,
                '[' + (i + 1) + '/4] ' + level.toUpperCase());
            updateMatrix(level, status);
        }
        addTimeline('info', '--- All levels tested ---');
    }

    window.App = {
        loadProfile: loadProfile,
        testResource: testResource,
        testAll: testAll
    };
})();
