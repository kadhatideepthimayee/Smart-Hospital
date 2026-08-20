import React, { createContext, useState, useEffect, useContext } from 'react';
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged,
  signInWithCredential,
  signInWithPopup,
  GoogleAuthProvider
} from 'firebase/auth';
import { doc, getDoc, setDoc, serverTimestamp } from 'firebase/firestore';
import { auth, db } from '../lib/firebase';
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

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  // Use Firebase onAuthStateChanged listener to automatically fetch user profile
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        try {
          const userDocRef = doc(db, 'users', firebaseUser.uid);
          const userDoc = await getDoc(userDocRef);
          
          if (userDoc.exists()) {
            const userData = userDoc.data();
            const profileUser = {
              uid: firebaseUser.uid,
              fullName: userData.fullName || '',
              email: userData.email || firebaseUser.email || '',
              phone: userData.phone || '',
              role: (userData.role || 'PATIENT') as UserRole,
              profileImage: userData.profileImage || ''
            };
            setUser(profileUser);
            // Firebase Auth automatically handles tokens, but we keep token string in state for API compat
            const idTokenResult = await firebaseUser.getIdToken();
            setToken(idTokenResult);
            localStorage.setItem('medplus_token', idTokenResult);
          } else {
            setUser(null);
            setToken(null);
            localStorage.removeItem('medplus_token');
          }
        } catch (err) {
          console.error('Error fetching user profile from Firestore', err);
          setUser(null);
          setToken(null);
          localStorage.removeItem('medplus_token');
        }
      } else {
        setUser(null);
        setToken(null);
        localStorage.removeItem('medplus_token');
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const login = async (email: string, password: string): Promise<User> => {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const firebaseUser = userCredential.user;
    
    const userDocRef = doc(db, 'users', firebaseUser.uid);
    const userDoc = await getDoc(userDocRef);
    
    if (!userDoc.exists()) {
      throw new Error('User profile data not found in database.');
    }
    
    const userData = userDoc.data();
    const profileUser: User = {
      uid: firebaseUser.uid,
      fullName: userData.fullName || '',
      email: userData.email || firebaseUser.email || '',
      phone: userData.phone || '',
      role: (userData.role || 'PATIENT') as UserRole,
      profileImage: userData.profileImage || ''
    };

    const idToken = await firebaseUser.getIdToken();
    setToken(idToken);
    localStorage.setItem('medplus_token', idToken);
    setUser(profileUser);
    
    return profileUser;
  };

  const register = async (
    fullName: string,
    email: string,
    phone: string,
    password: string,
    role: UserRole
  ): Promise<User> => {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const firebaseUser = userCredential.user;
    
    const profileUser: User = {
      uid: firebaseUser.uid,
      fullName,
      email,
      phone,
      role,
      profileImage: ''
    };

    // Save profile to Firestore
    await setDoc(doc(db, 'users', firebaseUser.uid), {
      uid: firebaseUser.uid,
      fullName,
      email,
      phone,
      role,
      createdAt: serverTimestamp()
    });

    const idToken = await firebaseUser.getIdToken();
    setToken(idToken);
    localStorage.setItem('medplus_token', idToken);
    setUser(profileUser);
    
    return profileUser;
  };

  const googleSignIn = async (role?: UserRole): Promise<User> => {
    const provider = new GoogleAuthProvider();
    const userCredential = await signInWithPopup(auth, provider);
    const firebaseUser = userCredential.user;

    const userDocRef = doc(db, 'users', firebaseUser.uid);
    const userDoc = await getDoc(userDocRef);

    let profileUser: User;

    if (userDoc.exists()) {
      const userData = userDoc.data();
      profileUser = {
        uid: firebaseUser.uid,
        fullName: userData.fullName || firebaseUser.displayName || 'Google User',
        email: userData.email || firebaseUser.email || '',
        phone: userData.phone || '',
        role: (userData.role || 'PATIENT') as UserRole,
        profileImage: userData.profileImage || firebaseUser.photoURL || ''
      };
    } else {
      // First time Google sign-in, save profile
      const selectedRole = role || 'PATIENT';
      profileUser = {
        uid: firebaseUser.uid,
        fullName: firebaseUser.displayName || 'Google User',
        email: firebaseUser.email || '',
        phone: '',
        role: selectedRole,
        profileImage: firebaseUser.photoURL || ''
      };

      await setDoc(userDocRef, {
        uid: firebaseUser.uid,
        fullName: profileUser.fullName,
        email: profileUser.email,
        phone: '',
        role: selectedRole,
        profileImage: profileUser.profileImage,
        createdAt: serverTimestamp()
      });
    }

    const newIdToken = await firebaseUser.getIdToken();
    setToken(newIdToken);
    localStorage.setItem('medplus_token', newIdToken);
    setUser(profileUser);
    
    return profileUser;
  };

  const logout = async () => {
    await signOut(auth);
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
