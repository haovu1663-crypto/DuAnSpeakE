/* ─────────────────────────────────────────────
   SpeakE · app.js  (Voice Recording Edition)
   Uses MediaRecorder API + Web Audio API
───────────────────────────────────────────── */

// ── Config ──────────────────────────────────
const API_BASE = 'http://localhost:8080/result-service';
const ENDPOINTS = {
  process: '/result/process'
};
const MAX_RECORDING_SEC = 300; // 5 minutes max

// ── State ────────────────────────────────────
let mediaRecorder  = null;
let audioChunks    = [];
let audioBlob      = null;
let audioURL       = null;
let isRecording    = false;
let timerInterval  = null;
let elapsedSeconds = 0;
let analyserNode   = null;
let animFrameId    = null;
let audioCtx       = null;
let sourceNode     = null;

// ── DOM refs ─────────────────────────────────
const micBtn          = document.getElementById('mic-btn');
const iconMic         = document.getElementById('icon-mic');
const iconStop        = document.getElementById('icon-stop');
const micWrapper      = document.getElementById('mic-wrapper');
const recordTimer     = document.getElementById('record-timer');
const recordLabel     = document.getElementById('record-label');
const btnReset        = document.getElementById('btn-reset');
const btnSubmit       = document.getElementById('btn-submit');
const btnCopy         = document.getElementById('btn-copy');
const playbackSection = document.getElementById('playback-section');
const audioPlayback   = document.getElementById('audio-playback');
const recordingMeta   = document.getElementById('recording-meta');
const waveformCanvas  = document.getElementById('waveform-canvas');
const errorMsg        = document.getElementById('error-msg');
const errorText       = document.getElementById('error-text');
const permMsg         = document.getElementById('perm-msg');
const emptyState      = document.getElementById('empty-state');
const skeletonGrp     = document.getElementById('skeleton-group');
const resultContent   = document.getElementById('result-content');
const scoreRow        = document.getElementById('score-row');
const transcriptBlock = document.getElementById('transcript-block');
const analysisBlock   = document.getElementById('analysis-block');
const rawJson         = document.getElementById('raw-json');
const micStatusDot    = document.getElementById('mic-status-dot');
const micStatusText   = document.getElementById('mic-status-text');
const headerBadge     = document.querySelector('.header-badge');
const canvasCtx       = waveformCanvas.getContext('2d');

// ── Mic Button click ──────────────────────────
micBtn.addEventListener('click', async () => {
  if (isRecording) {
    stopRecording();
  } else {
    await startRecording();
  }
});

// ── Start Recording ───────────────────────────
async function startRecording() {
  hideError();
  permMsg.hidden = true;

  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        sampleRate: 44100,
      }
    });

    // Setup Web Audio API for waveform
    audioCtx    = new (window.AudioContext || window.webkitAudioContext)();
    analyserNode = audioCtx.createAnalyser();
    analyserNode.fftSize = 256;
    sourceNode  = audioCtx.createMediaStreamSource(stream);
    sourceNode.connect(analyserNode);
    drawWaveform();

    // Setup MediaRecorder
    const mimeType = getSupportedMimeType();
    mediaRecorder = new MediaRecorder(stream, mimeType ? { mimeType } : {});
    audioChunks   = [];

    mediaRecorder.ondataavailable = e => {
      if (e.data.size > 0) audioChunks.push(e.data);
    };

    mediaRecorder.onstop = () => {
      const mime = mimeType || 'audio/webm';
      audioBlob = new Blob(audioChunks, { type: mime });
      audioURL  = URL.createObjectURL(audioBlob);
      audioPlayback.src = audioURL;
      showPlayback(mime);
      stream.getTracks().forEach(t => t.stop());
      stopWaveform();
    };

    mediaRecorder.start(100); // collect every 100ms
    isRecording = true;

    // UI → recording state
    setRecordingUI(true);
    startTimer();

  } catch (err) {
    if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
      permMsg.hidden = false;
    } else {
      showError(`Lỗi microphone: ${err.message}`);
    }
  }
}

// ── Stop Recording ────────────────────────────
function stopRecording() {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop();
  }
  isRecording = false;
  stopTimer();
  setRecordingUI(false);

  // Close AudioContext
  if (audioCtx) {
    audioCtx.close();
    audioCtx = null;
  }
}

