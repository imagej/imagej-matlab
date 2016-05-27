ImageJ();
IJM.run('Embryos (42K)');
I=IJM.getCurrentImage;
E = imadjust(wiener2(im2double(I(:,:,1))));
imshow(E);
IJM.createImage('result',E, true);
