/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        slate: {
          950: '#090d16',
          900: '#0f172a',
          850: '#151e32',
          800: '#1e293b',
          750: '#283548',
          700: '#334155',
          600: '#475569',
          500: '#64748b',
          400: '#94a3b8',
          300: '#cbd5e1',
          200: '#e2e8f0',
          100: '#f1f5f9',
          50: '#f8fafc',
        },
        violet: {
          primary: '#6366f1',
          light: '#818cf8',
          dark: '#4f46e5',
          container: '#2e2d5e',
        },
        cyan: {
          accent: '#06b6d4',
          light: '#22d3ee',
        },
        emerald: {
          success: '#10b981',
        },
        amber: {
          warning: '#f59e0b',
        },
        rose: {
          urgent: '#f43f5e',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
