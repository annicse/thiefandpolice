import { useEffect, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { Navigate, useNavigate, useParams } from "react-router-dom";
import { api, ApiError, isServerUnavailable } from "../api";
import type { Phase } from "../api";
import { announcement, phaseHeading } from "../announcements";
import HostScreen from "../components/HostScreen";
import GameOver from "../components/GameOver";
import Lobby from "../components/Lobby";
import PlayerResult from "../components/PlayerResult";
import PlayerRound from "../components/PlayerRound";
import { useSessionStore } from "../store";
import { useDocumentTitle } from "../useDocumentTitle";

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

  const heading = data ? phaseHeading(data) : "";
  useDocumentTitle(heading ? `${heading} · ${code?.toUpperCase()}` : "Game");

  // phase বদলালে (প্রথম লোড বাদে) ফোকাস শিরোনামে নিই: বাটন উধাও হলে কীবোর্ড/স্ক্রিন রিডার ব্যবহারকারী হারিয়ে যায় না
  const headingRef = useRef<HTMLHeadingElement>(null);
  const previousPhase = useRef<Phase | undefined>(undefined);
  const phase = data?.phase;
  useEffect(() => {
    if (phase && previousPhase.current && previousPhase.current !== phase) {
      headingRef.current?.focus();
    }
    previousPhase.current = phase;
  }, [phase]);

  if (!activeSession) return <Navigate to="/" replace />;

  // সার্ভার restart হলে গেম হারিয়ে যায়: তখন 404 (গেম নেই) বা 403 (token অচেনা) আসে
  const gameGone = error instanceof ApiError && (error.status === 404 || error.status === 403);
  const serverDown = isServerUnavailable(error);

  function leave() {
    clearSession();
    navigate("/");
  }

  return (
    <div className="space-y-4 py-6">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-300">
            {activeSession.isHost ? "Host" : activeSession.name}
          </p>
          <h1 className="text-4xl font-bold tracking-widest">
            <span aria-hidden="true">{activeSession.code}</span>
            <span className="sr-only">
              Game code {activeSession.code.split("").join(" ")}
            </span>
          </h1>
        </div>
        <button
          onClick={leave}
          className="min-h-11 rounded-lg bg-slate-700 px-4 text-sm font-medium"
        >
          Leave game
        </button>
      </div>

      {/* গেমের অবস্থা বদলালে স্ক্রিন রিডার এটা পড়ে শোনায় (শুধু public তথ্য) */}
      <div aria-live="polite" aria-atomic="true" className="sr-only">
        {data ? announcement(data) : ""}
      </div>

      {data && serverDown && (
        <output className="block rounded-xl bg-amber-950 p-3 text-amber-100">
          Connection lost. Trying to reconnect...
        </output>
      )}

      {gameGone ? (
        <div className="space-y-3 rounded-xl bg-slate-800 p-4">
          <p role="alert" className="text-red-300">
            This game no longer exists. The server may have restarted.
          </p>
          <button
            onClick={leave}
            className="min-h-11 w-full rounded-lg bg-sky-500 font-semibold text-slate-900"
          >
            Back to home
          </button>
        </div>
      ) : !data ? (
        <output className="block text-slate-300">
          {serverDown
            ? "Waking up the server. This can take up to a minute on the free hosting tier..."
            : error
              ? error.message
              : "Loading game..."}
        </output>
      ) : (
        <>
          <h2 ref={headingRef} tabIndex={-1} className="text-xl font-semibold outline-none">
            {heading}
          </h2>
          {data.phase === "LOBBY" ? (
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
        </>
      )}
    </div>
  );
}
