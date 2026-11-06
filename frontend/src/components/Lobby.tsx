import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../api";
import type { GameState } from "../api";
import type { Session } from "../store";

type Props = {
  state: GameState;
  session: Session;
};

export default function Lobby({ state, session }: Props) {
  const queryClient = useQueryClient();

  const startMutation = useMutation({
    mutationFn: () => api.startGame(session.code, session.token),
    // start হওয়ার সাথে সাথে state আবার আনো, পরের polling-এর জন্য অপেক্ষা না করে
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["state", session.code] }),
  });

  // সবসময় playersNeeded সংখ্যক ঘর দেখাই: ভরা ঘরে নাম, খালি ঘরে "Waiting..."
  const slots = Array.from({ length: state.playersNeeded }, (_, i) => state.players[i] ?? null);
  const missing = state.playersNeeded - state.players.length;
  const isFull = missing <= 0;

  return (
    <div className="space-y-4">
      <p className="text-slate-300">
        {session.isHost
          ? "Share this code with the players. Everyone joins from their own phone."
          : "You are in! Waiting for everyone to join."}
      </p>

      <p className="text-sm text-slate-400">
        {state.totalRounds} rounds · {state.players.length}/{state.playersNeeded} players
      </p>

      <ul aria-label="Players" className="space-y-2">
        {slots.map((player, i) => (
          <li
            key={i}
            className={`flex items-center justify-between rounded-lg px-3 py-3 ${
              player
                ? "bg-slate-800"
                : "border border-dashed border-slate-500 text-slate-300"
            }`}
          >
            <span>{player ? player.name : "Waiting for player..."}</span>
            {player && state.me?.id === player.id && (
              <span className="text-xs font-semibold text-emerald-400">You</span>
            )}
          </li>
        ))}
      </ul>

      {session.isHost ? (
        <div className="space-y-2">
          <button
            onClick={() => startMutation.mutate()}
            disabled={!isFull || startMutation.isPending}
            className="w-full rounded-lg bg-emerald-500 py-3 font-semibold text-slate-900 disabled:opacity-50"
          >
            {startMutation.isPending
              ? "Starting..."
              : isFull
                ? "Start game"
                : `Waiting for ${missing} more player${missing === 1 ? "" : "s"}...`}
          </button>
          {startMutation.error && (
            <p role="alert" className="text-sm text-red-300">
              {startMutation.error.message}
            </p>
          )}
        </div>
      ) : (
        <p className="text-center text-slate-400">Waiting for the host to start the game...</p>
      )}
    </div>
  );
}
