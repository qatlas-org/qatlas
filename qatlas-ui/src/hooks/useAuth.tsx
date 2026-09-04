import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { api } from '../api/client';
import { getToken, setToken, clearToken, isTokenValid } from '../lib/auth';

interface AuthContextValue {
  isAdmin: boolean;
  username: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const initialToken = getToken();
  const initialValid = isTokenValid(initialToken);
  const [isAdmin, setIsAdmin] = useState(initialValid);
  const [username, setUsername] = useState<string | null>(initialValid ? decodeUsername(initialToken) : null);

  const login = useCallback(async (user: string, password: string) => {
    const res = await api.auth.login(user, password);
    setToken(res.token);
    setIsAdmin(true);
    setUsername(res.username);
  }, []);

  const logout = useCallback(() => {
    clearToken();
    setIsAdmin(false);
    setUsername(null);
  }, []);

  return (
    <AuthContext.Provider value={{ isAdmin, username, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider');
  return ctx;
}

function decodeUsername(token: string | null): string | null {
  if (!token) return null;
  try {
    const [encodedPayload] = token.split('.');
    const payload = atob(encodedPayload.replace(/-/g, '+').replace(/_/g, '/'));
    const [user] = payload.split('|');
    return user;
  } catch {
    return null;
  }
}
