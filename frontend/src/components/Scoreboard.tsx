import type { PlayerView } from "../api";
import { ROLE_INFO } from "../roles";

type Props = {
  players: PlayerView[];
  meId?: string | null; // নিজের সারিতে "You" দেখানোর জন্য (host-এর ক্ষেত্রে দরকার নেই)
  sortByScore?: boolean; // শেষে বেশি স্কোর উপরে
  showRoles?: boolean; // চূড়ান্ত তালিকায় শেষ রাউন্ডের role দেখানোর দরকার নেই
};

// নাম + স্কোর (+ role, যদি সার্ভার পাঠায়)। role সার্ভার শুধু RESULT/GAME_OVER-এ পাঠায়,
// তাই আগের phase-গুলোতে role ব্যাজ নিজে থেকেই দেখা যায় না।
export default function Scoreboard({
  players,
  meId = null,
  sortByScore = false,
  showRoles = true,
}: Props) {
  // [...players] দিয়ে কপি করে সাজাই, কারণ props-এর আসল অ্যারে বদলানো ঠিক না
  const rows = sortByScore ? [...players].sort((a, b) => b.score - a.score) : players;

  return (
    <ul className="space-y-2">
      {rows.map((p) => (
        <li
          key={p.id}
          className="flex items-center justify-between rounded-lg bg-slate-800 px-3 py-3"
        >
          <div className="flex items-center gap-2">
            <span>{p.name}</span>
            {p.id === meId && (
              <span className="text-xs font-semibold text-emerald-400">You</span>
            )}
            {showRoles && p.role && (
              <span
                className={`rounded px-2 py-0.5 text-xs font-semibold ${ROLE_INFO[p.role].cardClass}`}
              >
                {ROLE_INFO[p.role].title}
              </span>
            )}
          </div>
          <span className="font-mono text-lg">{p.score}</span>
        </li>
      ))}
    </ul>
  );
}
