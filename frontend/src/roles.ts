import type { GameConfig, Role } from "./api";
import { formatMoney } from "./money";

type RoleInfo = {
  title: string;
  cardClass: string;
  outlaw: boolean;
};

function outlawInfo(title: string, cardClass: string): RoleInfo {
  return { title, cardClass, outlaw: true };
}

// How each role looks on screen. To add a role, add one line here.
// Record<Role, ...> makes TypeScript complain if a role is missing.
export const ROLE_INFO: Record<Role, RoleInfo> = {
  KING: { title: "King", cardClass: "bg-amber-400 text-slate-900", outlaw: false },
  POLICE: { title: "Police", cardClass: "bg-sky-400 text-slate-900", outlaw: false },
  VILLAGER: { title: "Villager", cardClass: "bg-emerald-400 text-slate-900", outlaw: false },
  THIEF: outlawInfo("Thief", "bg-rose-400 text-slate-900"),
  BURGLAR: outlawInfo("Burglar", "bg-orange-400 text-slate-900"),
  SMUGGLER: outlawInfo("Smuggler", "bg-fuchsia-400 text-slate-900"),
  PICKPOCKET: outlawInfo("Pickpocket", "bg-pink-400 text-slate-900"),
  GANGSTER: outlawInfo("Gangster", "bg-violet-400 text-slate-900"),
  DAKAT: outlawInfo("Dakat", "bg-red-400 text-slate-900"),
  KIDNAPPER: outlawInfo("Kidnapper", "bg-yellow-400 text-slate-900"),
  TOKAI: outlawInfo("Tokai", "bg-lime-400 text-slate-900"),
  DALAL: outlawInfo("Dalal", "bg-teal-400 text-slate-900"),
  JAAL_NOTE: outlawInfo("Jaal Note", "bg-cyan-400 text-slate-900"),
  CHANDABAZ: outlawInfo("Chandabaz", "bg-indigo-400 text-slate-900"),
  MASTAN: outlawInfo("Mastan", "bg-stone-300 text-slate-900"),
  HIJACKER: outlawInfo("Hijacker", "bg-purple-400 text-slate-900"),
  HITMAN: outlawInfo("Hitman", "bg-blue-400 text-slate-900"),
  DON: outlawInfo("Don", "bg-amber-600 text-white"),
};

export function isOutlaw(role: Role): boolean {
  return ROLE_INFO[role].outlaw;
}

// The hint under the role card. All amounts come from the backend config.
export function roleHint(role: Role, config: GameConfig): string {
  const amount = formatMoney(config.payouts[role]);
  const fine = formatMoney(config.villagerFine);

  switch (role) {
    case "KING":
      return `You earn ${amount}. Reveal yourself, then ask aloud: "Who is my police?"`;
    case "POLICE":
      return `Reveal yourself when the King asks. Then find an outlaw and take their loot. Wrong guess: no money. Picking the innocent Villager costs you a fine of up to ${fine}.`;
    case "VILLAGER":
      return `You are innocent and earn ${amount}. If the Police wrongly picks you, you also get a fine of up to ${fine} from the Police.`;
    default:
      return `You are an outlaw! Stay hidden. You keep your loot of ${amount} unless the Police picks you.`;
  }
}
