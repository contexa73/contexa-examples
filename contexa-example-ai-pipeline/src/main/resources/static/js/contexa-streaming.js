/**
 * Contexa Streaming Client - Simplified version of contexa-llm.bundle.js
 *
 * Provides SSE streaming and modal UI for AI Engine examples.
 * Based on the full StreamingClient + ModalUIAdapter from contexa-iam aiam package.
 */
(function(global) {
    'use strict';

    // ============================================================
    // StreamingClient - SSE fetch-based streaming
    // ============================================================
    class StreamingClient {
        static DEFAULT_CONFIG = {
            markers: {
                FINAL_RESPONSE: '###FINAL_RESPONSE###',
                GENERATING_RESULT: '###GENERATING_RESULT###',
                DONE: '[DONE]',
                ERROR_PREFIX: 'ERROR:'
            },
            maxRetries: 3,
            retryDelay: 1000,
            retryMultiplier: 1.5,
            timeoutMs: 300000
        };

        constructor(config = {}) {
            this.config = { ...StreamingClient.DEFAULT_CONFIG, ...config };
            if (config.markers) {
                this.config.markers = { ...StreamingClient.DEFAULT_CONFIG.markers, ...config.markers };
            }
            this.abortController = null;
            this.isAborted = false;
            this.retryCount = 0;
            this.finalResponseReceived = false;
            this.finalResponseBuffer = '';
        }

        async stream(url, requestData, callbacks = {}) {
            return new Promise((resolve, reject) => {
                let finalResponse = null;

                this.startStreaming(url, requestData, {
                    onChunk: (chunk) => { if (callbacks.onChunk) callbacks.onChunk(chunk); },
                    onFinalResponse: (data) => {
                        if (callbacks.onFinalResponse) callbacks.onFinalResponse(data);
                    },
                    onComplete: () => {
                        if (this.finalResponseBuffer) {
                            finalResponse = this.parseFinalResponse(this.finalResponseBuffer);
                        }
                        if (callbacks.onComplete) callbacks.onComplete();
                        resolve(finalResponse);
                    },
                    onError: (error) => {
                        if (callbacks.onError) callbacks.onError(error);
                        reject(error);
                    },
                    onRetry: (attempt, max) => { if (callbacks.onRetry) callbacks.onRetry(attempt, max); },
                    onAbort: () => { if (callbacks.onAbort) callbacks.onAbort(); resolve(null); }
                });
            });
        }

        async startStreaming(url, requestData, callbacks) {
            this.isAborted = false;
            this.abortController = new AbortController();
            this.finalResponseReceived = false;
            this.finalResponseBuffer = '';

            const timeoutId = setTimeout(() => {
                if (!this.isAborted) {
                    this.abortController.abort();
                    if (callbacks.onError) callbacks.onError(new Error('Request timeout'));
                }
            }, this.config.timeoutMs);

            try {
                const response = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'text/event-stream',
                        ...this.getCsrfHeaders()
                    },
                    body: JSON.stringify(requestData),
                    signal: this.abortController.signal
                });

                if (!response.ok) throw new Error('HTTP ' + response.status + ': ' + response.statusText);

                this.retryCount = 0;
                await this.processStream(response, callbacks);
                clearTimeout(timeoutId);
            } catch (error) {
                clearTimeout(timeoutId);
                if (error.name === 'AbortError' || this.isAborted) {
                    if (callbacks.onAbort) callbacks.onAbort();
                    return;
                }
                if (this.retryCount < this.config.maxRetries) {
                    this.retryCount++;
                    const delay = this.config.retryDelay * Math.pow(this.config.retryMultiplier, this.retryCount - 1);
                    if (callbacks.onRetry) callbacks.onRetry(this.retryCount, this.config.maxRetries);
                    await new Promise(r => setTimeout(r, delay));
                    if (!this.isAborted) return this.startStreaming(url, requestData, callbacks);
                }
                if (callbacks.onError) callbacks.onError(error);
            }
        }

        async processStream(response, callbacks) {
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let buffer = '';
            try {
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) {
                        if (buffer.trim()) this.processSSEData(buffer, callbacks);
                        break;
                    }
                    buffer += decoder.decode(value, { stream: true });
                    const lines = buffer.split('\n');
                    buffer = lines.pop() || '';
                    for (const line of lines) this.processSSELine(line, callbacks);
                }
                if (callbacks.onComplete) callbacks.onComplete();
            } finally {
                reader.releaseLock();
            }
        }

        processSSELine(line, callbacks) {
            const trimmed = line.trim();
            if (!trimmed || trimmed.startsWith(':')) return;
            if (trimmed.startsWith('data:')) {
                this.processSSEData(trimmed.substring(5).trim(), callbacks);
            }
        }

        processSSEData(data, callbacks) {
            if (!data) return;
            const m = this.config.markers;
            if (data === m.DONE) return;
            if (data.startsWith(m.ERROR_PREFIX)) {
                if (callbacks.onError) callbacks.onError(new Error(data.substring(m.ERROR_PREFIX.length).trim()));
                return;
            }
            if (this.finalResponseReceived) { this.finalResponseBuffer += data; return; }
            if (data.includes(m.FINAL_RESPONSE)) {
                this.finalResponseReceived = true;
                this.finalResponseBuffer = data;
                if (callbacks.onFinalResponse) callbacks.onFinalResponse(data);
                return;
            }
            if (callbacks.onChunk) callbacks.onChunk(data);
        }

        parseFinalResponse(data) {
            if (!data) return null;
            try {
                const marker = this.config.markers.FINAL_RESPONSE;
                const idx = data.lastIndexOf(marker);
                const json = idx >= 0 ? data.substring(idx + marker.length) : data;
                return this.parseJson(json);
            } catch (e) {
                return { parseError: true, raw: data, errorMessage: e.message };
            }
        }

        parseJson(str) {
            if (!str || typeof str !== 'string') throw new Error('Invalid JSON');
            let cleaned = str.trim();
            if (cleaned.startsWith('```json')) cleaned = cleaned.replace(/^```json\s*/, '').replace(/```\s*$/, '').trim();
            else if (cleaned.startsWith('```')) cleaned = cleaned.replace(/^```\s*/, '').replace(/```\s*$/, '').trim();
            const first = cleaned.indexOf('{'), last = cleaned.lastIndexOf('}');
            if (first !== -1 && last > first) cleaned = cleaned.substring(first, last + 1);
            return JSON.parse(cleaned);
        }

        getCsrfHeaders() {
            if (typeof document === 'undefined') return {};
            const token = document.querySelector('meta[name="_csrf"]')?.content;
            const header = document.querySelector('meta[name="_csrf_header"]')?.content;
            return (token && header) ? { [header]: token } : {};
        }

        abort() {
            this.isAborted = true;
            if (this.abortController) { this.abortController.abort(); this.abortController = null; }
        }
    }

    // ============================================================
    // ModalAdapter - Streaming progress modal
    // ============================================================
    class ModalAdapter {
        constructor() { this.overlay = null; this.content = null; this.stepCount = 0; }

        show(title) {
            this.stepCount = 0;
            this.overlay = document.createElement('div');
            this.overlay.className = 'ctx-modal-overlay';
            this.overlay.innerHTML =
                '<div class="ctx-modal">' +
                '  <div class="ctx-modal-header">' +
                '    <div class="ctx-spinner"></div>' +
                '    <span class="ctx-modal-title">' + (title || 'AI Analyzing...') + '</span>' +
                '  </div>' +
                '  <div class="ctx-modal-content"></div>' +
                '  <div class="ctx-modal-footer"><span class="ctx-dots"><span>.</span><span>.</span><span>.</span></span></div>' +
                '</div>';
            document.body.appendChild(this.overlay);
            this.content = this.overlay.querySelector('.ctx-modal-content');
            requestAnimationFrame(() => this.overlay.classList.add('visible'));
        }

        addStep(message) {
            if (!this.content) return;
            this.stepCount++;
            const step = document.createElement('div');
            step.className = 'ctx-modal-step';
            step.innerHTML = '<span class="ctx-step-num">' + this.stepCount + '</span> ' + message;
            this.content.appendChild(step);
            this.content.scrollTop = this.content.scrollHeight;
        }

        complete() {
            if (!this.overlay) return;
            const title = this.overlay.querySelector('.ctx-modal-title');
            if (title) title.textContent = 'Analysis Complete';
            const spinner = this.overlay.querySelector('.ctx-spinner');
            if (spinner) spinner.classList.add('done');
            const footer = this.overlay.querySelector('.ctx-modal-footer');
            if (footer) footer.innerHTML = '<span class="ctx-check">&#10003;</span> Done';
        }

        error(msg) {
            if (!this.overlay) return;
            const title = this.overlay.querySelector('.ctx-modal-title');
            if (title) { title.textContent = 'Error'; title.style.color = '#ef4444'; }
            this.addStep('Error: ' + (msg || 'Unknown error'));
        }

        hide() {
            if (!this.overlay) return;
            this.overlay.classList.remove('visible');
            setTimeout(() => { if (this.overlay && this.overlay.parentNode) this.overlay.parentNode.removeChild(this.overlay); this.overlay = null; }, 300);
        }
    }

    // ============================================================
    // Public API
    // ============================================================
    global.ContexaStreaming = {
        StreamingClient: StreamingClient,
        ModalAdapter: ModalAdapter
    };

})(typeof window !== 'undefined' ? window : this);
