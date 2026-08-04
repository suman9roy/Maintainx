import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { sendChatMessage } from '../../api/aiChat';
import './ChatWidget.css';

// Role-based suggested questions (Phase 8 / Phase 10 of steps.md)
const RESIDENT_SUGGESTIONS = [
  'What is the visitor policy?',
  'What are the parking rules?',
  'How do I raise a complaint?',
  'When is my next payment due?',
];

const ADMIN_SUGGESTIONS = [
  'Summarize this month\'s expenses',
  'Summarize open complaints',
  'Give me a budget analysis',
  'Draft a notice for residents',
];

function initialGreeting(role) {
  return role === 'ADMIN' || role === 'SUPER_ADMIN'
    ? "Hi, I'm your MaintainX AI Assistant. I can help with expenses, complaints, budgets and reports."
    : "Hi, I'm your MaintainX AI Assistant. Ask me about visitor rules, parking, payments, complaints or notices.";
}

export default function ChatWidget() {
  const { role, user } = useAuth();
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const bodyRef = useRef(null);
  const seededRef = useRef(false);

  const suggestions = role === 'ADMIN' || role === 'SUPER_ADMIN' ? ADMIN_SUGGESTIONS : RESIDENT_SUGGESTIONS;

  // Seed the greeting once, the first time the widget is opened.
  useEffect(() => {
    if (open && !seededRef.current) {
      seededRef.current = true;
      setMessages([{ role: 'assistant', content: initialGreeting(role) }]);
    }
  }, [open, role]);

  // Auto-scroll to the latest message.
  useEffect(() => {
    if (bodyRef.current) {
      bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
    }
  }, [messages, loading, open]);

  async function handleSend(text) {
    const trimmed = (text ?? input).trim();
    if (!trimmed || loading) return;

    setError(null);
    setInput('');
    setMessages((prev) => [...prev, { role: 'user', content: trimmed }]);
    setLoading(true);

    try {
      const data = await sendChatMessage(trimmed);
      setMessages((prev) => [...prev, { role: 'assistant', content: data.reply }]);
    } catch (err) {
      const msg = err?.response?.data?.message || 'Something went wrong. Please try again.';
      setError(msg);
      setMessages((prev) => [...prev, { role: 'assistant', content: `⚠️ ${msg}` }]);
    } finally {
      setLoading(false);
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  }

  function handleNewChat() {
    seededRef.current = true;
    setMessages([{ role: 'assistant', content: initialGreeting(role) }]);
    setError(null);
  }

  return (
    <div className="ai-widget-root">
      {open && (
        <div className="ai-chat-window" role="dialog" aria-label="MaintainX AI Assistant">
          <div className="ai-chat-header">
            <div className="ai-chat-header-title">
              <span className="ai-avatar">🤖</span>
              <div>
                <div className="ai-name">MaintainX Assistant</div>
                <div className="ai-status"><span className="ai-status-dot" /> online</div>
              </div>
            </div>
            <div className="ai-chat-header-actions">
              <button className="ai-icon-btn" title="New chat" onClick={handleNewChat}>⟲</button>
              <button className="ai-icon-btn" title="Close" onClick={() => setOpen(false)}>✕</button>
            </div>
          </div>

          <div className="ai-chat-body" ref={bodyRef}>
            {messages.map((m, i) => (
              <div key={i} className={`ai-msg ai-msg-${m.role}`}>
                <div className="ai-bubble">{m.content}</div>
              </div>
            ))}
            {loading && (
              <div className="ai-msg ai-msg-assistant">
                <div className="ai-bubble ai-typing">
                  <span /><span /><span />
                </div>
              </div>
            )}

            {!loading && messages.length <= 1 && (
              <div className="ai-suggestions">
                {suggestions.map((q) => (
                  <button key={q} className="ai-suggestion-chip" onClick={() => handleSend(q)}>
                    {q}
                  </button>
                ))}
              </div>
            )}
          </div>

          <div className="ai-chat-input">
            <textarea
              rows={1}
              value={input}
              placeholder={`Message MaintainX Assistant${user?.email ? ' (' + user.email + ')' : ''}...`}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={loading}
            />
            <button className="ai-send-btn" onClick={() => handleSend()} disabled={loading || !input.trim()}>
              ➤
            </button>
          </div>
        </div>
      )}

      <button
        className="ai-fab"
        onClick={() => setOpen((o) => !o)}
        aria-label={open ? 'Close AI Assistant' : 'Open AI Assistant'}
      >
        {open ? '✕' : '🤖'}
      </button>
    </div>
  );
}
