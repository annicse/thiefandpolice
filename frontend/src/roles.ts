import type { Role } from "./api";

type RoleInfo = {
  title: string;
  hint: string;
  cardClass: string;
  outlaw: boolean;
};

// outlaw-দের কার্ড একই ধরনের; hint-এর loot শুধু দেখানোর জন্য (আসল হিসাব backend-এর Role.java-তে)
function outlawInfo(title: string, loot: string, cardClass: string): RoleInfo {
  return {
    title,
    hint: `You are an outlaw! Stay hidden. You keep your loot of ${loot} unless the Police picks you.`,
    cardClass,
    outlaw: true,
  };
}

// প্রতিটা role স্ক্রিনে কীভাবে দেখাবে। নতুন role যোগ করলে এখানে এক লাইন বাড়াতে হবে।
// Record<Role, ...> লেখায় কোনো role বাদ গেলে TypeScript নিজেই এরর দেয়।
export const ROLE_INFO: Record<Role, RoleInfo> = {
  KING: {
    title: "King",
    hint: 'You earn $1,000. Reveal yourself, then ask aloud: "Who is my police?"',
    cardClass: "bg-amber-400 text-slate-900",
    outlaw: false,
  },
  POLICE: {
    title: "Police",
    hint: "Reveal yourself when the King asks. Then find an outlaw and take their loot. Wrong guess: $0. Picking the innocent Villager costs you a fine.",
    cardClass: "bg-sky-400 text-slate-900",
    outlaw: false,
  },
  VILLAGER: {
    title: "Villager",
    hint: "You are innocent and earn $200. If the Police wrongly picks you, you also get a fine from the Police.",
    cardClass: "bg-emerald-400 text-slate-900",
    outlaw: false,
  },
  THIEF: outlawInfo("Thief", "$500", "bg-rose-400 text-slate-900"),
  BURGLAR: outlawInfo("Burglar", "$700", "bg-orange-400 text-slate-900"),
  SMUGGLER: outlawInfo("Smuggler", "$800", "bg-fuchsia-400 text-slate-900"),
  PICKPOCKET: outlawInfo("Pickpocket", "$400", "bg-pink-400 text-slate-900"),
  GANGSTER: outlawInfo("Gangster", "$900", "bg-violet-400 text-slate-900"),
  DAKAT: outlawInfo("Dakat", "$1,000", "bg-red-400 text-slate-900"),
  KIDNAPPER: outlawInfo("Kidnapper", "$1,100", "bg-yellow-400 text-slate-900"),
};

export function isOutlaw(role: Role): boolean {
  return ROLE_INFO[role].outlaw;
}
