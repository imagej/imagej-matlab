function I = copytoMatlab(img)
%copytoMatlab copies the content of an ImgLib2 Image (ImageJ2) or ImagePlus
% (ImageJ1) to MATLAB.
%
% SYNTAX
% I = copytoMatlab(img)
% I = copytoMatlab(imp)
%
% I = copytoMatlab(img) returns a MATLAB copy of the array stored in the
% specified ImgLib2 Img object. This function only works for ImgLib2 images
% that are ArrayImgs, and whose types are native real or integer ones.
%
% I = copytoMatlab(imp) returns a MATLAB copy of the array stored in the
% specified ij.ImagePlus object (ImageJ1).
%
% INPUT ARGUMENTS
% img         a net.imglib2.img.array.ArrayImg object (ImgLib2/ImageJ2)
%
% imp         a ij.ImagePlus object (ImageJ1)
%
%
% OUTPUT ARGUMENTS
% I           array of image data.
%
% NOTE
%
% We rely on Miji to set up classpath, so you would have to add Miji to
% your MATLAB path and call
%  >> Miji(false); % or true
% prior to using this function.
%
%
% Image:
% Jean-Yves Tinevez - 2013
%
% ImagePlus:
% Written by Kouichi C. Nakamura Ph.D.
% MRC Brain Network Dynamics Unit
% University of Oxford
% kouichi.c.nakamura@gmail.com
% 29-Sep-2018 10:20:48
%
% see also
% copytoImgPlus, copytoImg, copytoImagePlus


if isa(img,'net.imglib2.img.array.ArrayImg')
    
    %% CONSTANTS
    
    ACCEPTED_TYPES = {
        'net.imglib2.type.numeric.integer.UnsignedByteType'
        'net.imglib2.type.numeric.integer.UnsignedShortType'
        'net.imglib2.type.numeric.integer.UnsignedIntType'
        'net.imglib2.type.numeric.integer.ByteType'
        'net.imglib2.type.numeric.integer.ShortType'
        'net.imglib2.type.numeric.integer.IntType'
        'net.imglib2.type.numeric.integer.LongType'
        'net.imglib2.type.numeric.integer.LongType'
        'net.imglib2.type.numeric.real.FloatType'
        'net.imglib2.type.numeric.real.DoubleType'
        };
    
    %% Check input
    
    if ~isa(img, 'net.imglib2.img.array.ArrayImg')
        error('MATLAB:copytoMatlab:IllegalArgument', ...
            'Expected argument to be an ImgLib2 ArrayImg, got a %s.', ...
            class(img) )
    end
    
    fel = img.firstElement;
    knowType = false;
    for i = 1 : numel(ACCEPTED_TYPES)
        if isa(fel, ACCEPTED_TYPES{i})
            knowType = true;
            break
        end
    end
    
    if ~knowType
        error('MATLAB:copytoMatlab:IllegalArgument', ...
            'Can only deal with native real or integer types, got a %s.', ...
            class(fel) )
    end

    
    %% Operate on source image
    
    % Retrieve dimensions
    numDims = img.numDimensions();
    sizes = NaN(1, numDims);
    for i = 1 : numDims
        sizes(i) = img.dimension(i-1);
    end
    
    % Retrieve array container
    J = img.update([]).getCurrentStorageArray;
    
    % Deal with unsigned types
    if isa(fel, 'net.imglib2.type.numeric.integer.UnsignedByteType')
        J = typecast(J, 'uint8');
    elseif isa(fel, 'net.imglib2.type.numeric.integer.UnsignedShortType')
        J = typecast(J, 'uint16');
    elseif isa(fel, 'net.imglib2.type.numeric.integer.UnsignedIntType')
        J = typecast(J, 'uint32');
    end
    
    % Build MATLAB array
    I = reshape(J, sizes);
    I = permute(I, [2 1]);
    
elseif isa(img,'ij.ImagePlus')
    
    imp = img;
    clear img;
    
    dim = double(imp.getDimensions);
    
    switch imp.getBitDepth
        case 8
            I = zeros(dim(2),dim(1),dim(3),dim(4),dim(5),'uint8');
            
        case 16
            I = zeros(dim(2),dim(1),dim(3),dim(4),dim(5),'uint16');
            
        case 24 %RGB
            I = zeros(dim(2),dim(1),dim(3),'uint8');
            
            channels = ij.plugin.ChannelSplitter.split(imp);
            
            for c = 1:3
                for z = 1:dim(4)
                    channels(c).setZ(z);
                    for t = 1:dim(5)
                        channels(c).setT(t);
                        I(:,:,c,z,t) = local_getPixels(channels(c),dim);
                    end
                end
            end
            
            return
        case 32
            error('not supported yet')
        case 64
            error('not supported yet')
        otherwise
            error('not supported yet')
    end
    
    
    if nnz(dim(3:5) == 1) == 2
        
        DIM = dim(find(dim(3:5) ~= 1)+2);
        for i = 1:DIM
            imp.setSlice(i);
            
            switch find(dim(3:5) ~= 1)
                case 1
                    c = i;
                    z = 1;
                    t = 1;
                case 2
                    c = 1;
                    z = i;
                    t = 1;
                case 3
                    c = 1;
                    z = 1;
                    t = i;
            end
            
            P = local_getPixels(imp,dim);
            
            I(:,:,c,z,t) = P;
            
        end
        
        
    elseif imp.isHyperStack
        
        for c = 1:dim(3) %TODO parfor? I cannot be used as is
            imp.setC(c);
            
            for z = 1:dim(4)
                imp.setZ(z);
                for t = 1:dim(5)
                    imp.setT(t);
                    
                    P = local_getPixels(imp,dim);
                    
                    I(:,:,c,z,t) = P;
                    
                end
            end
        end
        
    elseif imp.isComposite
        
        
        for c= 1:dim(3)
            imp.setSlice(c); % MUCH faster than setC(c) for big images
            
            for z = 1:dim(4)
                imp.setZ(z);
                for t = 1:dim(5)
                    imp.setZ(t);
                    
                    P = local_getPixels(imp,dim);
                    
                    I(:,:,c,z,t) = P;
                end
            end
        end
        
    else
        
        P = local_getPixels(imp,dim);
        
        I = P;
        
    end

end

end

%--------------------------------------------------------------------------

function P = local_getPixels(imp,dim)

ip = imp.getChannelProcessor;

% f = ip.getFloatArray; % single, SLOW
% c = ip.getIntArray; % int32, SLOW

p = ip.getPixels; % vector, int16, very quick

switch class(p)
    case 'int32' %RGB or 32-bit
        
    case 'int16'
        ptc = typecast(p,'uint16'); %NOTE need to convert to uint16 from int16
    case 'int8'
        ptc = typecast(p,'uint8'); %NOTE need to convert to uint16 from int16
        
end

P = reshape(ptc,dim(1),dim(2))';

end