// ── Get best supported MIME ───────────────────
function getSupportedMimeType() {
  const types = [
    'audio/webm;codecs=opus',
    'audio/webm',
    'audio/ogg;codecs=opus',
    'audio/mp4',
  ];
  return types.find(t => MediaRecorder.isTypeSupported(t)) || '';
}

// ── Timer ─────────────────────────────────────
function startTimer() {
  elapsedSeconds = 0;
  updateTimerDisplay();
  timerInterval = setInterval(() => {
    elapsedSeconds++;
    updateTimerDisplay();
    if (elapsedSeconds >= MAX_RECORDING_SEC) stopRecording();
  }, 1000);
}

function stopTimer() {
  clearInterval(timerInterval);
  timerInterval = null;
}

function updateTimerDisplay() {
  const m = String(Math.floor(elapsedSeconds / 60)).padStart(2, '0');
  const s = String(elapsedSeconds % 60).padStart(2, '0');
  recordTimer.textContent = `${m}:${s}`;
}

// ── Waveform drawing ──────────────────────────
function drawWaveform() {
  const bufLen = analyserNode.frequencyBinCount;
  const dataArr = new Uint8Array(bufLen);
  const W = waveformCanvas.width;
  const H = waveformCanvas.height;

  function draw() {
    animFrameId = requestAnimationFrame(draw);
    analyserNode.getByteTimeDomainData(dataArr);

    // Gradient background
    canvasCtx.fillStyle = 'rgba(23,32,53,0.85)';
    canvasCtx.fillRect(0, 0, W, H);

    // Waveform line
    const gradient = canvasCtx.createLinearGradient(0, 0, W, 0);
    gradient.addColorStop(0,   '#7c6ef5');
    gradient.addColorStop(0.5, '#00d2ff');
    gradient.addColorStop(1,   '#ff6b9d');

    canvasCtx.lineWidth   = 2.5;
    canvasCtx.strokeStyle = gradient;
    canvasCtx.shadowColor = '#7c6ef5';
    canvasCtx.shadowBlur  = 8;
    canvasCtx.beginPath();

    const sliceW = W / bufLen;
    let x = 0;
    for (let i = 0; i < bufLen; i++) {
      const v = dataArr[i] / 128.0;
      const y = (v * H) / 2;
      i === 0 ? canvasCtx.moveTo(x, y) : canvasCtx.lineTo(x, y);
      x += sliceW;
    }
    canvasCtx.lineTo(W, H / 2);
    canvasCtx.stroke();
  }

  waveformCanvas.classList.add('active');
  draw();
}

function stopWaveform() {
  if (animFrameId) {
    cancelAnimationFrame(animFrameId);
    animFrameId = null;
  }
  // Draw flat line after stop
  canvasCtx.fillStyle = 'rgba(23,32,53,1)';
  canvasCtx.fillRect(0, 0, waveformCanvas.width, waveformCanvas.height);
  canvasCtx.strokeStyle = '#22d3a5';
  canvasCtx.lineWidth = 2;
  canvasCtx.shadowBlur = 6;
  canvasCtx.shadowColor = '#22d3a5';
  canvasCtx.beginPath();
  canvasCtx.moveTo(0, waveformCanvas.height / 2);
  canvasCtx.lineTo(waveformCanvas.width, waveformCanvas.height / 2);
  canvasCtx.stroke();
}

// ── UI states ─────────────────────────────────
function setRecordingUI(recording) {
  // Mic button
  micBtn.classList.toggle('recording', recording);
  iconMic.hidden  =  recording;
  iconStop.hidden = !recording;

  // Rings
  micWrapper.classList.toggle('recording', recording);

  // Timer
  recordTimer.classList.toggle('active',    true);
  recordTimer.classList.toggle('recording', recording);

  // Label
  if (recording) {
    recordLabel.textContent = '● Đang ghi âm... Nhấn để dừng';
    recordLabel.className   = 'record-label recording';
  } else {
    recordLabel.textContent = '✔ Ghi âm hoàn tất';
    recordLabel.className   = 'record-label done';
    micBtn.classList.add('has-audio');
  }

  // Header badge
  headerBadge.classList.toggle('recording', recording);
  micStatusDot.classList.toggle('recording', recording);
  micStatusText.textContent = recording ? '● Đang ghi âm' : 'Sẵn sàng ghi âm';

  // Buttons
  btnReset.disabled = recording;
}

