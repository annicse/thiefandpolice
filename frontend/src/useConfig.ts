import { useQuery } from "@tanstack/react-query";
import { api } from "./api";

// Game numbers (round limits, loot amounts...) come from the backend's GameConfig,
// so they are defined in one place only. The result is cached for the whole session.
export function useConfig() {
  return useQuery({
    queryKey: ["config"],
    queryFn: () => api.getConfig(),
    staleTime: Infinity,
    // The server may be asleep (free hosting): keep asking until it answers
    refetchInterval: (query) => (query.state.data ? false : 3000),
  });
}
