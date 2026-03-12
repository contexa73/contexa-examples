/**
 * LLM test page logic.
 * Chat UI with streaming + Advisor log SSE sidebar.
 */

var streamingClient = new ContexaStreaming.StreamingClient();
var advisorEventSource = null;
var selectedLevel = 'QUICK';

// Start advisor SSE connection
function connectAdvisorSSE() {
    if (advisorEventSource) advisorEventSource.close();
    advisorEventSource = new EventSource('/api/sse/advisor-log');
    advisorEventSource.addEventListener('advisor-log', function(event) {
        try {
            var data = JSON.parse(event.data);
            addAdvisorEntry(data);
            addTimelineEntry(data);
        } catch (e) { /* ignore */ }
    });
    advisorEventSource.onerror = function() {
        advisorEventSource.close();
        advisorEventSource = null;
        // Reconnect after 3 seconds
        setTimeout(connectAdvisorSSE, 3000);
    };
}

function selectLevel(btn) {
    document.querySelectorAll('.level-btn').forEach(function(b) { b.classList.remove('active'); });
    btn.classList.add('active');
    selectedLevel = btn.dataset.level;
}

function handleKeyDown(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendChat();
    }
}

function setButtonsEnabled(enabled) {
    document.getElementById('btnSend').disabled = !enabled;
    document.getElementById('btnStream').disabled = !enabled;
}

function addChatMessage(role, text) {
    var messages = document.getElementById('chatMessages');
    var div = document.createElement('div');
    div.className = 'chat-msg ' + (role === 'user' ? 'user-msg' : 'ai-msg');

    var label = document.createElement('div');
    label.className = 'msg-label';
    label.textContent = role === 'user' ? 'You' : 'AI (' + selectedLevel + ')';

    var content = document.createElement('div');
    content.textContent = text;

    div.appendChild(label);
    div.appendChild(content);
    messages.appendChild(div);
    messages.scrollTop = messages.scrollHeight;
    return content;
}

function addAdvisorEntry(data) {
    var log = document.getElementById('advisorLog');
    var entry = document.createElement('div');
    entry.className = 'advisor-entry ' + data.advisorName;
    entry.innerHTML =
        '<span class="advisor-name">' + escapeHtml(data.advisorName) + '</span>' +
        '<span class="advisor-phase">' + escapeHtml(data.phase) + '</span>' +
        '<div class="advisor-data">' + escapeHtml(data.data || '') + '</div>';
    log.appendChild(entry);
    log.scrollTop = log.scrollHeight;
}

function addTimelineEntry(data) {
    var timeline = document.getElementById('advisorTimeline');
    var entry = document.createElement('div');
    entry.className = 'timeline-entry';
    var time = new Date(data.timestamp).toLocaleTimeString();
    entry.innerHTML =
        '<span class="timeline-time">' + time + '</span>' +
        '<span class="timeline-label">' + escapeHtml(data.advisorName) + ' ' + escapeHtml(data.phase) + '</span>';
    timeline.appendChild(entry);
    timeline.scrollTop = timeline.scrollHeight;
}

function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Synchronous chat.
 */
function sendChat() {
    var input = document.getElementById('chatInput');
    var message = input.value.trim();
    if (!message) return;

    addChatMessage('user', message);
    input.value = '';
    setButtonsEnabled(false);

    fetch('/api/llm/chat', {
        method: 'POST',
        headers: Object.assign({ 'Content-Type': 'application/json' }, streamingClient.getCsrfHeaders()),
        body: JSON.stringify({ message: message, level: selectedLevel })
    })
    .then(function(res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(function(result) {
        addChatMessage('ai', result.response || JSON.stringify(result));
    })
    .catch(function(err) {
        addChatMessage('ai', 'Error: ' + err.message);
    })
    .finally(function() {
        setButtonsEnabled(true);
    });
}

/**
 * Streaming chat.
 */
function sendStream() {
    var input = document.getElementById('chatInput');
    var message = input.value.trim();
    if (!message) return;

    addChatMessage('user', message);
    input.value = '';
    setButtonsEnabled(false);

    // Create AI message element for progressive updates
    var aiContent = addChatMessage('ai', '');
    var fullText = '';

    streamingClient.stream('/api/llm/chat/stream', { message: message, level: selectedLevel }, {
        onChunk: function(chunk) {
            fullText += chunk;
            aiContent.textContent = fullText;
            var messages = document.getElementById('chatMessages');
            messages.scrollTop = messages.scrollHeight;
        },
        onFinalResponse: function(finalJson) {
            // Final response replaces streamed content if it's valid JSON
            try {
                var parsed = JSON.parse(finalJson);
                if (parsed.response) {
                    aiContent.textContent = parsed.response;
                }
            } catch (e) {
                // Keep streamed text
            }
        },
        onComplete: function() {
            setButtonsEnabled(true);
        },
        onError: function(err) {
            if (!fullText) aiContent.textContent = 'Error: ' + err;
            setButtonsEnabled(true);
        },
        onRetry: function(attempt, max) {
            aiContent.textContent = 'Retrying... (' + attempt + '/' + max + ')';
        }
    });
}

// Auto-connect advisor SSE on page load
connectAdvisorSSE();
