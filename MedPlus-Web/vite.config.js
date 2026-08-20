import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import fs from 'fs'
import os from 'os'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // Load standard env files from current directory
  const env = loadEnv(mode, process.cwd(), '')

  // Load secrets from user home directory if it exists
  const homeSecretsPath = path.join(os.homedir(), '.medplus_secrets')
  let extraEnv = {}
  if (fs.existsSync(homeSecretsPath)) {
    try {
      const content = fs.readFileSync(homeSecretsPath, 'utf-8')
      content.split(/\r?\n/).forEach(line => {
        const match = line.match(/^\s*([\w.-]+)\s*=\s*(.*)?\s*$/)
        if (match) {
          const key = match[1]
          let value = match[2] || ''
          // Remove wrapping quotes if present
          if ((value.startsWith('"') && value.endsWith('"')) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.slice(1, -1)
          }
          extraEnv[key] = value.trim()
        }
      })
    } catch (e) {
      console.error('Error reading secrets file:', e)
    }
  }

  const firebaseApiKey = extraEnv.VITE_FIREBASE_API_KEY || env.VITE_FIREBASE_API_KEY || ''

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 5173,
      host: '0.0.0.0'
    },
    define: {
      'import.meta.env.VITE_FIREBASE_API_KEY': JSON.stringify(firebaseApiKey)
    }
  }
})

