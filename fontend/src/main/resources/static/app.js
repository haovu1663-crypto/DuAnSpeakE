/* ─────────────────────────────────────────────
   SpeakE · app.js  (Voice Recording Edition)
   Uses MediaRecorder API + Web Audio API
───────────────────────────────────────────── */

// ── Config ──────────────────────────────────
const API_BASE = 'http://localhost:8080';
const ENDPOINTS = {
  process: '/api/result/process'
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

  const options = {
    analyzePronunciation: document.getElementById('opt-pronunciation').checked,
    analyzeGrammar:       document.getElementById('opt-grammar').checked,
    analyzeVocab:         document.getElementById('opt-vocab').checked,
    useAI:                document.getElementById('opt-ai').checked,
  };

  const endpoint = ENDPOINTS.process;

  try {
    const formData = new FormData();
    const ext  = audioBlob.type.includes('ogg') ? 'ogg' : audioBlob.type.includes('mp4') ? 'mp4' : 'webm';
    formData.append('file', audioBlob, `recording.${ext}`);

    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const errBody = await response.text();
      throw new Error(`Server trả về lỗi ${response.status}: ${errBody || response.statusText}`);
    }

    const data = await response.json();
    renderResult(data);

  } catch (err) {
    hideSkeleton();
    if (err.name === 'TypeError' && err.message.toLowerCase().includes('fetch')) {
      showError('Không kết nối được Gateway (:8080). Hiển thị kết quả demo.');
      renderResult(buildDemoResult());
    } else {
      showError(err.message);
      hideResult();
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
function renderResult(data) {
  hideSkeleton();

  // Mapping ProcessResultResponse to UI
  scoreRow.innerHTML = '';
  const overall = Math.round((data.pronunciationScore + data.fluencyScore + data.clarityScore) / 3) || 0;
  
  // Tính điểm ngữ pháp giả lập (nếu có sửa lỗi thì trừ điểm)
  let grammarScore = 95;
  if (data.grammarCorrection && data.grammarCorrection !== data.transcript) {
     grammarScore -= 15;
  }
  
  // Đổi từ level sang score
  let vocabScore = 80;
  if (data.vocabularyLevel === 'Advanced') vocabScore = 95;
  else if (data.vocabularyLevel === 'Beginner') vocabScore = 65;

  [
    { label: 'Phát âm',  value: data.pronunciationScore || 0 },
    { label: 'Trôi chảy', value: data.fluencyScore || 0 },
    { label: 'Rõ ràng',  value: data.clarityScore || 0 },
    { label: 'Tổng thể', value: overall },
  ].forEach((item, i) => {
    const card = document.createElement('div');
    card.className = 'score-card';
    card.style.animationDelay = `${i * 0.06}s`;
    const val = typeof item.value === 'number' ? Math.round(item.value) + '%' : '—';
    card.innerHTML = `<div class="score-value">${val}</div><div class="score-label">${item.label}</div>`;
    scoreRow.appendChild(card);
  });

  // Transcript
  transcriptBlock.innerHTML = '';
  if (data.transcript) {
    transcriptBlock.innerHTML = `
      <div class="transcript-title">📝 Văn bản nhận dạng (Speech-to-Text)</div>
      <div class="transcript-text">${data.transcript}</div>
    `;
  }

  // Analysis items
  analysisBlock.innerHTML = '';
  const tips = [];
  
  if (data.pronunciationMistakes && data.pronunciationMistakes.length > 0) {
    data.pronunciationMistakes.forEach(m => {
       tips.push({
         label: 'Lỗi phát âm',
         text: `Từ: "${m.word}"`,
         feedback: `Lỗi nhận diện: ${m.problem}`,
         tags: [{type:'warn', label:'⚡ Cần luyện'}]
       });
    });
  }

  if (data.grammarCorrection && data.grammarCorrection !== data.transcript) {
    tips.push({
      label: 'Sửa lỗi Ngữ pháp',
      text: data.grammarCorrection,
      feedback: data.grammarExplanation || 'Câu của bạn có lỗi ngữ pháp.',
      tags: [{type:'error', label:'✕ Sai ngữ pháp'}]
    });
  }

  if (data.generalFeedback) {
    tips.push({
      label: 'Nhận xét tổng quan',
      text: `Trình độ từ vựng ước tính: ${data.vocabularyLevel || 'N/A'}`,
      feedback: data.generalFeedback,
      tags: [{type:'good', label:'✓ Đánh giá'}]
    });
  }

  tips.forEach((item, i) => {
    const el = document.createElement('div');
    el.className = 'analysis-item';
    el.style.animationDelay = `${i * 0.07}s`;
    const tags = (item.tags || []).map(t => `<span class="tag tag-${t.type}">${t.label}</span>`).join('');
    el.innerHTML = `
      <div class="analysis-label">${item.label || 'Nhận xét'}</div>
      <div class="analysis-original">${item.text || ''}</div>
      <div class="analysis-feedback">${item.feedback || ''}</div>
      ${tags ? `<div class="tag-row">${tags}</div>` : ''}
    `;
    analysisBlock.appendChild(el);
  });

  // Raw JSON
  rawJson.textContent = JSON.stringify(data, null, 2);

  showResult();
}

// ── Demo result ───────────────────────────────
function buildDemoResult() {
  const tips = [
    { label: 'Nhịp điệu & Tốc độ', text: 'Tốc độ nói vừa phải, dễ nghe.', feedback: 'Tốc độ của bạn khá tốt. Thử duy trì nhịp điệu đều đặn trong suốt đoạn hội thoại.', tags: [{type:'good', label:'✓ Tốt'}] },
    { label: 'Nguyên âm', text: '/æ/ → "cat", "bad", "man"', feedback: 'Âm /æ/ cần mở miệng rộng hơn. Hạ hàm dưới và kéo môi sang hai bên.', tags: [{type:'warn', label:'⚡ Cần luyện'}] },
    { label: 'Phụ âm khó', text: '/θ/ → "think", "three", "through"', feedback: 'Âm /θ/ cần đặt đầu lưỡi nhẹ giữa hai hàm răng và thổi hơi. Không dùng âm /s/ hay /d/ thay thế.', tags: [{type:'warn', label:'⚡ Cần luyện'}, {type:'error', label:'✕ Lỗi phổ biến'}] },
    { label: 'Intonation (ngữ điệu)', text: 'Câu hỏi Yes/No: giọng lên cuối câu', feedback: 'Ngữ điệu câu hỏi của bạn khá ổn. Hãy đảm bảo giọng đi lên rõ ràng ở cuối câu hỏi.', tags: [{type:'good', label:'✓ Intonation'}] },
    { label: 'Trọng âm từ', text: '"pho-TO-graph" vs "pho-TOG-ra-phy"', feedback: 'Chú ý vị trí trọng âm thay đổi theo dạng từ (danh từ ↔ động từ, v.d. "RE-cord" vs "re-CORD").', tags: [{type:'warn', label:'⚡ Trọng âm'}] },
  ];

  return {
    transcript: '(Demo) Hello, how are you today? I am fine, thank you very much.',
    scores: {
      pronunciation: Math.floor(72 + Math.random() * 22),
      grammar:       Math.floor(68 + Math.random() * 26),
      vocabulary:    Math.floor(75 + Math.random() * 20),
      overall:       Math.floor(70 + Math.random() * 24),
    },
    analysis: tips,
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
