import type { Role } from "../api";
import { ROLE_INFO } from "../roles";

type Props = {
  role: Role;
  flipped: boolean;
  onToggle: () => void;
};

// চিঠির মতো কার্ড: উল্টানো থাকলে পিঠ, চাপলে role দেখায়, আবার চাপলে লুকায়
export default function RoleCard({ role, flipped, onToggle }: Props) {
  const info = ROLE_INFO[role];

  return (
    <button
      onClick={onToggle}
      className={`w-full rounded-2xl p-6 text-center shadow-lg ${
        flipped ? info.cardClass : "bg-slate-700"
      }`}
    >
      {flipped ? (
        <>
          <p className="text-sm uppercase tracking-widest opacity-70">Your role</p>
          <p className="mt-1 text-4xl font-extrabold">{info.title}</p>
          <p className="mt-3 text-sm">{info.hint}</p>
          <p className="mt-4 text-xs opacity-60">Tap to hide</p>
        </>
      ) : (
        <>
          <p className="text-xl font-semibold">Tap to see your role</p>
          <p className="mt-2 text-sm text-slate-400">Make sure nobody is looking</p>
        </>
      )}
    </button>
  );
}
