const TOKEN_KEY = 'qatlas_admin_token';

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  sessionStorage.removeItem(TOKEN_KEY);
}

/** Decodes the token's expiry client-side (no network call) so the UI can
 * proactively show "logged out" instead of waiting for a 401. */
export function isTokenValid(token: string | null): boolean {
  if (!token || !token.includes('.')) return false;
  try {
    const [encodedPayload] = token.split('.');
    const payload = atob(encodedPayload.replace(/-/g, '+').replace(/_/g, '/'));
    const [, expiryStr] = payload.split('|');
    const expiry = Number(expiryStr);
    return Number.isFinite(expiry) && Date.now() < expiry;
  } catch {
    return false;
  }
}
