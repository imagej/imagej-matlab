function Miji(open_imagej)
    %% This script displays a notice that the MATLAB update site is required.
    %% It will open a link to the Fiji wiki page on following update sites if desired.
    % Author: Mark Hiner

    display_update_site = questdlg({'Use of Miji requires the MATLAB update site.' 'Would you like to open the Fiji wiki page on enabling update sites?'},'Update site required','Yes', 'No','Yes');
    switch display_update_site
        case 'Yes'
            web('http://www.fiji.sc/How_to_follow_a_3rd_party_update_site','-browser')
    end
end
