/**
 * Pipeline test page logic.
 * Dual SSE: step progress events + streaming analysis results.
 */

var streamingClient = new ContexaStreaming.StreamingClient();
var stepEventSource = null;

var PIPELINE_STEPS = [
    'CONTEXT_RETRIEVAL', 'PREPROCESSING', 'PROMPT_GENERATION',
    'LLM_EXECUTION', 'RESPONSE_PARSING', 'POSTPROCESSING'
];

function getRequestData() {
    return {
        code: document.getElementById('codeInput').value.trim(),
        language: document.getElementById('langSelect').value
    };
}

function setButtonsEnabled(enabled) {
    document.getElementById('btnAnalyze').disabled = !enabled;
    document.getElementById('btnStream').disabled = !enabled;
}

function addLogEntry(message, type) {
    var log = document.getElementById('streamLog');
    var entry = document.createElement('div');
    entry.className = 'ctx-log-entry ctx-log-' + (type || 'info');
    var time = new Date().toLocaleTimeString();
    entry.textContent = '[' + time + '] ' + message;
    log.appendChild(entry);
    log.scrollTop = log.scrollHeight;
}

function clearLog() {
    document.getElementById('streamLog').innerHTML = '';
}

function resetSteps() {
    PIPELINE_STEPS.forEach(function(step) {
        var el = document.getElementById('step-' + step);
        if (el) {
            el.className = 'pipeline-step';
            var timeEl = document.getElementById('time-' + step);
            if (timeEl) timeEl.textContent = '';
        }
    });
}

function updateStep(stepName, status, elapsedMs) {
    var el = document.getElementById('step-' + stepName);
    if (!el) return;
    el.className = 'pipeline-step ' + status;
    if (elapsedMs !== undefined) {
        var timeEl = document.getElementById('time-' + stepName);
        if (timeEl) timeEl.textContent = elapsedMs + 'ms';
    }
}

function connectStepSSE() {
    if (stepEventSource) { stepEventSource.close(); }
    stepEventSource = new EventSource('/api/sse/pipeline');
    stepEventSource.addEventListener('pipeline-progress', function(event) {
        try {
            var data = JSON.parse(event.data);
            updateStep(data.stepName, data.status, data.elapsedMs);
            addLogEntry('[' + data.stepName + '] ' + data.status + (data.message ? ' - ' + data.message : '') + (data.elapsedMs ? ' (' + data.elapsedMs + 'ms)' : ''), data.status === 'error' ? 'error' : 'info');
        } catch (e) { /* ignore parse errors */ }
    });
    stepEventSource.onerror = function() {
        stepEventSource.close();
        stepEventSource = null;
    };
}

function showResult(data) {
    document.getElementById('resultPlaceholder').style.display = 'none';
    document.getElementById('resultArea').style.display = 'block';

    // Severity badge
    var badge = document.getElementById('severityBadge');
    var severity = (data.overallSeverity || 'LOW').toUpperCase();
    badge.textContent = severity;
    badge.className = 'severity-badge severity-' + severity;

    // Vulnerabilities
    var vulnList = document.getElementById('vulnList');
    vulnList.innerHTML = '';
    if (data.vulnerabilities && data.vulnerabilities.length > 0) {
        data.vulnerabilities.forEach(function(v) {
            var sev = (v.severity || 'LOW').toUpperCase();
            var card = document.createElement('div');
            card.className = 'vuln-card ' + sev;
            card.innerHTML =
                '<div class="vuln-header">' +
                '  <span class="vuln-type">' + escapeHtml(v.type || 'Unknown') + '</span>' +
                '  <span class="vuln-severity ' + sev + '">' + sev + '</span>' +
                '</div>' +
                '<div class="vuln-desc">' + escapeHtml(v.description || '') + '</div>' +
                (v.location ? '<div class="vuln-location">Location: ' + escapeHtml(v.location) + '</div>' : '') +
                (v.fix ? '<div class="vuln-fix">Fix: ' + escapeHtml(v.fix) + '</div>' : '');
            vulnList.appendChild(card);
        });
    } else {
        vulnList.innerHTML = '<div class="ctx-placeholder">No vulnerabilities detected.</div>';
    }

    // Recommendations
    var recList = document.getElementById('recList');
    recList.innerHTML = '';
    if (data.recommendations && data.recommendations.length > 0) {
        data.recommendations.forEach(function(r) {
            var li = document.createElement('li');
            li.textContent = r;
            recList.appendChild(li);
        });
    }

    // Summary
    document.getElementById('summaryText').textContent = data.summary || '';
}

function resetResult() {
    document.getElementById('resultPlaceholder').style.display = 'block';
    document.getElementById('resultArea').style.display = 'none';
}

function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Synchronous pipeline analysis.
 */
function analyzePipeline() {
    var data = getRequestData();
    if (!data.code) { alert('Please enter code to analyze.'); return; }

    clearLog();
    resetResult();
    resetSteps();
    setButtonsEnabled(false);
    connectStepSSE();
    addLogEntry('Starting pipeline analysis...', 'info');

    fetch('/api/pipeline/analyze', {
        method: 'POST',
        headers: Object.assign({ 'Content-Type': 'application/json' }, streamingClient.getCsrfHeaders()),
        body: JSON.stringify(data)
    })
    .then(function(res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
    })
    .then(function(result) {
        addLogEntry('Pipeline analysis complete', 'success');
        showResult(result);
    })
    .catch(function(err) {
        addLogEntry('Error: ' + err.message, 'error');
    })
    .finally(function() {
        setButtonsEnabled(true);
        if (stepEventSource) { stepEventSource.close(); stepEventSource = null; }
    });
}

/**
 * SSE streaming pipeline analysis.
 */
function analyzeStream() {
    var data = getRequestData();
    if (!data.code) { alert('Please enter code to analyze.'); return; }

    clearLog();
    resetResult();
    resetSteps();
    setButtonsEnabled(false);
    connectStepSSE();
    addLogEntry('Starting streaming pipeline analysis...', 'info');

    var chunkCount = 0;

    streamingClient.stream('/api/pipeline/analyze/stream', data, {
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
            if (stepEventSource) { stepEventSource.close(); stepEventSource = null; }
        },
        onError: function(err) {
            addLogEntry('Stream error: ' + err, 'error');
            setButtonsEnabled(true);
            if (stepEventSource) { stepEventSource.close(); stepEventSource = null; }
        },
        onRetry: function(attempt, max) {
            addLogEntry('Retrying... (' + attempt + '/' + max + ')', 'warn');
        }
    });
}
