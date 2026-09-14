#!/usr/bin/env node
// Phone-viewport screenshots + smoke test of the *web* build (Kotlin/Wasm +
// Compose Multiplatform), driven by headless Chromium via Playwright. This is
// the cloud stand-in for an Android emulator: the container has no KVM, so the
// app is built for the browser and rendered at a phone-sized viewport instead.
//
// Usage:
//   node tool/screenshot.mjs [--levels 1,5,20] [--settings] [--tutorial] [--hint]
//                            [--stroke 0,1,2,0] [--completed 1-59] [--out shots]
//                            [--viewport 390x844] [--scale 2] [--dump] [--no-strict]
//                            [--build-dir web/build/dist/wasmJs/productionExecutable] [--port 0]
//
// Prereq: ./gradlew :web:wasmJsBrowserDistribution
//
//   --levels     level ids to open (one screenshot each); their predecessors are
//                seeded as completed so the tiles are unlocked
//   --completed  ids to seed as completed instead ("1-59", "1,2,3", "none")
//   --hint       press the hint button on every opened level
//   --stroke     dot ids to trace, in order, on the FIRST opened level (e.g. the
//                "One solution" comment in Graph.kt): exercises drawing + the win overlay
//   --tutorial   start as a first launch and screenshot the tutorial overlay
//   --settings   open the settings screen
//   --dump       print the tags the UI exposes and where they are
//
// Exit status: 1 (after writing whatever screenshots it could) if the app
// threw (an uncaught Kotlin exception, a JS error, a "captured in composition"
// error) while the script drove it, unless --no-strict. That is the CI smoke
// test contract.
//
// How it drives the UI: Compose paints to a canvas, so there is no DOM to
// query. The app registers the window bounds of its tappable parts under tags
// (see UiProbe.kt in :shared) and the web entry point exposes them as
// window.lineflowProbe.bounds(tag); the script sends real pointer events there.
//
// Fonts: text uses the bundled default face, but Compose fetches the *emoji*
// fallback font from fonts.gstatic.com at runtime. Headless Chromium cannot
// reach that host through the cloud egress proxy, so requests to it are
// answered from Node (which can) and cached under web/build/font-cache/.

process.env.NODE_USE_ENV_PROXY ??= '1'; // let Node's fetch honour HTTPS_PROXY

import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);
const { chromium } = require('playwright');

const args = parseArgs(process.argv.slice(2));
const buildDir = path.resolve(args['build-dir'] ?? 'web/build/dist/wasmJs/productionExecutable');
const fontCache = path.resolve('web/build/font-cache');
const outDir = path.resolve(args.out ?? 'shots');
const levels = parseIds(args.levels);
const stroke = parseIds(args.stroke);
const scale = Number(args.scale ?? 2);
const port = Number(args.port ?? 0);
const strict = !args['no-strict'];
const viewport = parseViewport(args.viewport);
if (args.completed === true) {
  throw new Error('--completed expects level ids ("1,3-5"), or "none"');
}
const completed = args.completed === undefined
  ? levels.map((id) => id - 1).filter((id) => id >= 1)
  : args.completed === 'none' ? [] : parseIds(args.completed);

if (!fs.existsSync(path.join(buildDir, 'index.html'))) {
  console.error(`No web build at ${buildDir}. Run: ./gradlew :web:wasmJsBrowserDistribution`);
  process.exit(2);
}
fs.mkdirSync(outDir, { recursive: true });
fs.mkdirSync(fontCache, { recursive: true });

// --------------------------------------------------------------------------
// Static server for the distribution
// --------------------------------------------------------------------------

const MIME = {
  '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript', '.css': 'text/css',
  '.json': 'application/json', '.map': 'application/json', '.wasm': 'application/wasm',
  '.png': 'image/png', '.ico': 'image/x-icon', '.svg': 'image/svg+xml', '.mp3': 'audio/mpeg',
  '.ttf': 'font/ttf', '.otf': 'font/otf', '.woff2': 'font/woff2',
};

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent(req.url.split('?')[0]);
  const file = path.join(buildDir, urlPath === '/' ? '/index.html' : urlPath);
  if (!file.startsWith(buildDir) || !fs.existsSync(file) || fs.statSync(file).isDirectory()) {
    res.writeHead(404);
    res.end('not found');
    return;
  }
  res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] ?? 'application/octet-stream' });
  fs.createReadStream(file).pipe(res);
});
await new Promise((resolve) => server.listen(port, '127.0.0.1', resolve));
const base = `http://127.0.0.1:${server.address().port}`;

