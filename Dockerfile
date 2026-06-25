# openjdk images on Docker Hub are deprecated/removed -> using eclipse-temurin as replacement
FROM eclipse-temurin:25-jdk-noble

RUN apt-get update && apt-get install -y supervisor curl nginx \
  && curl -sL https://deb.nodesource.com/setup_22.x | bash - \
  && apt-get install -y nodejs \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
COPY src src
COPY frontend frontend

RUN cd frontend && npm ci && npm run build

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw package -DskipTests

# Configure nginx
RUN cat > /etc/nginx/sites-available/default <<'EOF'
server {
    listen 80;

    # Security-Header (Clickjacking, MIME-Sniffing, Referrer, HSTS)
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header Content-Security-Policy "frame-ancestors 'none'" always;

    # Backend routes
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # MCP endpoints - support both /mcp and /sse
    location /mcp {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection '';
        proxy_http_version 1.1;
        chunked_transfer_encoding off;
        proxy_buffering off;
        proxy_cache off;
    }
    
    # Frontend (everything else)
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
    }
}
EOF

# Enable the nginx site configuration
RUN rm -f /etc/nginx/sites-enabled/default \
    && ln -s /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default

# Use supervisor to start nginx, frontend and backend
RUN cat > /etc/supervisor/conf.d/supervisord.conf <<'EOF'
[supervisord]
nodaemon=true
user=root

[program:nginx]
command=/usr/sbin/nginx -g "daemon off;"
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0

[program:backend]
command=java -jar /usr/src/app/target/praesto-0.0.1-SNAPSHOT.jar
directory=/usr/src/app
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0

[program:frontend]
directory=/usr/src/app/frontend
command=sh -c "PORT=3000 node build"
autostart=true
autorestart=true
stdout_logfile=/dev/stdout
stdout_logfile_maxbytes=0
stderr_logfile=/dev/stderr
stderr_logfile_maxbytes=0
EOF

EXPOSE 80

ENV NODE_ENV=production

# Azure Web App uses this to route external traffic to the container
ENV WEBSITES_PORT=80

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]