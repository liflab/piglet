#! /bin/bash

# This script is a "hack" to create a runnable "fat" jar for the tool.
# It removes the signature of the JEclipse jar file in the output jar,
# which otherwise would prevent Java from executing it (invoking an
# invalid signature).
# See: https://stackoverflow.com/q/999489

ant
zip -d codefinder-1.0.jar META-INF/ECLIPSE_.RSA META-INF/ECLIPSE_.SF
