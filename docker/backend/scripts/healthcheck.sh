#!/busybox sh

# Since production uses HTTPS but dev does not, we try both URLs
/busybox wget --no-check-certificate --quiet --tries=1 --spider https://127.0.0.1:8443/folding/actuator/health || \
/busybox wget --no-check-certificate --quiet --tries=1 --spider http://127.0.0.1:8443/folding/actuator/health || \
exit 1