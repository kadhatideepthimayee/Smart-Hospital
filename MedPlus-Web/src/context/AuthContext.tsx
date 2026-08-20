import React, { createContext, useState, useEffect, useContext } from 'react';
import { User, UserRole } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<User>;
  register: (fullName: string, email: string, phone: string, password: string, role: UserRole) => Promise<User>;
  googleSignIn: (role?: UserRole) => Promise<User>;
  logout: () => void;
  updateProfileState: (updatedUser: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:5000/api';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Load user profile on app startup if token and uid are in local storage
  useEffect(() => {
    const loadStoredUser = async () => {
      const storedToken = localStorage.getItem('medplus_token');
      const storedUid = localStorage.getItem('medplus_uid');
      
      if (storedToken && storedUid) {
        try {
          const res = await fetch(`${API_BASE_URL}/auth/profile/${storedUid}`, {
            headers: {
              'Authorization': `Bearer ${storedToken}`
            }
          });
          if (res.ok) {
            const userData = await res.json();
            setUser({
              uid: userData.uid,
              fullName: userData.fullName,
              email: userData.email,
              phone: userData.phone,
              role: userData.role as UserRole,
              profileImage: userData.profileImage || ''
            });
            setToken(storedToken);
          } else {
            // Token expired or invalid
            localStorage.removeItem('medplus_token');
            localStorage.removeItem('medplus_uid');
          }
        } catch (err) {
          console.error('Error fetching stored profile from local backend:', err);
        }
      }
      setLoading(false);
    };

    loadStoredUser();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    const res = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!res.ok) {
      const errData = await res.json();
      throw new Error(errData.error || 'Login failed.');
    }

    const data = await res.json();
    const profileUser: User = {
      uid: data.uid,
      fullName: data.fullName,
      email: data.email,
      phone: data.phone,
      role: data.role as UserRole,
      profileImage: data.profileImage || ''
    };

    setToken(data.token);
    setUser(profileUser);
    localStorage.setItem('medplus_token', data.token);
    localStorage.setItem('medplus_uid', data.uid);

    return profileUser;
  };

  const register = async (
    fullName: string,
    email: string,
    phone: string,
    password: string,
    role: UserRole
  ): Promise<User> => {
    const res = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, email, phone, password, role })
    });

    if (!res.ok) {
      const errData = await res.json();
      throw new Error(errData.error || 'Registration failed.');
    }

    // Auto-login after successful registration
    return await login(email, password);
  };

  const googleSignIn = async (role?: UserRole): Promise<User> => {
    throw new Error('Google Sign-In is not supported on the local server. Please use standard email registration.');
  };

  const logout = () => {
    localStorage.removeItem('medplus_token');
    localStorage.removeItem('medplus_uid');
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
