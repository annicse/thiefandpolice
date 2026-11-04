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
  setSession: (session: Session) => void;
  clearSession: () => void;
};

export const useSessionStore = create<SessionStore>()(
  persist(
    (set) => ({
      session: null,
      setSession: (session) => set({ session }),
      clearSession: () => set({ session: null }),
    }),
    {
      name: "thiefandpolice-session",
      // sessionStorage: প্রতিটা ব্রাউজার ট্যাবের নিজস্ব সেশন, তাই এক ল্যাপটপে অনেক "প্লেয়ার" টেস্ট করা যায়
      storage: createJSONStorage(() => sessionStorage),
    },
  ),
);
