import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../api";
import type { GameState } from "../api";
import { useSessionStore } from "../store";
import type { Session } from "../store";
import RoleCard from "./RoleCard";

type Props = {
  state: GameState;
  session: Session;
};

// খেলোয়াড়ের রাউন্ড স্ক্রিন: KING_REVEAL, POLICE_REVEAL আর GUESSING phase-এর জন্য
export default function PlayerRound({ state, session }: Props) {
  const queryClient = useQueryClient();
  const flippedRound = useSessionStore((s) => s.flippedRound);
  const setFlippedRound = useSessionStore((s) => s.setFlippedRound);
  const [accusedId, setAccusedId] = useState<string | null>(null);

  // কোনো বাটন সফল হলে সাথে সাথে state আবার আনো
  const refresh = () =>
    queryClient.invalidateQueries({ queryKey: ["state", session.code] });

  const revealKing = useMutation({
    mutationFn: () => api.revealKing(session.code, session.token),
    onSuccess: refresh,
  });
  const revealPolice = useMutation({
    mutationFn: () => api.revealPolice(session.code, session.token),
    onSuccess: refresh,
  });
  const guess = useMutation({
    mutationFn: (playerId: string) => api.guess(session.code, session.token, playerId),
    onSuccess: refresh,
  });

  const role = state.myRole;
  if (!role) return <p className="text-slate-400">Waiting for your role...</p>;

  const flipped = flippedRound === state.currentRound;
  const nameOf = (id: string | null) =>
    state.players.find((p) => p.id === id)?.name ?? "?";
  const error = revealKing.error ?? revealPolice.error ?? guess.error;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between text-sm text-slate-400">
        <span>
          Round {state.currentRound} of {state.totalRounds}
        </span>
        <span>Your score: {state.me?.score ?? 0}</span>
      </div>

      <RoleCard
        role={role}
        flipped={flipped}
        onToggle={() => setFlippedRound(flipped ? null : state.currentRound)}
      />

      {(state.kingId || state.policeId) && (
        <div className="space-y-1 rounded-xl bg-slate-800 p-3 text-sm">
          {state.kingId && (
            <p>
              King: <b>{nameOf(state.kingId)}</b>
            </p>
          )}
          {state.policeId && (
            <p>
              Police: <b>{nameOf(state.policeId)}</b>
            </p>
          )}
        </div>
      )}

      {state.phase === "KING_REVEAL" &&
        (role === "KING" ? (
          <ActionButton
            label="Reveal yourself as King"
            pending={revealKing.isPending}
            onClick={() => revealKing.mutate()}
          />
        ) : (
          <Waiting text="Waiting for the King to reveal..." />
        ))}

      {state.phase === "POLICE_REVEAL" &&
        (role === "POLICE" ? (
          <div className="space-y-2">
            <p className="text-center text-sm text-slate-400">
              Tap after the King asks: &quot;Who is my police?&quot;
            </p>
            <ActionButton
              label="Reveal yourself as Police"
              pending={revealPolice.isPending}
              onClick={() => revealPolice.mutate()}
            />
          </div>
        ) : (
          <Waiting text="The King asks: Who is my police?" />
        ))}

      {state.phase === "GUESSING" &&
        (role === "POLICE" ? (
          <div className="space-y-2">
            <h3 className="font-semibold">Who is the Thief?</h3>
            <ul className="space-y-2">
              {state.players
                .filter((p) => p.id !== state.me?.id)
                .map((p) => (
                  <li key={p.id}>
                    <button
                      onClick={() => setAccusedId(p.id)}
                      aria-pressed={accusedId === p.id}
                      className={`flex w-full items-center justify-between rounded-lg px-3 py-3 text-left ${
                        accusedId === p.id ? "bg-rose-700 text-white" : "bg-slate-800"
                      }`}
                    >
                      <span className="break-words">{p.name}</span>
                      {/* রঙের পাশাপাশি লেখা: নির্বাচিত বোঝাতে শুধু রঙের ওপর নির্ভর করি না */}
                      {accusedId === p.id && <span className="text-sm font-semibold">Selected</span>}
                    </button>
                  </li>
                ))}
            </ul>
            <ActionButton
              label={accusedId ? `Accuse ${nameOf(accusedId)}` : "Pick a player"}
              pending={guess.isPending}
              disabled={!accusedId}
              onClick={() => accusedId && guess.mutate(accusedId)}
            />
          </div>
        ) : (
          <Waiting text="The Police is choosing the Thief..." />
        ))}

      {error && (
        <p role="alert" className="text-sm text-red-300">
          {error.message}
        </p>
      )}
    </div>
  );
}

type ActionButtonProps = {
  label: string;
  pending: boolean;
  disabled?: boolean;
  onClick: () => void;
};

function ActionButton({ label, pending, disabled = false, onClick }: ActionButtonProps) {
  return (
    <button
      onClick={onClick}
      disabled={pending || disabled}
      className="w-full rounded-lg bg-emerald-500 py-3 font-semibold text-slate-900 disabled:opacity-50"
    >
      {pending ? "Please wait..." : label}
    </button>
  );
}

function Waiting({ text }: { text: string }) {
  return <p className="rounded-xl bg-slate-800 p-3 text-center text-slate-400">{text}</p>;
}
