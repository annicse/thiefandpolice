import type { PlayerView } from "../api";
import { formatMoney } from "../money";
import { rankPlayers } from "../ranking";
import { ROLE_INFO } from "../roles";

type Props = {
  players: PlayerView[];
  meId?: string | null; // নিজের সারিতে "You" দেখানোর জন্য (host-এর ক্ষেত্রে দরকার নেই)
  sortByMoney?: boolean; // বেশি টাকা উপরে, সাথে position (#1, #2...)
  showRoles?: boolean; // চূড়ান্ত তালিকায় শেষ রাউন্ডের role দেখানোর দরকার নেই
};

// নাম + টাকা (+ role, যদি সার্ভার পাঠায়)। role সার্ভার শুধু RESULT/GAME_OVER-এ পাঠায়,
// তাই আগের phase-গুলোতে role ব্যাজ নিজে থেকেই দেখা যায় না।
export default function Scoreboard({
  players,
  meId = null,
  sortByMoney = false,
  showRoles = true,
}: Props) {
  const rows = sortByMoney
    ? rankPlayers(players)
    : players.map((player) => ({ player, rank: null }));

  return (
    <ol aria-label="Leaderboard" className="space-y-2">
      {rows.map(({ player: p, rank }) => (
        <li
          key={p.id}
          className="flex items-center justify-between gap-2 rounded-lg bg-slate-800 px-3 py-3"
        >
          <div className="flex min-w-0 flex-wrap items-center gap-2">
            {rank !== null && (
              <span className="w-8 font-mono font-semibold text-slate-300">#{rank}</span>
            )}
            <span className="break-words">{p.name}</span>
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
          <span className="font-mono text-lg">{formatMoney(p.money)}</span>
        </li>
      ))}
    </ol>
  );
}
