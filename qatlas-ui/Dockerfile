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


# Runtime stage
FROM nginx:alpine

# QAtlas report data is owned by GID 1000.
# Give the nginx user access to the shared attachment volume.
#
# This matches the Classic UI container and prevents
# 403 Forbidden when serving /attachment/*.
RUN addgroup -g 1000 testreports && \
    addgroup nginx testreports

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80