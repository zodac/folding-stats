#!/bin/bash
# Runs the jOOQ generation tool using all JAR files in the current directory
java -cp "./*:." org.jooq.codegen.GenerationTool ./jooq-config.xml