// Starts a theme: themes/themes/<id>/ with a working page to restyle, made from the "modern" theme.
//
//   node scripts/new-theme.mjs <id> "<Tên hiển thị>"
//
// The new theme stands alone: its style.css holds a copy of shared/modern.css plus modern's colours, so changing it
// never touches the modern themes. It gets the panel style of "modern" and the next free order (last + 10).
// Then: restyle index.html and style.css, set "panel" in theme.json, and run scripts/preview.mjs <id>.
import { mkdir, readdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const WEB = join(dirname(fileURLToPath(import.meta.url)), '..', 'themes');
const BASE = 'modern';

process.on('uncaughtException', e => {
  console.error(`new-theme: ${e.message}`);
  process.exit(1);
});

const [id, name] = process.argv.slice(2);
if (!id || !name) throw new Error('usage: node scripts/new-theme.mjs <id> "<Tên hiển thị>"');
if (!/^[a-z0-9][a-z0-9-]*$/.test(id)) throw new Error(`id "${id}": use lowercase letters, digits and "-"`);

const themes = (await readdir(join(WEB, 'themes'), { withFileTypes: true })).filter(e => e.isDirectory()).map(e => e.name);
if (themes.includes(id)) throw new Error(`themes/themes/${id} already exists`);
let order = 0;
for (const t of themes) order = Math.max(order, JSON.parse(await readFile(join(WEB, 'themes', t, 'theme.json'), 'utf8')).order);

const base = join(WEB, 'themes', BASE);
const shared = await readFile(join(WEB, 'shared', 'modern.css'), 'utf8');
const html = (await readFile(join(base, 'index.html'), 'utf8'))
  .replace(/<link rel="stylesheet" href="\.\.\/\.\.\/shared\/modern\.css">\n/, '')
  .replace(/<title>.*?<\/title>/, `<title>${name}</title>`);
const css = `/* ${name}: started from the "${BASE}" theme (scripts/new-theme.mjs); restyle freely. */\n`
  + shared.replace(/^\/\*.*?\*\/\n/, '').replace(/url\('\.\.\/fonts\//g, "url('../../fonts/")
  + '\n' + await readFile(join(base, 'style.css'), 'utf8');
const meta = JSON.parse(await readFile(join(base, 'theme.json'), 'utf8'));

const dir = join(WEB, 'themes', id);
await mkdir(dir);
await writeFile(join(dir, 'index.html'), html);
await writeFile(join(dir, 'style.css'), css);
await writeFile(join(dir, 'theme.json'), JSON.stringify({ name, order: order + 10, panel: meta.panel }, null, 2) + '\n');
console.log(`themes/themes/${id}: index.html, style.css, theme.json (order ${order + 10})
next: restyle them, then  node scripts/preview.mjs ${id}  and  app/gradlew -p app assembleRelease -Pthemes=${id}`);
