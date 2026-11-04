// ডামি প্লেয়ার (bot): একটা চলমান গেমে ঢুকে নিজে নিজে খেলে।
// ব্যবহার:  node scripts/bots.mjs CODE [কতজন, ডিফল্ট 3]
//
// bot যা করে: king হলে reveal-king, police হলে reveal-police, তারপর random একজনকে thief বলে guess।
// bot যা করে না: start আর next-round। ওগুলো Host (তুমি) চাপবে।

const BASE = process.env.API ?? "http://localhost:8080/api";
const [codeArg, countArg] = process.argv.slice(2);

if (!codeArg) {
  console.log("Usage: node scripts/bots.mjs CODE [count=3]");
  process.exit(1);
}

const code = codeArg.toUpperCase();
const count = Number(countArg ?? 3);
const NAMES = ["Bot-Rafi", "Bot-Karim", "Bot-Sumi", "Bot-Tania", "Bot-Jamal"];

async function call(path, { method = "GET", token, body } = {}) {
  const headers = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers["X-Token"] = token;

  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const data = await res.json().catch(() => ({}));
    throw new Error(data.message ?? `HTTP ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}

async function act(bot) {
  try {
    const s = await call(`/games/${code}/state`, { token: bot.token });

    if (s.myRole === "KING" && s.phase === "KING_REVEAL") {
      await call(`/games/${code}/reveal-king`, { method: "POST", token: bot.token });
      console.log(`${bot.name} (KING) revealed`);
    } else if (s.myRole === "POLICE" && s.phase === "POLICE_REVEAL") {
      await call(`/games/${code}/reveal-police`, { method: "POST", token: bot.token });
      console.log(`${bot.name} (POLICE) revealed`);
    } else if (s.myRole === "POLICE" && s.phase === "GUESSING") {
      const others = s.players.filter((p) => p.id !== bot.id);
      const pick = others[Math.floor(Math.random() * others.length)];
      await call(`/games/${code}/guess`, {
        method: "POST",
        token: bot.token,
        body: { playerId: pick.id },
      });
      console.log(`${bot.name} (POLICE) guessed ${pick.name}`);
    }
  } catch (e) {
    // একই সময়ে দুইবার চেষ্টা বা ভুল phase হলে 409 আসতে পারে, এগুলো নিরীহ
    if (!String(e.message).includes("phase")) console.log(`${bot.name}: ${e.message}`);
  }
}

async function main() {
  const bots = [];
  for (let i = 0; i < count; i++) {
    const joined = await call(`/games/${code}/join`, {
      method: "POST",
      body: { name: NAMES[i] },
    });
    bots.push({ name: joined.name, id: joined.playerId, token: joined.token });
    console.log(`joined: ${joined.name}`);
  }

  console.log("Bots are running. Stop with Ctrl+C.");
  setInterval(() => bots.forEach(act), 1500);
}

main().catch((e) => {
  console.error("Failed:", e.message);
  process.exit(1);
});
