import type { GameState } from "../api";
import { formatMoney } from "../money";
import { rankPlayers } from "../ranking";

// স্ক্রিনের নিচে সবসময় লেগে থাকা বার (মোবাইলে সুবিধার জন্য): খেলোয়াড় দেখে নিজের position আর টাকা,
// host (যে খেলে না) দেখে এখন কে এগিয়ে আছে। শুধু public তথ্য ব্যবহার হয়।
export default function StandingsBar({ state }: { state: GameState }) {
  const ranked = rankPlayers(state.players);
  const mine = ranked.find((r) => r.player.id === state.me?.id);
  const leader = ranked[0];

  if (!leader) return null;

  const label = mine ? `You · #${mine.rank} of ${ranked.length}` : `Leader · ${leader.player.name}`;
  const money = formatMoney((mine ?? leader).player.money);

  return (
    <aside
      aria-label="Your standing"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-slate-600 bg-slate-800 pb-[env(safe-area-inset-bottom)]"
    >
      <div className="mx-auto flex w-full max-w-md items-center justify-between gap-3 px-4 py-3">
        <span className="min-w-0 break-words font-semibold">{label}</span>
        <span className="font-mono text-lg">{money}</span>
      </div>
    </aside>
  );
}