// --------------------------------------------------------------------------
// Browser
// --------------------------------------------------------------------------

const browser = await chromium.launch();
const context = await browser.newContext({
  // The container's default locale is "en-US@posix", which Intl.Locale rejects
  // and Compose then throws on at boot; real browsers never report that.
  locale: 'en-US',
  viewport,
  deviceScaleFactor: scale,
  colorScheme: 'dark',
});

// Emoji fallback font: serve fonts.gstatic.com from Node, cached on disk.
await context.route('https://fonts.gstatic.com/**', async (route) => {
  const url = route.request().url();
  const cached = path.join(fontCache, crypto.createHash('sha1').update(url).digest('hex') + path.extname(new URL(url).pathname));
  try {
    if (!fs.existsSync(cached)) {
      const res = await fetch(url);
      if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
      fs.writeFileSync(cached, Buffer.from(await res.arrayBuffer()));
    }
    await route.fulfill({ body: fs.readFileSync(cached), contentType: 'font/woff2' });
  } catch (error) {
    console.warn(`font mirror: ${url}: ${error.message}`);
    await route.abort();
  }
});

// Seed preferences before the app boots (LocalStorageStore keys).
const prefs = {
  tutorial_seen: args.tutorial ? 'false' : 'true',
  music_enabled: 'false',
  vibration_enabled: 'true',
  last_played_level: String(levels[0] ?? 1),
  completed_levels: completed.join('\n'),
};
await context.addInitScript((seed) => {
  for (const [key, value] of Object.entries(seed)) {
    localStorage.setItem(`lineflow_progress.${key}`, value);
  }
}, prefs);

const page = await context.newPage();
const problems = [];
page.on('pageerror', (error) => problems.push(`pageerror: ${error.message}`));
page.on('console', (message) => {
  const text = message.text();
  if (message.type() === 'error' && !/WebGL|GL Driver|swiftshader/i.test(text)) {
    problems.push(`console.error: ${text}`);
  } else if (/Error was captured in composition|Exception|Uncaught/.test(text)) {
    problems.push(`console.${message.type()}: ${text}`);
  }
});

let shotIndex = 0;
async function snap(name) {
  shotIndex += 1;
  const file = path.join(outDir, `${String(shotIndex).padStart(2, '0')}_${name}.png`);
  await page.screenshot({ path: file });
  console.log(`wrote ${path.relative(process.cwd(), file)}`);
}

async function bounds(tag) {
  const raw = await page.evaluate(
    (t) => (typeof window.lineflowProbe?.bounds === 'function' ? window.lineflowProbe.bounds(t) : null),
    tag,
  );
  if (!raw) return null;
  const [x, y, width, height] = raw.split(',').map(Number);
  return { x, y, width, height, cx: x + width / 2, cy: y + height / 2 };
}

async function waitFor(tag, timeoutMs = 15_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const b = await bounds(tag);
    if (b && b.width > 0 && b.height > 0) return b;
    await page.waitForTimeout(100);
  }
  throw new Error(`"${tag}" did not appear within ${timeoutMs} ms`);
}

/** Scrolls the level grid until the tag is composed and inside the viewport. */
async function scrollIntoView(tag) {
  for (let attempt = 0; attempt < 60; attempt += 1) {
    const b = await bounds(tag);
    if (b && b.y >= 0 && b.y + b.height <= viewport.height) return b;
    // Off-screen tiles are not composed at all (lazy grid), so scroll blind:
    // items are numbered, and the grid opens near the last-played level.
    const direction = b ? Math.sign(b.y) : (await lazyGridDirection(tag));
    await page.mouse.move(viewport.width / 2, viewport.height * 0.6);
    await page.mouse.wheel(0, direction * viewport.height * 0.5);
    await page.waitForTimeout(250);
  }
  throw new Error(`could not scroll "${tag}" into view`);
}

async function lazyGridDirection(tag) {
  const wanted = Number(tag.replace('level-', ''));
  const tags = (await page.evaluate(() => window.lineflowProbe?.tags() ?? '')).split(',');
  const visible = tags.filter((t) => t.startsWith('level-')).map((t) => Number(t.replace('level-', '')));
  if (visible.length === 0) return 1;
  return wanted > Math.max(...visible) ? 1 : -1;
}

async function tap(tag) {
  const b = await waitFor(tag);
  await page.mouse.click(b.cx, b.cy);
}

