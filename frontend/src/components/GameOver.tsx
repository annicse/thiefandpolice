import { useNavigate } from "react-router-dom";
import type { GameState } from "../api";
import { getWinners, joinNames } from "../announcements";
import { useSessionStore } from "../store";
import Scoreboard from "./Scoreboard";

// শেষ স্ক্রিন, সবার জন্য (host আর player দুজনেই)
export default function GameOver({ state }: { state: GameState }) {
  const navigate = useNavigate();
  const clearSession = useSessionStore((s) => s.clearSession);

  // সবচেয়ে বেশি স্কোর যাদের, তারা সবাই বিজয়ী: দুজনের স্কোর সমান হলে দুজনই জেতে
  const { winners, topScore } = getWinners(state.players);
  const winnerNames = joinNames(winners.map((p) => p.name));
  const iWon = winners.some((p) => p.id === state.me?.id);

  function startOver() {
    clearSession();
    navigate("/");
  }

  return (
    <div className="space-y-4">
      <div className="rounded-2xl bg-amber-400 p-6 text-center text-slate-900">
        <p className="text-sm font-semibold uppercase tracking-widest">
          {winners.length > 1 ? "Winners" : "Winner"}
        </p>
        <p className="mt-1 text-3xl font-extrabold">{winnerNames}</p>
        <p className="mt-1 font-mono text-lg">{topScore} points</p>
        {iWon && <p className="mt-2 font-semibold">You won!</p>}
      </div>

      <h3 className="text-lg font-semibold">Final scores</h3>
      <Scoreboard players={state.players} meId={state.me?.id} sortByScore showRoles={false} />

      <button
        onClick={startOver}
        className="w-full rounded-lg bg-sky-500 py-3 font-semibold text-slate-900"
      >
        New game
      </button>
    </div>
  );
}
