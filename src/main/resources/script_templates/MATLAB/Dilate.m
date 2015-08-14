% @matrix data

% Performs dilation on the active dataset with a preset structure

data = uint8(data); % convert to uint8
mask = im2bw(data,0.5); % make bw mask
se = strel('line',4,180); % create structure to use in dilation
mask = imdilate(mask,se); % perform dilation on the mask
data(~mask) = 0; % subtract mask from original dataset
