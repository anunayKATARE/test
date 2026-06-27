# Cloud Token Chat

A minimal chat app that uses your own Anthropic API token (a "cloud token") to chat with Claude.

## How it works

- You paste your Anthropic API key into the settings panel in the browser. It's stored in `localStorage` only.
- The browser sends chat requests to this app's small Express server, which forwards them to the Anthropic Messages API using your token.
- The server never stores or logs your token — it's only used to make the single upstream request and then discarded.

## Setup

```bash
npm install
npm start
```

Then open http://localhost:3000, click the gear icon, paste your Anthropic API key, pick a model, and start chatting.

## Notes

- Requires Node.js 18+ (uses the built-in `fetch`).
- Your API token never leaves your browser except to this server, which only relays it to `https://api.anthropic.com`.
