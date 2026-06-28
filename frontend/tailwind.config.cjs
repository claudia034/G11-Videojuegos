module.exports = {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: '#6D28D9',
          700: '#5b21b6'
        },
        gamingblue: '#0f172a',
        cyana: '#00ffd1'
      },
      boxShadow: {
        glow: '0 8px 30px rgba(99,102,241,0.15)'
      }
    }
  },
  plugins: []
}
