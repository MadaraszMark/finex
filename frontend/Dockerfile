# FineX frontend – Docker image (a gyökérben lévő docker-compose.yml használja)

# Éles build: TypeScript-ellenőrzés és Vite build a dist/ mappába
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# Kiszolgálás nginx-szel: statikus fájlok, /api proxy és biztonsági fejlécek
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
