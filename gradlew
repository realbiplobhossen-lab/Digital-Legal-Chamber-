#!/usr/bin/env sh

# ------------------------------------------------------------------------
# Gradle Wrapper startup script for System V init derived platforms
# ------------------------------------------------------------------------

DIRNAME=$(dirname "$0")
if [ -z "$DIRNAME" ]; then
  DIRNAME="."
fi

APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$DIRNAME" && pwd)

# Help Message
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    echo "Usage: gradlew [task...]"
    exit 0
fi

# Find the java executable
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME is not defined correctly." >&2
    echo "  We cannot execute $JAVACMD" >&2
    exit 1
fi

# Execute Gradle
exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"

