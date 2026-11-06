import { useRef } from "react";
import type { MouseEvent } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import GamePage from "./pages/GamePage";

export default function App() {
  const mainRef = useRef<HTMLElement>(null);

  // Skip link: URL-এ hash না বসিয়ে সরাসরি <main>-এ ফোকাস সরায় (React Router-এর সাথে ঝামেলা এড়াতে)
  function skipToMain(e: MouseEvent<HTMLAnchorElement>) {
    e.preventDefault();
    mainRef.current?.focus();
  }

  return (
    <div className="flex min-h-screen flex-col bg-slate-900 text-white">
      <a
        href="#main"
        onClick={skipToMain}
        className="sr-only focus:not-sr-only focus:fixed focus:left-2 focus:top-2 focus:z-50 focus:rounded-lg focus:bg-white focus:px-4 focus:py-3 focus:font-semibold focus:text-slate-900"
      >
        Skip to main content
      </a>

      <header className="mx-auto w-full max-w-md px-4 pt-4">
        <p className="text-sm font-semibold tracking-wide text-slate-300">Thief and Police</p>
      </header>

      <main
        id="main"
        ref={mainRef}
        tabIndex={-1}
        className="mx-auto w-full max-w-md flex-1 px-4 outline-none"
      >
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/game/:code" element={<GamePage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <footer className="mx-auto w-full max-w-md px-4 pb-6 pt-2 text-sm text-slate-400">
        A digital version of Raja-Rani-Chor-Police.
      </footer>
    </div>
  );
}
