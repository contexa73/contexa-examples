(function() {
    'use strict';

    var els = {
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline'),
        docList: document.getElementById('doc-list'),
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
        if (status === 404) return 'status-404';
        return 'status-500';
    }

    function renderResponse(status, data) {
        els.responseStatus.innerHTML =
            '<span class="response-status ' + getStatusClass(status) + '">HTTP ' + status + '</span>';
        els.responseBody.textContent = JSON.stringify(data, null, 2);
    }

    function renderDocuments(docs) {
        els.docList.innerHTML = '';
        docs.forEach(function(doc) {
            var item = document.createElement('div');
            item.className = 'doc-item';
            item.onclick = function() {
                document.getElementById('input-doc-id').value = doc.id;
            };
            item.innerHTML =
                '<span class="doc-title">#' + doc.id + ' ' + escapeHtml(doc.title) + '</span>' +
                '<span class="doc-owner">' + escapeHtml(doc.ownerId) + '</span>' +
                '<span class="doc-level doc-level-' + doc.securityLevel + '">' + doc.securityLevel + '</span>';
            els.docList.appendChild(item);
        });
    }

    async function callApi(method, url, body, label) {
        addTimeline('info', label);
        addTimeline('info', method + ' ' + url);

        var opts = {
            method: method,
            headers: { 'Accept': 'application/json' },
            credentials: 'same-origin'
        };

        if (body) {
            opts.headers['Content-Type'] = 'application/json';
            opts.body = JSON.stringify(body);
        }

        try {
            var response = await fetch(url, opts);

            var data;
            try {
                data = await response.json();
            } catch (e) {
                data = { raw: 'Non-JSON response', status: response.status };
            }

            renderResponse(response.status, data);

            if (response.ok) {
                addTimeline('success', 'ALLOWED - HTTP ' + response.status);
            } else if (response.status === 403) {
                addTimeline('error', 'DENIED - HTTP 403 (insufficient permission)');
            } else if (response.status === 401) {
                addTimeline('warning', 'UNAUTHORIZED - HTTP 401');
            } else if (response.status === 404) {
                addTimeline('warning', 'NOT FOUND - HTTP 404');
            } else {
                addTimeline('error', 'Error - HTTP ' + response.status);
            }

            return { status: response.status, data: data };
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
            return null;
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

    async function loadDocuments() {
        var result = await callApi('GET', '/api/documents', null, 'Load all documents');
        if (result && result.status >= 200 && result.status < 300 && Array.isArray(result.data)) {
            renderDocuments(result.data);
            addTimeline('info', 'Loaded ' + result.data.length + ' documents');
        }
    }

    async function readDocument() {
        var id = document.getElementById('input-doc-id').value || '1';
        await callApi('GET', '/api/documents/' + id, null,
            'READ document #' + id + ' - hasPermission(#id, DOCUMENT, READ)');
    }

    async function createDocument() {
        var title = document.getElementById('input-title').value || 'New Report';
        var dept = document.getElementById('input-dept').value || 'engineering';
        var level = document.getElementById('input-level').value || 'INTERNAL';

        var body = { title: title, departmentId: dept, securityLevel: level };
        await callApi('POST', '/api/documents', body,
            'CREATE document - hasPermission(null, DOCUMENT, CREATE)');
    }

    async function updateDocument() {
        var id = document.getElementById('input-doc-id').value || '1';
        var title = document.getElementById('input-title').value || 'Updated Report';
        var dept = document.getElementById('input-dept').value || 'engineering';
        var level = document.getElementById('input-level').value || 'INTERNAL';

        var body = { title: title, departmentId: dept, securityLevel: level };
        await callApi('PUT', '/api/documents/' + id, body,
            'UPDATE document #' + id + ' - hasPermission(#id, DOCUMENT, UPDATE)');
    }

    async function deleteDocument() {
        var id = document.getElementById('input-doc-id').value || '1';
        await callApi('DELETE', '/api/documents/' + id, null,
            'DELETE document #' + id + ' - hasPermission(#id, DOCUMENT, DELETE)');
    }

    window.App = {
        loadProfile: loadProfile,
        loadDocuments: loadDocuments,
        readDocument: readDocument,
        createDocument: createDocument,
        updateDocument: updateDocument,
        deleteDocument: deleteDocument
    };
})();
