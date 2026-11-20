// খেলার টাকা দেখানোর একটাই জায়গা। মুদ্রা বদলাতে শুধু এখানে বদলালেই চলবে।
const formatter = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD",
  maximumFractionDigits: 0,
});

export function formatMoney(amount: number): string {
  return formatter.format(amount);
}
