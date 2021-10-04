FROM openjdk:17-jdk-slim

RUN apt-get update && \
    apt-get install --yes binutils && \
    apt-get clean all && \
    jlink \
        --compress=2 \
        --no-header-files \
        --no-man-pages \
        --strip-debug \
        --output /opt/jdk \
        --add-modules \
java.net.http,\
jdk.compiler,\
jdk.sctp,\
java.scripting,\
jdk.unsupported,\
java.base,\
java.compiler,\
java.desktop,\
java.instrument,\
java.management.rmi,\
java.prefs,\
java.security.sasl,\
java.sql,\
jdk.security.auth,\
java.datatransfer,\
java.logging,\
java.naming,\
java.rmi,\
java.sql.rowset,\
java.transaction.xa,\
java.xml,\
java.xml.crypto,\
jdk.xml.dom,\
jdk.crypto.ec,\
jdk.crypto.cryptoki,\
jdk.jfr,\
jdk.naming.dns,\
jdk.zipfs,\
jdk.management.agent,\
java.base,\
java.compiler,\
java.datatransfer,\
java.desktop,\
java.instrument,\
java.logging,\
java.management,\
java.management.rmi,\
java.naming,\
java.net.http,\
java.prefs,\
java.rmi,\
java.scripting,\
java.se,\
java.security.jgss,\
java.security.sasl,\
java.sql,\
java.sql.rowset,\
java.transaction.xa,\
java.xml,\
java.xml.crypto,\
jdk.accessibility,\
jdk.attach,\
jdk.charsets,\
jdk.compiler,\
jdk.crypto.cryptoki,\
jdk.crypto.ec,\
jdk.dynalink,\
jdk.editpad,\
jdk.hotspot.agent,\
jdk.httpserver,\
jdk.internal.ed,\
jdk.internal.jvmstat,\
jdk.internal.le,\
jdk.internal.opt,\
jdk.internal.vm.ci,\
jdk.internal.vm.compiler,\
jdk.internal.vm.compiler.management,\
jdk.jartool,\
jdk.javadoc,\
jdk.jcmd,\
jdk.jconsole,\
jdk.jdeps,\
jdk.jdi,\
jdk.jdwp.agent,\
jdk.jfr,\
jdk.jlink,\
jdk.jshell,\
jdk.jsobject,\
jdk.jstatd,\
jdk.localedata,\
jdk.management,\
jdk.management.agent,\
jdk.management.jfr,\
jdk.naming.dns,\
jdk.naming.rmi,\
jdk.net,\
jdk.sctp,\
jdk.security.auth,\
jdk.security.jgss,\
jdk.unsupported,\
jdk.unsupported.desktop,\
jdk.xml.dom,\
jdk.zipfs \
    && strip --strip-unneeded /opt/jdk/lib/server/libjvm.so

CMD ["echo", "JDK image only to be used for building, killing container"]