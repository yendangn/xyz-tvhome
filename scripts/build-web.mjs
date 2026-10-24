// Builds the app's web assets from themes/: the chosen themes plus only the files they use.
//
//   node scripts/build-web.mjs --list
//   node scripts/build-web.mjs [--themes retro,modern] [--owner "Tên"] [--minify] [--out <dir>]
//
// No --themes builds every theme in themes/themes. The output keeps themes/'s layout (core.js, fonts/, shared/,
// themes/<id>/), so pages find their files by the same relative paths as in themes/, plus themes.json: the built
// themes in panel order, with each one's name, native panel style and music, read by the app (theme/Themes.kt).
// Gradle runs this for every build (app/build.gradle); --minify is for release builds. The pages greet --owner, or
// themes/site.json's owner (scripts/site.mjs).
import { copyFile, mkdir, readdir, readFile, rm, stat, writeFile } from 'node:fs/promises';
import { dirname, extname, join, posix, relative } from 'node:path';
import { fileURLToPath } from 'node:url';
import CleanCSS from 'clean-css';
import { minify as minifyHtml } from 'html-minifier-terser';
import { minify as minifyJs } from 'terser';
import { fillOwner, owner } from './site.mjs';

const WEB = join(dirname(fileURLToPath(import.meta.url)), '..', 'themes');

// A mistake in the input (an unknown theme, a missing file) is reported as one line, not a stack trace.
process.on('uncaughtException', e => {
  console.error(`build-web: ${e.message}`);
  process.exit(1);
});

function args(argv) {
  const a = { themes: null, owner: null, minify: false, out: 'build/web', list: false };
  for (let i = 0; i < argv.length; i++) {
    const k = argv[i];
    if (k === '--themes') a.themes = argv[++i].split(',').map(s => s.trim()).filter(Boolean);
    else if (k === '--owner') a.owner = argv[++i];
    else if (k === '--minify') a.minify = true;
    else if (k === '--out') a.out = argv[++i];
    else if (k === '--list') a.list = true;
    else throw new Error(`unknown option ${k}`);
  }
  return a;
}

async function exists(p) {
  try { await stat(p); return true; } catch { return false; }
}

/** Every theme in themes/themes: its folder name is its id. */
async function themes() {
  const out = [];
  for (const e of await readdir(join(WEB, 'themes'), { withFileTypes: true })) {
    if (!e.isDirectory()) continue;
    const meta = JSON.parse(await readFile(join(WEB, 'themes', e.name, 'theme.json'), 'utf8'));
    out.push({ id: e.name, ...meta });
  }
  return out.sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
}

// ---- Which files the chosen themes use ----

