const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const ANTHROPIC_API_URL = 'https://api.anthropic.com/v1/messages';
const ANTHROPIC_VERSION = '2023-06-01';

app.use(express.json({ limit: '1mb' }));
app.use(express.static(path.join(__dirname, 'public')));

// Proxies chat requests to Anthropic using the token supplied by the client.
// The token is never stored or logged server-side; it only lives for the
// duration of this single request.
app.post('/api/chat', async (req, res) => {
  const apiKey = req.get('x-api-key');
  const { model, messages, system, max_tokens } = req.body || {};

  if (!apiKey) {
    return res.status(401).json({ error: 'Missing API token (x-api-key header).' });
  }
  if (!Array.isArray(messages) || messages.length === 0) {
    return res.status(400).json({ error: 'Request must include a non-empty "messages" array.' });
  }

  try {
    const upstream = await fetch(ANTHROPIC_API_URL, {
      method: 'POST',
      headers: {
        'content-type': 'application/json',
        'x-api-key': apiKey,
        'anthropic-version': ANTHROPIC_VERSION
      },
      body: JSON.stringify({
        model: model || 'claude-sonnet-4-6',
        max_tokens: max_tokens || 1024,
        system,
        messages
      })
    });

    const data = await upstream.json();
    if (!upstream.ok) {
      return res.status(upstream.status).json({ error: data.error?.message || 'Upstream API error.' });
    }
    res.json(data);
  } catch (err) {
    res.status(502).json({ error: 'Failed to reach Anthropic API.' });
  }
});

app.listen(PORT, () => {
  console.log(`Cloud token chat app listening on http://localhost:${PORT}`);
});
