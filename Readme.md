A tool for analyzing assertions in unit tests
=============================================

Compiling and Installing
------------------------

First make sure you have the following installed:

- The Java Development Kit (JDK) to compile. The project is developed to comply
  with Java version 11; it is probably safe to use any later version.
- [Ant](http://ant.apache.org) to automate the compilation and build process

Download the sources for SealTest from
[GitHub](https://github.com/liflab/sealtest) or clone the
repository using Git:

    git@github.com:liflab/codefinder.git

If the project you want to compile has dependencies,
you can automatically download any libraries missing from your
system by typing:

    ant download-deps

This will put the missing JAR files in the `dep` folder in the project's
root.

### Compiling

Compile the sources by simply typing:

    ant

This will produce a file called `codefinder-x.x.jar` in the folder
(where `x.x` is the version number). This file
is runnable and stand-alone, or can be used as a library, so it can be moved
around to the location of your choice.

In addition, the script generates in the `doc` folder the Javadoc
documentation for using BeepBeep. To show documentation in Eclipse,
right-click on the jar, click "Properties", then fill the Javadoc location.

Usage
-----

Once the compiled JAR is created, one can run it at the command line
as follows:

    java -jar codefinder-1.0.jar [options] <folders>

Where `<folders>` is to be replaced by a list of folders containing Java
unit tests.

Options are:

`-o <path>`, `--output <path>`
: Output file (default: report.html)

`-s <path>`, `--source <path>`
: Additional source in path

`-t <n>`, `--threads <n>`
: Use up to n threads (default: 2)

`-q`, `--quiet`
: Do not show error messages

`-m`, `--summary`
: Only show a summary at the CLI

`-c`, `--no-color`
: Disable colored output

Developing using Eclipse 
------------------------

If you wish to develop the tool in Eclipse:

In short:

- Create a new empty workspace (preferably in a new, empty folder).
- Create a new projects for the folder `Source`.

If imported from Ant, all dependencies should already been included.