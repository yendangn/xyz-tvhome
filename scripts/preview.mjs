// Makes themes/themes/<id>/preview.webp, the 768×432 picture of a theme in the app's theme panel.
//
//   node scripts/preview.mjs <id> [<id>…]
//   node scripts/preview.mjs --all
//
// Headless Chrome draws the page at 1920×1080 with sample prices in place of the app's (window.TV is stubbed),
// then sharp scales it down. Set CHROME=/path/to/chrome when Chrome is not in the usual place.
import { execFile } from 'node:child_process';
import { mkdtemp, readdir, readFile, rm, stat, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { promisify } from 'node:util';
import sharp from 'sharp';
import { fillOwner, owner } from './site.mjs';

const THEMES = join(dirname(fileURLToPath(import.meta.url)), '..', 'themes', 'themes');
const CHROME = process.env.CHROME || {
  darwin: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  win32: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
}[process.platform] || 'google-chrome';

process.on('uncaughtException', e => {
  console.error(`preview: ${e.message}`);
  process.exit(1);
});

// What the app's Bridge would answer; prices as PriceBoard.toJson() writes them.
const STUB = `<script>window.TV = {
  prices: function () { var n = Date.now(); return JSON.stringify({ sjc: { buy: 139000000, sell: 142000000, t: n },
    ring: { buy: 138000000, sell: 141000000, t: n }, e10: { price: 28250, t: n }, e5: { price: 27700, t: n },
    'do': { price: 29120, t: n } }); },
  online: function () { return true; },
  launch: function () {}, openSettings: function () {}, openThemes: function () {}, openApps: function () {}
};</script>`;

async function shoot(id, tmp) {
  const dir = join(THEMES, id);
  const html = fillOwner(await readFile(join(dir, 'index.html'), 'utf8'), await owner());
  const core = /<script src="[^"]*core\.js"><\/script>/;
  if (!core.test(html)) throw new Error(`${id}/index.html does not load core.js`);
  // A copy outside themes/, pointed back at the theme's folder, so themes/ is never touched. Transitions are off: the shot
  // could otherwise land halfway through the focused tile's change of colour.
  const page = html
    .replace('<head>', `<head><base href="${pathToFileURL(dir).href}/"><style>*{transition:none!important}</style>`)
    .replace(core, m => STUB + m);
  const src = join(tmp, `${id}.html`);
  const png = join(tmp, `${id}.png`);
  await writeFile(src, page);
  await promisify(execFile)(CHROME, [
    '--headless=new', '--hide-scrollbars', '--window-size=1920,1080',
    // Lets fonts load and the clock tick before the shot.
    '--virtual-time-budget=4000', '--allow-file-access-from-files',
    `--user-data-dir=${join(tmp, 'profile')}`, `--screenshot=${png}`, pathToFileURL(src).href,
  ]);
  const out = join(dir, 'preview.webp');
  await sharp(png).resize(768, 432).webp({ quality: 80, effort: 6 }).toFile(out);
  console.log(`${id}: ${(await stat(out)).size} bytes -> themes/themes/${id}/preview.webp`);
}

const argv = process.argv.slice(2);
const all = (await readdir(THEMES, { withFileTypes: true })).filter(e => e.isDirectory()).map(e => e.name).sort();
const ids = argv.includes('--all') ? all : argv;
if (!ids.length) throw new Error('usage: node scripts/preview.mjs <id> [<id>…] | --all');
const unknown = ids.filter(id => !all.includes(id));
if (unknown.length) throw new Error(`unknown theme(s): ${unknown.join(', ')}; there are: ${all.join(', ')}`);

const tmp = await mkdtemp(join(tmpdir(), 'retro-preview-'));
try {
  for (const id of ids) await shoot(id, tmp);
} finally {
  await rm(tmp, { recursive: true, force: true });
}
