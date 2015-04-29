[![](http://jenkins.imagej.net/job/ImageJ-MATLAB/lastBuild/badge/icon)](http://jenkins.imagej.net/job/ImageJ-MATLAB/)

ImageJ MATLAB
=============

This library provides a variety of features for integrating ImageJ with MATLAB, including:

* ImageJMATLAB - An entry point for launching ImageJ from within MATLAB and ensuring all ImageJ dependencies are loaded in the MATLAB class loader.
* IJM commands - When ImageJ is started within MATLAB, utility methods for converting between ImageJ and MATLAB data structures will be available. Run `IJM.help` for a list of commands.
* MATLAB dataset conversion - ImageJ Datasets can now be converted, using the `ConvertService`, to the Matlab Control MatlabNumericArray types. These arrays can then be passed to a running MATLAB instance as a matrix.
* MATLAB array preprocessor - MATLAB scripts (.m) can now have `@matrix` annotations. This will automatically take the active ImageJ Dataset and convert it to a matrix in MATLAB.
