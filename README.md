# TRADE CHECK REPORT DOCUMENTATION #

## Current Deployed Version: *7.6.24* ##


### v 7.6.24 ###

start: 09/30/16

* Added template date range in store lists.
* Improved card design of store list items.
* Added import and export database.
* Improved log in design.

### v 7.6.23 ###

start: 09/09/16

deployed: 2016-09-19

* Fixed bug in dot character in numeric edittexts.
* added progress percentage in downloading masterfile
* Fix bug in log in (not creating table -> conditional_answers)
* Fix bug in Android M os devices. In image capturing. Added request permissions for read,write and camera function for Android M higher versions.

### v 7.6.22 ###

start: 08/22/16

deployed: 09/05/16

* Added additional reports.
    * Customer summary report
    * Customer region summary report
    * OSA Report - Per SKU
    * NPI Report - Per SKU
    * SOS Report
    * Customised Planogram report
    * PJP Frequency report

### v 7.6.21 ###

start: 08/16/16

deployed: 08/22/16

* Fix bug in displaying secondary display.

### v 7.6.20 ###

start: 08/12/16

deployed: 08/12/16

* Fix bug in computation of osa, npi and planogram not including image capture and other forms not related. (Basis of score is a question with expected answer).

### v 7.6.19 ###

start: 07/05/16

* Add checking of active or inactive users in login, prompt message if not active.
* Add new line strings in question prompts.
* Add filtering of store area, remarks etc.
* Add message prompt in stores list where number of unposted audits are displayed
* Fix bug in multiple image capture in specific group (Customized Planogram)
* Fix bug in image capture (application stopped while/after/before capturing image)
* Added sorting of category and group, by alphabetical order or default order
* Fix computation of total question for OSA, NPI and Planogram (not including image capture, date, time, multiline and sign capture).
* Integrated to the new web tool link in 'ulp-tcr.com’

### v 7.6.18 ###

start: 06/30/16

deployed: 06/30/16

- fix bug in admin mode.

### v 7.6.17 ###

start: 06/22/2016 

deployed: 06/29/2016

- added admin mode login function
- fixed bug in conditional child forms.
- added test or beta mode. Connecting to testtcr.chasetech.com.
- fix other bugs in conditionals.
- fix other bugs in preview audit.
- Added ‘area' field in stores. (db version 9)
- Added automatic posting of pjp compliance of store in posting audit.
- Added version code in downloading file for ‘area’ field
- Fix perfect store percentage viewing in store reports.
- Fix date items in store and user audit summary reports.
- Added ‘template_type’ field in stores. (db version 10)

### v 7.6.16 ###

start: 06/17/2016

deployed: 06/21/2016 

* fix bug in not viewing multi select items.
* fix smudged design if os version is below or equal to 4.2.2
* modified downloading of appication update using downloadManager
* fix bug in error in updating apk in notification.
* fix bug in computational fields in survey module.
* fix bug in not ordered conditional items.

### v 7.6.14 ###
- fixed bug in start application 

### v 7.6.11 ###
- fixed bug in validation of available posting date.

### v 7.6.09 ###
- added error logging
- added error logs posting to web.
- added perfect store value in audit summary

### v 7.6.07 ###
- add design in preview audit summary per store
- add osa, npi, and planogram fields in store
- added osa, npi, and planogram total percentage computations

### v 7.6.03 ###
- replace endlines from multiline textbox values to white spaces (in posting) -> Pplus_main_preview.java
- added intellisearch bar in stores.
- added osa score in preview of store audit"# Unilever-Audit-System" 
