(function() {
    'use strict';

    var els = {
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline'),
        inputName: document.getElementById('input-name')
    };

    function getTimeStr() {
        return new Date().toLocaleTimeString('en-US', { hour12: false });
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

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
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

    async function callApi(url, label) {
        addTimeline('info', 'Calling ' + label + ': GET ' + url);
        var start = performance.now();

        try {
            var response = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });

            var elapsed = Math.round(performance.now() - start);
            var data;

            try {
                data = await response.json();
            } catch (e) {
                data = { raw: await response.text() };
            }

            renderResponse(response.status, data);

            if (response.ok) {
                addTimeline('success', label + ' OK (' + elapsed + 'ms) - HTTP ' + response.status);
            } else {
                addTimeline('error', label + ' FAILED (' + elapsed + 'ms) - HTTP ' + response.status);
            }
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', label + ' network error: ' + err.message);
        }
    }

    function callHelloName() {
        var name = els.inputName.value.trim() || 'World';
        callApi('/api/hello/' + encodeURIComponent(name), 'Hello(' + name + ')');
    }

    // Public API
    window.App = {
        callApi: callApi,
        callHelloName: callHelloName
    };
})();
