#!/usr/bin/env node
// Analisis hasil OAT dari JTL CSV JMeter -> statistik per sampler.
// Pemakaian: oat/analyze.js results/<skenario>.jtl
const fs = require("fs");
const path = process.argv[2] || "results/oat02-login.jtl";

const total = new Map(), okn = new Map(), errn = new Map(),
      lat = new Map(), errcode = new Map();
const bump = (m, k, d = 1) => m.set(k, (m.get(k) || 0) + d);

const lines = fs.readFileSync(path, "utf8").split("\n");
const header = lines[0].split(",");
const idx = Object.fromEntries(header.map((h, i) => [h, i]));
for (const line of lines.slice(1)) {
  // Baris data CSV JMeter selalu diawali timeStamp (13 digit); baris lanjutan
  // assertion (failed message multiline) diabaikan.
  if (!/^17\d{11},/.test(line)) continue;
  // CSV sederhana: pecah di koma di luar tanda kutip (label boleh memuat koma dalam kutip).
  const c = [];
  let cur = "", inQ = false;
  for (const ch of line) {
    if (ch === '"') { inQ = !inQ; continue; }
    if (ch === "," && !inQ) { c.push(cur); cur = ""; continue; }
    cur += ch;
  }
  c.push(cur);
  const label = c[idx.label] ?? "(unknown)";
  const code = c[idx.responseCode] ?? "-";
  const ok = c[idx.success] === "true";
  const el = parseInt(c[idx.elapsed], 10);
  bump(total, label);
  if (ok) { bump(okn, label); if (!lat.has(label)) lat.set(label, []); lat.get(label).push(el); }
  else { bump(errn, label); bump(errcode.get(label) ?? errcode.set(label, new Map()).get(label), code); }
}

console.log("== Ringkasan per sampler ==");
for (const label of [...total.keys()].sort()) {
  const t = total.get(label), o = okn.get(label) || 0, e = errn.get(label) || 0;
  let extra = "";
  const ls = lat.get(label);
  if (ls && ls.length) {
    const s = [...ls].sort((a, b) => a - b);
    const mean = Math.round(ls.reduce((a, b) => a + b, 0) / ls.length);
    const q = (p) => s[Math.min(s.length - 1, Math.floor(s.length * p))];
    extra = `  mean=${mean} p50=${q(0.5)} p95=${q(0.95)} p99=${q(0.99)} max=${s[s.length - 1]}`;
  }
  console.log(
    `${label.padEnd(42)} total=${String(t).padEnd(6)} ok=${String(o).padEnd(6)} err=${String(e).padEnd(6)} (${((100 * e) / t).toFixed(2)}%)${extra}`
  );
}
console.log();
console.log("== Rincian error per kode ==");
for (const label of [...errcode.keys()].sort()) {
  for (const [code, count] of [...errcode.get(label)].sort((a, b) => b[1] - a[1])) {
    console.log(`${String(count).padStart(6)}  ${label.padEnd(42)} HTTP ${code}`);
  }
}
