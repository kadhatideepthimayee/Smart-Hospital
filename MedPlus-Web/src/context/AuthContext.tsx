import React, { createContext, useState, useEffect, useContext } from 'react';
import axiosInstance from '../api/axiosInstance';
import { User, UserRole } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<User>;
  register: (fullName: string, email: string, phone: string, password: string, role: UserRole) => Promise<User>;
  googleSignIn: (idToken: string, role?: UserRole) => Promise<User>;
  logout: () => void;
  updateProfileState: (updatedUser: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('medplus_token'));
  const [loading, setLoading] = useState(true);

  // Sync state if interceptor logs out the user
  useEffect(() => {
    const handleInterceptorLogout = () => {
      setUser(null);
      setToken(null);
    };
    window.addEventListener('auth_logout', handleInterceptorLogout);
    return () => {
      window.removeEventListener('auth_logout', handleInterceptorLogout);
    };
  }, []);

  // Auto-fetch profile if token is present on startup
  useEffect(() => {
    const fetchMe = async () => {
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        const response = await axiosInstance.get('/auth/me');
        setUser(response.data);
      } catch (err) {
        console.error('Failed to auto-authenticate user', err);
        logout();
      } finally {
        setLoading(false);
      }
    };

    fetchMe();
  }, [token]);

  const login = async (email: string, password: string): Promise<User> => {
    const response = await axiosInstance.post('/auth/login', { email, password });
    const { token: newToken, user: newUser } = response.data;
    
    localStorage.setItem('medplus_token', newToken);
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const register = async (
    fullName: string,
    email: string,
    phone: string,
    password: string,
    role: UserRole
  ): Promise<User> => {
    const response = await axiosInstance.post('/auth/register', {
      fullName,
      email,
      phone,
      password,
      role,
    });
    const { token: newToken, user: newUser } = response.data;
    
    localStorage.setItem('medplus_token', newToken);
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const googleSignIn = async (idToken: string, role?: UserRole): Promise<User> => {
    const response = await axiosInstance.post('/auth/google', { idToken, role });
    const { token: newToken, user: newUser } = response.data;
    
    localStorage.setItem('medplus_token', newToken);
    setToken(newToken);
    setUser(newUser);
    return newUser;
  };

  const logout = () => {
    localStorage.removeItem('medplus_token');
    setToken(null);
    setUser(null);
  };

  const updateProfileState = (updatedUser: Partial<User>) => {
    setUser((prev) => (prev ? { ...prev, ...updatedUser } : null));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        register,
        googleSignIn,
        logout,
        updateProfileState,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
