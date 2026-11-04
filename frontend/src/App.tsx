import { Navigate, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import GamePage from "./pages/GamePage";

export default function App() {
  return (
    <main className="min-h-screen bg-slate-900 text-white">
      <div className="mx-auto max-w-md px-4">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/game/:code" element={<GamePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </main>
  );
}
