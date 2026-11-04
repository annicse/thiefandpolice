import { useState } from "react";
import type { FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { api } from "../api";
import { useSessionStore } from "../store";

export default function Home() {
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
      <h1 className="text-center text-3xl font-bold">Thief and Police</h1>

      <form onSubmit={handleCreate} className="space-y-3 rounded-xl bg-slate-800 p-4">
        <h2 className="text-lg font-semibold">New game (Host)</h2>

        <div className="flex gap-2">
          {[10, 20].map((n) => (
            <button
              key={n}
              type="button"
              onClick={() => setTotalRounds(n)}
              className={`flex-1 rounded-lg py-2 font-medium ${
                totalRounds === n ? "bg-emerald-500 text-slate-900" : "bg-slate-700"
              }`}
            >
              {n} rounds
            </button>
          ))}
        </div>

        <button
          type="submit"
          disabled={createMutation.isPending}
          className="w-full rounded-lg bg-emerald-500 py-2 font-semibold text-slate-900 disabled:opacity-50"
        >
          {createMutation.isPending ? "Creating..." : "Create game"}
        </button>

        {createMutation.error && (
          <p className="text-sm text-red-400">{createMutation.error.message}</p>
        )}
      </form>

      <form onSubmit={handleJoin} className="space-y-3 rounded-xl bg-slate-800 p-4">
        <h2 className="text-lg font-semibold">Join a game (Player)</h2>

        <input
          value={joinCode}
          onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
          maxLength={4}
          placeholder="4-letter code"
          className="w-full rounded-lg bg-slate-700 px-3 py-2 uppercase tracking-widest"
        />
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          maxLength={20}
          placeholder="Your name"
          className="w-full rounded-lg bg-slate-700 px-3 py-2"
        />

        <button
          type="submit"
          disabled={joinMutation.isPending || joinCode.trim().length !== 4 || !name.trim()}
          className="w-full rounded-lg bg-sky-500 py-2 font-semibold text-slate-900 disabled:opacity-50"
        >
          {joinMutation.isPending ? "Joining..." : "Join"}
        </button>

        {joinMutation.error && (
          <p className="text-sm text-red-400">{joinMutation.error.message}</p>
        )}
      </form>
    </div>
  );
}
