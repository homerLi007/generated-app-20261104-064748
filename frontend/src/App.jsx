import { useState } from 'react';

export default function App() {
  const [input, setInput] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handleDelete() {
    setError(null);
    setResult(null);
    const paths = input.split('\n').map(s => s.trim()).filter(Boolean);
    if (paths.length === 0) {
      setError('Enter at least one path');
      return;
    }
    setLoading(true);
    try {
      const res = await fetch('/api/delete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paths })
      });
      if (!res.ok) throw new Error('HTTP ' + res.status);
      setResult(await res.json());
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>Safe File Deleter</h1>
      <p className="hint">One relative path per line. Symlinks, non-regular files, and paths outside the allowed root are refused.</p>
      <textarea
        value={input}
        onChange={e => setInput(e.target.value)}
        placeholder="e.g.\nnotes.txt\nsubdir/report.pdf"
        rows={8}
      />
      <button onClick={handleDelete} disabled={loading}>
        {loading ? 'Deleting...' : 'Delete Files'}
      </button>
      {error && <div className="error">{error}</div>}
      {result && (
        <div className="result">
          <h2>Deleted ({result.deleted.length})</h2>
          <ul>{result.deleted.map(p => <li key={p} className="ok">{p}</li>)}</ul>
          <h2>Refused ({result.refused.length})</h2>
          <ul>{result.refused.map((r, i) => <li key={i} className="bad">{r.path} — {r.reason}</li>)}</ul>
        </div>
      )}
    </div>
  );
}