function showPlayback(mime) {
  const kb = (audioBlob.size / 1024).toFixed(1);
  const dur = elapsedSeconds;
  const m   = String(Math.floor(dur / 60)).padStart(2, '0');
  const s   = String(dur % 60).padStart(2, '0');
  recordingMeta.textContent = `Thời lượng: ${m}:${s} · Kích thước: ${kb} KB · Định dạng: ${mime.split(';')[0]}`;
  playbackSection.hidden = false;
  btnSubmit.disabled     = false;
  btnReset.disabled      = false;
}

// ── Reset ─────────────────────────────────────
btnReset.addEventListener('click', () => {
  audioBlob    = null;
  audioURL     = null;
  audioChunks  = [];
  elapsedSeconds = 0;

  audioPlayback.src  = '';
  playbackSection.hidden = true;
  btnSubmit.disabled     = true;
  btnReset.disabled      = true;
  btnCopy.disabled       = true;

  recordTimer.textContent = '00:00';
  recordTimer.className   = 'record-timer';
  recordLabel.textContent = 'Nhấn để bắt đầu';
  recordLabel.className   = 'record-label';
  micBtn.className        = 'mic-btn';
  iconMic.hidden          = false;
  iconStop.hidden         = true;
  micWrapper.classList.remove('recording');
  waveformCanvas.classList.remove('active');

  // Clear canvas
  canvasCtx.fillStyle = 'rgba(23,32,53,1)';
  canvasCtx.fillRect(0, 0, waveformCanvas.width, waveformCanvas.height);

  hideError();
  hideResult();
  micStatusText.textContent = 'Sẵn sàng ghi âm';
  micStatusDot.classList.remove('recording');
  headerBadge.classList.remove('recording');
});

// ── Submit ────────────────────────────────────
btnSubmit.addEventListener('click', async () => {
  if (!audioBlob) {
    showError('Vui lòng ghi âm trước khi gửi!');
    return;
  }

  hideError();
  setLoading(true);
  showSkeleton();

  try {
    const formData = new FormData();
    // Xác định extension phù hợp với MIME type ghi âm được
    const mime = audioBlob.type || 'audio/webm';
    const ext  = mime.includes('ogg') ? 'ogg'
               : mime.includes('mp4') ? 'mp4'
               : mime.includes('wav') ? 'wav'
               : 'webm';
    formData.append('file', audioBlob, `recording.${ext}`);

    const response = await fetch(`${API_BASE}${ENDPOINTS.process}`, {
      method: 'POST',
      body: formData,
      // Không set Content-Type thủ công — browser tự set boundary cho multipart
    });

    if (!response.ok) {
      let errMsg = `Server lỗi ${response.status}`;
      try {
        const errBody = await response.json();
        errMsg = errBody.message || errBody.error || JSON.stringify(errBody);
      } catch (_) {
        errMsg = `${response.status}: ${response.statusText}`;
      }
      throw new Error(errMsg);
    }

    const data = await response.json();
    renderResult(data);

  } catch (err) {
    hideSkeleton();
    hideResult();
    if (err.name === 'TypeError' && (err.message.includes('fetch') || err.message.includes('Failed'))) {
      showError('❌ Không kết nối được Gateway (:8080). Kiểm tra các service đã chạy chưa? Hiển thị kết quả demo.');
      renderResult(buildDemoResult());
    } else {
      showError('❌ ' + err.message);
    }
  } finally {
    setLoading(false);
  }
});

// ── Copy ─────────────────────────────────────
btnCopy.addEventListener('click', () => {
  navigator.clipboard.writeText(rawJson.textContent).then(() => {
    const orig = btnCopy.innerHTML;
    btnCopy.textContent = '✅ Đã sao chép!';
    setTimeout(() => btnCopy.innerHTML = orig, 2000);
  });
});

