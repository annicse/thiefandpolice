import { useQuery } from "@tanstack/react-query";

type Health = { status: string; game: string };

async function fetchHealth(): Promise<Health> {
  const res = await fetch("/api/health");
  if (!res.ok) throw new Error("Server error");
  return res.json();
}

export default function App() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["health"],
    queryFn: fetchHealth,
  });

  return (
    <main className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
      <div className="text-center">
        <h1 className="text-3xl font-bold">Thief and Police</h1>
        {isLoading && <p className="mt-4">Calling the server...</p>}
        {error && <p className="mt-4 text-red-400">Server not reachable</p>}
        {data && <p className="mt-4 text-green-400">Server: {data.status}</p>}
      </div>
    </main>
  );
}