const REF = [/\b(?:src|href)\s*=\s*"([^"]+)"/g, /url\(\s*['"]?([^'")]+)['"]?\s*\)/g];

/** The local files an HTML or CSS file links to, as paths under themes/. */
function refs(path, text) {
  const out = [];
  for (const re of REF) {
    for (const m of text.matchAll(re)) {
      const ref = m[1].split(/[?#]/)[0];
      if (!ref || /^[a-z]+:/i.test(ref)) continue;
      const p = posix.normalize(posix.join(posix.dirname(path), ref));
      if (p.startsWith('..')) throw new Error(`${path} links outside themes/: ${m[1]}`);
      out.push(p);
    }
  }
  return out;
}

/** The OFL licence that travels with a font file: themes/fonts/<Family>-OFL.txt, matched on the name's first word. */
async function licence(font, licences) {
  const word = posix.basename(font).split(/[-_.]/)[0].toLowerCase();
  const hit = licences.find(l => l.toLowerCase().startsWith(word));
  return hit ? `fonts/${hit}` : null;
}

/** A theme's music ("music" in theme.json, a file beside its index.html) as a path under themes/. */
function music(t) {
  const p = posix.normalize(`themes/${t.id}/${t.music}`);
  if (!p.startsWith(`themes/${t.id}/`)) throw new Error(`themes/${t.id}/theme.json: music must be in the theme's folder`);
  return p;
}

/**
 * The page's own buttons the app relies on. The theme button is the only way into the theme panel: a page without it
 * would keep the user in that theme for good. A theme with music needs its on/off button, and one without has no use
 * for it.
 */
async function check(t) {
  const html = await readFile(join(WEB, 'themes', t.id, 'index.html'), 'utf8');
  const button = action => [...html.matchAll(/<[a-z][^>]*>/gi)]
    .some(m => m[0].includes(`data-action="${action}"`) && /\bdata-nav="(top|dock)"/.test(m[0]));
  const where = `themes/${t.id}/index.html`;
  if (!button('themes')) throw new Error(`${where} has no theme button (data-nav="top" data-action="themes"): it is the only way into the theme panel`);
  if (t.music && !button('music')) throw new Error(`${where} has no music button (data-nav="top" data-action="music") for its "music"`);
  if (!t.music && button('music')) throw new Error(`${where} has a music button but theme.json has no "music"`);
}

async function collect(chosen) {
  const files = new Set(['core.js']);
  const queue = [];
  const add = p => { if (!files.has(p)) { files.add(p); queue.push(p); } };
  for (const t of chosen) {
    add(`themes/${t.id}/index.html`);
    add(`themes/${t.id}/preview.webp`);
    for (const k of ['titleFont', 'nameFont', 'tagFont']) if (t.panel[k]) add(t.panel[k]);
    if (t.music) add(music(t));
  }
  while (queue.length) {
    const p = queue.shift();
    if (!await exists(join(WEB, p))) throw new Error(`missing themes/${p}`);
    if (['.html', '.css'].includes(extname(p))) refs(p, await readFile(join(WEB, p), 'utf8')).forEach(add);
  }
  const licences = (await readdir(join(WEB, 'fonts'))).filter(f => f.endsWith('-OFL.txt'));
  for (const p of [...files]) {
    if (p.startsWith('fonts/') && !p.endsWith('.txt')) {
      const l = await licence(p, licences);
      if (l) files.add(l);
    }
  }
  return [...files].sort();
}

// ---- Minifying (release builds) ----

// Kotlin reaches core.js only through window properties (nav, refresh, onPrices), which terser never renames.
const JS = { compress: true, mangle: true, format: { comments: false } };
// Level 1 only: it shortens values and drops whitespace but never reorders rules, so the cascade stays as written.
const CSS = { level: 1 };
const HTML = {
  collapseWhitespace: true,
  // Keep one space where there was any: text like "<span id=dayNum></span> tháng <span…" needs it.
  conservativeCollapse: true,
  removeComments: true,
  minifyCSS: CSS,
  minifyJS: JS,
};

async function minify(path, text) {
  switch (extname(path)) {
    case '.js': return (await minifyJs(text, JS)).code;
    case '.css': {
      const r = new CleanCSS(CSS).minify(text);
      if (r.errors.length) throw new Error(`${path}: ${r.errors.join('\n')}`);
      return r.styles;
    }
    case '.html': return minifyHtml(text, HTML);
    default: return null;
  }
}

// ---- Main ----

const a = args(process.argv.slice(2));
const all = await themes();
if (a.list) {
  for (const t of all) console.log(`${t.id.padEnd(14)} ${t.name}`);
  process.exit(0);
}
let chosen = all;
if (a.themes) {
  const unknown = a.themes.filter(id => !all.some(t => t.id === id));
  if (unknown.length) throw new Error(`unknown theme(s): ${unknown.join(', ')}; there are: ${all.map(t => t.id).join(', ')}`);
  chosen = all.filter(t => a.themes.includes(t.id));
}
if (!chosen.length) throw new Error('no themes to build');

for (const t of chosen) await check(t);
const files = await collect(chosen);
const name = await owner(a.owner);
await rm(a.out, { recursive: true, force: true });
let bytes = 0;
for (const p of files) {
  const to = join(a.out, p);
  await mkdir(dirname(to), { recursive: true });
  const html = extname(p) === '.html';
  if (html || (a.minify && ['.css', '.js'].includes(extname(p)))) {
    let text = await readFile(join(WEB, p), 'utf8');
    if (html) text = fillOwner(text, name);
    await writeFile(to, a.minify ? await minify(p, text) : text);
  } else {
    await copyFile(join(WEB, p), to);
  }
  bytes += (await stat(to)).size;
}
const index = chosen.map(t => ({ id: t.id, name: t.name, panel: t.panel, ...(t.music && { music: music(t) }) }));
await writeFile(join(a.out, 'themes.json'), JSON.stringify(index));
console.log(`web: ${chosen.map(t => t.id).join(', ')} ${name ? `for "${name}"` : '(no name)'}; ${files.length} files, ${bytes} bytes${a.minify ? ' (minified)' : ''} -> ${relative(process.cwd(), a.out) || '.'}`);
