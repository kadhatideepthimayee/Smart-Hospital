import axios from 'axios';

const getBaseURL = (): string => {
  const envUrl = import.meta.env.VITE_API_URL as string;
  if (envUrl) return envUrl;

  // If loading the site on a non-localhost domain (e.g. PC LAN IP over Wi-Fi),
  // dynamically connect to the same host's port 5000 API.
  const hostname = window.location.hostname;
  if (hostname && hostname !== 'localhost' && hostname !== '127.0.0.1') {
    return `http://${hostname}:5000/api`;
  }
  return 'http://127.0.0.1:5000/api';
};

const axiosInstance = axios.create({
  baseURL: getBaseURL(),
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('medplus_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('medplus_token');
      // Dispatch custom event to notify AuthContext to sync its state
      window.dispatchEvent(new Event('auth_logout'));
      
      const currentPath = window.location.pathname;
      if (currentPath !== '/login' && currentPath !== '/' && currentPath !== '/register') {
        window.location.href = '/login?expired=true';
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
