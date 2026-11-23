import type { GameState } from "../api";
import type { Session } from "../store";
import HostControls from "./HostControls";
import RoundSummary from "./RoundSummary";
import Scoreboard from "./Scoreboard";

// খেলোয়াড় RESULT phase-এ কী দেখে: কী ঘটল, সবার role, স্কোর। এগোনোর বাটন host-এর কাছে।
export default function PlayerResult({ state, session }: { state: GameState; session: Session }) {
  const isLastRound = state.currentRound >= state.totalRounds;

  return (
    <div className="space-y-4">
      <p className="text-sm text-slate-400">
        Round {state.currentRound} of {state.totalRounds} · Result
      </p>

      <RoundSummary state={state} />

      <h3 className="text-lg font-semibold">Leaderboard</h3>
      <Scoreboard players={state.players} meId={state.me?.id} sortByMoney />

      {state.host ? (
        // host নিজে খেললে তার নিজের বাটন থাকে
        <HostControls state={state} session={session} />
      ) : (
        <p className="text-center text-slate-400">
          {isLastRound
            ? "Waiting for the host to finish the game..."
            : "Waiting for the host to start the next round..."}
        </p>
      )}
    </div>
  );
}
