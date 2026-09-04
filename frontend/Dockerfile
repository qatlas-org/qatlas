FROM nginx:alpine
#COPY nginx.conf /etc/nginx/
COPY nginx.conf.template /etc/nginx/
CMD sh -c "envsubst \"`env | awk -F = '{printf \" \\\\$%s\", $1}'`\" < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && nginx -g 'daemon off;'"
COPY dist/frontend/ /usr/share/nginx/html
COPY nginx-oci/public/classic /usr/share/nginx/html/classic
COPY swagger /usr/share/nginx/html/swagger
RUN chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    chown -R nginx:nginx /etc/nginx
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid
    RUN addgroup -g 1000 testreports && \
    adduser nginx testreports
USER nginx