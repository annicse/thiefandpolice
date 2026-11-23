import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../api";
import type { GameState } from "../api";
import type { Session } from "../store";

type Props = {
  state: GameState;
  session: Session;
};

// host-এর বাটন: RESULT-এ "Next round" / "Finish game"। host-only স্ক্রিন আর খেলোয়াড়-host দুজনেই এটা ব্যবহার করে।
// (সার্ভার নিজেই যাচাই করে শুধু host পারবে, তাই এখানে লুকিয়ে রাখা শুধু সুবিধার জন্য)
export default function HostControls({ state, session }: Props) {
  const queryClient = useQueryClient();

  const nextRound = useMutation({
    mutationFn: () => api.nextRound(session.code, session.token),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["state", session.code] }),
  });

  if (state.phase !== "RESULT") return null;

  const isLastRound = state.currentRound >= state.totalRounds;

  return (
    <section aria-label="Host controls" className="space-y-2 rounded-xl bg-slate-800 p-4">
      <h3 className="font-semibold">Host controls</h3>
      <button
        onClick={() => nextRound.mutate()}
        disabled={nextRound.isPending}
        className="min-h-11 w-full rounded-lg bg-emerald-500 py-3 font-semibold text-slate-900 disabled:opacity-50"
      >
        {nextRound.isPending ? "Please wait..." : isLastRound ? "Finish game" : "Next round"}
      </button>
      {nextRound.error && (
        <p role="alert" className="text-sm text-red-300">
          {nextRound.error.message}
        </p>
      )}
    </section>
  );
}
