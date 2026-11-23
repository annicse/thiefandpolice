import type { GameState, PlayerView } from "./api";
import { formatMoney } from "./money";
import { ROLE_INFO, isOutlaw } from "./roles";

// সবচেয়ে বেশি টাকা যাদের, তারা সবাই বিজয়ী (সমান হলে একাধিক)
export function getWinners(players: PlayerView[]) {
  const topMoney = Math.max(...players.map((p) => p.money));
  return { topMoney, winners: players.filter((p) => p.money === topMoney) };
}

export function joinNames(names: string[]): string {
  return new Intl.ListFormat("en", { style: "long", type: "conjunction" }).format(names);
}

// RESULT-এ police-এর বাছাই ঠিক ছিল কি না, সেটা বুঝিয়ে এক বাক্যে (RoundSummary আর announcement দুজনেই ব্যবহার করে)
export function roundOutcome(state: GameState): { correct: boolean; text: string } {
  const guessed = state.players.find((p) => p.id === state.guessedId);
  // role RESULT-এর আগে null থাকে; এই ফাংশন শুধু RESULT-এ ডাকা হয়
  if (!guessed?.role) return { correct: false, text: "" };

  const title = ROLE_INFO[guessed.role].title;
  if (isOutlaw(guessed.role)) {
    return { correct: true, text: `Correct! The Police caught the ${title}.` };
  }

  const outlaws = state.players.filter((p) => p.role && isOutlaw(p.role));
  const names = joinNames(outlaws.map((p) => p.name));
  const extra = guessed.role === "VILLAGER" ? " The Police pays a fine to the Villager." : "";
  return {
    correct: false,
    text: `Wrong! ${guessed.name} is the ${title}.${extra} The outlaw${outlaws.length === 1 ? " was" : "s were"} ${names}.`,
  };
}

// স্ক্রিন রিডারের aria-live ঘোষণা। শুধু সবার জানা (public) তথ্য: নিজের role কখনো এখানে নেই।
// সার্ভার RESULT-এর আগে role পাঠায়ই না, তাই এই ফাংশন গোপন কিছু জানতেও পারে না।
export function announcement(state: GameState): string {
  const name = (id: string | null) => state.players.find((p) => p.id === id)?.name ?? "A player";

  switch (state.phase) {
    case "LOBBY":
      return `${state.players.length} players have joined. ${state.minPlayers} to ${state.maxPlayers} players can play.`;
    case "KING_REVEAL":
      return `Round ${state.currentRound} of ${state.totalRounds}. Roles are dealt. Waiting for the King to reveal.`;
    case "POLICE_REVEAL":
      return `${name(state.kingId)} is the King. Waiting for the Police to reveal.`;
    case "GUESSING":
      return `${name(state.policeId)} is the Police and is choosing the outlaw.`;
    case "RESULT": {
      const police = state.players.find((p) => p.id === state.policeId);
      const guessed = state.players.find((p) => p.id === state.guessedId);
      if (!police || !guessed) return "Round result is in.";
      return `Round result. ${police.name} picked ${guessed.name}. ${roundOutcome(state)}`;
    }
    case "GAME_OVER": {
      const { winners, topMoney } = getWinners(state.players);
      return `Game over. ${joinNames(winners.map((p) => p.name))} won with ${formatMoney(topMoney)}.`;
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
