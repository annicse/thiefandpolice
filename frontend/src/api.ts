// backend-এর GameDtos.java-র সাথে মিলিয়ে লেখা টাইপ

export type Phase =
  | "LOBBY"
  | "KING_REVEAL"
  | "POLICE_REVEAL"
  | "GUESSING"
  | "RESULT"
  | "GAME_OVER";

export type Role = "KING" | "QUEEN" | "POLICE" | "THIEF";

export type PlayerView = {
  id: string;
  name: string;
  score: number;
  role: Role | null; // RESULT/GAME_OVER-এর আগে সবসময় null
};

export type GameState = {
  code: string;
  phase: Phase;
  currentRound: number;
  totalRounds: number;
  playersNeeded: number;
  players: PlayerView[];
  viewerType: "HOST" | "PLAYER";
  me: PlayerView | null; // হোস্টের ক্ষেত্রে null
  myRole: Role | null; // শুধু নিজের role
  kingId: string | null;
  policeId: string | null;
};

export type CreateGameResponse = { code: string; hostToken: string };
export type JoinResponse = {
  code: string;
  playerId: string;
  name: string;
  token: string;
};

// backend যখন 4xx/5xx দেয়, তখন এই error ছোড়া হয় (message backend থেকে আসে)
export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

type RequestOptions = {
  method?: "GET" | "POST";
  token?: string;
  body?: unknown;
};

// সব API call এই একটা ফাংশন দিয়ে যায়
async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.body !== undefined) headers["Content-Type"] = "application/json";
  if (options.token) headers["X-Token"] = options.token;

  const res = await fetch(`/api${path}`, {
    method: options.method ?? "GET",
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (!res.ok) {
    let message = `Request failed (${res.status})`;
    try {
      const data = await res.json();
      if (data.message) message = data.message;
    } catch {
      // body JSON না হলে ডিফল্ট message-ই থাকবে
    }
    throw new ApiError(message, res.status);
  }

  // 204 No Content-এ কোনো body নেই
  if (res.status === 204) return undefined as T;
  return res.json();
}

export const api = {
  createGame: (totalRounds: number) =>
    request<CreateGameResponse>("/games", { method: "POST", body: { totalRounds } }),

  joinGame: (code: string, name: string) =>
    request<JoinResponse>(`/games/${code}/join`, { method: "POST", body: { name } }),

  getState: (code: string, token: string) =>
    request<GameState>(`/games/${code}/state`, { token }),

  startGame: (code: string, token: string) =>
    request<void>(`/games/${code}/start`, { method: "POST", token }),

  revealKing: (code: string, token: string) =>
    request<void>(`/games/${code}/reveal-king`, { method: "POST", token }),

  revealPolice: (code: string, token: string) =>
    request<void>(`/games/${code}/reveal-police`, { method: "POST", token }),

  guess: (code: string, token: string, playerId: string) =>
    request<void>(`/games/${code}/guess`, {
      method: "POST",
      token,
      body: { playerId },
    }),

  nextRound: (code: string, token: string) =>
    request<void>(`/games/${code}/next-round`, { method: "POST", token }),
};
