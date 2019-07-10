classdef copytoMatlab_test < matlab.unittest.TestCase
    %copytoMatlab_test is a unit test for coptyMatlab
    %
    % Written by Kouichi C. Nakamura Ph.D.
    % MRC Brain Network Dynamics Unit
    % University of Oxford
    % kouichi.c.nakamura@gmail.com
    % 11-Oct-2018 23:33:27
    %
    % See also
    % coptyMatlab


    
    properties
        FijiAppspath
    end
    
    methods
        function obj = copytoMatlab_test(fijiapppath)
            
            
       
        end
    end
    
    methods (Test)
        function test1(testCase)
            
            addpath(fullfile(testCase.FijiAppspath,'scripts')) %TODO avoid using getfijipath
            ImageJ
            
            eval('import ij.IJ')
            
            %%
            A = randi(255,200,300,'uint8');
            
            img = copytoImg(A);
            
            testCase.verifyTrue(isa(img,'net.imglib2.img.array.ArrayImg'))
            
            testCase.verifyEqual(img.numDimensions,2)
            testCase.verifyEqual(img.dimension(0),300)
            testCase.verifyEqual(img.dimension(1),200)

            
            
            
            
        end
        
        function test2(testCase)
            %METHOD1 Summary of this method goes here
            %   Detailed explanation goes here
            
            
             %%
            A = randi(255,200,300,'uint8');
            
            imp = copytoImagePlus(A);
            
            testCase.verifyTrue(isa(imp,'ij.ImagePlus'))
            testCase.verifyEqual(imp.getDimensions,[300 200 1 1 1])

        end
    end
end

