# Quick Reference Handbook (QRH)

<img src="./readme/screenshot1b.png" width="240px"> | <img src="./readme/screenshot2b.png" width="240px"> | <img src="./readme/screenshot3b.png" width="240px">

## Features
- Unofficial derivative of the Association of Anaesthetists Quick Reference Handbook (QRH): www.anaesthetists.org/qrh (CC BY-NC-SA 4.0)
- ***Not endorsed by the Association of Anaesthetists***
- Rapidly searchable guideline list
- Simple guideline layout echoing original handbook
- Clickable links between guidelines
- Easily updatable through modification of JSON assets
- [![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa] Released under same Creative Commons license as original work  


## License
This work is licensed under a [Creative Commons Attribution-NonCommercial-ShareAlike 4.0
International License][cc-by-nc-sa].

[![CC BY-NC-SA 4.0][cc-by-nc-sa-image]][cc-by-nc-sa]

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/
[cc-by-nc-sa-image]: https://licensebuttons.net/l/by-nc-sa/4.0/88x31.png
[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC%20SA%204.0-lightgrey.svg
You may distribute original version or adapt for yourself and distribute with acknowledgement of source. 
You may not use for commercial purposes.  

## Download

Available on Google Play:   
<a href='https://play.google.com/store/apps/details?id=com.mttrnd.qrh&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="150px"/></a>

Otherwise please download APK from releases:
https://github.com/mttrnd/qrh/releases

## Technical
*(For any updates, modifications or derivatives)*

The guideline list is generated from /assets/guidelines.json. 

Guidelines are stored as JSON objects in the assets folder.

Each array contains 'type', 'main', 'sub' and 'step' keys.

These populate different styled cards for each guideline page.

### Card views

Type integer value determines the appearance of the generated card:
1. Introductory text (main only)
2. START text (main only)
3. Guideline step with bold heading and separate content (main, sub and step)
4. Guideline step with single text field (main and step)
5. Orange expanding box (main and sub)
6. Blue expanding box (main and sub)
7. Green expanding box (main and sub)
8. Black expanding box (main and sub)
9. Purple expanding box (main and sub)
10. Image (path/URL in main, caption in sub)
11. Version text (main only)
12. Red disclaimer card (main only)  

### Card contents

Basic HTML tags (B, U, I, LI) can be used within for formatting where required.

Unicode subscript and superscript characters are used rather than SUB and SUP spans.

Links are parsed as following:

#### Guideline links
Generated when the following regex is matched:  
`/[(]?[→][\s]?[1-4][-][0-9]{1,2}[)]?/`  
Therefore can have with or without parentheses, and is insensitive to space between → and guideline code.

#### Phone links
Generated from 11 digit numbers beginning with 0

#### Web links
URLs beginning with http(s):// or www.  
  
## Accompanying Guidance & Disclaimers

### 1. 
This application is an unofficial adaptation of the Association of Anaesthetists Quick Reference Handbook (QRH); a collection of guidelines for unexpected or uncommon anaesthesia-related emergencies. It is not endorsed by the Association of Anaesthetists. As per the original accompanying guidance:

*"The QRH is not a substitute for learning and practising clinical skills. Nor is it a substitute for being familiar with more detailed guidelines and algorithms, such as those for managing cardiac arrest or difficult airway. It's essential that clinicians learn about these and practice their implementation elsewhere. For example, it's expected that in using the QRH guideline for managing cardiac arrest, clinicians will already have underlying knowledge of conditions causing cardiac arrest and will be competent in providing advanced life support.*

*The QRH is also not intended to be used as a rule book every single time one of the guideline situations is encountered. For instance, most ordinary occasions of hypotension will have a simple cause and will respond to obvious remedies. The hypotension guideline becomes useful if the situation is out of the ordinary or becomes unremitting. Conversely, for the more immediate and uncommon problems, such as cardiac arrest in theatre, the guideline should be used from the outset to help avoid missing out useful steps in resolving the situation."*

The guidelines in this handbook are not intended to be standards of medical care. The ultimate judgement with regard to a particular clinical procedure or treatment plan must be made by the clinician in the light of the clinical data presented and the diagnostic and treatment options available.


### 2.
This application is intended for educational use only by healthcare professionals in the United Kingdom who are already familiar with the QRH. It may be used to support learning and rehearsal of emergency responses. <b>It has not been professionally developed nor tested therefore is not recommended for clinical use.</b> The developer accepts no responsibility for or liability arising from any decision to use in a clinical setting despite this warning. You should consider the advice of your employer, regulatory and professional bodies regarding the use of software applications, and always exercise professional judgement before relying on information from any source.

This application is <u>not</u> presented as a medical device in the United Kingdom. <b>It carries no certification markings, regulatory approvals or technical assessment appraisals.</b> According to the latest Medicines and Healthcare products Regulatory Agency guidance <i>\"Medical device stand-alone software including apps\"</i> (Last updated 4th June 2020) at the time of this release; software is unlikely to be a device if it just reproduces a paper document in digital format, provides reference information to follow the path of a procedure/treatment, or it has decision points but the healthcare professional decides which path to take (as they ultimately rely on their own knowledge). The developer believes that the application falls within this \'non medical function\' remit.

Regulations may change with time. Distribution by the developer will be ceased if advised to do so or if this application subsequently becomes subject to regulatory approval. Please periodically check whether regulations have changed, and stop using this application if you are unsure about it\'s status. Due to the offline and open-source nature of the application, however, the developer cannot be held responsible for ongoing use and distribution of the application or its source code by other parties beyond such a time.

For more information please see: https://www.gov.uk/government/publications/medical-devices-software-applications-apps</string>

### 3. 
This application reproduces the August 2019 version of the QRH, adapted to fit the mobile application format. Content has largely been transcribed verbatim, except for minor formatting, punctuation and structure tweaks that do not meaningfully alter the guidelines, and the introduction of TALLman Lettering to improve the readability of certain words. The developer assumes no responsibility or liability for any errors or omissions in the content. The information is provided on an \"as is\" basis with no guarantees of completeness, accuracy, usefulness or timeliness. Drug indications and doses may be incorrect, and do not take into account individual circumstances such as comorbidities, interactions, renal and hepatic function. Likewise, the application is provided on an \"as is\" basis with no guarantees of usefulness, performance or reliability. The developer, original QRH authors, or anyone else connected in any way with the QRH or application, cannot be held responsible for your use of the information contained in or linked from this application. Links to external websites and telephone numbers are provided for convenience and informational purposes only; they do not constitute an endorsement and the developer is not responsible for any third-party content accessed through them.

The QRH is updated from time to time. A version number is displayed next to each guideline in the list view, and in the header and footer when opened; these correspond with the Association of Anaesthetists guideline versioning. You must check that the guidelines you are using are up to date. The developer will endeavour to keep the application updated with any new QRH revision, but there will be a delay between release from the Association of Anaesthetists and release of an application update. If any guidelines contained within the application become superseded, please revert to using the latest version from the Association of Anaesthetists. Alternatively, the source code could be obtained from github.com/mttrnd/qrh and updated by the end-user.

This application does not collect, store or transmit any personal data. This application allows you to set short reminders for locations of emergency equipment; please do not misuse this function to enter any personal, sensitive or patient-related data. These reminders are held on your device, are not intentionally transmitted anywhere by the application and not accessible to or stored by the developer. However, any data associated with the application should be assumed to be insecure, may be automatically backed up by the Android operating system (including transmission over the internet) or accessed by other applications. If you wish to clear any reminders entered into the application, please click the \'RESET DATA &amp; EXIT\' button on the About page.</string>

*Google Play and the Google Play logo are trademarks of Google LLC.*
