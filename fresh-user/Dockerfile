# ================= 构建阶段：Vite 打包 =================
FROM node:20-alpine AS build
WORKDIR /build
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# ================= 运行阶段：Nginx 托管静态资源并反代接口 =================
FROM nginx:alpine AS runtime
COPY --from=build /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80