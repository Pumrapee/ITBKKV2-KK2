FROM nginx:alpine
ENV TZ=UTC
COPY dist/ /usr/share/nginx/html/
COPY default.conf /etc/nginx/conf.d/default.conf