// ── Render result ─────────────────────────────
// ProcessResultResponse fields từ backend:
// audioId, transcript, pronunciationScore, fluencyScore, clarityScore, accuracyScore,
// pronunciationMistakes[], grammarCorrection, grammarExplanation, vocabularyLevel, generalFeedback
function renderResult(data) {
  hideSkeleton();

  // 1. Scores
  const scoreRow = document.getElementById('score-row');
  scoreRow.innerHTML = '';
  
  const pron    = data.pronunciationScore ?? data.scores?.pronunciation ?? 0;
  const fluency = data.fluencyScore       ?? data.scores?.fluency       ?? 0;
  const clarity = data.clarityScore       ?? data.scores?.clarity       ?? 0;
  const accuracy= data.accuracyScore      ?? data.scores?.accuracy      ?? 0;
  const overall = Math.round((pron + fluency + clarity) / 3);

  [
    { label: 'Phát âm',   value: pron },
    { label: 'Trôi chảy', value: fluency },
    { label: 'Rõ ràng',   value: clarity },
    { label: 'Chính xác', value: accuracy },
    { label: 'Tổng thể',  value: overall },
  ].forEach((item, i) => {
    const card = document.createElement('div');
    card.className = 'score-card';
    card.style.animationDelay = `${i * 0.05}s`;
    
    const pct = typeof item.value === 'number' && item.value > 0 ? Math.round(item.value) : 0;
    const color = pct >= 80 ? 'var(--clr-success)' : pct >= 60 ? 'var(--clr-warning)' : pct > 0 ? 'var(--clr-error)' : 'var(--clr-text-3)';
    
    card.innerHTML = `
      <div class="score-value" style="color:${color}">${pct > 0 ? pct + '%' : '—'}</div>
      <div class="score-label">${item.label}</div>
      <div class="score-bar">
        <div class="score-bar-fill" style="width: ${pct}%; background: ${color}"></div>
      </div>
    `;
    scoreRow.appendChild(card);
  });

  // 2. Transcript
  const transcript = data.transcript || '';
  const secTranscript = document.getElementById('section-transcript');
  if (transcript) {
    document.getElementById('transcript-text').innerHTML = escHtml(transcript);
    secTranscript.hidden = false;
  } else {
    secTranscript.hidden = true;
  }

  // 3. Grammar
  const corrected = data.grammarCorrection || '';
  const secGrammar = document.getElementById('section-grammar');
  if (corrected && corrected !== transcript) {
    document.getElementById('grammar-corrected').innerHTML = `<strong>Đề xuất:</strong> ${escHtml(corrected)}`;
    document.getElementById('grammar-explanation').innerHTML = escHtml(data.grammarExplanation || 'Câu của bạn có lỗi ngữ pháp.');
    
    const badge = document.getElementById('grammar-badge');
    badge.className = 'info-card-badge badge-warn';
    badge.textContent = 'Cần cải thiện';
    secGrammar.hidden = false;
  } else if (transcript) {
    document.getElementById('grammar-corrected').innerHTML = `<div class="no-mistakes">✨ Tuyệt vời! Câu của bạn đúng ngữ pháp.</div>`;
    document.getElementById('grammar-explanation').innerHTML = 'Tiếp tục phát huy nhé.';
    const badge = document.getElementById('grammar-badge');
    badge.className = 'info-card-badge badge-success';
    badge.textContent = 'Hoàn hảo';
    secGrammar.hidden = false;
  } else {
    secGrammar.hidden = true;
  }

  // 4. Mistakes
  const mistakes = data.pronunciationMistakes || data.analysis || [];
  const secMistakes = document.getElementById('section-mistakes');
  const mistakesContainer = document.getElementById('mistakes-container');
  mistakesContainer.innerHTML = '';
  
  if (mistakes.length > 0) {
    const list = document.createElement('div');
    list.className = 'mistake-list';
    
    mistakes.forEach(m => {
      // Backend format
      if (m.word !== undefined) {
        list.innerHTML += `
          <div class="mistake-item">
            <div class="mistake-word">Từ: <span>${escHtml(m.word)}</span></div>
            <div class="mistake-problem">Vấn đề: ${escHtml(m.problem || '')}</div>
          </div>
        `;
      } 
      // Demo format support
      else if (m.label) {
        list.innerHTML += `
          <div class="mistake-item">
            <div class="mistake-word">${escHtml(m.label)}</div>
            <div class="mistake-problem">${escHtml(m.feedback || '')}</div>
          </div>
        `;
      }
    });
    mistakesContainer.appendChild(list);
    secMistakes.hidden = false;
  } else if (transcript) {
    mistakesContainer.innerHTML = `<div class="no-mistakes">✨ Không phát hiện lỗi phát âm đáng kể.</div>`;
    secMistakes.hidden = false;
  } else {
    secMistakes.hidden = true;
  }

  // 5. Vocabulary
  const vocabLevel = data.vocabularyLevel || 'N/A';
  const secVocab = document.getElementById('section-vocab');
  if (vocabLevel !== 'N/A') {
    let icon = '📚'; let hint = 'Trình độ cơ bản';
    if (vocabLevel.toLowerCase().includes('intermediate')) { icon = '🚀'; hint = 'Trình độ trung cấp'; }
    if (vocabLevel.toLowerCase().includes('advanced')) { icon = '💎'; hint = 'Trình độ cao cấp'; }
    
    document.getElementById('vocab-display').innerHTML = `
      <div class="vocab-icon">${icon}</div>
      <div class="vocab-text-group">
        <div class="vocab-level-name">${escHtml(vocabLevel)}</div>
        <div class="vocab-level-hint">${hint}</div>
      </div>
    `;
    secVocab.hidden = false;
  } else {
    secVocab.hidden = true;
  }

  // 6. Feedback
  const feedback = data.generalFeedback || '';
  const secFeedback = document.getElementById('section-feedback');
  if (feedback) {
    document.getElementById('feedback-text').innerHTML = escHtml(feedback).replace(/\n/g, '<br/>');
    secFeedback.hidden = false;
  } else {
    secFeedback.hidden = true;
  }

  // 7. Raw JSON
  document.getElementById('raw-json').textContent = JSON.stringify(data, null, 2);

  showResult();
}

