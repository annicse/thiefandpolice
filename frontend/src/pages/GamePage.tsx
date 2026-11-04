import { useQuery } from "@tanstack/react-query";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { api } from "../api";
import { useSessionStore } from "../store";

// সাময়িক পেজ: /state কী ফেরত দিচ্ছে সেটা কাঁচা JSON-এ দেখায়।
// পরের ধাপে এখানে আসল Lobby / Player / Host স্ক্রিন বসবে।
export default function GamePage() {
  const { code } = useParams();
  const navigate = useNavigate();
  const session = useSessionStore((s) => s.session);
  const clearSession = useSessionStore((s) => s.clearSession);

  // সেশন না থাকলে বা অন্য গেমের কোড হলে এখানে থাকার কথা না
  const hasSession = session !== null && session.code === code?.toUpperCase();

  const { data, error } = useQuery({
    queryKey: ["state", code],
    queryFn: () => api.getState(session!.code, session!.token),
    enabled: hasSession,
    refetchInterval: 1200, // প্রতি ~১.২ সেকেন্ডে আবার জিজ্ঞেস করে (polling)
  });

  if (!hasSession) return <Navigate to="/" replace />;

  function leave() {
    clearSession();
    navigate("/");
  }

  return (
    <div className="space-y-4 py-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-400">
            {session.isHost ? "Host" : session.name}
          </p>
          <p className="text-4xl font-bold tracking-widest">{session.code}</p>
        </div>
        <button onClick={leave} className="rounded-lg bg-slate-700 px-3 py-2 text-sm">
          Leave
        </button>
      </div>

      {error && <p className="text-red-400">{error.message}</p>}

      <pre className="overflow-x-auto rounded-xl bg-slate-800 p-3 text-xs">
        {data ? JSON.stringify(data, null, 2) : "Loading..."}
      </pre>
    </div>
  );
}
