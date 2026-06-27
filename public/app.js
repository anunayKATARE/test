const STORAGE_KEY = 'cloud-token-chat:apiKey';
const STORAGE_MODEL = 'cloud-token-chat:model';

const settingsPanel = document.getElementById('settingsPanel');
const settingsBtn = document.getElementById('settingsBtn');
const apiKeyInput = document.getElementById('apiKey');
const modelSelect = document.getElementById('model');
const saveSettingsBtn = document.getElementById('saveSettings');
const messagesEl = document.getElementById('messages');
const chatForm = document.getElementById('chatForm');
const input = document.getElementById('input');
const sendBtn = document.getElementById('sendBtn');

let history = [];

function loadSettings() {
  const key = localStorage.getItem(STORAGE_KEY);
  const model = localStorage.getItem(STORAGE_MODEL);
  if (key) apiKeyInput.value = key;
  if (model) modelSelect.value = model;
  if (!key) settingsPanel.classList.remove('hidden');
}

settingsBtn.addEventListener('click', () => {
  settingsPanel.classList.toggle('hidden');
});

saveSettingsBtn.addEventListener('click', () => {
  localStorage.setItem(STORAGE_KEY, apiKeyInput.value.trim());
  localStorage.setItem(STORAGE_MODEL, modelSelect.value);
  settingsPanel.classList.add('hidden');
});

function appendMessage(role, text) {
  const div = document.createElement('div');
  div.className = `msg ${role}`;
  div.textContent = text;
  messagesEl.appendChild(div);
  messagesEl.scrollTop = messagesEl.scrollHeight;
  return div;
}

chatForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  const text = input.value.trim();
  if (!text) return;

  const apiKey = localStorage.getItem(STORAGE_KEY);
  if (!apiKey) {
    settingsPanel.classList.remove('hidden');
    appendMessage('error', 'Please enter your API token first.');
    return;
  }

  appendMessage('user', text);
  history.push({ role: 'user', content: text });
  input.value = '';
  sendBtn.disabled = true;

  const pending = appendMessage('assistant', '...');

  try {
    const res = await fetch('/api/chat', {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        'x-api-key': apiKey
      },
      body: JSON.stringify({
        model: localStorage.getItem(STORAGE_MODEL) || 'claude-sonnet-4-6',
        messages: history
      })
    });

    const data = await res.json();
    if (!res.ok) {
      pending.className = 'msg error';
      pending.textContent = data.error || 'Something went wrong.';
      history.pop();
      return;
    }

    const reply = (data.content || []).map((block) => block.text || '').join('');
    pending.textContent = reply;
    history.push({ role: 'assistant', content: reply });
  } catch (err) {
    pending.className = 'msg error';
    pending.textContent = 'Network error reaching the server.';
    history.pop();
  } finally {
    sendBtn.disabled = false;
    input.focus();
  }
});

input.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    chatForm.requestSubmit();
  }
});

loadSettings();
