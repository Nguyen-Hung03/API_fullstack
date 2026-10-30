export default function ErrorNote({ error }) {
  if (!error) return null;
  return (
    <div className="note error" role="alert">
      <strong>{error.message}</strong>
      {error.code && <span className="code">{error.code}</span>}
      {error.details?.length > 0 && (
        <ul>
          {error.details.map((item) => <li key={item}>{item}</li>)}
        </ul>
      )}
    </div>
  );
}