// Escape HTML để tránh XSS
function escHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ── Demo result ───────────────────────────────
// Giả lập cấu trúc ProcessResultResponse từ backend
function buildDemoResult() {
  return {
    audioId: 'demo-' + Date.now(),
    transcript: 'Hello, how are you today? I am fine, thank you very much.',
    pronunciationScore: Math.floor(72 + Math.random() * 22),
    fluencyScore:       Math.floor(68 + Math.random() * 26),
    clarityScore:       Math.floor(75 + Math.random() * 20),
    accuracyScore:      Math.floor(70 + Math.random() * 24),
    pronunciationMistakes: [
      { word: 'pronunciation', problem: 'Cần chú ý âm "un" - /prəˌnʌn.siˈeɪ.ʃən/' },
    ],
    grammarCorrection:  'Hello, how are you today? I am fine, thank you very much.',
    grammarExplanation: 'Câu của bạn đã đúng ngữ pháp.',
    vocabularyLevel:    'Intermediate',
    generalFeedback:    '(Demo) Phát âm của bạn khá tốt! Tốc độ nói vừa phải. Hãy chú ý thêm đến việc kéo dài nguyên âm đúng chỗ.',
  };
}

// ── UI helpers ────────────────────────────────
function setLoading(on) {
  btnSubmit.disabled = on;
  btnSubmit.querySelector('.btn-text').hidden   =  on;
  btnSubmit.querySelector('.btn-loader').hidden = !on;
}
function showSkeleton() {
  emptyState.hidden    = true;
  skeletonGrp.hidden   = false;
  resultContent.hidden = true;
}
function hideSkeleton() { skeletonGrp.hidden = true; }
function showResult() {
  resultContent.hidden = false;
  btnCopy.disabled     = false;
}
function hideResult() {
  resultContent.hidden = true;
  emptyState.hidden    = false;
  btnCopy.disabled     = true;
}
function showError(msg) {
  errorText.textContent = msg;
  errorMsg.hidden = false;
}
function hideError() { errorMsg.hidden = true; }

// ── Check browser support ─────────────────────
(function checkSupport() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    micBtn.disabled = true;
    recordLabel.textContent = 'Trình duyệt không hỗ trợ ghi âm';
    showError('Trình duyệt của bạn không hỗ trợ MediaRecorder API. Hãy dùng Chrome, Edge hoặc Firefox phiên bản mới.');
  }
})();
