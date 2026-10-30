import { useState } from "react";

export default function Photo({ src, name }) {
  const [failed, setFailed] = useState(false);
  if (!src || failed) {
    return <div className="photo fallback" aria-hidden="true">{name?.slice(0, 1) || "A"}</div>;
  }
  return <img className="photo" src={src} alt="" onError={() => setFailed(true)} />;
}
