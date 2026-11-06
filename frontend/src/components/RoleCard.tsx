import { useState } from "react";
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
  // স্ক্রিন রিডারের জন্য ছোট ঘোষণা। শুধু ব্যবহারকারী নিজে চাপলেই বসে,
  // তাই পেজ লোডে বা রাউন্ড বদলে নিজে থেকে role জোরে পড়া হয় না।
  const [announcement, setAnnouncement] = useState("");

  function handleToggle() {
    setAnnouncement(flipped ? "Your role is hidden." : `Your role is ${info.title}. ${info.hint}`);
    onToggle();
  }

  return (
    <>
      <button
        onClick={handleToggle}
        className={`min-h-11 w-full rounded-2xl p-6 text-center shadow-lg ${
          flipped ? info.cardClass : "bg-slate-700"
        }`}
      >
        {flipped ? (
          <>
            <span className="block text-sm font-semibold uppercase tracking-widest">
              Your role
            </span>
            <span className="mt-1 block text-4xl font-extrabold">{info.title}</span>
            <span className="mt-3 block text-sm">{info.hint}</span>
            <span className="mt-4 block text-sm font-medium">Tap to hide</span>
          </>
        ) : (
          <>
            <span className="block text-xl font-semibold">Tap to see your role</span>
            <span className="mt-2 block text-sm text-slate-300">Make sure nobody is looking</span>
          </>
        )}
      </button>
      <div aria-live="polite" className="sr-only">
        {announcement}
      </div>
    </>
  );
}
