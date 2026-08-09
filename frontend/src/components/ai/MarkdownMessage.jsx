function renderInline(text) {
  const parts = text.split(/(\*\*[^*]+\*\*)/g).filter(Boolean);

  return parts.map((part, index) => {
    if (part.startsWith('**') && part.endsWith('**')) {
      return <strong key={`${part}-${index}`}>{part.slice(2, -2)}</strong>;
    }

    return <span key={`${part}-${index}`}>{part}</span>;
  });
}

export default function MarkdownMessage({ content }) {
  if (!content) return null;

  const lines = String(content).split(/\n/);

  return (
    <div className="ai-markdown">
      {lines.map((line, index) => {
        const trimmed = line.trim();
        if (!trimmed) {
          return <div key={`${line}-${index}`} className="ai-markdown-spacer" />;
        }

        if (trimmed.startsWith('- ')) {
          return (
            <ul key={`${line}-${index}`} className="ai-markdown-list">
              <li>{renderInline(trimmed.slice(2))}</li>
            </ul>
          );
        }

        return <p key={`${line}-${index}`}>{renderInline(trimmed)}</p>;
      })}
    </div>
  );
}
