(function() {
    'use strict';

    var els = {
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline')
    };

    var flowSteps = ['flow-request', 'flow-filter', 'flow-exception', 'flow-asep', 'flow-advice', 'flow-response'];

    var TRIGGERS = {
        auth:   { url: '/api/trigger-auth-error',  label: 'AuthenticationException',        type: 'warning' },
        access: { url: '/api/trigger-access-error', label: 'AccessDeniedException',          type: 'error' },
        custom: { url: '/api/trigger-custom-error', label: 'SecurityPolicyViolationException', type: 'policy' },
        normal: { url: '/api/profile',              label: 'Normal API (no exception)',       type: 'success' }
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
        return 'status-500';
    }

    function renderResponse(status, data) {
        els.responseStatus.innerHTML =
            '<span class="response-status ' + getStatusClass(status) + '">HTTP ' + status + '</span>';
        els.responseBody.textContent = JSON.stringify(data, null, 2);
    }

    function resetFlow() {
        flowSteps.forEach(function(id) {
            var el = document.getElementById(id);
            if (el) {
                el.classList.remove('active', 'completed');
            }
        });
    }

    function animateFlow(isException) {
        resetFlow();
        var steps = isException ? flowSteps : ['flow-request', 'flow-filter', 'flow-response'];
        var delay = 200;

        steps.forEach(function(id, i) {
            setTimeout(function() {
                var el = document.getElementById(id);
                if (el) {
                    el.classList.add('active');
                    if (i > 0) {
                        var prev = document.getElementById(steps[i - 1]);
                        if (prev) {
                            prev.classList.remove('active');
                            prev.classList.add('completed');
                        }
                    }
                    // Mark last step as completed
                    if (i === steps.length - 1) {
                        setTimeout(function() {
                            el.classList.remove('active');
                            el.classList.add('completed');
                        }, delay);
                    }
                }
            }, delay * (i + 1));
        });
    }

    async function trigger(type) {
        var config = TRIGGERS[type];
        if (!config) return;

        var isException = type !== 'normal';

        addTimeline('info', 'Triggering: ' + config.label);
        addTimeline('info', 'GET ' + config.url);

        animateFlow(isException);

        try {
            var response = await fetch(config.url, {
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
                addTimeline('success', 'Response OK - HTTP ' + response.status);
            } else {
                var errorType = data.error || 'Unknown';
                var handler = data.handler || 'Unknown';
                addTimeline(config.type, 'Exception handled: ' + errorType + ' by ' + handler);
                addTimeline(config.type, 'HTTP ' + response.status + ' - ' + (data.message || ''));

                if (data.flow) {
                    addTimeline('info', 'Flow: ' + data.flow);
                }
                if (data.handlerMethod) {
                    addTimeline('info', 'Handler method: ' + data.handlerMethod);
                }
            }
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
        }
    }

    window.App = {
        trigger: trigger
    };
})();
