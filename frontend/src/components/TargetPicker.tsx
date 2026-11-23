import { useState } from "react";
import type { Role } from "../api";
import { formatMoney } from "../money";
import { ROLE_INFO } from "../roles";
import { useConfig } from "../useConfig";

type Props = {
  outlawRoles: Role[]; // the outlaw roles in play (public)
  pending: boolean;
  onConfirm: (role: Role) => void;
};

// The King picks which outlaw role the Police must find. The King does not know who holds it.
export default function TargetPicker({ outlawRoles, pending, onConfirm }: Props) {
  const { data: config } = useConfig();
  const [selected, setSelected] = useState<Role | null>(null);

  return (
    <div className="space-y-2">
      <h3 className="font-semibold">Which outlaw must the Police find?</h3>
      <ul className="space-y-2">
        {outlawRoles.map((role) => (
          <li key={role}>
            <button
              onClick={() => setSelected(role)}
              aria-pressed={selected === role}
              className={`flex min-h-11 w-full items-center justify-between gap-2 rounded-lg px-3 py-3 text-left ${
                selected === role ? "bg-amber-600 text-white" : "bg-slate-800"
              }`}
            >
              <span>{ROLE_INFO[role].title}</span>
              <span className="flex items-center gap-2 text-sm">
                {config && <span className="font-mono">{formatMoney(config.payouts[role])}</span>}
                {/* text as well as colour, so the selection never depends on colour alone */}
                {selected === role && <span className="font-semibold">Selected</span>}
              </span>
            </button>
          </li>
        ))}
      </ul>
      <button
        onClick={() => selected && onConfirm(selected)}
        disabled={!selected || pending}
        className="w-full rounded-lg bg-emerald-500 py-3 font-semibold text-slate-900 disabled:opacity-50"
      >
        {pending ? "Please wait..." : selected ? `Target: ${ROLE_INFO[selected].title}` : "Pick an outlaw"}
      </button>
    </div>
  );
}
