import type { GameState } from "../api";
import type { Session } from "../store";
import HostControls from "./HostControls";
import { ROLE_INFO } from "../roles";
import RoundSummary from "./RoundSummary";
import Scoreboard from "./Scoreboard";

type Props = {
  state: GameState;
  session: Session;
};

// host কোনো গোপন তথ্য দেখে না: সার্ভার RESULT-এর আগে কারো role পাঠায়ই না।
// king/police reveal হয়ে গেলে তাদের নাম সবাই জানে, তাই সেটা দেখানো যায়।
export default function HostScreen({ state, session }: Props) {
  const nameOf = (id: string | null) =>
    state.players.find((p) => p.id === id)?.name ?? "?";

  return (
    <div className="space-y-4">
      <p className="text-sm text-slate-400">
        Round {state.currentRound} of {state.totalRounds}
      </p>

      {state.phase === "RESULT" ? (
        <RoundSummary state={state} />
      ) : (
        <div className="rounded-xl bg-slate-800 p-4 text-lg">{statusText(state, nameOf)}</div>
      )}

      <h3 className="text-lg font-semibold">Leaderboard</h3>
      <Scoreboard players={state.players} sortByMoney />

      <HostControls state={state} session={session} />
    </div>
  );
}

// phase অনুযায়ী এক লাইনের অবস্থা-বার্তা
function statusText(state: GameState, nameOf: (id: string | null) => string): string {
  switch (state.phase) {
    case "KING_REVEAL":
      return "Roles are dealt. Waiting for the King to reveal...";
    case "POLICE_REVEAL":
      return `${nameOf(state.kingId)} is the King. Waiting for the Police to reveal...`;
    case "TARGET":
      return `${nameOf(state.kingId)} is the King and is choosing which outlaw the Police must find...`;
    case "GUESSING":
      return `${nameOf(state.policeId)} is the Police and is looking for the ${
        state.targetRole ? ROLE_INFO[state.targetRole].title : "target"
      }...`;
    default:
      return "";
  }
}
