/**
 * Lab test page logic.
 * Uses ContexaStreaming.StreamingClient for SSE streaming.
 */

const streamingClient = new ContexaStreaming.StreamingClient();

function getRequestData() {
    return {
        text: document.getElementById('textInput').value.trim(),
        language: document.getElementById('langSelect').value
    };
}

function setButtonsEnabled(enabled) {
    document.getElementById('btnAnalyze').disabled = !enabled;
    document.getElementById('btnStream').disabled = !enabled;
}

function addLogEntry(message, type) {
    const log = document.getElementById('streamLog');
    const entry = document.createElement('div');
    entry.className = 'ctx-log-entry ctx-log-' + (type || 'info');
    const time = new Date().toLocaleTimeString();
    entry.textContent = '[' + time + '] ' + message;
    log.appendChild(entry);
    log.scrollTop = log.scrollHeight;
}

function clearLog() {
    document.getElementById('streamLog').innerHTML = '';
}

function showResult(data) {
    document.getElementById('resultPlaceholder').style.display = 'none';
    document.getElementById('resultArea').style.display = 'block';

    // Sentiment badge
    const badge = document.getElementById('sentimentBadge');
    const sentiment = (data.sentiment || 'NEUTRAL').toUpperCase();
    badge.textContent = sentiment;
    badge.className = 'sentiment-badge sentiment-' + sentiment;

    // Confidence
    const confidence = data.confidence || 0;
    document.getElementById('confidenceValue').textContent = Math.round(confidence * 100) + '%';
    document.getElementById('confidenceFill').style.width = (confidence * 100) + '%';

    // Keywords
    const keywordsArea = document.getElementById('keywordsArea');
    keywordsArea.innerHTML = '';
    if (data.keywords && data.keywords.length > 0) {
        data.keywords.forEach(function(kw) {
            const tag = document.createElement('span');
            tag.className = 'keyword-tag';
            tag.textContent = kw;
            keywordsArea.appendChild(tag);
        });
    }

    // Summary
    document.getElementById('summaryText').textContent = data.summary || '';
}

function resetResult() {
    document.getElementById('resultPlaceholder').style.display = 'block';
    document.getElementById('resultArea').style.display = 'none';
}

/**
 * Synchronous analysis via POST /api/lab/analyze
 */
function analyzeSentiment() {
    const data = getRequestData();
    if (!data.text) { alert('Please enter text to analyze.'); return; }

    clearLog();
    resetResult();
    setButtonsEnabled(false);
    addLogEntry('Starting synchronous analysis...', 'info');

    fetch('/api/lab/analyze', {
        method: 'POST',
        headers: Object.assign({ 'Content-Type': 'application/json' }, streamingClient.getCsrfHeaders()),
        body: JSON.stringify(data)
    })
    .then(function(res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(function(result) {
        addLogEntry('Analysis complete', 'success');
        showResult(result);
    })
    .catch(function(err) {
        addLogEntry('Error: ' + err.message, 'error');
    })
    .finally(function() {
        setButtonsEnabled(true);
    });
}

/**
 * SSE streaming analysis via POST /api/lab/analyze/stream
 */
function analyzeStream() {
    var data = getRequestData();
    if (!data.text) { alert('Please enter text to analyze.'); return; }

    clearLog();
    resetResult();
    setButtonsEnabled(false);
    addLogEntry('Starting streaming analysis...', 'info');

    var chunkCount = 0;

    streamingClient.stream('/api/lab/analyze/stream', data, {
        onChunk: function(chunk) {
            chunkCount++;
            addLogEntry('Chunk ' + chunkCount + ': ' + chunk.substring(0, 80) + (chunk.length > 80 ? '...' : ''), 'info');
        },
        onFinalResponse: function(finalJson) {
            addLogEntry('Final response received', 'success');
            try {
                var result = JSON.parse(finalJson);
                showResult(result);
            } catch (e) {
                addLogEntry('Parse result: ' + finalJson.substring(0, 120), 'info');
            }
        },
        onComplete: function() {
            addLogEntry('Stream complete (' + chunkCount + ' chunks)', 'success');
            setButtonsEnabled(true);
        },
        onError: function(err) {
            addLogEntry('Stream error: ' + err, 'error');
            setButtonsEnabled(true);
        },
        onRetry: function(attempt, max) {
            addLogEntry('Retrying... (' + attempt + '/' + max + ')', 'warn');
        }
    });
}
