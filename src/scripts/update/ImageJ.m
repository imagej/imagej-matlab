function ImageJ(open_imagej, verbose)
% This function ImageJ adds the ImageJ libraries to the MATLAB classpath.
% By default, it will also start up an ImageJ instance within MATLAB.
%
% SYNTAX
%
% ImageJ
% ImageJ(open_imagej)
% ImageJ(open_imagej,verbose)
%
% PARAMETERS
%
%  open_imagej   true (default) | false | 1 | 0  
%
%                If false, an ImageJ instance will not be launched.
%
%  verbose       true | false (default) | 1 | 0  
%
%                If true, a confirmation message will be printed the first time
%                a jar is added to the MATLAB classpath.
%
% Author: Jacques Pecreaux, Johannes Schindelin, Jean-Yves Tinevez, Mark Hiner
%
% See also
% ImageJ_javaaddpath


if nargin < 1
    open_imagej = true;
end

if nargin < 2
    verbose = false;
end

%% Get the ImageJ directory

ImageJ_javaaddpath(verbose);

%% Maybe open the ImageJ window
import net.imagej.matlab.*;
if open_imagej
    ImageJMATLAB.start(verbose);
else
    ij.ImageJ([],ij.ImageJ.NO_SHOW); % same as Miji(false) .... but this prevents later use of ImageJ.m
    
    % initialize ImageJ with the headless flag
    % ImageJMATLAB.start(verbose, '--headless'); % this does not work
    
    % after ImageJ_javaaddpath, you have access to Java API of ImageJ
    % Cant't we just call it "headless mode"?
    %
    % If not, what functionality is the API missing to be called as
    % headless?
    
end

% Make sure that the scripts are found.
% Unfortunately, this causes a nasty bug with MATLAB: calling this
% static method modifies the static MATLAB java path, which is
% normally forbidden. The consequences of that are nasty: adding a
% class to the dynamic class path can be refused, because it would be
% falsy recorded in the static path. On top of that, the static
% path is fsck in a weird way, with file separator from Unix, causing a
% mess on Windows platform.
% So we give it up as now.
% %    imagej.User_Plugins.installScripts();
end

