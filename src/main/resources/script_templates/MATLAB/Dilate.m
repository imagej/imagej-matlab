% @matrix data
% @OUTPUT net.imagej.Dataset rval
% @OUTPUT net.imagej.Dataset mask

% Performs dilation with a 3x3 square,
% operating on the active dataset
% Outputs the dilated mask and the original image
% with the mask applied.

rval = uint8(data); % convert to uint8
rval = mat2gray(rval); % normalize data
mask = im2bw(rval,0.5); % make logical mask
se = strel('square',3); % create structure to use in dilation
mask = imdilate(mask,se); % perform dilation on the mask
rval(~mask) = 0; % subtract mask from original dataset
