import type { GameState } from "../api";

// RESULT-এর বার্তা: police কাকে বেছেছিল, ঠিক নাকি ভুল। player আর host দুজনেই এটা দেখে।
export default function RoundSummary({ state }: { state: GameState }) {
  const find = (id: string | null) => state.players.find((p) => p.id === id);

  const police = find(state.policeId);
  const guessed = find(state.guessedId);
  const thief = state.players.find((p) => p.role === "THIEF");

  if (!police || !guessed) return null;

  // role RESULT-এ সবার জানা, তাই ঠিক/ভুল এখান থেকেই বোঝা যায় (আলাদা ফিল্ড লাগেনি)
  const correct = guessed.role === "THIEF";

  return (
    <div
      className={`space-y-1 rounded-xl p-4 ${correct ? "bg-emerald-900/60" : "bg-rose-900/60"}`}
    >
      <p className="font-semibold">
        {police.name} picked {guessed.name}.
      </p>
      <p>
        {correct
          ? "Correct! The Police caught the Thief."
          : `Wrong! The Thief was ${thief?.name ?? "someone else"}.`}
      </p>
    </div>
  );
}
