// The person Home greets, shared by build-web.mjs and preview.mjs. Themes mark the name with <span data-owner>…</span>;
// it comes from themes/site.json's "owner" unless a build names someone else (--owner, Gradle's -Powner).
import { readFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const SITE = join(dirname(fileURLToPath(import.meta.url)), '..', 'themes', 'site.json');

/** [override] when given, else site.json's owner; empty (no name) when neither has one. */
export async function owner(override) {
  if (override != null && override.trim()) return override.trim();
  try {
    const name = JSON.parse(await readFile(SITE, 'utf8')).owner;
    return typeof name === 'string' ? name.trim() : '';
  } catch {
    return '';
  }
}

const ESCAPE = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' };

/**
 * The page with every <span data-owner>…</span> holding [name]. With no name the span goes, and so does the comma or
 * space before it, so "Xin chào, <span data-owner>…</span>" reads "Xin chào".
 */
export function fillOwner(html, name) {
  if (!name) return html.replace(/,?\s*<span data-owner>[^<]*<\/span>/g, '');
  const safe = name.replace(/[&<>"]/g, c => ESCAPE[c]);
  return html.replace(/(<span data-owner>)[^<]*(<\/span>)/g, (_, open, close) => open + safe + close);
}
