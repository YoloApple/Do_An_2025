# Sử dụng image Kong chính thức
FROM kong:3.7

# Thiết lập các biến môi trường cần thiết cho Kong
ENV KONG_DATABASE=off
ENV KONG_DECLARATIVE_CONFIG=/etc/kong/kong.production.yml
ENV KONG_PROXY_ACCESS_LOG=/dev/stdout
ENV KONG_ADMIN_ACCESS_LOG=/dev/stdout
ENV KONG_PROXY_ERROR_LOG=/dev/stderr
ENV KONG_ADMIN_ERROR_LOG=/dev/stderr
ENV KONG_ADMIN_LISTEN=0.0.0.0:8001

# Copy file cấu hình production vào trong image
COPY kong.production.yml /etc/kong/kong.production.yml
