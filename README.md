parawrap
========

What is it?
----------

EPFramework is a wrapper program to help run serial programs in an
embarrassingly parallel manner.

Install
---------

0. (OSX Only) Install Homebrew:
http://brew.sh/

1. Install gradle: 
Mac: brew install gradle
Linux: Use your preferred package manager

2. Clone this project

3. Build the project using gradle:
cd EPFramework
gradle build

Usage
--------
To run:
java -jar EPFramework-1.0.jar <Optional Config File>

When using the configuration file, the framework will immediatly begin dividing
up the work and processesing it.

Otherwise, the user will have to walk through the Configuration Wizard, where
they will set up the run. These settings can be saved so they do not have to be
input again.
