# Build stage
FROM node:20-alpine AS build
WORKDIR /app
ARG QATLAS_UI_VERSION=dev
ARG QATLAS_API_CLIENT_VERSION=dev

ENV VITE_QATLAS_UI_VERSION=${QATLAS_UI_VERSION}
ENV VITE_QATLAS_API_CLIENT_VERSION=${QATLAS_API_CLIENT_VERSION}
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Serve stage - small, no extra runtime deps
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
