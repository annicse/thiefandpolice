import type { GameState } from "../api";
import { roundOutcome } from "../announcements";

// RESULT-এর বার্তা: police কাকে বেছেছিল, ঠিক নাকি ভুল। player আর host দুজনেই এটা দেখে।
export default function RoundSummary({ state }: { state: GameState }) {
  const find = (id: string | null) => state.players.find((p) => p.id === id);

  const police = find(state.policeId);
  const guessed = find(state.guessedId);

  if (!police || !guessed) return null;

  // role RESULT-এ সবার জানা, তাই ঠিক/ভুল এখান থেকেই বোঝা যায় (আলাদা ফিল্ড লাগেনি)
  const { correct, text } = roundOutcome(state);

  return (
    <div
      className={`space-y-1 rounded-xl p-4 ${correct ? "bg-emerald-900/60" : "bg-rose-900/60"}`}
    >
      <p className="font-semibold">
        {police.name} picked {guessed.name}.
      </p>
      <p>{text}</p>
    </div>
  );
}
