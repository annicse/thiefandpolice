import type { Role } from "./api";

// প্রতিটা role স্ক্রিনে কীভাবে দেখাবে। নতুন role (যেমন DAKAT) যোগ করলে এখানে এক লাইন বাড়াতে হবে।
// Record<Role, ...> লেখায় কোনো role বাদ গেলে TypeScript নিজেই এরর দেয়।
// (পয়েন্টগুলো শুধু দেখানোর জন্য, আসল হিসাব backend-এর Role.java-তে)
export const ROLE_INFO: Record<Role, { title: string; hint: string; cardClass: string }> = {
  KING: {
    title: "King",
    hint: 'You earn 1000 points. Reveal yourself, then ask aloud: "Who is my police?"',
    cardClass: "bg-amber-400 text-slate-900",
  },
  QUEEN: {
    title: "Queen",
    hint: "You earn 800 points. Stay quiet and watch the Police.",
    cardClass: "bg-fuchsia-400 text-slate-900",
  },
  POLICE: {
    title: "Police",
    hint: "Reveal yourself when the King asks. Then find the Thief. Right guess: 500 points. Wrong guess: 0.",
    cardClass: "bg-sky-400 text-slate-900",
  },
  THIEF: {
    title: "Thief",
    hint: "Stay hidden! If the Police picks the wrong person, you get 500 points.",
    cardClass: "bg-rose-400 text-slate-900",
  },
};
