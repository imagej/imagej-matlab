% @matrix data
% @OUTPUT net.imagej.Dataset rval

% Performs dilation with a fixed structure, operating on the
% active dataset

rval = uint8(data); % convert to uint8
mask = im2bw(rval,0.5); % make bw mask
se = strel('line',4,180); % create structure to use in dilation
mask = imdilate(mask,se); % perform dilation on the mask
rval(~mask) = 0; % subtract mask from original dataset
