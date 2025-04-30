#!/bin/sh

# Replace environment variables in JavaScript files
echo "Replacing environment variables in JS files..."
find /usr/share/nginx/html -name "*.js" -exec sed -i "s|REACT_APP_API_URL_PLACEHOLDER|${REACT_APP_API_URL}|g" {} \;

# Start nginx
echo "Starting nginx..."
exec "$@" 