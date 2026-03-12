(function() {
    'use strict';

    var els = {
        responseStatus: document.getElementById('response-status'),
        responseBody: document.getElementById('response-body'),
        timeline: document.getElementById('timeline'),
        accountList: document.getElementById('account-list')
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

    function renderResponse(status, data, elapsed) {
        var timeStr = elapsed !== undefined ? ' <span class="response-time">(' + elapsed + 'ms)</span>' : '';
        els.responseStatus.innerHTML =
            '<span class="response-status ' + getStatusClass(status) + '">HTTP ' + status + '</span>' + timeStr;
        els.responseBody.textContent = JSON.stringify(data, null, 2);
    }

    function renderAccounts(accounts) {
        els.accountList.innerHTML = '';
        accounts.forEach(function(acc) {
            var item = document.createElement('div');
            item.className = 'account-item';
            item.innerHTML =
                '<span class="user-id">User ' + acc.userId + '</span>' +
                '<span class="balance">' + Number(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 }) + '</span>';
            els.accountList.appendChild(item);
        });
    }

    function interpretResponse(status) {
        if (status >= 200 && status < 300) {
            addTimeline('success', 'ALLOW - Request permitted (HTTP ' + status + ')');
            addTimeline('shadow', 'In Shadow mode: always allowed. In Enforce mode: AI decided ALLOW.');
        } else if (status === 401) {
            addTimeline('warning', 'CHALLENGE - Authentication required (HTTP 401)');
            addTimeline('info', 'Enforce mode: AI requires additional verification.');
        } else if (status === 403) {
            addTimeline('error', 'BLOCK - Access denied (HTTP 403)');
            addTimeline('info', 'Enforce mode: AI blocked this request.');
        } else if (status === 423) {
            addTimeline('escalate', 'ESCALATE - Manual review required (HTTP 423)');
            addTimeline('info', 'Enforce mode: AI flagged for human review.');
        }
    }

    async function callApi(method, url, params, label) {
        addTimeline('info', label);
        addTimeline('info', method + ' ' + url);

        var start = performance.now();
        var fullUrl = url;
        if (params) {
            var qs = Object.keys(params).map(function(k) {
                return encodeURIComponent(k) + '=' + encodeURIComponent(params[k]);
            }).join('&');
            fullUrl += '?' + qs;
        }

        try {
            var response = await fetch(fullUrl, {
                method: method,
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            var elapsed = Math.round(performance.now() - start);

            var data;
            try {
                data = await response.json();
            } catch (e) {
                data = { raw: 'Non-JSON response', status: response.status };
            }

            renderResponse(response.status, data, elapsed);
            interpretResponse(response.status);

            return { status: response.status, data: data };
        } catch (err) {
            renderResponse(0, { error: err.message });
            addTimeline('error', 'Network error: ' + err.message);
            return null;
        }
    }

    async function loadAccounts() {
        var result = await callApi('GET', '/api/accounts', null, 'Load all accounts');
        if (result && result.status >= 200 && result.status < 300 && Array.isArray(result.data)) {
            renderAccounts(result.data);
            addTimeline('info', 'Loaded ' + result.data.length + ' accounts');
        }
    }

    async function getBalance() {
        var userId = document.getElementById('input-balance-user').value || '1';
        await callApi('GET', '/api/accounts/' + userId, null,
            'Get balance for User ' + userId + ' (ownerField check)');
    }

    async function transfer() {
        var fromUserId = document.getElementById('input-from').value || '1';
        var toUserId = document.getElementById('input-to').value || '2';
        var amount = document.getElementById('input-amount').value || '500';

        addTimeline('warning', 'Transfer is SYNC - AI must decide before processing');

        await callApi('POST', '/api/accounts/transfer', {
            fromUserId: fromUserId,
            toUserId: toUserId,
            amount: amount
        }, 'Transfer ' + amount + ' from User ' + fromUserId + ' to User ' + toUserId);
    }

    window.App = {
        loadAccounts: loadAccounts,
        getBalance: getBalance,
        transfer: transfer
    };
})();
