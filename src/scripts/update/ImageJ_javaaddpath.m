function ImageJ_javaaddpath(imagej_directory,varargin)
% ImageJ_javaaddpath will add the paths for ImageJ/Fiji jar files to
% dyanamic Java class path using javaaddpath, without launching ImageJ
% instance. 
%
% When you want to use Fiji/ImageJ within parfor loop for
% parallel computation, you need to add Java class paths for Fiji/ImageJ
% within parfor loop in order to change dynamic Java class path for each
% MATLAB worker.
%
% Virtually all of the code is taken from ImageJ.m.
%
% SYNTAX
% ImageJ_javaaddpath(imagej_directory)
% ImageJ_javaaddpath(imagej_directory,verbose)
%
% INPUT ARGUMENTS
% imagej_directory           
%             char vector
%             The directory path for 'Fiji.app'.
%
%             Note that you can't include 'scripts' as 'Fiji.app\scripts'
%
% verbose     true | false (default) | 1 | 0
%             (Optional) true or 1 will print added java paths to the
%             Command Window.
%
%
% EXAMPLE
%
%     fijipath = 'D\\Fiji.app\\';
% 
%     addpath(fullfile(fijipath,'scripts'))
%     ImageJ
% 
%     I = imread('corn.tif',3); % uint8
% 
%     parfor i = 1:10
%         addpath(fullfile(fijipath,'scripts')) % to make MATLAB functions for ImageJ available
%         ImageJ_javaaddpath(fijipath) % to make ImageJ Java class available
% 
%         I1 = I - uint8(10*i);
%         imp = copytoImagePlus(I1);
% 
%         disp(imp)
% 
%         local_saveasTIFF(imp,i) % you cannot directly access ij.IJ in parfor loop
%     end
% 
%     % a local function is needed to access ij.IJ
%     function local_saveasTIFF(imp,i)
%         ij.IJ.saveAsTiff(imp,sprintf('testimage%d.tif',i))
%     end
%
%
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 14-Aug-2018 10:34:01
%
%
% See also
% ImageJ, javaaddpath, parfor
%
% %TODO ImageJ.m can wrap this function


p = inputParser;
p.addRequired('imagej_directory',@(x) isfolder(x));
p.addOptional('verbose',false,@(x) isscalar(x) && x == 1 || x == 0);
p.parse(imagej_directory,varargin{:});

verbose = p.Results.verbose;


if endsWith(imagej_directory,[filesep,'scripts'])
    
   error('imagej_directory %s should not include ''scripts''',...
       imagej_directory) 
    
end

% 
% %% Get the ImageJ directory
% % imagej_directory = fileparts(fileparts(mfilename('fullpath')));
% 
% imagej_directory = fileparts(FijiScriptFolder);

%% Get the Java classpath
classpath = javaclasspath('-all');

%% Add all libraries in jars/ and plugins/ to the classpath

% Switch off warning
warning_state = warning('off');

add_to_classpath(classpath, fullfile(imagej_directory,'jars'), verbose);
add_to_classpath(classpath, fullfile(imagej_directory,'plugins'), verbose);

% Switch warning back to initial settings
warning(warning_state)

% Set the ImageJ directory (and plugins.dir which is not ImageJ.app/plugins/)
java.lang.System.setProperty('ij.dir', imagej_directory);
java.lang.System.setProperty('plugins.dir', imagej_directory);
end

%------------------------------------------------------------------

function add_to_classpath(classpath, directory, verbose)
% Get all .jar files in the directory
dirData = dir(directory);
dirIndex = [dirData.isdir];
jarlist = dir(fullfile(directory,'*.jar'));
path_= cell(0);
for i = 1:length(jarlist)
    if not_yet_in_classpath(classpath, jarlist(i).name)
        if verbose
            disp(strcat(['Adding: ',jarlist(i).name]));
        end
        path_{length(path_) + 1} = fullfile(directory,jarlist(i).name);
    end
end

%% Add them to the classpath
if ~isempty(path_)
    javaaddpath(path_, '-end');
end

%# Recurse over subdirectories
subDirs = {dirData(dirIndex).name};
validIndex = ~ismember(subDirs,{'.','..'});

for iDir = find(validIndex)
    nextDir = fullfile(directory,subDirs{iDir});
    add_to_classpath(classpath, nextDir, verbose);
end
end

%------------------------------------------------------------------

function test = not_yet_in_classpath(classpath, filename)
%% Test whether the library was already imported
expression = strcat([filesep filename '$']);
test = isempty(cell2mat(regexp(classpath, expression)));
end