import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface AuthContextType {
  token: string | null;
  userEmail: string | null;
  login: (token: string, email: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem('gridmind_token'));
  const [userEmail, setUserEmail] = useState<string | null>(localStorage.getItem('gridmind_email'));

  const login = (newToken: string, email: string) => {
    localStorage.setItem('gridmind_token', newToken);
    localStorage.setItem('gridmind_email', email);
    setToken(newToken);
    setUserEmail(email);
  };

  const logout = () => {
    localStorage.removeItem('gridmind_token');
    localStorage.removeItem('gridmind_email');
    setToken(null);
    setUserEmail(null);
  };

  useEffect(() => {
    const storedToken = localStorage.getItem('gridmind_token');
    if (storedToken) setToken(storedToken);
  }, []);

  return (
    <AuthContext.Provider value={{ token, userEmail, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
