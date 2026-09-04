// The backend stores attachments under ${app.data.root-path}/attachments and
// exposes that folder directly as a Spring static resource location — so
// files are reachable at GET {backendOrigin}/attachments/<relative-path>,
// with no dedicated controller or separate file server involved.
//
// The API returns attachmentRelativePath already prefixed with "../attachments/"
// (a relative-link trick that works for the classic UI, which is served from
// inside the same Spring app under /classic/). We don't share that serving
// context, so we normalize it into a clean absolute path here instead.
export function attachmentUrl(relativePath?: string): string | undefined {
  if (!relativePath) return undefined;
  const cleaned = relativePath.replace(/^(\.\.\/)+/, '').replace(/^\/+/, '');
  const base = import.meta.env.VITE_API_BASE_URL ?? '';
  return `${base}/${cleaned}`;
}
