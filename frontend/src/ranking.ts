import type { PlayerView } from "./api";

export type RankedPlayer = { player: PlayerView; rank: number };

// বেশি টাকা উপরে। সমান টাকা হলে একই position (যেমন 1, 1, 3)।
export function rankPlayers(players: PlayerView[]): RankedPlayer[] {
  return [...players]
    .sort((a, b) => b.money - a.money)
    .map((player) => ({
      player,
      rank: 1 + players.filter((other) => other.money > player.money).length,
    }));
}
