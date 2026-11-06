import { useState } from "react";
import type { FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import ServerStatus from "../components/ServerStatus";
import { useSessionStore } from "../store";
import { useDocumentTitle } from "../useDocumentTitle";

export default function Home() {
  useDocumentTitle("Start or join a game");
  const navigate = useNavigate();
  const setSession = useSessionStore((s) => s.setSession);

  const [totalRounds, setTotalRounds] = useState(10);
  const [joinCode, setJoinCode] = useState("");
  const [name, setName] = useState("");

  const createMutation = useMutation({
    mutationFn: () => api.createGame(totalRounds),
    onSuccess: (data) => {
      setSession({ code: data.code, token: data.hostToken, name: "Host", isHost: true });
      navigate(`/game/${data.code}`);
    },
  });

  const joinMutation = useMutation({
    mutationFn: () => api.joinGame(joinCode.trim().toUpperCase(), name.trim()),
    onSuccess: (data) => {
      setSession({ code: data.code, token: data.token, name: data.name, isHost: false });
      navigate(`/game/${data.code}`);
    },
  });

  function handleCreate(e: FormEvent) {
    e.preventDefault();
    createMutation.mutate();
  }

  function handleJoin(e: FormEvent) {
    e.preventDefault();
    joinMutation.mutate();
  }

  return (
    <div className="space-y-6 py-6">
      <h1 className="text-center text-3xl font-bold">Start or join a game</h1>

      <ServerStatus />

      <form onSubmit={handleCreate} className="space-y-3 rounded-xl bg-slate-800 p-4">
        <h2 className="text-lg font-semibold">New game (Host)</h2>

        <fieldset className="flex gap-2">
          <legend className="sr-only">Number of rounds</legend>
          {[10, 20].map((n) => (
            <button
              key={n}
              type="button"
              aria-pressed={totalRounds === n}
              onClick={() => setTotalRounds(n)}
              className={`min-h-11 flex-1 rounded-lg font-medium ${
                totalRounds === n ? "bg-emerald-500 text-slate-900" : "bg-slate-700"
              }`}
            >
              {n} rounds
            </button>
          ))}
        </fieldset>

        <button
          type="submit"
          disabled={createMutation.isPending}
          className="min-h-11 w-full rounded-lg bg-emerald-500 font-semibold text-slate-900 disabled:opacity-50"
        >
          {createMutation.isPending ? "Creating..." : "Create game"}
        </button>

        {createMutation.error && (
          <p role="alert" className="text-sm text-red-300">
            {createMutation.error.message}
          </p>
        )}
      </form>

      <form onSubmit={handleJoin} className="space-y-3 rounded-xl bg-slate-800 p-4">
        <h2 className="text-lg font-semibold">Join a game (Player)</h2>

        <div className="space-y-1">
          <label htmlFor="join-code" className="block text-sm text-slate-300">
            Game code
          </label>
          <input
            id="join-code"
            value={joinCode}
            onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
            maxLength={4}
            autoComplete="off"
            autoCapitalize="characters"
            spellCheck={false}
            placeholder="4-letter code"
            className="min-h-11 w-full rounded-lg bg-slate-700 px-3 uppercase tracking-widest placeholder:text-slate-300"
          />
        </div>

        <div className="space-y-1">
          <label htmlFor="join-name" className="block text-sm text-slate-300">
            Your name
          </label>
          <input
            id="join-name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            maxLength={20}
            autoComplete="off"
            placeholder="Your name"
            className="min-h-11 w-full rounded-lg bg-slate-700 px-3 placeholder:text-slate-300"
          />
        </div>

        <button
          type="submit"
          disabled={joinMutation.isPending || joinCode.trim().length !== 4 || !name.trim()}
          className="min-h-11 w-full rounded-lg bg-sky-500 font-semibold text-slate-900 disabled:opacity-50"
        >
          {joinMutation.isPending ? "Joining..." : "Join"}
        </button>

        {joinMutation.error && (
          <p role="alert" className="text-sm text-red-300">
            {joinMutation.error.message}
          </p>
        )}
      </form>
    </div>
  );
}
