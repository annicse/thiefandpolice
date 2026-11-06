import type { GameState, PlayerView } from "./api";

// সবচেয়ে বেশি স্কোর যাদের, তারা সবাই বিজয়ী (সমান হলে একাধিক)
export function getWinners(players: PlayerView[]) {
  const topScore = Math.max(...players.map((p) => p.score));
  return { topScore, winners: players.filter((p) => p.score === topScore) };
}

export function joinNames(names: string[]): string {
  return new Intl.ListFormat("en", { style: "long", type: "conjunction" }).format(names);
}

// স্ক্রিন রিডারের aria-live ঘোষণা। শুধু সবার জানা (public) তথ্য: নিজের role কখনো এখানে নেই।
// সার্ভার RESULT-এর আগে role পাঠায়ই না, তাই এই ফাংশন গোপন কিছু জানতেও পারে না।
export function announcement(state: GameState): string {
  const name = (id: string | null) => state.players.find((p) => p.id === id)?.name ?? "A player";

  switch (state.phase) {
    case "LOBBY":
      return `${state.players.length} of ${state.playersNeeded} players have joined.`;
    case "KING_REVEAL":
      return `Round ${state.currentRound} of ${state.totalRounds}. Roles are dealt. Waiting for the King to reveal.`;
    case "POLICE_REVEAL":
      return `${name(state.kingId)} is the King. Waiting for the Police to reveal.`;
    case "GUESSING":
      return `${name(state.policeId)} is the Police and is choosing the Thief.`;
    case "RESULT": {
      const police = state.players.find((p) => p.id === state.policeId);
      const guessed = state.players.find((p) => p.id === state.guessedId);
      if (!police || !guessed) return "Round result is in.";
      const thief = state.players.find((p) => p.role === "THIEF");
      const outcome =
        guessed.role === "THIEF"
          ? "Correct. The Police caught the Thief."
          : `Wrong. The Thief was ${thief?.name ?? "someone else"}.`;
      return `Round result. ${police.name} picked ${guessed.name}. ${outcome}`;
    }
    case "GAME_OVER": {
      const { winners, topScore } = getWinners(state.players);
      return `Game over. ${joinNames(winners.map((p) => p.name))} won with ${topScore} points.`;
    }
  }
}

// প্রতিটা phase-এর দৃশ্যমান শিরোনাম (<h2>), phase বদলালে এখানে ফোকাস যায়
export function phaseHeading(state: GameState): string {
  switch (state.phase) {
    case "LOBBY":
      return "Lobby";
    case "KING_REVEAL":
      return "King reveal";
    case "POLICE_REVEAL":
      return "Police reveal";
    case "GUESSING":
      return "Guessing";
    case "RESULT":
      return "Round result";
    case "GAME_OVER":
      return "Game over";
  }
}