/** Traces one continuous stroke through the given dots (mouse down, moves, up). */
async function trace(nodeIds) {
  const centres = [];
  for (const id of nodeIds) {
    const b = await waitFor(`node-${id}`);
    centres.push([b.cx, b.cy]);
  }
  await page.mouse.move(centres[0][0], centres[0][1]);
  await page.mouse.down();
  for (let i = 1; i < centres.length; i += 1) {
    const [x0, y0] = centres[i - 1];
    const [x1, y1] = centres[i];
    const steps = 12;
    for (let s = 1; s <= steps; s += 1) {
      await page.mouse.move(x0 + ((x1 - x0) * s) / steps, y0 + ((y1 - y0) * s) / steps);
      await page.waitForTimeout(16);
    }
  }
  await page.mouse.up();
}

async function settle(ms = 700) {
  await page.waitForTimeout(ms); // screen transitions are 300 ms; overlays take longer
}

async function dump() {
  const tags = (await page.evaluate(() => window.lineflowProbe?.tags() ?? '')).split(',').filter(Boolean);
  for (const tag of tags) {
    const b = await bounds(tag);
    console.log(`  ${tag.padEnd(16)} x=${b.x.toFixed(0)} y=${b.y.toFixed(0)} w=${b.width.toFixed(0)} h=${b.height.toFixed(0)}`);
  }
}

// --------------------------------------------------------------------------
// The walk
// --------------------------------------------------------------------------

try {
  await page.goto(`${base}/`);
  await waitFor(args.tutorial ? 'tutorial' : 'settings', 60_000); // Wasm boot + first frame
  await settle(1500);

  if (args.tutorial) {
    await snap('tutorial');
    await tap('tutorial');
    await waitFor('settings');
    await settle();
  }
  await snap('home');
  if (args.dump) await dump();

  for (const [index, level] of levels.entries()) {
    await scrollIntoView(`level-${level}`);
    await tap(`level-${level}`);
    await waitFor('hint');
    await waitFor('node-0');
    await settle();
    await snap(`level-${level}`);
    if (args.dump) await dump();

    if (args.hint) {
      await tap('hint');
      await settle();
      await snap(`level-${level}-hint`);
    }
    if (index === 0 && stroke.length > 1) {
      await trace(stroke);
      await settle(1800); // win overlay appears 600 ms after the last line, then animates in
      await snap(`level-${level}-stroke`);
      if (await bounds('back-to-levels')) {
        await tap('back-to-levels');
      } else {
        await tap('back');
      }
    } else {
      await tap('back');
    }
    await waitFor('settings');
    await settle();
  }

  if (args.settings) {
    await tap('settings');
    await waitFor('music');
    await settle();
    await snap('settings');
    if (args.dump) await dump();
    await tap('back');
    await waitFor('settings');
  }
} catch (error) {
  problems.push(`driver: ${error.message}`);
  await snap('failure').catch(() => {});
} finally {
  await browser.close();
  server.close();
}

if (problems.length > 0) {
  console.error(`\n${problems.length} problem(s):`);
  for (const problem of problems) console.error(`  - ${problem}`);
  process.exit(strict ? 1 : 0);
}
console.log('\nsmoke test passed');

// --------------------------------------------------------------------------

function parseArgs(argv) {
  const out = {};
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (!arg.startsWith('--')) continue;
    const eq = arg.indexOf('=');
    const key = eq === -1 ? arg.slice(2) : arg.slice(2, eq);
    const next = argv[i + 1];
    if (eq !== -1) {
      out[key] = arg.slice(eq + 1); // --flag=value
    } else if (next !== undefined && !next.startsWith('--')) {
      out[key] = next; // --flag value
      i += 1;
    } else {
      out[key] = true; // bare --flag
    }
  }
  return out;
}

function parseIds(value) {
  if (!value || value === true) return [];
  const ids = [];
  for (const part of String(value).split(',')) {
    const range = part.trim().match(/^(\d+)-(\d+)$/);
    if (range) {
      for (let id = Number(range[1]); id <= Number(range[2]); id += 1) ids.push(id);
    } else if (part.trim()) {
      ids.push(Number(part));
    }
  }
  return ids;
}

function parseViewport(value) {
  const match = String(value ?? '390x844').match(/^(\d+)x(\d+)$/);
  if (!match) throw new Error(`--viewport expects WIDTHxHEIGHT, got "${value}"`);
  return { width: Number(match[1]), height: Number(match[2]) };
}
