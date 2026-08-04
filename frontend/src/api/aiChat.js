import api from './axios';

// Sprint 1: stateless single-turn chat.
// Backend: POST /api/ai/chat  -> { reply, model, tookMs }
// When Sprint 2 (RAG) / Sprint 3 (Memory) land, the request/response shape
// may grow (citations, sessionId, sources) — keep this file as the single
// place that knows about the AI endpoint so the UI doesn't need to change.
export async function sendChatMessage(message, { signal } = {}) {
  const { data } = await api.post('/ai/chat', { message }, { signal });
  return data; // { reply, model, tookMs }
}
