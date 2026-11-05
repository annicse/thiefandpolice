import { useQuery } from "@tanstack/react-query";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { api, ApiError } from "../api";
import type { Phase } from "../api";
import HostScreen from "../components/HostScreen";
import GameOver from "../components/GameOver";
import Lobby from "../components/Lobby";
import PlayerResult from "../components/PlayerResult";
import PlayerRound from "../components/PlayerRound";
import { useSessionStore } from "../store";

// যে phase-গুলোতে খেলোয়াড়ের রাউন্ড স্ক্রিন দেখায়
const PLAYER_ROUND_PHASES: Phase[] = ["KING_REVEAL", "POLICE_REVEAL", "GUESSING"];
// যে phase-গুলোতে host স্ক্রিন দেখায় (RESULT-এ Next round বাটন লাগে)
const HOST_PHASES: Phase[] = ["KING_REVEAL", "POLICE_REVEAL", "GUESSING", "RESULT"];

// এই পেজ পুরো গেমের "কন্ট্রোল রুম": state আনে (polling), আর phase দেখে কোন স্ক্রিন দেখাবে ঠিক করে।
export default function GamePage() {
  const { code } = useParams();
  const navigate = useNavigate();
  const session = useSessionStore((s) => s.session);
  const clearSession = useSessionStore((s) => s.clearSession);

  // এই URL-এর গেমের জন্য আমার সেশন আছে কি? না থাকলে null
  const activeSession =
    session !== null && session.code === code?.toUpperCase() ? session : null;

  const { data, error } = useQuery({
    queryKey: ["state", activeSession?.code],
    queryFn: () => api.getState(activeSession!.code, activeSession!.token),
    enabled: activeSession !== null,
    refetchInterval: 1200, // প্রতি ~১.২ সেকেন্ডে আবার জিজ্ঞেস করে (polling)
    retry: false,
  });

  if (!activeSession) return <Navigate to="/" replace />;

  // সার্ভার restart হলে গেম হারিয়ে যায়: তখন 404 (গেম নেই) বা 403 (token অচেনা) আসে
  const gameGone = error instanceof ApiError && (error.status === 404 || error.status === 403);

  function leave() {
    clearSession();
    navigate("/");
  }

  return (
    <div className="space-y-4 py-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-400">
            {activeSession.isHost ? "Host" : activeSession.name}
          </p>
          <p className="text-4xl font-bold tracking-widest">{activeSession.code}</p>
        </div>
        <button onClick={leave} className="rounded-lg bg-slate-700 px-3 py-2 text-sm">
          Leave
        </button>
      </div>

      {gameGone ? (
        <div className="space-y-3 rounded-xl bg-slate-800 p-4">
          <p className="text-red-400">This game no longer exists. The server may have restarted.</p>
          <button
            onClick={leave}
            className="w-full rounded-lg bg-sky-500 py-2 font-semibold text-slate-900"
          >
            Back to home
          </button>
        </div>
      ) : !data ? (
        <p className="text-slate-400">{error ? error.message : "Loading..."}</p>
      ) : data.phase === "LOBBY" ? (
        <Lobby state={data} session={activeSession} />
      ) : data.phase === "GAME_OVER" ? (
        <GameOver state={data} />
      ) : data.viewerType === "PLAYER" && PLAYER_ROUND_PHASES.includes(data.phase) ? (
        <PlayerRound state={data} session={activeSession} />
      ) : data.viewerType === "PLAYER" && data.phase === "RESULT" ? (
        <PlayerResult state={data} />
      ) : data.viewerType === "HOST" && HOST_PHASES.includes(data.phase) ? (
        <HostScreen state={data} session={activeSession} />
      ) : (
        // কোনো স্ক্রিনের সাথে না মিললে (থাকার কথা না): ডিবাগের জন্য কাঁচা state
        <pre className="overflow-x-auto rounded-xl bg-slate-800 p-3 text-xs">
          {JSON.stringify(data, null, 2)}
        </pre>
      )}
    </div>
  );
}
