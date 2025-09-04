A tool for analyzing assertions in unit tests
=============================================

This repository contains source code for a tool that can parse and analyze
Java source code. More specifically, it traverses the parse tree of each
source file provided, and looks for occurrence of JUnit's various `assert`
statements, collecting those that correspond to one of more particular
patterns.

The tool works by running multiple `TokenFinder` objects over a source
file. Each token finder traverses the parse tree, and looks for whatever
pattern it has been coded to look for. Every time a token finder finds an
occurrence of that pattern, it creates a `FoundToken` object that encapsulates
the file name and location withing that file (line number ranges) where this
pattern is contained.

Once all token finders have been run on all test files, a final report
collating their findings is displayed in summarized form at the command line,
and in a more legible form in an external HTML file that can be opened in a
web browser. For every found pattern, a hyperlink is provided to the actual
source file where the pattern was found.

Usage
-----

Assuming you have a compiled version of the tool (if not, see below), you can
run it at the command line as follows:

    java -jar codefinder-1.0.jar [options] <folders>

Where `<folders>` is to be replaced by a list of folders containing Java
unit tests.

In the following, we shall simply write `codefinder` instead of the whole
`java -jar ...` prefix.

### Basic usage

Suppose that `/my/project/srctest` contains a set of Java files with JUnit
tests. To analyze assertions in these files, simply type:

	codefinder /my/project/srctest

This will produce a report at the command line, as well as a pretty-printed
HTML report in the file `report.html`.

### Symbol resolution

Some token finders need to determine the type of various expressions:
variables, method arguments or return types. Most often these definitions
are not in the test files to analyze, but either in additional source files
or in referenced libraries. It is possible to specify these files and
libraries as follows.

1. **Java source folders** can be added using the `--source` argument.
   You can provide a colon-separated list of folders containing additional
   source files, e.g.:
```
codefinder --source /my/project/src:/my/project/otherfolder /my/project/srctest
```
   This will tell that the folders `src` and `otherfolder` are to be
   scanned for additional definitions of the symbols.

2. **Dependent libraries** (i.e. JAR files) can be added using the
   `--jar` argument. As with source folders, supply a colon-separated
   list of folders containing JAR files to consider.

Symbol resolution is a costly operation in some cases. To speed up the analysis,
it is possible to set a timeout to the resolution of a symbol (using the
`--resolution-timeout` command line parameter). The symbol will
be considered unresolved if the timeout expires. The default value is 100 ms.

### Suggested setup

- Create a `root` folder containing the CodeFinder JARs, and a `Projects`
  folder containing (each in its folder) the projects that need to be
  analyzed.
- Create one profile for each project under the root, and use *relative*
  paths to refer to these projects in the profiles.

### Options

The full list of command line option is given below.

`-o <path>`, `--output <path>`
: Name of the output file where the HTML report is to be written
  (default: report.html)

`-s <path>`, `--source <path>`
: Additional source in path

`-t <n>`, `--threads <n>`
: Use up to `n` threads (default: number of cores minus 1). Note that
  the use of multi-threading provides disputable speed-up, since each
  thread must instantiate and update its own `JavaParser` object.
  [YMMV](https://dictionary.cambridge.org/dictionary/english/ymmv).

`-q`, `--quiet`
: Do not show error messages

`-u`, `--unresolved`
: Add the list of unresolved symbols in the final report

`-r <p>`, `--root <p>`
: Search in source tree for package 

`-m`, `--summary`
: Only show a summary at the CLI

`-l <n>`, `--limit <n>`
: Stop analysis after the first `n` files

`-c`, `--no-color`
: Disable colored output

`-h`, `--help`
: Displays a help message

### Using a profile

It may become tedious to invoke the program with a long list of arguments every time.
The tool can be "configured" by putting those arguments in a text file, called a "profile".
Here is an example of such a file:

```
# A sample profile
-o ../report.html
--no-color
--source /path/to/folder
/path/to/project
```

As you can see, arguments can be split across multiple lines. Blank lines and
lines starting with a `#` are ignored, so you can also add legible comments.
If the tool is called with the `--profile` parameter pointing to that file
(say `myprofile.txt`), it will read the contents of this file as if they had
been passed directly to the command line. If you call the tool with additional
command line arguments, they will be merged with whatever was found in the profile.

Compiling and Installing
------------------------

First make sure you have the following installed:

- The Java Development Kit (JDK) to compile. The project is developed to comply
  with Java version 11; it is probably safe to use any later version.
- [Ant](http://ant.apache.org) to automate the compilation and build process

Download the sources for CodeFinder from
[GitHub](https://github.com/liflab/codefinder) or clone the
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

Developing using Eclipse 
------------------------

If you wish to develop the tool in [Eclipse](https://eclipse.org):

In short:

- Create a new empty workspace (preferably in a new, empty folder).
- Create a new projects for the folder `Source`.

If imported from Ant, all dependencies should already been included.