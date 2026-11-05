import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../api";
import type { GameState } from "../api";
import type { Session } from "../store";
import RoundSummary from "./RoundSummary";
import Scoreboard from "./Scoreboard";

type Props = {
  state: GameState;
  session: Session;
};

// host কোনো গোপন তথ্য দেখে না: সার্ভার RESULT-এর আগে কারো role পাঠায়ই না।
// king/police reveal হয়ে গেলে তাদের নাম সবাই জানে, তাই সেটা দেখানো যায়।
export default function HostScreen({ state, session }: Props) {
  const queryClient = useQueryClient();

  const nextRound = useMutation({
    mutationFn: () => api.nextRound(session.code, session.token),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["state", session.code] }),
  });

  const nameOf = (id: string | null) =>
    state.players.find((p) => p.id === id)?.name ?? "?";
  const isLastRound = state.currentRound >= state.totalRounds;

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

      <Scoreboard players={state.players} />

      {state.phase === "RESULT" && (
        <div className="space-y-2">
          <button
            onClick={() => nextRound.mutate()}
            disabled={nextRound.isPending}
            className="w-full rounded-lg bg-emerald-500 py-3 font-semibold text-slate-900 disabled:opacity-50"
          >
            {nextRound.isPending ? "Please wait..." : isLastRound ? "Finish game" : "Next round"}
          </button>
          {nextRound.error && <p className="text-sm text-red-400">{nextRound.error.message}</p>}
        </div>
      )}
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
    case "GUESSING":
      return `${nameOf(state.policeId)} is the Police and is choosing the Thief...`;
    default:
      return "";
  }
}
