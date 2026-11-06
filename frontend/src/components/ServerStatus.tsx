import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../api";

// Home পেজ খুললেই /api/health ডেকে ফ্রি-টিয়ার সার্ভারকে আগেভাগে জাগায়।
// দ্রুত সাড়া এলে কিছুই দেখায় না; দেরি হলে "waking up" বার্তা; অনেকবার ফেল করলে Retry বাটন।
export default function ServerStatus() {
  const health = useQuery({
    queryKey: ["health"],
    queryFn: api.health,
    retry: 15, // ~৫০ সেকেন্ডের জাগার সময় ঢাকতে যথেষ্ট
    retryDelay: 4000,
    staleTime: 5 * 60 * 1000,
    refetchOnWindowFocus: false,
  });

  // সাড়া দ্রুত এলে বার্তার ঝলক এড়াতে ১.৫ সেকেন্ড পর দেখাই
  const [slow, setSlow] = useState(false);
  useEffect(() => {
    const timer = setTimeout(() => setSlow(true), 1500);
    return () => clearTimeout(timer);
  }, []);

  if (health.isSuccess) return null;

  if (health.isError) {
    return (
      <div role="alert" className="space-y-2 rounded-xl bg-rose-950 p-4 text-rose-100">
        <p>The server did not respond. It may still be starting up.</p>
        <button
          onClick={() => health.refetch()}
          className="min-h-11 rounded-lg bg-rose-200 px-4 font-semibold text-slate-900"
        >
          Try again
        </button>
      </div>
    );
  }

  if (!slow) return null;

  return (
    <output className="block rounded-xl bg-slate-800 p-4 text-slate-200">
      Waking up the server. This can take up to a minute on the free hosting tier. You can fill in
      the form meanwhile.
    </output>
  );
}
