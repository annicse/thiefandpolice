import { create } from "zustand";
import { createJSONStorage, persist } from "zustand/middleware";

export type Session = {
  code: string;
  token: string; // host হলে hostToken, player হলে নিজের token
  name: string;
  isHost: boolean;
};

type SessionStore = {
  session: Session | null;
  // কোন রাউন্ডে কার্ড উল্টানো হয়েছে। রাউন্ড বদলালে মান মেলে না, তাই কার্ড নিজে থেকে আবার উল্টে যায়
  flippedRound: number | null;
  setSession: (session: Session) => void;
  setFlippedRound: (round: number | null) => void;
  clearSession: () => void;
};

export const useSessionStore = create<SessionStore>()(
  persist(
    (set) => ({
      session: null,
      flippedRound: null,
      setSession: (session) => set({ session, flippedRound: null }),
      setFlippedRound: (flippedRound) => set({ flippedRound }),
      clearSession: () => set({ session: null, flippedRound: null }),
    }),
    {
      name: "thiefandpolice-session",
      // sessionStorage: প্রতিটা ব্রাউজার ট্যাবের নিজস্ব সেশন, তাই এক ল্যাপটপে অনেক "প্লেয়ার" টেস্ট করা যায়
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
